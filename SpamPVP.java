package com.spampvp;

import com.spampvp.commands.SpamCommand;
import com.spampvp.listeners.CombatListener;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SpamPVP extends JavaPlugin {

    private static SpamPVP instance;
    private boolean pluginEnabled;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        pluginEnabled = getConfig().getBoolean("enabled", true);

        getServer().getPluginManager().registerEvents(new CombatListener(this), this);

        SpamCommand cmd = new SpamCommand(this);
        getCommand("spampvp").setExecutor(cmd);
        getCommand("spampvp").setTabCompleter(cmd);

        applySpeedToAll(pluginEnabled);

        getLogger().info("SpamPVP enabled successfully!");
    }

    @Override
    public void onDisable() {
        applySpeedToAll(false);
        getLogger().info("SpamPVP disabled.");
    }

    public void applySpeedToAll(boolean active) {
        double speed = active ? getConfig().getDouble("spam.attack-speed", 1024.0) : 4.0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            setAttackSpeed(p, speed);
        }
    }

    public void setAttackSpeed(Player player, double speed) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attr != null) {
            attr.setBaseValue(speed);
        }
    }

    public boolean isPluginEnabled() {
        return pluginEnabled;
    }

    public void setPluginEnabled(boolean value) {
        pluginEnabled = value;
    }

    public String color(String text) {
        return text.replace("&", "\u00a7");
    }

    public String getPrefix() {
        return color(getConfig().getString("messages.prefix", "&c[SpamPVP] "));
    }

    public static SpamPVP getInstance() {
        return instance;
    }
}
