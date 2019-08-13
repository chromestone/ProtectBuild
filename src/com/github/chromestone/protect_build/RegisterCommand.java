package com.github.chromestone.protect_build;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * The RegisterCommand class
 * Created by Derek Zhang on 8/11/19.
 */
public class RegisterCommand implements CommandExecutor {

    private static final String REGISTER_KEY = "chromestone_register";
    private static final long COOLDOWN = TimeUnit.NANOSECONDS.convert(5, TimeUnit.SECONDS);

    private final JavaPlugin plugin;
    private final MyIdentifier identifier;
    private final List<String> passwords;
    private final int size;

    RegisterCommand(JavaPlugin plugin, MyIdentifier identifier, List<String> passwords) {

        this.plugin = plugin;
        this.identifier = identifier;
        this.passwords = passwords;
        size = passwords.size();
    }

    private void addCooldown(Player p, boolean first) {

        long time = System.nanoTime();

        if (!first) {

            p.removeMetadata(REGISTER_KEY, plugin);
            p.setMetadata(REGISTER_KEY, new FixedMetadataValue(plugin, time));
        }
        else {

            p.setMetadata(REGISTER_KEY, new FixedMetadataValue(plugin, time));
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (commandSender instanceof Player) {

            Player player = (Player) commandSender;
            UUID id = player.getUniqueId();

            Optional<Integer> wrapId = identifier.getIdentity(id, plugin.getLogger());
            if (wrapId.isPresent()) {

                player.sendMessage("Already registered.");
                return true;
            }

            List<MetadataValue> metaList = player.getMetadata(REGISTER_KEY);
            boolean first = metaList.isEmpty();
            if (!first) {

                if (metaList.size() != 1) {

                    plugin.getLogger().log(Level.SEVERE,
                            "collision with REGISTER KEY, multiple metadata found [{0}]",
                            metaList.size());
                    return true;
                }

                MetadataValue meta = metaList.get(0);

                if (meta.getOwningPlugin() != plugin) {

                    plugin.getLogger().log(Level.SEVERE, "REGISTER KEY set by other plugin");
                    return true;
                }

                long time = System.nanoTime();
                Object val = meta.value();

                if (val instanceof Long) {

                    if (time - (long) val < COOLDOWN) {

                        return true;
                    }
                }
                else if (val != null) {

                    plugin.getLogger().log(Level.SEVERE,
                                           "REGISTER KEY maps to value of type [{0}]", val.getClass());
                    return true;
                }
            }

            if (size != args.length) {

                addCooldown(player, first);

                return true;
            }

            boolean match = true;
            for (int i = 0; i < size; i++) {

                if (!passwords.get(i).equals(args[i])) {

                    match = false;
                    break;
                }
            }

            if (!match) {

                addCooldown(player, first);

                return true;
            }

            identifier.registerIdentity(id);

            player.sendMessage("Welcome to the server.");

            return true;
        }

        commandSender.sendMessage("Invalid.");
        return false;
    }
}