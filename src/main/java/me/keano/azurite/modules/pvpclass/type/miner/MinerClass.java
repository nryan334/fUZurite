package me.keano.azurite.modules.pvpclass.type.miner;

import lombok.Getter;
import me.keano.azurite.modules.pvpclass.PvPClass;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.utils.Serializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class MinerClass extends PvPClass {

    private final Map<Integer, PotionEffect> minerEffects;
    private final List<UUID> invisible;
    private int minerInvisibilityLevel;

    public MinerClass(PvPClassManager manager) {
        super(manager, "Miner");

        this.minerEffects = new HashMap<>();
        this.invisible = new ArrayList<>();

        this.load();
    }

    @Override
    public void load() {
        this.minerInvisibilityLevel = getClassesConfig().getInt("MINER_CLASS.MINER_INVISIBILITY");

        getClassesConfig().getConfigurationSection("MINER_CLASS.MINER_EFFECTS").getKeys(false).forEach(s -> {
            Integer diamonds = Integer.valueOf(s);
            String effect = getClassesConfig().getString("MINER_CLASS.MINER_EFFECTS." + s);

            minerEffects.put(diamonds, Serializer.getEffect(effect));
        });
    }

    @Override
    public void handleEquip(Player player) {
        this.checkInvis(player);
    }

    @Override
    public void handleUnequip(Player player) {
        invisible.remove(player.getUniqueId());
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    @Override
    public void reload() {
        minerEffects.clear();
        this.load();
        this.loadEffectsArmor();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        // Refrain checking if they are on the same y level.
        if (e.getFrom().getBlockY() == e.getTo().getBlockY()) return;
        if (!players.contains(player.getUniqueId())) return;

        this.checkInvis(player);
    }

    @Override
    public void removeEffects(Player player) {
        super.removeEffects(player);

        for (PotionEffect effect : minerEffects.values()) {
            player.removePotionEffect(effect.getType());
        }
    }

    @Override
    public void addEffects(Player player) {
        super.addEffects(player);

        int diamonds = getInstance().getUserManager().getByUUID(player.getUniqueId()).getDiamonds();

        // Custom effects
        for (Integer integer : minerEffects.keySet()) {
            if (integer > diamonds) continue;
            getManager().addEffect(player, minerEffects.get(integer));
        }
    }

    private void checkInvis(Player player) {
        int yLevel = player.getLocation().getBlockY();

        if (!invisible.contains(player.getUniqueId()) && yLevel <= minerInvisibilityLevel) {
            // Don't override invis from a ce or some.
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;

            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
            player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.MINER_CLASS.INVIS_ENABLED"));
            invisible.add(player.getUniqueId());

        } else if (invisible.contains(player.getUniqueId()) && yLevel > minerInvisibilityLevel) {
            // Don't remove if they don't have it
            if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;

            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.MINER_CLASS.INVIS_DISABLED"));
            invisible.remove(player.getUniqueId());
        }
    }
}