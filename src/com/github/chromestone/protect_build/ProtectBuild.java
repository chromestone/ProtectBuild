package com.github.chromestone.protect_build;

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

    //ticks per second
    private final static long TPS = 20;

    private MyIdentifier identifier = null;
    private ProtectHandler protectHandler = null;
    private boolean doSpawnCooldown = false;
    private long spawnCooldown = -1;
    private HashSet<UUID> spawnCooldownSet = null;
    private Location spawnLocation = null;

    @Override
    public void onEnable() {

        // take care of class attributes

        String dataDir = this.getDataFolder().getAbsolutePath();

        this.identifier = new MyIdentifier(dataDir);
        boolean result = this.identifier.init(getLogger());
        if (!result) {

            getLogger().log(Level.SEVERE, "ProtectBuild plugin will NOT load: register init error");
            return;
        }

        this.protectHandler = new ProtectHandler(this.getDataFolder().getAbsolutePath());

        // take care of configuration

        this.saveDefaultConfig();

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

            PluginCommand pC = this.getCommand(REGISTER_COMMAND);
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

        this.spawnCooldown = config.getLong("command-cooldown.spawn", 1800);
        if (this.spawnCooldown > 0) {

            this.spawnCooldown *= TPS;
            spawnCooldownSet = new HashSet<>();
            this.doSpawnCooldown = true;
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

            BlockState block;

            block = world.getBlockAt(0, 255, 0).getState();
            block.setType(Material.SMOOTH_QUARTZ_STAIRS);
            block.update(true);

            spawnLocation = block.getLocation().clone();

            block = world.getBlockAt(0, 254, 0).getState();
            block.setType(Material.WATER);
            block.update(true);

            for (Chunk chunk : world.getLoadedChunks()) {

                protectHandler.loadChunk(chunk, getLogger());
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

        if (command.getName().equals(SPAWN_COMMAND)) {

            if (sender instanceof Player) {

                Player player = (Player) sender;
                final UUID id = player.getUniqueId();

                Optional<Integer> wrapId = identifier.getIdentity(id, getLogger());
                if (!wrapId.isPresent()) {

                    return true;
                }

                if ((doSpawnCooldown && spawnCooldownSet.contains(id))) {

                    return true;
                }

                if (spawnLocation == null) {

                    player.sendMessage("Command not setup properly. Contact admin?");
                    return true;
                }

                if (doSpawnCooldown) {

                    spawnCooldownSet.add(id);
                    getServer().getScheduler()
                               .runTaskLater(this,
                                             () -> spawnCooldownSet.remove(id),
                                             spawnCooldown);
                }

                player.teleport(spawnLocation);
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

        if (identifier != null) {

            identifier.save(getLogger());
            identifier = null;
        }

        if (protectHandler != null) {

            protectHandler.finalSave(getLogger());
            protectHandler = null;
        }
    }
}
