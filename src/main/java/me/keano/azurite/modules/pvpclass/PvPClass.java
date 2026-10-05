package me.keano.azurite.modules.pvpclass;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.pvpclass.cooldown.CustomCooldown;
import me.keano.azurite.modules.timers.listeners.playertimers.WarmupTimer;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public abstract class PvPClass extends Module<PvPClassManager> {

    protected final String name;
    protected final List<UUID> players;
    protected final List<CustomCooldown> customCooldowns; // basically your speed, jump cooldown made easier!
    protected final List<PotionEffect> effects;
    protected final int limit;
    protected Material[] armor;

    public PvPClass(PvPClassManager manager, String name) {
        super(manager);

        this.name = name;
        this.players = new ArrayList<>();
        this.customCooldowns = new ArrayList<>();
        this.effects = new ArrayList<>();
        this.limit = manager.getClassesConfig().getInt(name.toUpperCase() + "_CLASS.CLASS_LIMIT");
        this.armor = new Material[4];

        this.loadEffectsArmor();
        manager.getClasses().put(name, this);
    }

    public abstract void load();

    public abstract void handleEquip(Player player);

    public abstract void handleUnequip(Player player);

    public void reload() {
    }

    // Load armor and effects using the name.
    protected void loadEffectsArmor() {
        this.armor = new Material[]{
                ItemUtils.getMat(getClassesConfig().getString(name.toUpperCase() + "_CLASS.ARMOR.HELMET")),
                ItemUtils.getMat(getClassesConfig().getString(name.toUpperCase() + "_CLASS.ARMOR.CHESTPLATE")),
                ItemUtils.getMat(getClassesConfig().getString(name.toUpperCase() + "_CLASS.ARMOR.LEGGINGS")),
                ItemUtils.getMat(getClassesConfig().getString(name.toUpperCase() + "_CLASS.ARMOR.BOOTS"))
        };

        this.effects.clear();
        this.effects.addAll(getClassesConfig().getStringList(name.toUpperCase() + "_CLASS.PASSIVE_EFFECTS")
                .stream()
                .map(Serializer::getEffect)
                .collect(Collectors.toList())
        );
    }

    public boolean hasArmor(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        if (helmet == null || chestplate == null || leggings == null || boots == null) return false;

        return helmet.getType() == armor[0] && chestplate.getType() == armor[1]
                && leggings.getType() == armor[2] && boots.getType() == armor[3];
    }

    public void checkArmor(Player player) {
        WarmupTimer timer = getInstance().getTimerManager().getWarmupTimer();

        if (hasArmor(player) && !players.contains(player.getUniqueId())) {
            timer.putTimerWithClass(player, this);

        } else {
            if (players.contains(player.getUniqueId())) {
                unEquip(player);

            } else if (timer.hasTimer(player) && timer.getWarmups().get(player.getUniqueId()).equals(name)) {
                timer.removeTimer(player);
            }
        }
    }

    public void equip(Player player) {
        player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.EQUIPPED")
                .replace("%class%", name)
        );

        players.add(player.getUniqueId());
        handleEquip(player);
        addEffects(player);
        getManager().getActiveClasses().put(player.getUniqueId(), this);
    }

    public void unEquip(Player player) {
        player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.UNEQUIPPED")
                .replace("%class%", name)
        );

        players.remove(player.getUniqueId());
        handleUnequip(player);
        removeEffects(player);
        getManager().getRestores().removeFirst(player.getUniqueId());
        getManager().getActiveClasses().remove(player.getUniqueId());
    }

    public void addEffects(Player player) {
        for (PotionEffect effect : effects) {
            getManager().addEffect(player, effect);
        }
    }

    public void removeEffects(Player player) {
        for (PotionEffect effect : effects) {
            player.removePotionEffect(effect.getType());
        }
    }
}