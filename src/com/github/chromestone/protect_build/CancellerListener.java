package com.github.chromestone.protect_build;

import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * The CancellerListener class
 * Created by Derek Zhang on 8/11/19.
 */
public class CancellerListener implements Listener {

    private final JavaPlugin plugin;
    private final MyIdentifier identifier;
    private final int maxEntities;

    CancellerListener(JavaPlugin plugin, MyIdentifier identifier, int maxEntities) {

        this.plugin = plugin;
        this.identifier = identifier;
        this.maxEntities = maxEntities;
    }

    private void checkRegistration(Cancellable cancellable, Player player) {

        UUID id = player.getUniqueId();

        Optional<Integer> wrapId = identifier.getIdentity(id, plugin.getLogger());
        if (!wrapId.isPresent()) {

            cancellable.setCancelled(true);
        }
    }

    private <T extends PlayerEvent & Cancellable> void checkRegistration(T event) {

        checkRegistration(event, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {

        Entity entity = event.getEntity();

        if (entity instanceof Player) {

            checkRegistration(event, (Player) entity);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        Entity entity = event.getEntity();

        if (entity instanceof Player) {

            checkRegistration(event, (Player) entity);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {

        Entity entity = event.getEntity();

        if (entity instanceof Player) {

            checkRegistration(event, (Player) entity);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onOpenInventory(InventoryOpenEvent event) {

        Entity entity = event.getPlayer();
        if (entity instanceof Player) {

            checkRegistration(event, (Player) entity);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {

        checkRegistration(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        checkRegistration(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        checkRegistration(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {

        checkRegistration(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {

        checkRegistration(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {

        checkRegistration(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {

        checkRegistration(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {

        checkRegistration(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {

        checkRegistration(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {

        checkRegistration(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {

        if (event.getLocation().getChunk().getEntities().length > maxEntities) {

            event.setCancelled(true);
        }
        else if (event.getEntityType() == EntityType.ENDER_DRAGON) {

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {

        if (event.getBucket() == Material.LAVA_BUCKET) {

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {

        if (event.getItem().getType() == Material.LAVA_BUCKET) {

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {

        event.setCancelled(true);
    }
/*
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntity(EntityEvent event) {

        if (event.getEntityType() == EntityType.ENDER_DRAGON) {

            event.getEntity().remove();
        }
    }
*/
}
