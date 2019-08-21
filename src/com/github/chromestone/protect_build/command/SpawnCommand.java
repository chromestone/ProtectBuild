package com.github.chromestone.protect_build.command;

import com.github.chromestone.protect_build.MyIdentifier;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * The SpawnCommand class
 * Created by Derek Zhang on 8/20/19.
 */
public class SpawnCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    private final MyIdentifier identifier;

    private final long cooldownTime;
    private final boolean doCooldown;
    private final HashSet<UUID> cooldownSet;

    private final Location spawnLocation;

    public SpawnCommand(JavaPlugin plugin, MyIdentifier identifier, long cooldownTime, Location spawnLocation) {

        this.plugin = plugin;
        this.identifier = identifier;

        this.cooldownTime = cooldownTime;
        this.doCooldown = cooldownTime > 0;

        this.cooldownSet = doCooldown ? new HashSet<>() : null;

        this.spawnLocation = spawnLocation;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;
            final UUID id = player.getUniqueId();

            Optional<Integer> wrapId = identifier.getIdentity(id, plugin.getLogger());
            if (!wrapId.isPresent()) {

                return true;
            }

            if ((doCooldown && cooldownSet.contains(id))) {

                player.sendMessage("Cooldown.");
                return true;
            }

            if (spawnLocation == null) {

                player.sendMessage("Command not setup properly. Contact admin?");
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

            player.teleport(spawnLocation);
        }
        else {

            sender.sendMessage("Invalid.");
        }

        return true;
    }
}
