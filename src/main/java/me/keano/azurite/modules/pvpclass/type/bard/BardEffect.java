package me.keano.azurite.modules.pvpclass.type.bard;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Utils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class BardEffect extends Module<PvPClassManager> {

    private PotionEffect effect;

    private int bardDistance;
    private int energyRequired;

    private boolean effectFriendlies;
    private boolean effectSelf;
    private boolean effectEnemies;
    private boolean clickable;

    public BardEffect(PvPClassManager manager, Map<String, Object> map, boolean clickable) {
        super(manager);
        this.effect = Serializer.getEffect((String) map.get("EFFECT"));
        this.bardDistance = getClassesConfig().getInt("BARD_CLASS.BARD_DISTANCE");
        this.effectFriendlies = (boolean) map.get("EFFECT_FRIENDLIES");
        this.effectSelf = (boolean) map.get("EFFECT_SELF");
        this.effectEnemies = (boolean) map.get("EFFECT_ENEMIES");
        this.clickable = clickable;

        if (clickable) this.energyRequired = (int) map.get("ENERGY_REQUIRED");
    }

    public BardEffect(PvPClassManager manager, boolean clickable, PotionEffect effect) {
        super(manager);
        this.clickable = clickable;
        this.effect = effect;
        this.bardDistance = getClassesConfig().getInt("BARD_CLASS.BARD_DISTANCE");
        this.energyRequired = 0;
        this.effectFriendlies = true;
        this.effectSelf = true;
        this.effectEnemies = false;
    }

    public void applyEffect(Player player) {
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (effectSelf) {
            getManager().addEffect(player, effect);
        }

        int halfDist = bardDistance / 2; // Half the distance on y-axis.
        int affected = (effectSelf ? 1 : 0);

        for (Entity nearbyEntity : player.getNearbyEntities(bardDistance, halfDist, bardDistance)) {
            if (!(nearbyEntity instanceof Player)) continue;
            if (nearbyEntity == player) continue;

            Player nearby = (Player) nearbyEntity;
            Team atNearby = getInstance().getTeamManager().getClaimManager().getTeam(nearby.getLocation());

            // never apply any effects to players in safezones.
            if (atNearby instanceof SafezoneTeam) continue;

            if (effectFriendlies && pt != null && pt.getPlayers().contains(nearby.getUniqueId())) {
                getManager().addEffect(nearby, effect);

                if (clickable) {
                    affected++;
                    nearby.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BARD_CLASS.TEAM_EFFECT")
                            .replace("%player%", player.getName())
                            .replace("%effect%", Utils.convertName(effect.getType()))
                    );
                }
            }

            if (effectEnemies) {
                // Don't affect allies or teammates
                if (pt != null) {
                    if (pt.isAlly(nearby)) continue;
                    if (pt.getPlayers().contains(nearby.getUniqueId())) continue;
                }

                affected++;
                getManager().addEffect(nearby, effect);
            }
        }

        if (clickable) {
            if (energyRequired == 0) {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BARD_CLASS.USED_EFFECT_NO_ENERGY")
                        .replace("%effect%", Utils.convertName(effect.getType()))
                );

            } else {
                player.sendMessage(getLanguageConfig().getString("PVP_CLASSES.BARD_CLASS.USED_EFFECT")
                        .replace("%effect%", Utils.convertName(effect.getType()))
                        .replace("%energy%", String.valueOf(energyRequired))
                        .replace("%affected%", String.valueOf(affected))
                );
            }
        }
    }
}