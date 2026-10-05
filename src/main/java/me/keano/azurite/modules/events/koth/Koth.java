package me.keano.azurite.modules.events.koth;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.discord.type.KothWebhook;
import me.keano.azurite.modules.events.EventType;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.staff.StaffManager;
import me.keano.azurite.modules.teams.claims.Coordinate3D;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.utils.BukkitSerialization;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.cuboid.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class Koth extends Module<KothManager> {

    private String name;
    private String color;
    private Cuboid captureZone;

    private List<Player> onCap;
    private List<ItemStack> loot;
    private Player capturing;
    private BukkitRunnable bukkitTask;

    private boolean active;
    private int pointsReward;
    private long minutes;
    private long remaining;
    private long startTime;

    public Koth(KothManager manager, Map<String, Object> map) {
        super(manager);

        this.name = (String) map.get("name");
        this.color = (String) map.get("color");
        this.pointsReward = Integer.parseInt((String) map.get("pointsReward"));
        this.active = Boolean.parseBoolean((String) map.get("active"));
        this.minutes = Long.parseLong((String) map.get("minutes"));
        this.loot = new ArrayList<>(Arrays.asList(BukkitSerialization.itemStackArrayFromBase64((String) map.get("loot"))));

        this.onCap = new ArrayList<>();

        this.capturing = null;
        this.bukkitTask = null;

        this.remaining = minutes;
        this.startTime = Long.parseLong((String) map.get("startTime"));

        if (map.containsKey("captureZone")) {
            this.captureZone = Serializer.fetchCuboid((String) map.get("captureZone"));
        }
    }

    public Koth(KothManager manager, String name, String color, int pointsReward, long minutes) {
        super(manager);

        this.name = name;
        this.color = color;
        this.onCap = new ArrayList<>();
        this.loot = new ArrayList<>();

        this.captureZone = null;
        this.capturing = null;
        this.bukkitTask = null;

        this.pointsReward = pointsReward;
        this.active = false;

        this.minutes = (60 * 1000L) * minutes;
        this.remaining = minutes;
        this.startTime = 0L;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("name", name);
        map.put("color", color);
        map.put("pointsReward", String.valueOf(pointsReward));
        map.put("active", String.valueOf(active));
        map.put("minutes", String.valueOf(minutes));
        map.put("type", EventType.KOTH.toString());
        map.put("loot", BukkitSerialization.itemStackArrayToBase64(loot.toArray(new ItemStack[0])));
        map.put("startTime", String.valueOf(startTime));

        if (captureZone != null) {
            map.put("captureZone", Serializer.serializeCuboid(captureZone));
        }

        return map;
    }

    public void tick() {
        long capMessage = getConfig().getInt("KOTHS_CONFIG.SEND_CAPTURING_MESSAGE");
        long teamCapMessage = getConfig().getInt("KOTHS_CONFIG.TEAM_SEND_CAPTURING_MESSAGE");
        long remSimplified = getRemaining() / 1000L;

        if (capturing == null && !onCap.isEmpty()) {
            Collections.shuffle(onCap);
            capturing = onCap.get(0);
        }

        if (capturing != null) {
            TimerManager timerManager = getInstance().getTimerManager();
            StaffManager staffManager = getInstance().getStaffManager();

            if (!capturing.isOnline() || capturing.isDead() ||
                    staffManager.isStaffEnabled(capturing) || staffManager.isVanished(capturing) ||
                    timerManager.getInvincibilityTimer().hasTimer(capturing) ||
                    timerManager.getPvpTimer().hasTimer(capturing) ||
                    getManager().getZone(capturing.getLocation()) == null ||
                    (captureZone != null && !captureZone.contains(capturing.getLocation()))) {

                checkLostCapMessage(capturing);
                onCap.remove(capturing);
                capturing = null;
                remaining = minutes;

                if (bukkitTask != null) {
                    bukkitTask.cancel();
                    bukkitTask = null;
                }
                return;
            }
        }

        boolean isSame = remaining == minutes;
        remaining = remaining - 1000L;

        if (remaining <= 0L) {
            this.handleCapture();
            return;
        }

        if (isSame) return;

        // Some messages we need to send
        if (capturing != null && remSimplified % teamCapMessage == 0 && remSimplified > 5) {
            PlayerTeam pt = getInstance().getTeamManager().getByPlayer(capturing.getUniqueId());

            if (pt != null) {
                for (String s : getLanguageConfig().getStringList("KOTH_EVENTS.TEAM_CONTROLLING"))
                    pt.broadcast(s
                            .replace("%koth%", name)
                            .replace("%color%", color)
                    );
                return; // we don't want it to send below aswell.
            }
        }

        // Some messages we need to send
        if (capturing != null && remSimplified % capMessage == 0 && remSimplified > 5) {
            for (String s : getLanguageConfig().getStringList("KOTH_EVENTS.PLAYER_CONTROLLING"))
                capturing.sendMessage(s
                        .replace("%koth%", name)
                        .replace("%color%", color)
                );
        }
    }

    private void handleCapture() {
        if (capturing == null) return;

        this.reward(); // send these messages first

        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(capturing.getUniqueId());
        List<String> message = getLanguageConfig().getStringList("KOTH_EVENTS.BROADCAST_END");

        // We need to send it to console aswell.
        for (String s : message) {
            Bukkit.getConsoleSender().sendMessage(s
                    .replace("%koth%", name)
                    .replace("%color%", color)
                    .replace("%team%", (pt == null ? "None" : pt.getName()))
                    .replace("%player%", capturing.getName())
                    .replace("%uptime%", Formatter.formatDetailed(System.currentTimeMillis() - startTime))
            );
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            String team = (pt == null ? "None" : pt.getDisplayName(player));
            String playerString = (pt == null ? capturing.getName() : pt.getDisplayColor(player) + capturing.getName());

            for (String s : message) {
                player.sendMessage(s
                        .replace("%koth%", name)
                        .replace("%team%", team)
                        .replace("%color%", color)
                        .replace("%player%", playerString)
                        .replace("%uptime%", Formatter.formatDetailed(System.currentTimeMillis() - startTime))
                );
            }
        }

        this.end();
        this.save(); // Save active:false
    }

    public void start() {
        this.capturing = null;
        this.active = true;
        this.remaining = minutes;
        this.startTime = System.currentTimeMillis();
        this.onCap.clear(); // just in case

        if (Config.WEBHOOKS_ENABLED) {
            new KothWebhook(getManager(), this).executeAsync();
        }

        if (captureZone != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                getInstance().getWaypointManager().getKothWaypoint().send(player, captureZone.getCenter(), s -> s
                        .replace("%name%", name)
                );
            }
        }
    }

    public void end() {
        this.capturing = null;
        this.active = false;
        this.remaining = minutes;
        this.startTime = 0L;
        this.onCap.clear();

        if (captureZone != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                getInstance().getWaypointManager().getKothWaypoint().remove(player, captureZone.getCenter(), s -> s
                        .replace("%name%", name)
                );
            }
        }
    }

    public void reward() {
        // This can't be possible but incase?
        if (capturing == null) return;

        PlayerTeam pt = getInstance().getTeamManager().getByPlayer(capturing.getUniqueId());

        if (pt != null) {
            pt.setKothCaptures(pt.getKothCaptures() + 1);
            pt.setPoints(pt.getPoints() + pointsReward);
            pt.save();

            for (String s : getLanguageConfig().getStringList("KOTH_EVENTS.TEAM_RECEIVED_POINTS")) {
                pt.broadcast(s
                        .replace("%points%", String.valueOf(pointsReward))
                );
            }
        }

        for (String s : getConfig().getStringList("KOTHS_CONFIG.COMMANDS_ON_CAPTURE")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s
                    .replace("%player%", capturing.getName())
            );
        }

        for (ItemStack itemStack : loot) {
            ItemUtils.giveItem(capturing, itemStack, capturing.getLocation());
        }
    }

    public void checkLostCapMessage(Player player) {
        long percent = (getRemaining() * 100) / minutes;

        if (percent <= getConfig().getInt("KOTHS_CONFIG.LOST_CONTROL_PERCENT")) {
            for (String s : getLanguageConfig().getStringList("KOTH_EVENTS.BROADCAST_LOST")) {
                Bukkit.broadcastMessage(s
                        .replace("%koth%", name)
                        .replace("%color%", color)
                        .replace("%player%", player.getName())
                );
            }
        }
    }

    public void checkZone(boolean delete) {
        if (captureZone == null) return;

        String world = captureZone.getWorldName();
        Location center = captureZone.getCenter();
        int radius = Config.ANTICLEAN_KOTH_RADIUS;
        int blockX = center.getBlockX();
        int blockY = center.getBlockY();
        int blockZ = center.getBlockZ();

        // Save it to the capture zones map
        for (int x = captureZone.getMinimumX(); x <= captureZone.getMaximumX(); x++) {
            for (int y = captureZone.getMinimumY(); y <= captureZone.getMaximumY(); y++) {
                for (int z = captureZone.getMinimumZ(); z <= captureZone.getMaximumZ(); z++) {
                    if (delete) {
                        getManager().getCaptureZones().remove(world, new Coordinate3D(x, y, z));
                        continue;
                    }

                    getManager().getCaptureZones().put(world, new Coordinate3D(x, y, z), this);
                }
            }
        }

        // Anti Clean around koth
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (delete) {
                        getManager().getNoAntiClean().remove(world, new Coordinate3D(blockX + x, blockY + y, blockZ + z));
                        continue;
                    }

                    getManager().getNoAntiClean().put(world, new Coordinate3D(blockX + x, blockY + y, blockZ + z), this);
                }
            }
        }

        if (delete) {
            captureZone = null;
        }
    }

    public void save() {
        this.checkZone(false);
        getManager().getKoths().put(name, this);
        getEventsData().getValues().put(name, serialize());
        getEventsData().save();
    }

    public void delete() {
        this.checkZone(true);
        getManager().getKoths().remove(name);
        getEventsData().getValues().remove(name);
        getEventsData().save();
    }
}