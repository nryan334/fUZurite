package me.keano.azurite.modules.events.conquest;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.events.EventType;
import me.keano.azurite.modules.events.conquest.extra.Capzone;
import me.keano.azurite.modules.events.conquest.extra.ConquestType;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class Conquest extends Module<ConquestManager> {

    private Map<ConquestType, Capzone> capzones;
    private Map<UUID, Integer> points;
    private boolean active;

    public Conquest(ConquestManager manager, Map<String, Object> map) {
        super(manager);
        this.capzones = new HashMap<>();
        this.points = new LinkedHashMap<>(); // keep order
        this.active = Boolean.parseBoolean((String) map.get("active"));

        List<String> pointsDeserialize = Utils.createList(map.get("points"), String.class);

        for (String point : pointsDeserialize) {
            String[] split = point.split(":");
            points.put(UUID.fromString(split[0]), Integer.parseInt(split[1]));
        }

        List<Capzone> deserialize = Utils.createList(map.get("capzones"), String.class)
                .stream()
                .map(s -> Serializer.fetchCapzone(getManager(), s))
                .collect(Collectors.toList());

        for (Capzone capzone : deserialize) {
            capzones.put(capzone.getType(), capzone);
            capzone.checkZone(false);
        }
    }

    public void start() {
        this.active = true;

        for (Capzone capzone : capzones.values()) {
            capzone.setCapturing(null);
            capzone.getOnCap().clear();
            points.clear();

            for (Player player : Bukkit.getOnlinePlayers()) {
                getInstance().getWaypointManager().getConquestWaypoint().send(player, capzone.getZone().getCenter(), s -> s
                        .replace("%name%", capzone.getType().getName())
                        .replace("%color%", String.valueOf(Utils.translateChatColorToColor(capzone.getType().getColor()).getRGB()))
                );
            }
        }
    }

    public void end() {
        this.active = false;

        for (Capzone capzone : capzones.values()) {
            capzone.setCapturing(null);
            capzone.getOnCap().clear();
            points.clear();

            for (Player player : Bukkit.getOnlinePlayers()) {
                getInstance().getWaypointManager().getConquestWaypoint().remove(player, capzone.getZone().getCenter(), s -> s
                        .replace("%name%", capzone.getType().getName())
                        .replace("%color%", String.valueOf(Utils.translateChatColorToColor(capzone.getType().getColor()).getRGB()))
                );
            }
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("active", String.valueOf(active));
        map.put("capzones", capzones.values().stream().map(Serializer::serializeCapzone).collect(Collectors.toList()));
        map.put("points", points.entrySet().stream().map(entry -> entry.getKey().toString() + ":" + entry.getValue().toString()).collect(Collectors.toList()));
        map.put("type", EventType.CONQUEST.toString());

        return map;
    }

    public String getPoints(int pos) {
        if (points.size() >= pos) {
            List<Map.Entry<UUID, Integer>> entries = new ArrayList<>(points.entrySet());
            Map.Entry<UUID, Integer> entry = entries.get(pos - 1);
            PlayerTeam pt = getInstance().getTeamManager().getPlayerTeam(entry.getKey());
            return (pt != null ? getScoreboardConfig().getString("CONQUEST.FORMAT_POINTS")
                    .replace("%pos%", String.valueOf(pos))
                    .replace("%team%", pt.getName())
                    .replace("%points%", String.valueOf(entry.getValue())) : "None");
        }

        return "None";
    }

    public void handleDeath(Player player) {
        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

        if (pt != null && points.containsKey(pt.getUniqueID())) {
            points.put(pt.getUniqueID(), Math.max(points.get(pt.getUniqueID()) - Config.CONQUEST_POINTS_DEATH, 0));
            sortPoints();

            for (String s : getLanguageConfig().getStringList("CONQUEST_EVENTS.BROADCAST_DEATH")) {
                pt.broadcast(s
                        .replace("%deathpoints%", String.valueOf(Config.CONQUEST_POINTS_DEATH))
                        .replace("%player%", player.getName())
                        .replace("%points%", String.valueOf(points.get(pt.getUniqueID())))
                        .replace("%maxpoints%", String.valueOf(Config.CONQUEST_POINTS_CAPTURE))
                );
            }
        }
    }

    public void sortPoints() {
        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(points.entrySet());
        list.sort(Comparator.comparingInt(Map.Entry::getValue));
        Collections.reverse(list);
        points.clear();

        for (Map.Entry<UUID, Integer> entry : list) {
            points.put(entry.getKey(), entry.getValue());
        }
    }

    public void reward(Player capturing) {
        List<Map.Entry<UUID, Integer>> entries = new ArrayList<>(points.entrySet());
        Map.Entry<UUID, Integer> entry = entries.get(0); // 1st pos
        PlayerTeam pt = getInstance().getTeamManager().getPlayerTeam(entry.getKey());

        if (pt != null) {
            for (String s : getLanguageConfig().getStringList("CONQUEST_EVENTS.BROADCAST_END")) {
                Bukkit.broadcastMessage(s
                        .replace("%team%", pt.getName())
                );
            }

            for (String s : getConfig().getStringList("CONQUEST.COMMANDS_WON")) {
                Tasks.execute(getManager(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s
                        .replace("%player%", capturing.getName())
                ));
            }

            pt.setPoints(pt.getPoints() + getConfig().getInt("CONQUEST.TEAM_POINTS_WON"));
            pt.save();
        }
    }

    public void save() {
        for (Capzone capzone : capzones.values()) {
            capzone.checkZone(false);
        }
        getEventsData().getValues().put("Conquest", serialize());
        getEventsData().save();
    }

    public void delete() {
        for (Capzone capzone : capzones.values()) {
            capzone.checkZone(true);
        }
        getEventsData().getValues().remove("Conquest");
        getEventsData().save();
    }
}