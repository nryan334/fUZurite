package me.keano.azurite.modules.pvpclass.type.rogue;

import lombok.Getter;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.pvpclass.cooldown.ClassBuff;
import me.keano.azurite.modules.pvpclass.cooldown.CustomCooldown;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.extra.Triple;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class RogueClass extends PvPClass {

    private final Triple<Material, Short, ClassBuff> buffs;
    private final List<PotionEffect> backstabEffects;

    private CustomCooldown backstabCooldown;
    private Material backstabItem;

    public RogueClass(PvPClassManager manager) {
        super(manager, "Rogue");

        this.buffs = new Triple<>();
        this.backstabEffects = new ArrayList<>();

        this.load();
    }

    @Override
    public void load() {
        this.backstabEffects.addAll(getClassesConfig().getStringList("ROGUE_CLASS.BACKSTAB_EFFECTS")
                .stream()
                .map(Serializer::getEffect)
                .collect(Collectors.toList()));

        this.backstabCooldown = new CustomCooldown(this, getScoreboardConfig().getString("ROGUE_CLASS.BACKSTAB"));
        this.backstabItem = ItemUtils.getMat(getClassesConfig().getString("ROGUE_CLASS.BACKSTAB_ITEM"));

        for (String key : getClassesConfig().getConfigurationSection("ROGUE_CLASS.ROGUE_BUFFS").getKeys(false)) {
            String path = "ROGUE_CLASS.ROGUE_BUFFS." + key + ".";
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
        backstabEffects.clear();
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
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.ROGUE_CLASS.BUFF_COOLDOWN")
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
    public void onBackstab(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player damager = (Player) e.getDamager();
        Player damaged = (Player) e.getEntity();
        ItemStack hand = getManager().getItemInHand(damager);

        if (hand == null || hand.getType() != backstabItem) return;
        if (!players.contains(damager.getUniqueId())) return;

        // Used to calculate the direction to make sure they are behind the player.
        Vector damagerVector = damager.getLocation().getDirection().setY(0);
        Vector damagedVector = damaged.getLocation().getDirection().setY(0);

        double degree = damagerVector.angle(damagedVector);
        double backstabDamage = getClassesConfig().getDouble("ROGUE_CLASS.BACKSTAB_DAMAGE") * 2.0;

        if (Math.abs(degree) < 1.4) {
            if (backstabCooldown.hasCooldown(damager)) {
                damager.sendMessage(getLanguageConfig().getString("PVP_CLASSES.ROGUE_CLASS.BACKSTAB_COOLDOWN")
                        .replace("%seconds%", backstabCooldown.getRemaining(damager))
                );
                return;
            }

            backstabCooldown.applyCooldown(damager, getClassesConfig().getInt("ROGUE_CLASS.BACKSTAB_COOLDOWN"));
            getManager().takeItemInHand(damager, 1);
            getManager().playSound(damaged, getClassesConfig().getString("ROGUE_CLASS.BACKSTAB_SOUND"), true);

            damaged.getWorld().playEffect(
                    damaged.getLocation().add(0, 1, 0), Effect.STEP_SOUND,
                    ItemUtils.getMat(getClassesConfig().getString("ROGUE_CLASS.BACKSTAB_EFFECT")));

            damaged.setLastDamageCause(new RogueBackstabEvent(damaged, damager, EntityDamageEvent.DamageCause.CUSTOM, backstabDamage));
            damaged.setHealth(Math.max(damaged.getHealth() - backstabDamage, 0));

            e.setDamage(0D);

            if (damaged.isDead()) {
                e.setCancelled(true); // we cancel the event so PlayerDeathEvent doesn't get called twice.
            }

            // add the slowness and all that
            for (PotionEffect backstabEffect : backstabEffects) {
                damager.addPotionEffect(backstabEffect);
            }

        } else {
            damager.sendMessage(getLanguageConfig().getString("PVP_CLASSES.ROGUE_CLASS.BACKSTAB_FAILED"));
        }
    }
}