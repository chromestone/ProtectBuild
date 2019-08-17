package com.github.chromestone.protect_build;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerRespawnEvent;
//import org.bukkit.event.world.ChunkLoadEvent;
//import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;

/**
 * The ChunkListener class
 * Created by Derek Zhang on 8/6/19.
 */
public class MyListener implements Listener {

    private final JavaPlugin plugin;
    private final ProtectHandler handler;
    private final int resistDuration;
    private final boolean applyResistance;

    MyListener(JavaPlugin plugin, ProtectHandler handler, int resistDuration) {

        this.plugin = plugin;
        this.handler = handler;
        this.resistDuration = resistDuration;
        applyResistance = resistDuration > 0;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        if (!applyResistance) {

            return;
        }

        final Location location = event.getRespawnLocation();
        final int x = location.getBlockX(), z = location.getBlockZ();
        if (x == 0 && z == 0) {

            final Player player = event.getPlayer();
            plugin.getServer()
                   .getScheduler()
                   .runTask(plugin,
                            () -> player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,
                                                                          resistDuration, 5,
                                                                          false, false),
                                                         true));
        }
    }
/*
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

        handler.loadChunk(event.getChunk(), plugin.getLogger());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {

        handler.unloadChunk(event.getChunk());
    }
    */
}
