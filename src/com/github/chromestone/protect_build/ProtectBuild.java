package com.github.chromestone.protect_build;

import com.github.chromestone.protect_build.command.*;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.logging.Level;

/**
 * The ProtectBuild class
 * Created by Derek Zhang on 8/6/19.
 */
public class ProtectBuild extends JavaPlugin {

    private static final String REGISTER_COMMAND = "register";
    private static final String SPAWN_COMMAND = "tpspawn";
    private static final String HOME_COMMAND = "tphome";
    private static final String SET_HOME_COMMAND = "settphome";

    //ticks per second
    private final static long TPS = 20;

    private MyIdentifier identifier = null;
    private ProtectHandler protectHandler = null;

    private long spawnCooldown = -1;
    private long homeCooldown = -1;

    @Override
    public void onEnable() {

        // take care of class attributes

        String dataDir = getDataFolder().getAbsolutePath();

        this.identifier = new MyIdentifier(dataDir);
        boolean result = this.identifier.init(getLogger());
        if (!result) {

            getLogger().log(Level.SEVERE, "ProtectBuild plugin will NOT load: register init error");
            return;
        }

        this.protectHandler = new ProtectHandler(getDataFolder().getAbsolutePath());

        // take care of configuration

        saveDefaultConfig();

        FileConfiguration config = getConfig();

        int resistDuration = config.getInt("resistance-duration", 5);
        if (resistDuration > 0) {

            resistDuration *= (int) TPS;
        }

        List<String> passphrases = config.getStringList("passphrases");
        if (!passphrases.isEmpty()) {

            long cooldownTime = config.getLong("command-cooldown.register", 5);
            if (cooldownTime > 0) {

                cooldownTime *= TPS;
            }

            PluginCommand pC = getCommand(REGISTER_COMMAND);
            if (pC != null) {

                pC.setExecutor(new RegisterCommand(this, this.identifier, passphrases,
                                                   cooldownTime, resistDuration));
            }
            else {

                getLogger().log(Level.SEVERE, "getCommand failed for /" + REGISTER_COMMAND);
            }
        }
        else {

            getLogger().log(Level.WARNING, "disabled register command since passphrases empty\n" +
                                           "unless previously registered, players cannot do anything\n" +
                                           "please modify ProtectBuild/config.yml");
        }

        // other configurations

        this.spawnCooldown = config.getLong("command-cooldown.spawn", 1800);
        if (this.spawnCooldown > 0) {

            this.spawnCooldown *= TPS;
        }

        this.homeCooldown = config.getLong(("command-cooldown.home"), 1800);
        if (this.homeCooldown > 0) {

            this.homeCooldown *= TPS;
        }

        int maxEntities = config.getInt("chunk-spawns-until");
        if (maxEntities < 0) {

            getLogger().log(Level.INFO, "negative chunk-spawns-until, NO spawning limits will be imposed");
        }

        // take care of other commands

        // take care of listeners

        Server server = getServer();
        PluginManager pM = server.getPluginManager();

        pM.registerEvents(new CancellerListener(this, this.identifier, maxEntities), this);

        pM.registerEvents(new MyListener(this, this.identifier, this.protectHandler, resistDuration),
                          this);

        // run later stuff here

        BukkitScheduler scheduler = server.getScheduler();

        scheduler.runTask(this, () -> {

            Server s = getServer();
            ConsoleCommandSender cs = s.getConsoleSender();

            //TODO make do game rule config
            s.dispatchCommand(cs, "gamerule mobGriefing false");
            s.dispatchCommand(cs, "gamerule doFireTick false");
            s.dispatchCommand(cs, "gamerule maxEntityCramming 4");

            s.dispatchCommand(cs, "setworldspawn 0 255 0");
            s.dispatchCommand(cs, "gamerule spawnRadius 0");

            //TODO make size configurable
            s.dispatchCommand(cs, "worldborder center 0 0");
            s.dispatchCommand(cs, "worldborder set 2048");

            World world = null;
            for (World w : s.getWorlds()) {

                if (w.getEnvironment() == World.Environment.NORMAL) {

                    world = w;
                    break;
                }
            }

            if (world == null) {

                getLogger().log(Level.SEVERE, "unable to find overworld (normal) for spawn adjustments");
                return;
            }

            PluginCommand pC;

            pC = getCommand(HOME_COMMAND);
            if (pC != null) {

                pC.setExecutor(new HomeCommand(this, this.identifier,
                                               this.homeCooldown, world.getUID()));
            }
            else {

                getLogger().log(Level.SEVERE, "getCommand failed for /" + HOME_COMMAND);
            }

            BlockState block;

            block = world.getBlockAt(0, 255, 0).getState();
            block.setType(Material.SMOOTH_QUARTZ_STAIRS);
            block.update(true);

            pC = getCommand(SPAWN_COMMAND);
            if (pC != null) {

                pC.setExecutor(new SpawnCommand(this, this.identifier,
                                                this.spawnCooldown, block.getLocation().clone()));
            }
            else {

                getLogger().log(Level.SEVERE, "getCommand failed for /" + SPAWN_COMMAND);
            }



            block = world.getBlockAt(0, 254, 0).getState();
            block.setType(Material.WATER);
            block.update(true);

            for (Chunk chunk : world.getLoadedChunks()) {

                this.protectHandler.loadChunk(chunk, getLogger());
            }
        });

        scheduler.runTaskTimer(this, () -> {

            for (Player player : getServer().getOnlinePlayers()) {

                if (!player.isDead()) {

                    Location loc = player.getLocation();

                    //TODO make size configurable
                    // assumes world spawn at (x=0, z=0)
                    if (loc.getX() > 1024 || loc.getX() < -1024 || loc.getZ() > 1024 || loc.getZ() < -1024) {

                        player.setHealth(0);
                        player.kickPlayer("out of world");
                    }
                }
            }
        }, 1, TPS);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equals(SET_HOME_COMMAND)) {

            if (sender instanceof Player) {

                Player player = (Player) sender;

                Location location = player.getLocation();
                World world = location.getWorld();
                if (world == null || world.getEnvironment() != World.Environment.NORMAL) {

                    player.sendMessage(ChatColor.RED + "You can only use this command in the normal world.");
                    return true;
                }

                Optional<My3DPoint> wrapper = this.identifier.setLocation(player.getUniqueId(),
                                                                          location,
                                                                          getLogger());
                if (!wrapper.isPresent()) {

                    player.sendMessage("Previous home at ?\nHome is set.");
                    return true;
                }
                else {

                    My3DPoint point = wrapper.get();
                    player.sendMessage("Previous home at " + point + ".\nHome is set.");
                }
            }
            else {

                sender.sendMessage("Invalid.");
            }

            return true;
        }
        return false;
    }

    @Override
    public void onDisable() {

        if (this.identifier != null) {

            this.identifier.save(getLogger());
            this.identifier = null;
        }

        if (this.protectHandler != null) {

            this.protectHandler.finalSave(getLogger());
            this.protectHandler = null;
        }
    }
}
