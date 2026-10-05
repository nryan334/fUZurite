package me.keano.azurite.modules.nametags.adapter;

import me.keano.azurite.modules.events.king.KingManager;
import me.keano.azurite.modules.events.sotw.SOTWManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.nametags.Nametag;
import me.keano.azurite.modules.nametags.NametagAdapter;
import me.keano.azurite.modules.nametags.NametagManager;
import me.keano.azurite.modules.nametags.extra.NameVisibility;
import me.keano.azurite.modules.staff.StaffManager;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.CC;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class AzuriteNametags extends Module<NametagManager> implements NametagAdapter {

    public AzuriteNametags(NametagManager manager) {
        super(manager);
    }

    @Override
    public String getAndUpdate(Player player, Player target) {
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());
        SOTWManager sotwManager = getInstance().getSotwManager();
        KingManager kingManager = getInstance().getKingManager();
        StaffManager staffManager = getInstance().getStaffManager();

        if (player == target) {
            return createTeam(player, target, "team", "", Config.RELATION_TEAMMATE);

        } else if (staffManager.isStaffEnabled(target)) {
            return createTeam(player, target, "staff", Config.PREFIX_STAFF, Config.RELATION_STAFF);

        } else if (kingManager.isActive() && kingManager.getKing() == target) {
            return createTeam(player, target, "king", Config.PREFIX_KING, Config.RELATION_KING);

        } else if (pt != null && pt.getPlayers().contains(target.getUniqueId())) {
            return createTeam(player, target, "team", "", Config.RELATION_TEAMMATE);

        } else if (target.hasPotionEffect(PotionEffectType.INVISIBILITY) && !staffManager.isStaffEnabled(player)) {
            return createTeam(player, target, "invis", "", "", NameVisibility.NEVER);

        } else if (pt != null && pt.isAlly(target)) {
            return createTeam(player, target, "allies", "", Config.RELATION_ALLIED);

        } else if (sotwManager.isActive() && !sotwManager.isEnabled(target)) {
            return createTeam(player, target, "sotw", "", Config.RELATION_SOTW);

        } else if (getInstance().getTimerManager().getArcherTagTimer().hasTimer(target)) {
            return createTeam(player, target, "tags", "", Config.RELATION_ARCHERTAG);

        } else if (pt != null && (pt.isFocused(target) || pt.getSingularFocus().contains(target.getUniqueId()))) {
            return createTeam(player, target, "focused", "", Config.RELATION_FOCUSED);

        } else if (getInstance().getTimerManager().getPvpTimer().hasTimer(target)) {
            return createTeam(player, target, "pvptimers", "", Config.RELATION_PVPTIMER);

        } else if (getInstance().getTimerManager().getInvincibilityTimer().hasTimer(target)) {
            return createTeam(player, target, "invinc", "", Config.RELATION_INVINCIBLES);
        }

        return createTeam(player, target, "enemies", "", Config.RELATION_ENEMY);
    }

    private String createTeam(Player player, Player target, String name, String prefix, String color) {
        return createTeam(player, target, name, prefix, color, NameVisibility.ALWAYS);
    }

    // yes it needs color because 1.16 is stupid
    private String createTeam(Player player, Player target, String name, String prefix, String color, NameVisibility visibility) {
        Nametag nametag = getManager().getNametags().get(player.getUniqueId());
        String formattedPrefix = (prefix.isEmpty() ? "" : prefix
                .replace("%rank-color%", CC.t(getInstance().getRankHook().getRankColor(target)))
                .replace("%rank-name%", CC.t(getInstance().getRankHook().getRankName(target))));

        if (nametag != null) {
            nametag.getPacket().create(name, color, formattedPrefix, "", true, visibility);
            nametag.getPacket().addToTeam(target.getName(), name);
        }

        return formattedPrefix + color;
    }
}