package me.keano.azurite.modules.ability.type;

import lombok.Getter;
import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.extra.Cooldown;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class AntiBuildAbility extends Ability {

    private final Map<UUID, Integer> hits;
    private final List<Material> deniedInteract;
    private final List<PotionEffect> effects;
    private final Cooldown antiBuild;

    private final int maxHits;
    private final int antiBuildTime;

    public AntiBuildAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.HIT_PLAYER,
                "Anti Build"
        );
        this.hits = new HashMap<>();
        this.deniedInteract = new ArrayList<>();
        this.effects = new ArrayList<>();
        this.antiBuild = new Cooldown(manager);

        this.maxHits = getAbilitiesConfig().getInt(nameConfig + ".HITS_REQUIRED");
        this.antiBuildTime = getAbilitiesConfig().getInt(nameConfig + ".ANTI_BUILD_TIME");

        this.load();
    }

    private void load() {
        for (String s : getAbilitiesConfig().getStringList(nameConfig + ".DISABLED_INTERACT")) {
            deniedInteract.add(ItemUtils.getMat(s));
        }

        for (String s : getAbilitiesConfig().getStringList(nameConfig + ".EFFECTS_DAMAGER")) {
            effects.add(Serializer.getEffect(s));
        }
    }

    @Override
    public void onHit(Player damager, Player damaged) {
        UUID damagerUUID = damager.getUniqueId();

        if (cannotUse(damager)) return;
        if (hasCooldown(damager)) return;
        if (!hits.containsKey(damagerUUID)) hits.put(damagerUUID, 0);

        int current = hits.get(damagerUUID) + 1;
        hits.put(damagerUUID, current);

        if (current == maxHits) {
            hits.remove(damager.getUniqueId());
            antiBuild.applyCooldown(damaged, antiBuildTime);

            takeItem(damager);
            applyCooldown(damager);

            for (PotionEffect effect : effects) {
                getInstance().getClassManager().addEffect(damager, effect); // So it restores properly
            }

            for (String s : getLanguageConfig().getStringList("ABILITIES.ANTI_BUILD.USED"))
                damager.sendMessage(s
                        .replace("%player%", damaged.getName())
                        .replace("%seconds%", String.valueOf(antiBuildTime))
                );

            for (String s : getLanguageConfig().getStringList("ABILITIES.ANTI_BUILD.BEEN_HIT"))
                damaged.sendMessage(s
                        .replace("%player%", damager.getName())
                        .replace("%seconds%", String.valueOf(antiBuildTime))
                );
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBuild(BlockBreakEvent e) {
        Player player = e.getPlayer();

        // Disable block breaking
        if (antiBuild.hasCooldown(player)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_BUILD.DENIED_BUILD")
                    .replace("%seconds%", antiBuild.getRemaining(player))
            );
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockPlaceEvent e) {
        Player player = e.getPlayer();

        // Disable block placing
        if (antiBuild.hasCooldown(player)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_BUILD.DENIED_BUILD")
                    .replace("%seconds%", antiBuild.getRemaining(player))
            );
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();

        if (block == null) return;
        if (!antiBuild.hasCooldown(player)) return;

        // Disable pressure plate interaction
        if (e.getAction() == Action.PHYSICAL) {
            e.setCancelled(true);
            return;
        }

        // Disable opening chests, etc...
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && deniedInteract.contains(block.getType())) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_BUILD.DENIED_BUILD")
                    .replace("%seconds%", antiBuild.getRemaining(player))
            );
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        hits.remove(player.getUniqueId());
        antiBuild.removeCooldown(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        hits.remove(player.getUniqueId());
    }
}