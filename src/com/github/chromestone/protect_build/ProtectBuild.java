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

    //ticks per second
    private final static long TPS = 20;

    private MyIdentifier identifier = null;

    @Override
    public void onEnable() {

        // take care of class attributes

        String dataDir = this.getDataFolder().getAbsolutePath();

        identifier = new MyIdentifier(dataDir);
        boolean result = identifier.init(getLogger());
        if (!result) {

            getLogger().log(Level.SEVERE, "ProtectBuild plugin NOT loading due to error");
            return;
        }

        // take care of configuration

        this.saveDefaultConfig();

        FileConfiguration config = getConfig();

        List<String> passphrases = config.getStringList("passphrases");
        if (!passphrases.isEmpty()) {

            long cooldownTime = config.getLong("register-cooldown", 5);
            if (cooldownTime > 0) {

                cooldownTime *= TPS;
            }
            PluginCommand pC = this.getCommand(REGISTER_COMMAND);
            if (pC != null) {

                pC.setExecutor(new RegisterCommand(this, identifier, passphrases, cooldownTime));
            }
            else {

                getLogger().log(Level.SEVERE, "getCommand failed");
            }
        }
        else {

            getLogger().log(Level.WARNING, "disabled register command since passphrases empty\n" +
                                           "unless previously registered, players cannot do anything\n" +
                                           "please modify ProtectBuild/config.yml");
        }

        Server server = getServer();
        PluginManager pM = server.getPluginManager();

        int maxEntities = config.getInt("chunk-spawns-until");
        if (maxEntities < 0) {

            getLogger().log(Level.INFO, "negative chunk-spawns-until, NO spawning limits will be imposed");
        }
        pM.registerEvents(new CancellerListener(this, identifier, maxEntities), this);


        //ProtectHandler handler = new ProtectHandler(this.getDataFolder().getAbsolutePath());
        //pM.registerEvents(new ChunkListener(this, handler), this);


        // run later stuff here

        BukkitScheduler scheduler = server.getScheduler();

        scheduler.runTask(this, () -> {

            Server s = getServer();
            ConsoleCommandSender cs = s.getConsoleSender();

            s.dispatchCommand(cs, "gamerule mobGriefing false");
            s.dispatchCommand(cs, "gamerule doFireTick false");
            s.dispatchCommand(cs, "gamerule maxEntityCramming 4");

            s.dispatchCommand(cs, "setworldspawn 0 255 0");
            s.dispatchCommand(cs, "gamerule spawnRadius 0");

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

            block = world.getBlockAt(0, 254, 0).getState();
            block.setType(Material.WATER);
            block.update(true);
        });

        scheduler.runTaskTimer(this, () -> {

            for (Player player : getServer().getOnlinePlayers()) {

                if (!player.isDead()) {

                    Location loc = player.getLocation();

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
    public void onDisable() {

        if (identifier != null) {

            identifier.save(getLogger());
        }
    }
}
