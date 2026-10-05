package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class AntiTrapHaloAbility extends Ability {

    private final Set<UUID> halo;
    private final List<Material> denied;
    private final int seconds;
    private final int radius;
    private final boolean denyPlace;
    private final boolean denyBreak;
    private final boolean denyInteract;

    public AntiTrapHaloAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "AntiTrap Halo"
        );
        this.halo = new HashSet<>();
        this.denied = getAbilitiesConfig().getStringList("ANTITRAP_HALO.DENIED_INTERACT").stream().map(ItemUtils::getMat).collect(Collectors.toList());
        this.seconds = getAbilitiesConfig().getInt("ANTITRAP_HALO.SECONDS_ANTI_BUILD");
        this.radius = getAbilitiesConfig().getInt("ANTITRAP_HALO.RADIUS");
        this.denyPlace = getAbilitiesConfig().getBoolean("ANTITRAP_HALO.DENY_PLACE");
        this.denyBreak = getAbilitiesConfig().getBoolean("ANTITRAP_HALO.DENY_BREAK");
        this.denyInteract = getAbilitiesConfig().getBoolean("ANTITRAP_HALO.DENY_INTERACT");
    }

    @Override
    public void onClick(Player player) {
        if (cannotUse(player)) return;
        if (hasCooldown(player)) return;

        takeItem(player);
        applyCooldown(player);

        halo.add(player.getUniqueId());

        for (String s : getLanguageConfig().getStringList("ABILITIES.ANTI_TRAP_HALO.USED")) {
            player.sendMessage(s);
        }

        Tasks.executeLater(getManager(), seconds * 20L, () -> {
            halo.remove(player.getUniqueId());

            for (String s : getLanguageConfig().getStringList("ABILITIES.ANTI_TRAP_HALO.EXPIRED")) {
                player.sendMessage(s);
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();

        if (denyPlace && checkNear(player)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_TRAP_HALO.DENIED"));
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();

        if (denyBreak && checkNear(player)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_TRAP_HALO.DENIED"));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("BLOCK")) return;
        if (e.getClickedBlock() == null) return;

        Player player = e.getPlayer();

        if (denyInteract && denied.contains(e.getClickedBlock().getType()) && checkNear(player)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("ABILITIES.ANTI_TRAP_HALO.DENIED"));
        }
    }

    public boolean checkNear(Player player) {
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof Player)) continue;

            Player near = (Player) entity;

            if (halo.contains(near.getUniqueId()) && !getInstance().getTeamManager().canHit(player, near, false)) {
                return true;
            }
        }

        return false;
    }
}