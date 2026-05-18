package com.spampvp.listeners;

import com.spampvp.SpamPVP;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class CombatListener implements Listener {

    private final SpamPVP plugin;
    private final Random random = new Random();
    private final Set<UUID> sweeping = new HashSet<>();

    public CombatListener(SpamPVP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!plugin.isPluginEnabled()) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (sweeping.contains(attacker.getUniqueId())) return;

        // Reset attack cooldown
        Material held = attacker.getInventory().getItemInMainHand().getType();
        attacker.setCooldown(held, 0);

        double damage = event.getDamage();
        damage += plugin.getConfig().getDouble("spam.extra-damage", 2.0);

        boolean isCrit = false;
        boolean isSweep = false;

        // Crits
        if (plugin.getConfig().getBoolean("crits.enabled", true)) {
            int chance = plugin.getConfig().getInt("crits.chance", 85);
            if (random.nextInt(100) < chance) {
                double mult = plugin.getConfig().getDouble("crits.multiplier", 2.0);
                damage *= mult;
                isCrit = true;
                spawnCritParticles(victim.getLocation().add(0, 1, 0));
            }
        }

        event.setDamage(damage);

        // Sweeps
        if (plugin.getConfig().getBoolean("sweeps.enabled", true)) {
            int chance = plugin.getConfig().getInt("sweeps.chance", 90);
            if (random.nextInt(100) < chance) {
                isSweep = true;
                doSweep(attacker, victim, damage);
            }
        }

        // Sounds
        if (isCrit && isSweep) {
            attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1f);
            attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.9f);
            attacker.sendActionBar(plugin.color("&c⚡ CRITICO &6+ &b BARRIDO &7| &e" + String.format("%.1f", damage) + " dmg"));
        } else if (isCrit) {
            attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1f);
            attacker.sendActionBar(plugin.color("&c⚡ ¡CRITICO! &7| &e" + String.format("%.1f", damage) + " dmg"));
        } else if (isSweep) {
            attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);
            attacker.sendActionBar(plugin.color("&b⟳ ¡BARRIDO! &7| &e" + String.format("%.1f", damage) + " dmg"));
        }
    }

    private void doSweep(Player attacker, LivingEntity mainVictim, double mainDamage) {
        sweeping.add(attacker.getUniqueId());
        try {
            double radius = plugin.getConfig().getDouble("sweeps.radius", 3.5);
            int pct = plugin.getConfig().getInt("sweeps.damage-percent", 75);
            int maxTargets = plugin.getConfig().getInt("sweeps.max-targets", 6);
            double sweepDamage = mainDamage * pct / 100.0;

            Location center = mainVictim.getLocation();
            World world = center.getWorld();
            if (world == null) return;

            // Sweep particles circle
            world.spawnParticle(Particle.SWEEP_ATTACK, center.clone().add(0, 1, 0), 6, 0.4, 0.1, 0.4, 0);
            for (int i = 0; i < 8; i++) {
                double angle = (2 * Math.PI / 8) * i;
                double px = Math.cos(angle) * 1.5;
                double pz = Math.sin(angle) * 1.5;
                world.spawnParticle(Particle.SWEEP_ATTACK, center.clone().add(px, 1, pz), 1, 0, 0, 0, 0);
            }

            int count = 0;
            for (Entity nearby : world.getNearbyEntities(center, radius, radius, radius)) {
                if (count >= maxTargets) break;
                if (!(nearby instanceof LivingEntity target)) continue;
                if (target.equals(attacker)) continue;
                if (target.equals(mainVictim)) continue;

                target.damage(sweepDamage, attacker);

                // Knockback
                Vector dir = target.getLocation().toVector()
                        .subtract(attacker.getLocation().toVector());
                if (dir.lengthSquared() > 0) {
                    dir.normalize().multiply(0.5).setY(0.2);
                    target.setVelocity(dir);
                }

                world.spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0);
                count++;
            }
        } finally {
            sweeping.remove(attacker.getUniqueId());
        }
    }

    private void spawnCritParticles(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;
        world.spawnParticle(Particle.CRIT, loc, 12, 0.3, 0.3, 0.3, 0.1);
        world.spawnParticle(Particle.CRIT_MAGIC, loc, 8, 0.2, 0.2, 0.2, 0.05);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.isPluginEnabled()) return;
        double speed = plugin.getConfig().getDouble("spam.attack-speed", 1024.0);
        plugin.setAttackSpeed(event.getPlayer(), speed);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.setAttackSpeed(event.getPlayer(), 4.0);
        sweeping.remove(event.getPlayer().getUniqueId());
    }
}
