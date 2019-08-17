package com.github.chromestone.protect_build;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.event.world.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.*;

import java.util.Optional;

import static org.bukkit.event.EventPriority.HIGH;

/**
 * The ChunkListener class
 * Created by Derek Zhang on 8/6/19.
 */
public class MyListener implements Listener {

    private final JavaPlugin plugin;
    private final MyIdentifier identifier;
    private final ProtectHandler handler;
    private final int resistDuration;
    private final boolean applyResistance;

    MyListener(JavaPlugin plugin, MyIdentifier identifier, ProtectHandler handler, int resistDuration) {

        this.plugin = plugin;
        this.identifier = identifier;
        this.handler = handler;
        this.resistDuration = resistDuration;
        applyResistance = resistDuration > 0;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        event.getPlayer().sendMessage("Welcome to the (Alpha) Protect Build server.\n" +
                                      "Please note the nether and the end are disabled.");
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

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

        //plugin.getServer().broadcastMessage(My2DPoint.fromChunk(event.getChunk()).toString());
        if (event.getWorld().getEnvironment() == World.Environment.NORMAL) {

            handler.loadChunk(event.getChunk(), plugin.getLogger());
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {

        //plugin.getServer().broadcastMessage(My2DPoint.fromChunk(event.getChunk()).toString());
        if (event.getWorld().getEnvironment() == World.Environment.NORMAL) {

            handler.unloadChunk(event.getChunk(), plugin.getLogger());
        }
    }

    @EventHandler(priority = HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {

        final Player player = event.getPlayer();

        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {

            return;
        }

        Optional<Integer> wrapped = identifier.getIdentity(player.getUniqueId(), plugin.getLogger());
        if (!wrapped.isPresent()) {

            event.setCancelled(true);
            return;
        }

        Integer identity = wrapped.get();
        final Block block = event.getBlock();

        //plugin.getServer().broadcastMessage(My2DPoint.fromChunk(block.getChunk()).toString());

        Optional<Boolean> isOwner = handler.isBlockOwner(block, identity);
        if (!isOwner.isPresent()) {

            event.setCancelled(true);
            player.sendMessage(ChatColor.YELLOW + "Something is slowing down grief protection. Contact admin?");
            return;
        }

        if (!isOwner.get()) {

            event.setCancelled(true);
        }

        handler.setBlockOwner(block, identity);
    }

    @EventHandler(priority = HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        final Player player = event.getPlayer();

        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {

            return;
        }

        Optional<Integer> wrapped = identifier.getIdentity(player.getUniqueId(), plugin.getLogger());
        if (!wrapped.isPresent()) {

            event.setCancelled(true);
            return;
        }

        Integer identity = wrapped.get();
        final Block block = event.getBlock();

        Optional<Boolean> isOwner = handler.isBlockOwner(block, identity);
        if (!isOwner.isPresent()) {

            event.setCancelled(true);
            player.sendMessage(ChatColor.YELLOW + "Something is slowing down grief protection. Contact admin?");
            return;
        }

        if (!isOwner.get()) {

            event.setCancelled(true);
        }

        handler.removeBlockOwner(block);
    }
}
