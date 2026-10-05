package me.keano.azurite.modules.users;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.deathban.Deathban;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.users.extra.StoredInventory;
import me.keano.azurite.modules.users.settings.ActionBar;
import me.keano.azurite.modules.users.settings.TeamChatSetting;
import me.keano.azurite.modules.users.settings.TeamListSetting;
import me.keano.azurite.utils.Formatter;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.configs.StorageJson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class User extends Module<UserManager> {

    private UUID uniqueID;
    private UUID replied;
    private String name;
    private String killtag;
    private Deathban deathban;

    private TeamListSetting teamListSetting;
    private TeamChatSetting teamChatSetting;
    private StorageJson storageJson;
    private ActionBar actionBar;

    private Set<UUID> ignoring;
    private List<StoredInventory> inventories;
    private List<String> lastKills;
    private List<String> lastDeaths;

    private int balance;
    private int kills;
    private int deaths;
    private int diamonds;
    private int lives;
    private int killstreak;
    private int highestKillstreak;

    private int falltrapTokens;
    private int baseTokens;

    private long playtime;
    private long lastLogin;
    private long dailyCooldown;

    private boolean privateMessages;
    private boolean privateMessagesSound;
    private boolean reclaimed;
    private boolean redeemed;

    private boolean scoreboard;
    private boolean scoreboardClaim;
    private boolean publicChat;
    private boolean cobblePickup;
    private boolean foundDiamondAlerts;
    private boolean deathMessages;
    private boolean lunarNametags;
    private boolean claimsShown;
    private boolean clearedNametags;

    // For deserialization
    public User(UserManager manager, Map<String, Object> map) {
        super(manager);

        this.uniqueID = UUID.fromString((String) map.get("uniqueID"));
        this.name = (String) map.get("name");
        this.replied = null;
        this.teamListSetting = TeamListSetting.valueOf((String) map.get("listSetting"));
        this.teamChatSetting = TeamChatSetting.valueOf((String) map.get("chatSetting"));
        this.storageJson = null;

        this.ignoring = Serializer.fetchUUIDs(map.get("ignoring"));
        this.inventories = Serializer.fetchInventories(map.get("inventories"));
        this.lastKills = Utils.createList(map.get("lastKills"), String.class);
        this.lastDeaths = Utils.createList(map.get("lastDeaths"), String.class);
        this.balance = Integer.parseInt((String) map.get("balance"));
        this.kills = Integer.parseInt((String) map.get("kills"));
        this.deaths = Integer.parseInt((String) map.get("deaths"));
        this.diamonds = Integer.parseInt((String) map.get("diamonds"));
        this.lives = Integer.parseInt((String) map.get("lives"));
        this.killstreak = Integer.parseInt((String) map.get("killstreak"));
        this.highestKillstreak = Integer.parseInt((String) map.get("highestKillstreak"));
        this.reclaimed = Boolean.parseBoolean((String) map.get("reclaimed"));
        this.redeemed = Boolean.parseBoolean((String) map.get("redeemed"));
        this.falltrapTokens = Integer.parseInt((String) map.get("falltrapTokens"));
        this.baseTokens = Integer.parseInt((String) map.get("baseTokens"));
        this.playtime = Long.parseLong((String) map.get("playtime"));
        this.dailyCooldown = Long.parseLong((String) map.get("lastDaily"));
        this.scoreboardClaim = Boolean.parseBoolean((String) map.get("scoreboardClaim"));
        this.scoreboard = Boolean.parseBoolean((String) map.get("scoreboard"));
        this.publicChat = Boolean.parseBoolean((String) map.get("publicChat"));
        this.cobblePickup = Boolean.parseBoolean((String) map.get("cobblePickup"));
        this.foundDiamondAlerts = Boolean.parseBoolean((String) map.get("foundDiamondAlerts"));
        this.deathMessages = Boolean.parseBoolean((String) map.get("deathMessages"));
        this.lunarNametags = Boolean.parseBoolean((String) map.get("lunarNametags"));

        this.lastLogin = 0L;
        this.privateMessages = true;
        this.privateMessagesSound = true;
        this.claimsShown = false;

        if (map.containsKey("deathban")) this.deathban = Serializer.fetchDeathban(getManager(), map.get("deathban"));
        if (map.containsKey("killtag")) this.killtag = (String) map.get("killtag");

        manager.getUsers().put(uniqueID, this);
        manager.getUuidCache().put(name, uniqueID);
    }

    public User(UserManager manager, UUID uniqueID, String name) {
        super(manager);

        this.uniqueID = uniqueID;
        this.name = name;
        this.replied = null;
        this.deathban = null;
        this.killtag = null;

        this.teamListSetting = TeamListSetting.ONLINE_HIGH;
        this.teamChatSetting = TeamChatSetting.PUBLIC;
        this.storageJson = null;

        this.ignoring = new HashSet<>();
        this.inventories = new ArrayList<>();
        this.lastKills = new ArrayList<>();
        this.lastDeaths = new ArrayList<>();

        this.balance = 0;
        this.kills = 0;
        this.deaths = 0;
        this.diamonds = 0;
        this.lives = 0;
        this.killstreak = 0;
        this.highestKillstreak = 0;

        this.falltrapTokens = 0;
        this.baseTokens = 0;

        this.playtime = 0L;
        this.lastLogin = 0L;
        this.dailyCooldown = 0L;

        this.privateMessages = true;
        this.privateMessagesSound = true;
        this.reclaimed = false;
        this.redeemed = false;

        this.scoreboardClaim = Config.DEFAULT_CLAIM_SCOREBOARD;
        this.scoreboard = true;
        this.publicChat = true;
        this.cobblePickup = true;
        this.foundDiamondAlerts = true;
        this.deathMessages = Config.DEFAULT_DEATH_MESSAGES;
        this.lunarNametags = true;
        this.claimsShown = false;

        manager.getUsers().put(uniqueID, this);
        manager.getUuidCache().put(name, uniqueID);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>(); // keep order

        map.put("name", name);
        map.put("uniqueID", uniqueID.toString());
        map.put("listSetting", teamListSetting.toString());
        map.put("chatSetting", teamChatSetting.toString());
        map.put("ignoring", Serializer.serializeUUIDs(ignoring));
        map.put("lastKills", lastKills);
        map.put("lastDeaths", lastDeaths);
        map.put("balance", String.valueOf(balance));
        map.put("kills", String.valueOf(kills));
        map.put("deaths", String.valueOf(deaths));
        map.put("diamonds", String.valueOf(diamonds));
        map.put("lives", String.valueOf(lives));
        map.put("killstreak", String.valueOf(killstreak));
        map.put("highestKillstreak", String.valueOf(highestKillstreak));
        map.put("falltrapTokens", String.valueOf(falltrapTokens));
        map.put("baseTokens", String.valueOf(baseTokens));
        map.put("playtime", String.valueOf(getUpdatedPlaytime()));
        map.put("lastDaily", String.valueOf(dailyCooldown));
        map.put("reclaimed", String.valueOf(reclaimed));
        map.put("redeemed", String.valueOf(redeemed));
        map.put("scoreboardClaim", String.valueOf(scoreboardClaim));
        map.put("scoreboard", String.valueOf(scoreboard));
        map.put("publicChat", String.valueOf(publicChat));
        map.put("cobblePickup", String.valueOf(cobblePickup));
        map.put("foundDiamondAlerts", String.valueOf(foundDiamondAlerts));
        map.put("deathMessages", String.valueOf(deathMessages));
        map.put("lunarNametags", String.valueOf(lunarNametags));
        map.put("inventories", Serializer.serializeInventories(new ArrayList<>(inventories)));

        if (deathban != null) map.put("deathban", Serializer.serializeDeathban(deathban));
        if (killtag != null) map.put("killtag", killtag);

        return map;
    }

    public double getKDR() {
        double kdr = (double) kills / (double) deaths;
        return (Double.isNaN(kdr) || Double.isInfinite(kdr) ? 0.0D : kdr);
    }

    public String getName() {
        Player player = Bukkit.getPlayer(uniqueID);

        if (player != null) {
            return player.getName(); // Support disguise systems
        }

        return (name == null ? "Null User" : name);
    }

    public String getKDRString() {
        return Formatter.formatKDR(getKDR());
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueID);
    }

    public long getUpdatedPlaytime() {
        // This won't actually update playtime but get the updated time
        return playtime + (lastLogin > 0L ? System.currentTimeMillis() - lastLogin : 0L);
    }

    public void updatePlaytime() {
        this.setPlaytime(getUpdatedPlaytime());
        this.setLastLogin(System.currentTimeMillis());
    }

    public void save() {
        getInstance().getStorageManager().getStorage().saveUser(this, true);
    }

    public void delete() {
        getInstance().getStorageManager().getStorage().deleteUser(this);
    }
}