package me.keano.azurite.modules.pvpclass.type.bard;

import lombok.Getter;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.pvpclass.cooldown.CustomCooldown;
import me.keano.azurite.modules.pvpclass.cooldown.EnergyCooldown;
import me.keano.azurite.modules.teams.type.EventTeam;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.extra.Triple;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class BardClass extends PvPClass {

    // uses maps/tables for instant lookup times.
    private final Map<UUID, EnergyCooldown> bardEnergy;
    private final Triple<Material, Short, BardEffect> holdableEffects;
    private final Triple<Material, Short, BardEffect> clickableEffects;

    private CustomCooldown bardEffectCooldown;

    private Material kbItem;
    private boolean kbItemEnabled;
    private int kbItemRadius;
    private int kbItemData;
    private int kbItemEnergy;

    private int maxBardEnergy;
    private int bardCooldown;

    public BardClass(PvPClassManager manager) {
        super(manager, "Bard");

        this.bardEnergy = new HashMap<>();
        this.holdableEffects = new Triple<>();
        this.clickableEffects = new Triple<>();

        this.load();
        Tasks.executeScheduled(getManager(), 20, this::tick);
    }

    @Override
    public void load() {
        this.bardEffectCooldown = new CustomCooldown(this, getScoreboardConfig().getString("BARD_CLASS.BARD_EFFECT"));
        this.kbItem = ItemUtils.getMat(getClassesConfig().getString("BARD_CLASS.KNOCKBACK_ITEM.MATERIAL"));
        this.kbItemEnabled = getClassesConfig().getBoolean("BARD_CLASS.KNOCKBACK_ITEM.ENABLED");
        this.kbItemRadius = getClassesConfig().getInt("BARD_CLASS.KNOCKBACK_ITEM.RADIUS");
        this.kbItemData = getClassesConfig().getInt("BARD_CLASS.KNOCKBACK_ITEM.DATA");
        this.kbItemEnergy = getClassesConfig().getInt("BARD_CLASS.KNOCKBACK_ITEM.ENERGY_REQUIRED");
        this.maxBardEnergy = getClassesConfig().getInt("BARD_CLASS.MAX_ENERGY");
        this.bardCooldown = getClassesConfig().getInt("BARD_CLASS.BARD_COOLDOWN");

        for (String s : getClassesConfig().getConfigurationSection("BARD_CLASS.CLICKABLE_EFFECTS").getKeys(false)) {
            String path = "BARD_CLASS.CLICKABLE_EFFECTS." + s;
            String material = getClassesConfig().getString(path + ".MATERIAL");
            Map<String, Object> map = getClassesConfig().getConfigurationSection(path).getValues(false);

            clickableEffects.put(
                    ItemUtils.getMat(material),
                    (short) getClassesConfig().getInt(path + ".DATA"),
                    new BardEffect(getManager(), map, true)
            );
        }

        for (String s : getClassesConfig().getConfigurationSection("BARD_CLASS.HOLDABLE_EFFECTS").getKeys(false)) {
            String path = "BARD_CLASS.HOLDABLE_EFFECTS." + s;
            String material = getClassesConfig().getString(path + ".MATERIAL");
            Map<String, Object> map = getClassesConfig().getConfigurationSection(path).getValues(false);

            holdableEffects.put(
                    ItemUtils.getMat(material),
                    (short) getClassesConfig().getInt(path + ".DATA"),
                    new BardEffect(getManager(), map, false)
            );
        }
    }

    @Override
    public void handleEquip(Player player) {
        bardEnergy.put(player.getUniqueId(), new EnergyCooldown(player.getUniqueId(), maxBardEnergy));
    }

    @Override
    public void handleUnequip(Player player) {
        bardEnergy.remove(player.getUniqueId());
    }

    @Override
    public void reload() {
        clickableEffects.clear();
        holdableEffects.clear();
        this.load();
        this.loadEffectsArmor();
    }

    @EventHandler // This will make it, so we don't have to run the check task as fast.
    public void onHeld(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItem(e.getNewSlot());

        if (item != null && players.contains(player.getUniqueId())) {
            short data = (short) getManager().getData(item);
            BardEffect effect = holdableEffects.get(item.getType(), data);

            if (effect == null) return;
            if (checkBard(player, false)) return;

            effect.applyEffect(player);
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (item == null) return;
        if (!players.contains(player.getUniqueId())) return;

        EnergyCooldown energyCooldown = getEnergyCooldown(player);

        if (kbItemEnabled && item.getType() == kbItem && getManager().getData(item) == kbItemData) {
            if (bardEffectCooldown.hasCooldown(player)) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BARD_CLASS.BUFF_COOLDOWN")
                        .replace("%seconds%", bardEffectCooldown.getRemaining(player))
                );
                return;
            }

            if (energyCooldown.checkEnergy(kbItemEnergy)) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BARD_CLASS.INSUFFICIENT_ENERGY")
                        .replace("%energy%", String.valueOf(kbItemEnergy))
                        .replace("%current%", String.valueOf((int) energyCooldown.getEnergy()))
                );
                return;
            }

            if (getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof EventTeam) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BARD_CLASS.CANNOT_KNOCKBACK_EVENT"));
                return;
            }

            if (checkBard(player, true)) return;

            energyCooldown.takeEnergy(kbItemEnergy);
            bardEffectCooldown.applyCooldown(player, bardCooldown);
            getInstance().getTimerManager().getCombatTimer().applyTimer(player);
            getManager().takeItemInHand(player, 1);

            for (Entity entity : player.getNearbyEntities(kbItemRadius, kbItemRadius, kbItemRadius)) {
                if (!(entity instanceof Player)) continue;

                Player p = (Player) entity;
                if (!getInstance().getTeamManager().canHit(player, p, false)) continue;

                Location centerLoc = player.getEyeLocation();
                Location throwLoc = p.getEyeLocation();

                double x = throwLoc.getX() - centerLoc.getX();
                double y = throwLoc.getY() - centerLoc.getY();
                double z = throwLoc.getZ() - centerLoc.getZ();

                Vector throwVector = new Vector(x, y, z);

                throwVector.normalize();
                throwVector.multiply(getClassesConfig().getDouble("BARD_CLASS.KNOCKBACK_ITEM.MULTIPLIER"));
                throwVector.setY(getClassesConfig().getDouble("BARD_CLASS.KNOCKBACK_ITEM.YVELOCITY"));

                p.setVelocity(throwVector);
            }
            return;
        }

        short data = (short) getManager().getData(item);
        BardEffect effect = clickableEffects.get(item.getType(), data);

        if (item.hasItemMeta() && item.getItemMeta().hasLore()) return;
        if (effect == null) return;

        if (bardEffectCooldown.hasCooldown(player)) {
            player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BARD_CLASS.BUFF_COOLDOWN")
                    .replace("%seconds%", bardEffectCooldown.getRemaining(player))
            );
            return;
        }

        if (energyCooldown.checkEnergy(effect.getEnergyRequired())) {
            player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BARD_CLASS.INSUFFICIENT_ENERGY")
                    .replace("%energy%", String.valueOf(effect.getEnergyRequired()))
                    .replace("%current%", String.valueOf((int) energyCooldown.getEnergy()))
            );
            return;
        }

        if (checkBard(player, true)) return;

        energyCooldown.takeEnergy(effect.getEnergyRequired());

        getInstance().getTimerManager().getCombatTimer().applyTimer(player);
        getManager().takeItemInHand(player, 1);

        bardEffectCooldown.applyCooldown(player, bardCooldown);
        effect.applyEffect(player);
    }

    public boolean checkBard(Player player, boolean message) {
        if (getInstance().getTimerManager().getPvpTimer().hasTimer(player) ||
                getInstance().getTimerManager().getInvincibilityTimer().hasTimer(player)) {
            if (message) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BARD_CLASS.CANNOT_BARD_PVPTIMER"));
            }
            return true;

        } else if (getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof SafezoneTeam) {
            if (message) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BARD_CLASS.CANNOT_BARD_SAFEZONE"));
            }
            return true;
        }

        return false;
    }

    public EnergyCooldown getEnergyCooldown(Player player) {
        return bardEnergy.get(player.getUniqueId());
    }

    public void tick() {
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            ItemStack hand = getManager().getItemInHand(player);
            if (hand == null) continue;

            short data = (short) getManager().getData(hand);
            BardEffect effect = holdableEffects.get(hand.getType(), data);

            if (effect != null) {
                if (checkBard(player, false)) continue;
                effect.applyEffect(player);
            }
        }
    }
}