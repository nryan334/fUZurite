package me.keano.azurite.modules.pvpclass.type.archer;

import lombok.Getter;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.pvpclass.cooldown.ClassBuff;
import me.keano.azurite.modules.timers.listeners.playertimers.ArcherTagTimer;
import me.keano.azurite.modules.timers.listeners.servertimers.anticlean.AntiCleanTimer;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Triple;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@SuppressWarnings("deprecation")
public class ArcherClass extends PvPClass {

    private final Triple<Material, Short, ClassBuff> buffs;
    private final Map<UUID, Float> arrowForce;

    private final ArcherTagTimer archerTag;

    public ArcherClass(PvPClassManager manager) {
        super(manager, "Archer");

        this.buffs = new Triple<>();
        this.arrowForce = new HashMap<>();
        this.archerTag = getInstance().getTimerManager().getArcherTagTimer();

        this.load();
    }

    @Override
    public void load() {
        for (String key : getClassesConfig().getConfigurationSection("ARCHER_CLASS.ARCHER_BUFFS").getKeys(false)) {
            String path = "ARCHER_CLASS.ARCHER_BUFFS." + key + ".";
            String material = getClassesConfig().getString(path + "MATERIAL");
            String displayName = getClassesConfig().getString(path + "DISPLAY_NAME");
            PotionEffect effect = Serializer.getEffect(getClassesConfig().getString(path + "EFFECT"));

            int data = getClassesConfig().getInt(path + "DATA");
            int cooldown = getClassesConfig().getInt(path + "COOLDOWN");

            buffs.put(ItemUtils.getMat(material), (short) data, new ClassBuff(this, displayName, effect, cooldown));
        }
    }

    @Override
    public void handleEquip(Player player) {
    }

    @Override
    public void handleUnequip(Player player) {
    }

    @Override
    public void reload() {
        buffs.clear();
        this.load();
        this.loadEffectsArmor();
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();
        ItemStack hand = getManager().getItemInHand(player);

        if (hand == null) return;
        if (!players.contains(player.getUniqueId())) return;
        if (hand.hasItemMeta() && hand.getItemMeta().hasLore()) return;

        ClassBuff buff = buffs.get(hand.getType(), (short) getManager().getData(hand));

        if (buff != null) {
            if (buff.hasCooldown(player)) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.ARCHER_CLASS.BUFF_COOLDOWN")
                        .replace("%seconds%", buff.getRemaining(player))
                );
                return;
            }

            buff.applyCooldown(player, buff.getCooldown());
            getManager().addEffect(player, buff.getEffect());
            getManager().takeItemInHand(player, 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getProjectile() instanceof Arrow)) return;

        arrowForce.put(e.getProjectile().getUniqueId(), e.getForce());
    }

    @EventHandler(ignoreCancelled = true)
    public void onArrow(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Arrow)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player damager = Utils.getDamager(e.getDamager());

        if (damager == null) return;
        if (!players.contains(damager.getUniqueId())) return;

        this.archerTag(e);
    }

    // Focus Mode = LOW, Archer Class = NORMAL, Strength = HIGH
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player damaged = (Player) e.getEntity();

        if (archerTag.hasTimer(damaged)) {
            e.setDamage(e.getDamage() * Config.ARCHER_TAGGED_MULTIPLIER);
        }
    }

    public void archerTag(EntityDamageByEntityEvent e) {
        Arrow arrow = (Arrow) e.getDamager();
        Player damaged = (Player) e.getEntity();
        Player damager = Utils.getDamager(e.getDamager());

        if (!arrowForce.containsKey(arrow.getUniqueId())) return;

        if (players.contains(damaged.getUniqueId())) {
            damager.sendMessage(getLanguageConfig().getString("PVP_CLASSES.ARCHER_CLASS.CANNOT_MARK"));
            return;
        }

        double damage = (archerTag.hasTimer(damaged) ? Config.ARCHER_TAGGED_DAMAGE : Config.ARCHER_TAG_DAMAGE);
        float force = arrowForce.remove(arrow.getUniqueId());
        int distance = (int) damaged.getLocation().distance(damager.getLocation());
        if (force < 0.5F) damage = Config.ARCHER_HALF_FORCE_DAMAGE;
        double damageString = damage / 2;

        damaged.setHealth(Math.max(damaged.getHealth() - damage, 0));

        e.setDamage(0D); // WE NEVER DO THE ENCHANT DAMAGE + ARCHER TAG... it's too much!

        if (damaged.isDead()) {
            damaged.setLastDamageCause(new EntityDamageByEntityEvent(
                    damager,
                    damaged,
                    EntityDamageEvent.DamageCause.PROJECTILE, damage
            )); // set last damage to projectile otherwise death message won't be accurate.

            e.setCancelled(true); // we cancel the event so PlayerDeathEvent doesn't get called twice.
        }

        if (force >= 0.5F) {
            damager.sendMessage(getLanguageConfig().getString("PVP_CLASSES.ARCHER_CLASS.MARKED_PLAYER")
                    .replace("%distance%", String.valueOf(distance))
                    .replace("%seconds%", String.valueOf(archerTag.getSeconds()))
                    .replace("%damage%", damageString + " heart" + (damageString > 1 ? "s" : ""))
            );

            // If they don't have the timer send the message
            if (!archerTag.hasTimer(damaged)) {
                damaged.sendMessage(getLanguageConfig().getString("PVP_CLASSES.ARCHER_CLASS.PLAYER_MARKED")
                        .replace("%seconds%", String.valueOf(archerTag.getSeconds()))
                );
            }

            if (getClassesConfig().getBoolean("ARCHER_CLASS.TAKE_INVIS")) {
                for (PotionEffect effect : damaged.getActivePotionEffects()) {
                    if (!effect.getType().equals(PotionEffectType.INVISIBILITY)) continue;
                    getManager().getRestores().put(damaged.getUniqueId(), effect.getType(), effect);
                    break;
                }

                damaged.removePotionEffect(PotionEffectType.INVISIBILITY);
            }

            archerTag.applyTimer(damaged);

            AntiCleanTimer antiCleanTimer = getInstance().getTimerManager().getAntiCleanTimer();
            antiCleanTimer.incrementDamage(damager, damage);
            antiCleanTimer.incrementArcherTag(damager);

        } else {
            damager.sendMessage(getLanguageConfig().getString("PVP_CLASSES.ARCHER_CLASS.NOT_DRAWN_BACK")
                    .replace("%distance%", String.valueOf(distance))
                    .replace("%damage%", damageString + " heart" + (damageString > 1 ? "s" : ""))
            );
        }
    }
}