package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.Serializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class MagicRockAbility extends Ability {

    private final Map<Integer, PotionEffect> effects;

    public MagicRockAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.HIT_PLAYER,
                "Magic Rock"
        );
        this.effects = new HashMap<>();
        this.load();
    }

    private void load() {
        for (String s : getAbilitiesConfig().getStringList("MAGIC_ROCK.EFFECTS_PER_BLOCK")) {
            String[] split = s.split(": ");
            effects.put(Integer.parseInt(split[0]), Serializer.getEffect(split[1]));
        }
    }

    @Override
    public void onHit(Player damager, Player damaged) {
        if (cannotUse(damager)) return;
        if (hasCooldown(damager)) return;

        int height = getBaseHeight(damaged);
        PotionEffect effect = effects.get(height);

        if (height == 0 || effect == null) {
            damager.sendMessage(getLanguageConfig().getString("ABILITIES.MAGIC_ROCK.NOT_FOUND"));
            return;
        }

        takeItem(damager);
        applyCooldown(damager);
        getInstance().getClassManager().addEffect(damager, effect);

        for (String s : getLanguageConfig().getStringList("ABILITIES.MAGIC_ROCK.USED")) {
            damager.sendMessage(s
                    .replace("%height%", String.valueOf(height))
            );
        }
    }

    public int getBaseHeight(Player player) {
        Location location = player.getLocation().clone();

        int height = 0;

        for (int i = location.getBlockY(); i < location.getWorld().getMaxHeight(); i++) {
            Block block = location.getWorld().getBlockAt(location.getBlockX(), i, location.getBlockZ());

            if (block.getType() != Material.AIR && block.getType().isBlock()) {
                height = i - location.getBlockY();
            }
        }

        return height;
    }
}