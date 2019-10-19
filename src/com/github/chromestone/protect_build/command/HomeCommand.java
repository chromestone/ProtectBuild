package com.github.chromestone.protect_build.command;

import com.github.chromestone.protect_build.MyIdentifier;
import com.github.chromestone.protect_build.My3DPoint;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * The HomeCommand class
 * Created by Derek Zhang on 8/20/19.
 */
public class HomeCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    private final MyIdentifier identifier;

    private final long cooldownTime;
    private final boolean doCooldown;
    private final HashSet<UUID> cooldownSet;

    private final UUID worldId;

    public HomeCommand(JavaPlugin plugin, MyIdentifier identifier, long cooldownTime, UUID worldId) {

        this.plugin = plugin;
        this.identifier = identifier;

        this.cooldownTime = cooldownTime;
        this.doCooldown = cooldownTime > 0;

        this.cooldownSet = doCooldown ? new HashSet<>() : null;

        this.worldId = worldId;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            final Player player = (Player) sender;
            final UUID id = player.getUniqueId();

            Optional<My3DPoint> wrapper = identifier.getLocation(id, plugin.getLogger());
            if (!wrapper.isPresent()) {

                player.sendMessage("No home set.");
                return true;
            }

            if ((doCooldown && cooldownSet.contains(id))) {

                player.sendMessage("Cooldown.");
                return true;
            }

            if (doCooldown) {

                cooldownSet.add(id);
                plugin.getServer()
                        .getScheduler()
                        .runTaskLater(plugin,
                                () -> cooldownSet.remove(id),
                                cooldownTime);
            }

            My3DPoint point = wrapper.get();

            World world = plugin.getServer().getWorld(worldId);
            if (world == null) {

                player.sendMessage("Null world error. Contact admin?");
                return true;
            }

            Block whereBlock = world.getBlockAt​(point.x, point.y, point.z);
            Chunk whereChunk = world.getChunkAt(whereBlock);
            if (!whereChunk.isLoaded()) {

                player.sendMessage("Chunk not loaded, try again later.");
                return true;
            }

            final Location whereLoc = new Location(world, point.x + 0.5, point.y, point.z + 0.5);
            plugin.getServer()
                    .getScheduler()
                    .runTask(plugin,
                            () -> player.teleport(whereLoc));
        }
        else {

            sender.sendMessage("Invalid.");
        }

        return true;
    }
}
