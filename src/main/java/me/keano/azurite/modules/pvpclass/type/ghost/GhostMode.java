package me.keano.azurite.modules.pvpclass.type.ghost;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class GhostMode extends Module<PvPClassManager> {

    private final String name;
    private final List<PotionEffect> effects;
    private final List<PotionEffectType> removeEffects;
    private final boolean hideArmor;
    private final int hideArmorTime;

    public GhostMode(PvPClassManager manager, String second) {
        super(manager);
        this.name = CC.t(getClassesConfig().getString("GHOST_CLASS.PER_SECOND_MODE." + second + ".MODE"));
        this.effects = getClassesConfig().getStringList("GHOST_CLASS.PER_SECOND_MODE." + second + ".EFFECTS")
                .stream()
                .map(Serializer::getEffect)
                .collect(Collectors.toList());
        this.removeEffects = getClassesConfig().getStringList("GHOST_CLASS.PER_SECOND_MODE." + second + ".REMOVE_EFFECTS")
                .stream()
                .map(PotionEffectType::getByName)
                .collect(Collectors.toList());
        this.hideArmor = getClassesConfig().getBoolean("GHOST_CLASS.PER_SECOND_MODE." + second + ".HIDE_ARMOR");
        this.hideArmorTime = getClassesConfig().getInt("GHOST_CLASS.PER_SECOND_MODE." + second + ".HIDE_ARMOR_TIME");
    }

    public void applyMode(Player player) {
        GhostClass ghostClass = getManager().getGhostClass();
        GhostData ghostData = ghostClass.getData().get(player.getUniqueId());

        if (!ghostData.getMode().equals(name))
            player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.GHOST_CLASS.CHANGED_MODE")
                    .replace("%mode%", name)
            );

        ghostData.setMode(name);

        for (PotionEffectType removeEffect : removeEffects)
            player.removePotionEffect(removeEffect);

        for (PotionEffect effect : effects)
            getInstance().getClassManager().addEffect(player, effect);

        if (hideArmor) {
            ghostClass.hideArmor(player);
            Tasks.executeLater(getManager(), 5L, () -> ghostClass.getInvisible().put(player.getEntityId(), player.getUniqueId()));
            ghostData.getTasks().add(new BukkitRunnable() {
                @Override
                public void run() {
                    ghostClass.getInvisible().remove(player.getEntityId());
                    Tasks.executeLater(getManager(), 5L, () -> ghostClass.showArmor(player));
                }
            }.runTaskLater(getInstance(), 20L * hideArmorTime));
        }
    }
}