package me.keano.azurite.modules.pvpclass;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.pvpclass.listener.ArmorLegacyListener;
import me.keano.azurite.modules.pvpclass.listener.ArmorListener;
import me.keano.azurite.modules.pvpclass.listener.PvPClassListener;
import me.keano.azurite.modules.pvpclass.type.archer.ArcherClass;
import me.keano.azurite.modules.pvpclass.type.bard.BardClass;
import me.keano.azurite.modules.pvpclass.type.ghost.GhostClass;
import me.keano.azurite.modules.pvpclass.type.mage.MageClass;
import me.keano.azurite.modules.pvpclass.type.miner.MinerClass;
import me.keano.azurite.modules.pvpclass.type.rogue.RogueClass;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Triple;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
public class PvPClassManager extends Manager {

    private final Triple<UUID, PotionEffectType, PotionEffect> restores;

    private final Map<UUID, PvPClass> activeClasses;
    private final Map<String, PvPClass> classes;

    private BardClass bardClass;
    private ArcherClass archerClass;
    private MageClass mageClass;
    private MinerClass minerClass;
    private RogueClass rogueClass;
    private GhostClass ghostClass;

    public PvPClassManager(HCF instance) {
        super(instance);

        this.restores = new Triple<>();
        this.activeClasses = new HashMap<>();
        this.classes = new HashMap<>();

        if (getClassesConfig().getBoolean("BARD_CLASS.ENABLED")) {
            this.bardClass = new BardClass(this);
        }

        if (getClassesConfig().getBoolean("MAGE_CLASS.ENABLED")) {
            this.mageClass = new MageClass(this);
        }

        if (getClassesConfig().getBoolean("ARCHER_CLASS.ENABLED")) {
            this.archerClass = new ArcherClass(this);
        }

        if (getClassesConfig().getBoolean("MINER_CLASS.ENABLED")) {
            this.minerClass = new MinerClass(this);
        }

        if (getClassesConfig().getBoolean("ROGUE_CLASS.ENABLED")) {
            this.rogueClass = new RogueClass(this);
        }

        if (getClassesConfig().getBoolean("GHOST_CLASS.ENABLED")) {
            this.ghostClass = new GhostClass(this);
        }

        new PvPClassListener(this);

        if (Utils.isModernVer()) {
            new ArmorListener(this);

        } else new ArmorLegacyListener(this);
    }

    @Override
    public void reload() {
        for (PvPClass pvpClass : classes.values()) {
            pvpClass.reload();
        }
    }

    @Override
    public void disable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PvPClass pvpClass = getActiveClass(player);

            if (pvpClass != null) {
                pvpClass.unEquip(player);
            }
        }
    }

    public void checkArmor(Player player) {
        for (PvPClass pvpClass : getClasses().values()) {
            pvpClass.checkArmor(player);
        }
    }

    public PvPClass getActiveClass(Player player) {
        return activeClasses.get(player.getUniqueId());
    }

    public void checkClassLimit(Player player) {
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        PvPClass pvpClass = activeClasses.get(player.getUniqueId());

        if (pt != null && pvpClass != null) {
            if (pvpClass.getLimit() == -1) return;

            int amount = 0;

            for (Player onlinePlayer : pt.getOnlinePlayers(false)) {
                PvPClass memberClass = activeClasses.get(onlinePlayer.getUniqueId());

                if (memberClass != null && memberClass == pvpClass) {
                    amount++;
                }
            }

            if (amount > pvpClass.getLimit()) {
                pvpClass.unEquip(player);
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.LIMIT_REACHED")
                        .replace("%limit%", String.valueOf(pvpClass.getLimit()))
                );
            }
        }
    }

    public void addEffect(Player player, PotionEffect effect) {
        if (effect == null) return;

        // if they don't have a current effect to restore just add it normally
        if (!player.hasPotionEffect(effect.getType())) {
            player.addPotionEffect(effect);
            return;
        }

        for (PotionEffect activeEffect : player.getActivePotionEffects()) {
            // Just some checks to make sure we aren't overriding better effects
            if (!activeEffect.getType().equals(effect.getType())) continue;
            if (activeEffect.getAmplifier() > effect.getAmplifier()) break; // Use breaks now since its only 1 effect

            // Don't override if same level but has higher duration.
            if (activeEffect.getAmplifier() == effect.getAmplifier() &&
                    activeEffect.getDuration() > effect.getDuration()) break;

            // Make sure the active effect is longer than the effect
            // otherwise we will be restoring an effect that had already expired.
            if (activeEffect.getDuration() > effect.getDuration()) {
                restores.put(player.getUniqueId(), activeEffect.getType(), activeEffect);
                player.removePotionEffect(activeEffect.getType()); // Remove it so 1.16 spigot doesn't restore it.
            }

            player.addPotionEffect(effect, true); // override old one
            break;
        }
    }
}