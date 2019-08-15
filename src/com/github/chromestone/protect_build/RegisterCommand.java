package com.github.chromestone.protect_build;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;

/**
 * The RegisterCommand class
 * Created by Derek Zhang on 8/11/19.
 */
public class RegisterCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final MyIdentifier identifier;
    private final List<String> passwords;
    private final long cooldownTime;
    private final boolean doCooldown;
    private final HashSet<UUID> cooldownSet;

    RegisterCommand(JavaPlugin plugin, MyIdentifier identifier, List<String> passwords, long cooldownTime) {

        this.plugin = plugin;
        this.identifier = identifier;
        this.passwords = passwords;
        this.cooldownTime = cooldownTime;
        this.doCooldown = cooldownTime > 0;

        this.cooldownSet = doCooldown ? new HashSet<>() : null;
    }

    private boolean secureEquals(String sanitary, String tainted) {

        boolean result = true;

        final int sLen = sanitary.length(), tLen = tainted.length();

        int sIdx = 0, tIdx = 0;
        // no short circuit
        for (; sIdx < sLen & tIdx < tLen; sIdx++, tIdx++) {

            if (sanitary.charAt(sIdx) != tainted.charAt(tIdx)) {

                result = false;
            }
        }

        if (tIdx < tLen) {

            result = false;
            for (; tIdx < tLen; tIdx++) {

                if (tainted.charAt(tIdx) == '\0') {

                    result = true;
                }
            }
            // this prevents optimization
            if (result) {

                plugin.getLogger().log(Level.WARNING, "someone typed null character");
            }

            result = false;
        }
        else if (result) {

            result = sLen == tLen;
        }

        return result;
    }

    private boolean secureEquals(List<String> sanitary, List<String> tainted) {

        boolean result = true;

        final int sLen = sanitary.size(), tLen = tainted.size();

        int sIdx = 0, tIdx = 0;
        // no short circuit
        for (; sIdx < sLen & tIdx < tLen; sIdx++, tIdx++) {

            if (!secureEquals(sanitary.get(sIdx), tainted.get(tIdx))) {

                result = false;
            }
        }

        if (tIdx < tLen) {

            result = false;
            for (; tIdx < tLen; tIdx++) {

                if (secureEquals(tainted.get(tIdx), "\0")) {

                    result = true;
                }
            }
            // this prevents optimization
            if (result) {

                plugin.getLogger().log(Level.WARNING, "someone typed null character (tLen > sLen)");
            }

            result = false;
        }
        else if (result) {

            result = sLen == tLen;
        }

        return result;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (commandSender instanceof Player) {

            Player player = (Player) commandSender;
            final UUID id = player.getUniqueId();

            Optional<Integer> wrapId = identifier.getIdentity(id, plugin.getLogger());
            if (wrapId.isPresent()) {

                player.sendMessage("Already registered.");
                return true;
            }

            if (doCooldown && cooldownSet.contains(id)) {

                return true;
            }

            if (!secureEquals(passwords, Arrays.asList(args))) {

                if (doCooldown) {

                    cooldownSet.add(id);

                    plugin.getServer()
                            .getScheduler()
                            .runTaskLater(plugin,
                                    () -> cooldownSet.remove(id),
                                    cooldownTime);
                }

                player.sendMessage("Incorrect.");

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