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

import java.util.*;

import static org.bukkit.event.EventPriority.HIGH;

/**
 * The ChunkListener class
 * Created by Derek Zhang on 8/6/19.
 */
public class MyListener implements Listener {

    //TODO populate this as more blocks added or remove as functionality changes
    private static final EnumSet<Material> INTERACT_ALLOWED = EnumSet.of(
            Material.CRAFTING_TABLE,
            Material.ENCHANTING_TABLE,
            Material.GRINDSTONE,
            Material.LOOM,
            Material.STONECUTTER,
            Material.CARTOGRAPHY_TABLE
    );

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

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        if (!applyResistance) {

            return;
        }

        Location location = event.getRespawnLocation();
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

        if (event.getWorld().getEnvironment() == World.Environment.NORMAL) {

            handler.loadChunk(event.getChunk(), plugin.getLogger());
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {

        if (event.getWorld().getEnvironment() == World.Environment.NORMAL) {

            handler.unloadChunk(event.getChunk(), plugin.getLogger());
        }
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {

        if (event.getWorld().getEnvironment() == World.Environment.NORMAL) {

            handler.save(plugin.getLogger());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        Block block = event.getClickedBlock();

        if (block != null &&
            block.getWorld().getEnvironment() == World.Environment.NORMAL) {

            Player player = event.getPlayer();
            Optional<Integer> wrapped = identifier.getIdentity(player.getUniqueId(), plugin.getLogger());
            if (!wrapped.isPresent()) {

                event.setCancelled(true);
                return;
            }

            Action action = event.getAction();

            //Bukkit.broadcastMessage(action.toString());
            //Bukkit.broadcastMessage(block.getType().toString());

            //Block target = block.getRelative(event.getBlockFace());

            //Bukkit.broadcastMessage(target.getType().toString());

            boolean special = action == Action.RIGHT_CLICK_BLOCK;

            if (special) {

                Material material = block.getType();
                //Bukkit.broadcastMessage(material.toString());
                if (INTERACT_ALLOWED.contains(material)) {

                    return;
                }
            }

            Integer identity = wrapped.get();
            Block target = special ? block : block.getRelative(event.getBlockFace());

            Optional<Boolean> isOwner = handler.isBlockOwner(target, identity);
            if (!isOwner.isPresent()) {

                event.setCancelled(true);
                player.sendMessage(ChatColor.YELLOW + "Something is slowing down grief protection. Contact admin?");
                return;
            }

            if (!isOwner.get()) {

                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();

        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {

            return;
        }

        Optional<Integer> wrapped = identifier.getIdentity(player.getUniqueId(), plugin.getLogger());
        if (!wrapped.isPresent()) {

            event.setCancelled(true);
            return;
        }

        Integer identity = wrapped.get();
        Block block = event.getBlock();

        Optional<Boolean> isOwner = handler.isBlockOwner(block, identity);
        if (!isOwner.isPresent()) {

            event.setCancelled(true);
            player.sendMessage(ChatColor.YELLOW + "Something is slowing down grief protection. Contact admin?");
            return;
        }

        if (!isOwner.get()) {

            event.setCancelled(true);
            return;
        }

        boolean result = handler.setBlockOwner(block, identity);

        if (!result) {

            player.sendMessage(ChatColor.RED + "Unable to activate grief protection. You are on your own.");
        }
    }

    @EventHandler(priority = HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();

        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {

            return;
        }

        Optional<Integer> wrapped = identifier.getIdentity(player.getUniqueId(), plugin.getLogger());
        if (!wrapped.isPresent()) {

            event.setCancelled(true);
            return;
        }

        Integer identity = wrapped.get();
        Block block = event.getBlock();

        Optional<Boolean> isOwner = handler.isBlockOwner(block, identity);
        if (!isOwner.isPresent()) {

            event.setCancelled(true);
            player.sendMessage(ChatColor.YELLOW + "Something is slowing down grief protection. Contact admin?");
            return;
        }

        if (!isOwner.get()) {

            event.setCancelled(true);
            return;
        }

        handler.removeBlockOwner(block);
    }
}
