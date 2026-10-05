package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class BerserkAbility extends Ability {

    private final Set<UUID> berserkers;
    private final Set<ItemStack> deniedPotions;
    private final Set<PotionEffect> effects;
    private final int berserkSeconds;

    public BerserkAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "Berserk"
        );
        this.berserkers = new HashSet<>();
        this.deniedPotions = new HashSet<>();
        this.effects = getAbilitiesConfig().getStringList("BERSERK.EFFECTS").stream().map(Serializer::getEffect).collect(Collectors.toSet());
        this.berserkSeconds = getAbilitiesConfig().getInt("BERSERK.BERSERK_TIME");
        this.load();
    }

    private void load() {
        for (String s : getAbilitiesConfig().getStringList("BERSERK.DENY_POTION")) {
            String[] split = s.split(", ");
            deniedPotions.add(ItemUtils.tryGetPotion(getManager(), ItemUtils.getMat(split[0]), Integer.parseInt(split[1])));
        }
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        takeItem(player);
        applyCooldown(player);

        berserkers.add(player.getUniqueId());
        Tasks.executeLater(getManager(), 20L * berserkSeconds, () -> berserkers.remove(player.getUniqueId()));

        for (PotionEffect effect : effects) {
            getInstance().getClassManager().addEffect(player, effect);
        }

        for (String s : getLanguageConfig().getStringList("ABILITIES.BERSERK.USED")) {
            player.sendMessage(s);
        }
    }

    @EventHandler
    public void onThrow(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;
        if (!getAbilitiesConfig().getBoolean("BERSERK.DENY_POT")) return;
        if (e.getItem() == null) return;

        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (!berserkers.contains(player.getUniqueId())) return;

        for (ItemStack deniedPotion : deniedPotions) {
            if (deniedPotion.isSimilar(item)) {
                e.setCancelled(true);
                player.updateInventory();
                player.sendMessage(getLanguageConfig().getString("ABILITIES.BERSERK.DENY_POT"));
                break;
            }
        }
    }
}