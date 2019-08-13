package com.github.chromestone.protect_build;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The ChunkListener class
 * Created by Derek Zhang on 8/6/19.
 */
public class ChunkListener implements Listener {

    private final JavaPlugin plugin;
    private final ProtectHandler handler;

    ChunkListener(JavaPlugin plugin, ProtectHandler handler) {

        this.plugin = plugin;
        this.handler = handler;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

        handler.loadChunk(event.getChunk(), plugin.getLogger());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {

        handler.unloadChunk(event.getChunk());
    }
}
