package me.keano.azurite.modules.ability.type;

import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.utils.ReflectionUtils;
import me.keano.azurite.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class GrappleAbility extends Ability {

    private static final Field HOOK_ENTITY = ReflectionUtils.accessField(org.bukkit.event.player.PlayerFishEvent.class, "hookEntity");

    private final double multiplier;
    private final double yMultiplier;

    public GrappleAbility(AbilityManager manager) {
        super(
                manager,
                null,
                "Grapple"
        );
        this.multiplier = getAbilitiesConfig().getDouble("GRAPPLE.MULTIPLIER");
        this.yMultiplier = getAbilitiesConfig().getDouble("GRAPPLE.Y_MULTIPLIER");
    }

    @EventHandler
    public void onLaunch(PlayerInteractEvent e) {
        if (!(e.getAction().name().contains("RIGHT"))) return;

        Player player = e.getPlayer();

        if (hasAbilityInHand(player)) {
            if (cannotUse(player) || hasCooldown(player)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        Player player = e.getPlayer();

        // State when the rod is in the air
        if (e.getState() == PlayerFishEvent.State.FISHING) return;
        if (!hasAbilityInHand(player)) return;

        // FOR SOME FUCKING REASON "FISH" ENTITY DOESN'T FUCKING WORK ON 1.16+ FUCK
        try {

            applyCooldown(player);
            takeItem(player);
            pullEntityToLocation(player, (Utils.isModernVer() ? (FishHook) HOOK_ENTITY.get(e) : e.getHook()));

        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }


    private void pullEntityToLocation(Entity entity, Entity loc) {
        if (loc.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.AIR) return;
        Vector grapple = loc.getLocation().toVector().subtract(entity.getLocation().toVector());
        grapple.multiply(multiplier);
        grapple.setY(grapple.getY() + yMultiplier);
        entity.setVelocity(grapple);
    }
}