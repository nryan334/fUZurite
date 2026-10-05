package me.keano.azurite.modules.pvpclass.type.mage;

import lombok.Getter;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.pvpclass.cooldown.CustomCooldown;
import me.keano.azurite.modules.pvpclass.cooldown.EnergyCooldown;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.extra.Triple;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class MageClass extends PvPClass {

    // uses maps/tables for instant lookup times.
    private final Map<UUID, EnergyCooldown> mageEnergy;
    private final Triple<Material, Short, MageEffect> clickableEffects;

    private CustomCooldown mageEffectCooldown;
    private int maxMageEnergy;
    private int mageCooldown;

    public MageClass(PvPClassManager manager) {
        super(manager, "Mage");

        this.mageEnergy = new HashMap<>();
        this.clickableEffects = new Triple<>();

        this.load();
    }

    @Override
    public void load() {
        this.mageEffectCooldown = new CustomCooldown(this, getScoreboardConfig().getString("MAGE_CLASS.MAGE_EFFECT"));
        this.maxMageEnergy = getClassesConfig().getInt("MAGE_CLASS.MAX_ENERGY");
        this.mageCooldown = getClassesConfig().getInt("MAGE_CLASS.MAGE_COOLDOWN");

        getClassesConfig().getConfigurationSection("MAGE_CLASS.CLICKABLE_EFFECTS").getKeys(false).forEach(s -> {
            String path = "MAGE_CLASS.CLICKABLE_EFFECTS." + s;
            String material = getClassesConfig().getString(path + ".MATERIAL");
            Map<String, Object> map = getClassesConfig().getConfigurationSection(path).getValues(false);

            clickableEffects.put(
                    ItemUtils.getMat(material),
                    (short) getClassesConfig().getInt(path + ".DATA"),
                    new MageEffect(getManager(), map)
            );
        });
    }

    @Override
    public void handleEquip(Player player) {
        mageEnergy.put(player.getUniqueId(), new EnergyCooldown(player.getUniqueId(), maxMageEnergy));
    }

    @Override
    public void handleUnequip(Player player) {
        mageEnergy.remove(player.getUniqueId());
    }

    @Override
    public void reload() {
        clickableEffects.clear();
        this.load();
        this.loadEffectsArmor();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (item != null && players.contains(player.getUniqueId())) {
            short data = (short) getManager().getData(item);
            MageEffect effect = clickableEffects.get(item.getType(), data);
            EnergyCooldown energyCooldown = getEnergyCooldown(player);

            if (item.hasItemMeta() && item.getItemMeta().hasLore()) return;
            if (effect == null) return;

            if (mageEffectCooldown.hasCooldown(player)) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.MAGE_CLASS.BUFF_COOLDOWN")
                        .replace("%seconds%", mageEffectCooldown.getRemaining(player))
                );
                return;
            }

            if (energyCooldown.checkEnergy(effect.getEnergyRequired())) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.MAGE_CLASS.INSUFFICIENT_ENERGY")
                        .replace("%energy%", String.valueOf(effect.getEnergyRequired()))
                        .replace("%current%", String.valueOf((int) energyCooldown.getEnergy()))
                );
                return;
            }

            if (checkMage(player)) return;

            getInstance().getTimerManager().getCombatTimer().applyTimer(player);
            getManager().takeItemInHand(player, 1);
            energyCooldown.takeEnergy(effect.getEnergyRequired());

            mageEffectCooldown.applyCooldown(player, mageCooldown);
            effect.applyEffect(player);
        }
    }

    private boolean checkMage(Player player) {
        TimerManager timerManager = getInstance().getTimerManager();

        if (timerManager.getPvpTimer().hasTimer(player) || timerManager.getInvincibilityTimer().hasTimer(player)) {
            player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.MAGE_CLASS.CANNOT_MAGE_PVPTIMER"));
            return true;

        } else if (getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation()) instanceof SafezoneTeam) {
            player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.MAGE_CLASS.CANNOT_MAGE_SAFEZONE"));
            return true;
        }

        return false;
    }

    public EnergyCooldown getEnergyCooldown(Player player) {
        return mageEnergy.get(player.getUniqueId());
    }
}