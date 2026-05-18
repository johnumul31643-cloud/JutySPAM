package com.spampvp.commands;

import com.spampvp.SpamPVP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SpamCommand implements CommandExecutor, TabCompleter {

    private final SpamPVP plugin;

    public SpamCommand(SpamPVP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("spampvp.admin")) {
            sender.sendMessage(plugin.getPrefix() + plugin.color(
                    plugin.getConfig().getString("messages.no-permission", "&cNo tienes permiso.")));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on" -> {
                plugin.setPluginEnabled(true);
                plugin.applySpeedToAll(true);
                Bukkit.broadcastMessage(plugin.getPrefix() + plugin.color(
                        plugin.getConfig().getString("messages.enabled", "&aSpamPVP activado!")));
            }
            case "off" -> {
                plugin.setPluginEnabled(false);
                plugin.applySpeedToAll(false);
                Bukkit.broadcastMessage(plugin.getPrefix() + plugin.color(
                        plugin.getConfig().getString("messages.disabled", "&cSpamPVP desactivado!")));
            }
            case "reload" -> {
                plugin.reloadConfig();
                plugin.applySpeedToAll(plugin.isPluginEnabled());
                sender.sendMessage(plugin.getPrefix() + plugin.color(
                        plugin.getConfig().getString("messages.reloaded", "&eConfiguración recargada!")));
            }
            case "status" -> {
                String state = plugin.isPluginEnabled() ? plugin.color("&aACTIVO") : plugin.color("&cINACTIVO");
                sender.sendMessage(plugin.getPrefix() + plugin.color("&7Estado: ") + state);
                sender.sendMessage(plugin.getPrefix() + plugin.color("&7Velocidad de ataque: &e"
                        + plugin.getConfig().getDouble("spam.attack-speed", 1024.0)));
                sender.sendMessage(plugin.getPrefix() + plugin.color("&7Críticos: &e"
                        + plugin.getConfig().getInt("crits.chance", 85) + "% x"
                        + plugin.getConfig().getDouble("crits.multiplier", 2.0)));
                sender.sendMessage(plugin.getPrefix() + plugin.color("&7Barridos: &e"
                        + plugin.getConfig().getInt("sweeps.chance", 90) + "% radio "
                        + plugin.getConfig().getDouble("sweeps.radius", 3.5)));
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.color("&8&m--------------------------"));
        sender.sendMessage(plugin.color("  &c&lSpamPVP &7Commands"));
        sender.sendMessage(plugin.color("&8&m--------------------------"));
        sender.sendMessage(plugin.color("&e/spampvp on &7- Activar"));
        sender.sendMessage(plugin.color("&e/spampvp off &7- Desactivar"));
        sender.sendMessage(plugin.color("&e/spampvp reload &7- Recargar config"));
        sender.sendMessage(plugin.color("&e/spampvp status &7- Ver estado"));
        sender.sendMessage(plugin.color("&8&m--------------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("on", "off", "reload", "status");
        }
        return Collections.emptyList();
    }
}
