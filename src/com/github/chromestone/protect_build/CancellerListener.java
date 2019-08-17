package com.github.chromestone.protect_build;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.*;
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
    private final boolean doLimit;

    CancellerListener(JavaPlugin plugin, MyIdentifier identifier, int maxEntities) {

        this.plugin = plugin;
        this.identifier = identifier;
        this.maxEntities = maxEntities;
        this.doLimit = maxEntities >= 0;
    }

    private boolean checkRegistration(Cancellable cancellable, Player player) {

        UUID id = player.getUniqueId();

        Optional<Integer> wrapId = identifier.getIdentity(id, plugin.getLogger());
        if (!wrapId.isPresent()) {

            cancellable.setCancelled(true);
            return false;
        }

        return true;
    }

    private <T extends PlayerEvent & Cancellable> boolean checkRegistration(T event) {

        return checkRegistration(event, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {

        EntityType type = event.getEntityType();

        if (type == EntityType.PLAYER) {

            checkRegistration(event, (Player) event.getEntity());
        }
        else if (event.getEntityType() == EntityType.VILLAGER) {

            EntityDamageEvent.DamageCause cause = event.getCause();

            if (cause != EntityDamageEvent.DamageCause.CRAMMING &&
                cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                cause != EntityDamageEvent.DamageCause.VOID) {

                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        EntityType type = event.getEntityType();

        if (type == EntityType.PLAYER) {

            checkRegistration(event, (Player) event.getEntity());
        }
        else if (type == EntityType.VILLAGER) {

            if (event.getDamager() instanceof Player) {

                event.setCancelled(true);
            }
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

        if (!checkRegistration(event)) {

            return;
        }

        Block block = event.getClickedBlock();
        if (block != null) {

            if (block.getWorld().getEnvironment() != World.Environment.NORMAL) {

                return;
            }

            //TODO use blockface to prevent "overreach"

            final Location location = block.getLocation();
            final int x = location.getBlockX(), z = location.getBlockZ();

            if (x <= 16 && x >= -16 && z <= 16 && z >= -16) {

                Action action = event.getAction();
                if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) {

                    event.setCancelled(true);
                }
                else {

                    Material material = event.getMaterial();
                    switch (material) {

                        case BONE_MEAL:
                        case ACACIA_SAPLING:
                        case BIRCH_SAPLING:
                        case DARK_OAK_SAPLING:
                        case JUNGLE_SAPLING:
                        case OAK_SAPLING:
                        case SPRUCE_SAPLING:
                        case BROWN_MUSHROOM:
                        case RED_MUSHROOM:
                        case WATER_BUCKET:

                            break;
                        default:

                            event.setCancelled(true);
                    }
                }
            }
        }
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

        if (!checkRegistration(event)) {

            return;
        }

        if (event.getPlayer().getWorld().getEnvironment() != World.Environment.NORMAL) {

            event.setCancelled(true);
        }
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

        if (doLimit && event.getLocation().getChunk().getEntities().length > maxEntities) {

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
    public void onPortalCreate(PortalCreateEvent event) {

        final boolean netherPair = event.getReason() == PortalCreateEvent.CreateReason.NETHER_PAIR;

        if (event.getWorld().getEnvironment() != World.Environment.NORMAL) {

            if (!netherPair) {

                event.setCancelled(true);
            }
        }
        else if (netherPair) {

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event) {

        if (event.getEntity().getWorld().getEnvironment() != World.Environment.NORMAL) {

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {

        final Location location = event.getLocation();

        final World world = location.getWorld();
        if (world == null || world.getEnvironment() != World.Environment.NORMAL) {

            return;
        }

        final int x = location.getBlockX(), z = location.getBlockZ();

        if (x > 16 || x < -16 || z > 16 || z < -16) {

            event.setCancelled(true);
        }
/* //default to warnings instead
        else {

            for (BlockState block : event.getBlocks()) {

                final Location l = block.getLocation();
                final int l_x = l.getBlockX(), l_z = l.getBlockZ();

                if (l_x > 16 || l_x < -16 || l_z > 16 || l_z < -16) {

                    block.setType(Material.AIR);//no clean way to get block before?
                }
            }
        }
*/
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {

        event.setCancelled(true);
    }
}
