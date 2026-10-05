package me.keano.azurite.modules.teams;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.configs.StorageJson;
import me.keano.azurite.utils.fanciful.FancyMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class Team extends Module<TeamManager> {

    protected String name;
    protected String customColor;
    protected boolean deathban;
    protected boolean abilities;

    protected Map<Location, BukkitTask> cobwebs;
    protected List<Claim> claims;

    protected StorageJson storageJson;
    protected TeamType type;
    protected Location hq;

    protected UUID uniqueID;
    protected UUID leader;

    public Team(TeamManager manager, Map<String, Object> map, boolean deathban, TeamType type) {
        super(manager);

        this.claims = Serializer.fetchClaims(map.get("claims"));
        this.name = (String) map.get("name");
        this.uniqueID = UUID.fromString((String) map.get("uniqueID"));
        this.leader = UUID.fromString((String) map.get("leader"));
        this.abilities = Boolean.parseBoolean((String) map.get("abilities"));
        this.deathban = deathban;
        this.storageJson = null;
        this.type = type;

        if (map.containsKey("hq")) this.hq = Serializer.fetchLocation((String) map.get("hq"));
        if (map.containsKey("customColor")) this.customColor = CC.t((String) map.get("customColor"));

        if (type == TeamType.WILDERNESS || type == TeamType.WARZONE || type == TeamType.ROAD) {
            this.cobwebs = new ConcurrentHashMap<>();
        }

        if (isSystemTeam()) getManager().getSystemTeams().put(uniqueID, this);
    }

    public Team(TeamManager manager, String name, UUID leader, boolean deathban, TeamType type) {
        super(manager);

        this.name = name;
        this.deathban = deathban;
        this.type = type;
        this.claims = new ArrayList<>();
        this.hq = null;
        this.customColor = null;
        this.abilities = true;
        this.storageJson = null;
        this.uniqueID = UUID.randomUUID();
        this.leader = leader;

        if (type == TeamType.WILDERNESS || type == TeamType.WARZONE || type == TeamType.ROAD) {
            this.cobwebs = new ConcurrentHashMap<>();
        }

        if (isSystemTeam()) getManager().getSystemTeams().put(uniqueID, this);
    }

    public String getDisplayName(Player player) {
        return (customColor != null ? customColor + name : name);
    }

    public void sendTeamStats(CommandSender sender) {
        List<FancyMessage> toSend = Config.TEAM_INFO_SYSTEM.stream().map(FancyMessage::clone).collect(Collectors.toList());

        for (FancyMessage fancyMessage : toSend) {
            fancyMessage.send(sender, s -> s
                    .replace("%name%", (sender instanceof Player ? getDisplayName((Player) sender) : name))
                    .replace("%color%", customColor == null ? "" : customColor)
                    .replace("%hq%", getHQFormatted())
            );
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("name", name);
        map.put("claims", Serializer.serializeClaims(claims));
        map.put("teamType", type.toString());
        map.put("uniqueID", uniqueID.toString());
        map.put("leader", leader.toString());
        map.put("abilities", String.valueOf(abilities));

        if (hq != null) map.put("hq", Serializer.serializeLoc(hq));
        if (customColor != null) map.put("customColor", customColor.replaceAll("§", "&"));

        return map;
    }

    public boolean isSystemTeam() {
        return type != TeamType.PLAYER && type != TeamType.WARZONE && type != TeamType.WILDERNESS;
    }

    public String getHQFormatted() {
        if (hq == null) return Config.HQ_FORMAT_NONE;
        return (Config.HQ_FORMAT_SET
                .replace("%world%", hq.getWorld().getName())
                .replace("%x%", String.valueOf(hq.getBlockX()))
                .replace("%y%", String.valueOf(hq.getBlockY()))
                .replace("%z%", String.valueOf(hq.getBlockZ()))
        );
    }

    public void clearWebs() {
        if (cobwebs == null) return;

        for (Location location : cobwebs.keySet()) {
            if (location.getBlock().getType() == ItemUtils.getMat("WEB")) {
                location.getBlock().setType(Material.AIR);
            }
        }
    }

    public void save() {
        getManager().getTeams().put(uniqueID, this);
        getManager().getStringTeams().put(name, this);
        getInstance().getStorageManager().getStorage().saveTeam(this, true);
    }

    public void delete() {
        getManager().getTeams().remove(uniqueID);
        getManager().getSystemTeams().remove(uniqueID);
        getManager().getStringTeams().remove(name);
        getInstance().getStorageManager().getStorage().deleteTeam(this);
    }
}