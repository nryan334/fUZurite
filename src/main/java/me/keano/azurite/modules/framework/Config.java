package me.keano.azurite.modules.framework;

import me.keano.azurite.modules.framework.extra.Configs;
import me.keano.azurite.modules.framework.extra.FancyMessageData;
import me.keano.azurite.modules.framework.extra.TitlesConfig;
import me.keano.azurite.modules.tablist.extra.TablistSkin;
import me.keano.azurite.utils.BukkitSerialization;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.fanciful.FancyMessage;
import me.keano.azurite.utils.fastparticles.ParticleType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class Config {

    public static String PLAYER_NOT_FOUND;
    public static String PLAYER_ONLY;
    public static String INSUFFICIENT_PERM;
    public static String NOT_VALID_NUMBER;

    public static String NOT_IN_TEAM;
    public static String ALREADY_IN_TEAM;
    public static String TEAM_ALREADY_EXISTS;
    public static String TEAM_NOT_FOUND;
    public static String INSUFFICIENT_ROLE;

    public static String RELATION_TEAMMATE;
    public static String RELATION_ALLIED;
    public static String RELATION_ENEMY;
    public static String RELATION_FOCUSED;
    public static String RELATION_ARCHERTAG;
    public static String RELATION_PVPTIMER;
    public static String RELATION_INVINCIBLES;
    public static String RELATION_KING;
    public static String RELATION_SOTW;
    public static String RELATION_STAFF;
    public static String PREFIX_STAFF;
    public static String PREFIX_KING;

    public static String STAFF_TRUE_PLACEHOLDER;
    public static String STAFF_FALSE_PLACEHOLDER;
    public static String VANISHED_SYMBOL;

    public static String SYSTEAM_COLOR_ORE_MOUNTAIN;
    public static String SYSTEAM_COLOR_GLOWSTONE;

    public static String RAIDABLE_COLOR;
    public static String LOW_DTR_COLOR;
    public static String NORMAL_DTR_COLOR;

    public static String SYMBOL_REGENERATING;
    public static String SYMBOL_FREEZE;
    public static String SYMBOL_NORMAL;

    public static String HQ_FORMAT_NONE;
    public static String HQ_FORMAT_SET;

    public static String DEATH_FORMAT;
    public static String DEATH_KILLER;
    public static String DEATH_ENTITY;
    public static String DEATH_FALL;
    public static String DEATH_FALL_KILLER;
    public static String DEATH_LIGHTNING;
    public static String DEATH_VOID_KILLER;
    public static String DEATH_VOID;
    public static String DEATH_EXPLOSION;
    public static String DEATH_CONTACT;
    public static String DEATH_WITHER;
    public static String DEATH_STARVATION;
    public static String DEATH_SUFFOCATION;
    public static String DEATH_DROWN;
    public static String DEATH_POISON;
    public static String DEATH_LAVA;
    public static String DEATH_FIRE;
    public static String DEATH_LOGGER_KILLER;
    public static String DEATH_LOGGER;
    public static String DEATH_BACKSTABBED;
    public static String DEATH_PROJECTILE;
    public static String DEATH_PROJECTILE_KILLER;
    public static String DEATH_MAGIC;
    public static String DEATH_DEFAULT;

    public static String CHAT_PUBLIC_NO_TEAM;
    public static String CHAT_PUBLIC_TEAM;
    public static String CHAT_TEAM;
    public static String CHAT_ALLY;
    public static String CHAT_OFFICER;
    public static String CHAT_CO_LEADER;
    public static String CHAT_STAFF;
    public static String CHAT_FORBIDDEN;
    public static String CHAT_FTOP_FORMAT;
    public static String CHAT_COOLDOWN;

    public static String SERVER_NOT_LOADED;
    public static String COULD_NOT_LOAD_DATA;

    public static String SCHEDULE_TIMEZONE;
    public static String SCHEDULE_NONE_NAME;
    public static String SCHEDULE_NONE_TIME;
    public static String SCHEDULE_NO_PLAYERS_START;

    public static String DISCORD_REQUEST_WEBHOOKURL;
    public static String DISCORD_REQUEST_CONTENT;
    public static String DISCORD_REQUEST_DESCRIPTION;
    public static String DISCORD_REQUEST_COLOR;
    public static String DISCORD_REQUEST_FOOTER;
    public static String DISCORD_REQUEST_FOOTER_ICON;
    public static String DISCORD_REQUEST_THUMBNAIL;
    public static String DISCORD_REQUEST_AUTHOR;
    public static String DISCORD_REQUEST_AUTHOR_URL;
    public static String DISCORD_REQUEST_AUTHOR_ICON;

    public static String DISCORD_REPORT_WEBHOOKURL;
    public static String DISCORD_REPORT_CONTENT;
    public static String DISCORD_REPORT_DESCRIPTION;
    public static String DISCORD_REPORT_COLOR;
    public static String DISCORD_REPORT_FOOTER;
    public static String DISCORD_REPORT_FOOTER_ICON;
    public static String DISCORD_REPORT_THUMBNAIL;
    public static String DISCORD_REPORT_AUTHOR;
    public static String DISCORD_REPORT_AUTHOR_URL;
    public static String DISCORD_REPORT_AUTHOR_ICON;

    public static String DISCORD_KOTH_WEBHOOKURL;
    public static String DISCORD_KOTH_CONTENT;
    public static String DISCORD_KOTH_DESCRIPTION;
    public static String DISCORD_KOTH_COLOR;
    public static String DISCORD_KOTH_FOOTER;
    public static String DISCORD_KOTH_FOOTER_ICON;
    public static String DISCORD_KOTH_THUMBNAIL;
    public static String DISCORD_KOTH_AUTHOR;
    public static String DISCORD_KOTH_AUTHOR_URL;
    public static String DISCORD_KOTH_AUTHOR_ICON;

    public static String DISCORD_RESTORE_WEBHOOKURL;
    public static String DISCORD_RESTORE_CONTENT;
    public static String DISCORD_RESTORE_DESCRIPTION;
    public static String DISCORD_RESTORE_COLOR;
    public static String DISCORD_RESTORE_FOOTER;
    public static String DISCORD_RESTORE_FOOTER_ICON;
    public static String DISCORD_RESTORE_THUMBNAIL;
    public static String DISCORD_RESTORE_AUTHOR;
    public static String DISCORD_RESTORE_AUTHOR_URL;
    public static String DISCORD_RESTORE_AUTHOR_ICON;

    public static String ABILITY_ACTION_BAR_SYMBOL;
    public static String ABILITY_ACTION_BAR_NO_COLOR;
    public static String ABILITY_ACTION_BAR_YES_COLOR;
    public static String ABILITY_ACTION_BAR_STRING;

    public static String TABLIST_MEMBER_FORMAT;
    public static String TABLIST_LIST_FORMAT_DTR;
    public static String TABLIST_LIST_FORMAT_ONLINE;

    public static String DEATHBAN_STRING;
    public static String NON_DEATHBAN_STRING;

    public static String BLOCK_DIG_DENIED;
    public static String BLOCK_PLACE_DENIED;
    public static String BLOCK_INTERACT_DENIED;

    public static String DISPLAY_NAME_WARZONE;
    public static String DISPLAY_NAME_WILDERNESS;
    public static String DISPLAY_NAME_SAFEZONE;
    public static String DISPLAY_NAME_ROAD;
    public static String DISPLAY_NAME_CITADEL;
    public static String DISPLAY_NAME_CONQUEST;
    public static String DISPLAY_NAME_EVENT;

    public static String LUNAR_PREFIXES_ONE;
    public static String LUNAR_PREFIXES_TWO;
    public static String LUNAR_PREFIXES_THREE;

    public static String NAMETAGS_TEAM_TOP;
    public static String NAMETAGS_NORMAL;

    public static String HOLOGRAM_EMPTY;
    public static String HOLOGRAM_FTOP;
    public static String HOLOGRAM_FTOP_RAIDABLE;
    public static String HOLOGRAM_KILLS;
    public static String HOLOGRAM_DEATHS;
    public static String HOLOGRAM_KDR;
    public static String HOLOGRAM_KILLSTREAK;

    public static String LAST_KILL_STRING;
    public static String LAST_DEATH_STRING;

    public static String MEMBER_ONLINE;
    public static String MEMBER_DEATHBANNED;
    public static String MEMBER_OFFLINE;

    public static String ALLY_FORMAT;

    public static String LUNAR_ONE_COLOR;
    public static String LUNAR_ONE_PREFIX;
    public static String LUNAR_TWO_COLOR;
    public static String LUNAR_TWO_PREFIX;
    public static String LUNAR_THREE_COLOR;
    public static String LUNAR_THREE_PREFIX;

    public static String SCOREBOARD_TITLE;
    public static String DONOR_SOUND;
    public static String FOUND_DIAMOND;

    public static String FALLTRAP_ACTION_BAR_SYMBOL;
    public static String FALLTRAP_ACTION_BAR_NO_COLOR;
    public static String FALLTRAP_ACTION_BAR_YES_COLOR;
    public static String FALLTRAP_ACTION_BAR_STRING;
    public static String FALLTRAP_SOUND;
    public static ParticleType FALLTRAP_PARTICLE;

    public static String BASE_ACTION_BAR_SYMBOL;
    public static String BASE_ACTION_BAR_NO_COLOR;
    public static String BASE_ACTION_BAR_YES_COLOR;
    public static String BASE_ACTION_BAR_STRING;
    public static String BASE_SOUND;
    public static ParticleType BASE_PARTICLE;

    public static String KILL_HEADER;
    public static String DEATH_HEADER;
    public static String KILL_STAT;
    public static String DEATH_STAT;

    public static String TABLIST_REPLACE_KOTH_NAME;
    public static String TABLIST_REPLACE_KOTH_TIME;

    public static String ANTICLEAN_STATS_HITS_MESSAGE;
    public static String ANTICLEAN_STATS_EMPTY;

    public static TablistSkin TABLIST_TEAM_SKIN_SKIN;
    public static TablistSkin TABLIST_TEAM_CLAIM_SAFEZONE;
    public static TablistSkin TABLIST_TEAM_CLAIM_WARZONE;
    public static TablistSkin TABLIST_TEAM_CLAIM_WILDERNESS;
    public static TablistSkin TABLIST_TEAM_CLAIM_PLAYER_TEAM;
    public static TablistSkin TABLIST_TEAM_CLAIM_PLAYER_ALLY;
    public static TablistSkin TABLIST_TEAM_CLAIM_PLAYER_FOCUS;
    public static TablistSkin TABLIST_TEAM_CLAIM_PLAYER_ENEMY;
    public static TablistSkin TABLIST_TEAM_CLAIM_MOUNTAIN;
    public static TablistSkin TABLIST_TEAM_CLAIM_ROAD;
    public static TablistSkin TABLIST_TEAM_CLAIM_EVENT;
    public static TablistSkin TABLIST_TEAM_CLAIM_CITADEL;
    public static TablistSkin TABLIST_TEAM_CLAIM_CONQUEST;

    public static int MAX_KILL_STAT;
    public static int MAX_DEATH_STAT;

    public static int LAST_KILLS_LIMIT;
    public static int LAST_DEATHS_LIMIT;

    public static int REPAIR_ALL_COST;
    public static int REPAIR_HAND_COST;

    public static int ANTICLEAN_RADIUS;
    public static int ANTICLEAN_KOTH_RADIUS;

    public static boolean LUNAR_PREFIXES_ENABLED;
    public static boolean SCOREBOARD_CHANGER_ENABLED;
    public static boolean DEATH_STAT_ENABLED;
    public static boolean ANTICLEAN_ENABLED;
    public static boolean TABLIST_MEMBER_SKIN;
    public static boolean TABLIST_TEAM_SKIN;
    public static boolean TABLIST_CLAIM_SKIN;
    public static boolean TABLIST_REPLACE_KOTH;

    public static boolean JOIN_ITEMS_ENABLED;
    public static boolean JOIN_ITEMS_BOOK_ENABLED;
    public static boolean JOIN_COMMANDS_ENABLED;
    public static boolean JOIN_TEAM_INFO_ENABLED;
    public static boolean JOIN_MOTD_ENABLED;

    public static boolean COBWEB_PLACE_WARZONE;
    public static boolean COBWEB_PLACE_WILDERNESS;
    public static boolean COBWEB_PLACE_ROAD;
    public static int COBWEB_DESPAWN_TIME;

    public static boolean SCOREBOARD_ENABLED;
    public static boolean TABLIST_ENABLED;
    public static boolean MILK_FIX;
    public static boolean DISABLE_ENDER_EYE;

    public static boolean NATURAL_MOB_SPAWN;
    public static boolean DEFAULT_CLAIM_SCOREBOARD;
    public static boolean DEFAULT_DEATH_MESSAGES;

    public static boolean TITLE_CHANGER_ENABLED;
    public static boolean WEBHOOKS_ENABLED;

    public static boolean GIVE_EXP_MINE;
    public static boolean GIVE_EXP_KILL;
    public static boolean GIVE_ITEM_ON_KILL;
    public static boolean GIVE_SMELT_ON_MINE;
    public static boolean GIVE_BLOCKS_ON_MINE;
    public static boolean ALLOW_ORES_MINE_SPAWN;
    public static boolean ALLOW_BOW_BOOSTING;
    public static boolean ALLOW_LUNAR_WALLS;

    public static boolean STRENGTH_NERF;
    public static boolean ENDERMEN_HOSTILE;

    public static int TEAM_NAME_MAX_LENGTH;
    public static int TEAM_NAME_MIN_LENGTH;
    public static int TEAM_ANNOUNCEMENT_MAX;
    public static int TEAM_EVENT_ENTER_LIMIT;
    public static int TEAM_CITADEL_ENTER_LIMIT;
    public static int TEAM_CONQUEST_ENTER_LIMIT;

    public static int ABILITY_DISABLED_RADIUS;

    public static int WARZONE_NORMAL;
    public static int WARZONE_NETHER;

    public static int WARZONE_BREAK;
    public static int WARZONE_BREAK_NETHER;

    public static int DONOR_INTERVAL;
    public static int RESTORE_LIMIT;

    public static int NETHER_MULTIPLIER;
    public static int SMELT_MULTIPLIER;
    public static int DEFAULT_BAL;

    public static int CONQUEST_SECONDS;
    public static int CONQUEST_POINTS_CAPTURE;
    public static int CONQUEST_POINTS_DEATH;

    public static int TABLIST_PING;
    public static int ABILITY_ACTION_BAR_BARS;
    public static int FALLTRAP_ACTION_BAR_BARS;
    public static int BASE_ACTION_BAR_BARS;
    public static int FALLTRAP_PARTICLE_AMOUNT;
    public static int BASE_PARTICLE_AMOUNT;

    public static double DTR_REGEN_PER_MIN;
    public static double DTR_TAKE_DEATH_NORMAL;
    public static double DTR_TAKE_DEATH_END;
    public static double DTR_TAKE_DEATH_NETHER;
    public static double DTR_TAKE_DEATH_EVENTS;
    public static double DTR_TAKE_DEATH_CITADEL;

    public static double STRENGTH_NERF_LEVEL1;
    public static double STRENGTH_NERF_LEVEL2;
    public static double STRENGTH_NERF_LEVEL3PLUS;

    public static double LOW_DTR;

    public static List<Double> DTR_PER_MEMBERS;
    public static List<String> JOIN_MOTD;
    public static List<String> CLAIM_CHANGE;
    public static List<String> DONOR_MESSAGE;

    public static List<String> ANTICLEAN_STATS_MESSAGE;

    public static List<String> NAMETAG_MOD_MODE;
    public static List<String> NAMETAG_IN_TEAM;
    public static List<String> NAMETAG_NO_TEAM;
    public static List<String> TAB_SKIN_CACHE_LEFT;
    public static List<String> TAB_SKIN_CACHE_MIDDLE;
    public static List<String> TAB_SKIN_CACHE_RIGHT;
    public static List<String> TAB_SKIN_CACHE_FAR_RIGHT;

    public static Map<String, FancyMessageData> TEAM_INFO_FANCY_MESSAGES;
    public static List<FancyMessage> TEAM_INFO_SYSTEM;
    public static List<FancyMessage> TEAM_INFO_FRIENDLY;
    public static List<FancyMessage> TEAM_INFO_ENEMIES;
    public static List<FancyMessage> TEAM_INFO_STAFF;

    public static double ARCHER_TAG_DAMAGE;
    public static double ARCHER_HALF_FORCE_DAMAGE;
    public static double ARCHER_TAGGED_DAMAGE;
    public static double ARCHER_TAGGED_MULTIPLIER;

    public static byte PVP_TIMER_WALL_DATA;
    public static byte COMBAT_TAG_WALL_DATA;
    public static byte INVINCIBILITY_WALL_DATA;
    public static byte EVENT_DENIED_WALL_DATA;
    public static byte CITADEL_DENIED_WALL_DATA;
    public static byte CONQUEST_DENIED_WALL_DATA;
    public static byte LOCKED_CLAIM_WALL_DATA;

    public static Material PVP_TIMER_WALL_MATERIAL;
    public static Material COMBAT_TAG_WALL_MATERIAL;
    public static Material INVINCIBILITY_WALL_MATERIAL;
    public static Material EVENT_DENIED_WALL_MATERIAL;
    public static Material CITADEL_DENIED_WALL_MATERIAL;
    public static Material CONQUEST_DENIED_WALL_MATERIAL;
    public static Material LOCKED_CLAIM_WALL_MATERIAL;

    public static Color PVP_TIMER_WALL_COLOR;
    public static Color COMBAT_TAG_WALL_COLOR;
    public static Color INVINCIBILITY_WALL_COLOR;
    public static Color EVENT_DENIED_WALL_COLOR;
    public static Color CITADEL_DENIED_WALL_COLOR;
    public static Color CONQUEST_DENIED_WALL_COLOR;
    public static Color LOCKED_CLAIM_WALL_COLOR;

    public static ItemStack[] REFILL_SIGN;
    public static ItemStack[] QUICK_REFILL_SIGN;

    public static ItemStack CROWBAR;

    public static void load(Configs configs, boolean reload) {
        // Global
        PLAYER_NOT_FOUND = configs.getLanguageConfig().getString("GLOBAL_COMMANDS.PLAYER_NOT_FOUND");
        PLAYER_ONLY = configs.getLanguageConfig().getString("GLOBAL_COMMANDS.PLAYER_ONLY");
        INSUFFICIENT_PERM = configs.getLanguageConfig().getString("GLOBAL_COMMANDS.INSUFFICIENT_PERMISSION");
        NOT_VALID_NUMBER = configs.getLanguageConfig().getString("GLOBAL_COMMANDS.NOT_VALID_NUMBER");

        // Teams
        NOT_IN_TEAM = configs.getLanguageConfig().getString("TEAM_COMMAND.NOT_IN_TEAM");
        ALREADY_IN_TEAM = configs.getLanguageConfig().getString("TEAM_COMMAND.ALREADY_IN_TEAM");
        TEAM_ALREADY_EXISTS = configs.getLanguageConfig().getString("TEAM_COMMAND.TEAM_ALREADY_EXISTS");
        TEAM_NOT_FOUND = configs.getLanguageConfig().getString("TEAM_COMMAND.TEAM_NOT_FOUND");
        INSUFFICIENT_ROLE = configs.getLanguageConfig().getString("TEAM_COMMAND.INSUFFICIENT_ROLE");

        // Relation Color
        RELATION_TEAMMATE = configs.getTeamConfig().getString("RELATION_COLOR.TEAMMATE");
        RELATION_ALLIED = configs.getTeamConfig().getString("RELATION_COLOR.ALLIED");
        RELATION_ENEMY = configs.getTeamConfig().getString("RELATION_COLOR.ENEMY");
        RELATION_FOCUSED = configs.getTeamConfig().getString("RELATION_COLOR.FOCUSED");
        RELATION_ARCHERTAG = configs.getTeamConfig().getString("RELATION_COLOR.ARCHER_TAG");
        RELATION_PVPTIMER = configs.getTeamConfig().getString("RELATION_COLOR.PVP_TIMER");
        RELATION_INVINCIBLES = configs.getTeamConfig().getString("RELATION_COLOR.INVINCIBILITY");
        RELATION_KING = configs.getTeamConfig().getString("RELATION_COLOR.KING");
        RELATION_SOTW = configs.getTeamConfig().getString("RELATION_COLOR.SOTW");
        RELATION_STAFF = configs.getTeamConfig().getString("RELATION_COLOR.STAFF");
        PREFIX_STAFF = configs.getTeamConfig().getString("RELATION_COLOR.STAFF_PREFIX");
        PREFIX_KING = configs.getTeamConfig().getString("RELATION_COLOR.KING_PREFIX");

        // Staff
        STAFF_TRUE_PLACEHOLDER = configs.getConfig().getString("STAFF_MODE.PLACEHOLDERS.TRUE_PLACEHOLDER");
        STAFF_FALSE_PLACEHOLDER = configs.getConfig().getString("STAFF_MODE.PLACEHOLDERS.FALSE_PLACEHOLDER");
        VANISHED_SYMBOL = configs.getLunarConfig().getString("NAMETAGS.VANISHED_SYMBOL");

        // System Team Color
        SYSTEAM_COLOR_ORE_MOUNTAIN = configs.getTeamConfig().getString("SYSTEM_TEAMS.ORE_MOUNTAIN");
        SYSTEAM_COLOR_GLOWSTONE = configs.getTeamConfig().getString("SYSTEM_TEAMS.GLOWSTONE");

        // DTR Stuff
        RAIDABLE_COLOR = configs.getTeamConfig().getString("TEAM_DTR.COLOR.RAIDABLE");
        LOW_DTR_COLOR = configs.getTeamConfig().getString("TEAM_DTR.COLOR.LOW_DTR");
        NORMAL_DTR_COLOR = configs.getTeamConfig().getString("TEAM_DTR.COLOR.NORMAL");

        SYMBOL_REGENERATING = configs.getTeamConfig().getString("TEAM_DTR.SYMBOL.REGENERATING");
        SYMBOL_FREEZE = configs.getTeamConfig().getString("TEAM_DTR.SYMBOL.FREEZE");
        SYMBOL_NORMAL = configs.getTeamConfig().getString("TEAM_DTR.SYMBOL.NORMAL");

        HQ_FORMAT_NONE = configs.getLanguageConfig().getString("TEAM_COMMAND.TEAM_INFO.HQ_FORMAT.NONE");
        HQ_FORMAT_SET = configs.getLanguageConfig().getString("TEAM_COMMAND.TEAM_INFO.HQ_FORMAT.SET");

        // Deaths
        DEATH_FORMAT = configs.getLanguageConfig().getString("DEATH_LISTENER.PLAYER_FORMAT");
        DEATH_KILLER = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.KILLER");
        DEATH_ENTITY = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.ENTITY");
        DEATH_FALL = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.FALL");
        DEATH_FALL_KILLER = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.FALL_KILLER");
        DEATH_LIGHTNING = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.LIGHTNING");
        DEATH_VOID = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.VOID");
        DEATH_VOID_KILLER = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.VOID_KILLER");
        DEATH_EXPLOSION = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.EXPLOSION");
        DEATH_CONTACT = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.CONTACT");
        DEATH_WITHER = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.WITHER");
        DEATH_STARVATION = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.STARVATION");
        DEATH_SUFFOCATION = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.SUFFOCATION");
        DEATH_DROWN = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.DROWNED");
        DEATH_POISON = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.POISON");
        DEATH_FIRE = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.FIRE");
        DEATH_LAVA = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.LAVA");
        DEATH_LOGGER = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.LOGGER");
        DEATH_LOGGER_KILLER = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.LOGGER_KILLER");
        DEATH_BACKSTABBED = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.BACKSTABBED");
        DEATH_PROJECTILE = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.PROJECTILE");
        DEATH_PROJECTILE_KILLER = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.PROJECTILE_KILLER");
        DEATH_MAGIC = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.MAGIC");
        DEATH_DEFAULT = configs.getLanguageConfig().getString("DEATH_LISTENER.MESSAGES.DEFAULT");

        CHAT_PUBLIC_NO_TEAM = configs.getConfig().getString("CHAT_FORMAT.PUBLIC_NO_TEAM");
        CHAT_PUBLIC_TEAM = configs.getConfig().getString("CHAT_FORMAT.PUBLIC_TEAM");
        CHAT_TEAM = configs.getConfig().getString("CHAT_FORMAT.TEAM_CHAT.FORMAT");
        CHAT_ALLY = configs.getConfig().getString("CHAT_FORMAT.ALLY_CHAT.FORMAT");
        CHAT_OFFICER = configs.getConfig().getString("CHAT_FORMAT.OFFICER_CHAT.FORMAT");
        CHAT_CO_LEADER = configs.getConfig().getString("CHAT_FORMAT.CO_LEADER_CHAT.FORMAT");
        CHAT_STAFF = configs.getConfig().getString("CHAT_FORMAT.STAFF.FORMAT");
        CHAT_FTOP_FORMAT = configs.getConfig().getString("CHAT_FORMAT.FTOP_FORMAT");
        CHAT_FORBIDDEN = configs.getLanguageConfig().getString("CHAT_LISTENER.FORBIDDEN_MESSAGE");
        CHAT_COOLDOWN = configs.getLanguageConfig().getString("CHAT_LISTENER.COOLDOWN");

        SERVER_NOT_LOADED = configs.getLanguageConfig().getString("USER_LISTENER.SERVER_NOT_LOADED");
        COULD_NOT_LOAD_DATA = configs.getLanguageConfig().getString("USER_LISTENER.COULD_NOT_LOAD_DATA");

        SCHEDULE_TIMEZONE = configs.getSchedulesConfig().getString("SCHEDULE_CONFIG.TIME_ZONE");
        SCHEDULE_NONE_NAME = configs.getSchedulesConfig().getString("SCHEDULE_CONFIG.NO_SCHEDULE_NAME");
        SCHEDULE_NONE_TIME = configs.getSchedulesConfig().getString("SCHEDULE_CONFIG.NO_SCHEDULE_TIME");
        SCHEDULE_NO_PLAYERS_START = configs.getSchedulesConfig().getString("SCHEDULE_CONFIG.PLAYERS_REQUIRED_MESSAGE");

        // Discord stuff
        DISCORD_REQUEST_WEBHOOKURL = configs.getConfig().getString("DISCORD_WEBHOOK.REQUEST_WEBHOOK.WEBHOOK_URL");
        DISCORD_REQUEST_CONTENT = configs.getConfig().getString("DISCORD_WEBHOOK.REQUEST_WEBHOOK.CONTENT");
        DISCORD_REQUEST_DESCRIPTION = configs.getConfig().getString("DISCORD_WEBHOOK.REQUEST_WEBHOOK.DESCRIPTION");
        DISCORD_REQUEST_COLOR = configs.getConfig().getUntranslatedString("DISCORD_WEBHOOK.REQUEST_WEBHOOK.COLOR");
        DISCORD_REQUEST_FOOTER = configs.getConfig().getString("DISCORD_WEBHOOK.REQUEST_WEBHOOK.FOOTER");
        DISCORD_REQUEST_FOOTER_ICON = configs.getConfig().getString("DISCORD_WEBHOOK.REQUEST_WEBHOOK.FOOTER_ICON");
        DISCORD_REQUEST_THUMBNAIL = configs.getConfig().getString("DISCORD_WEBHOOK.REQUEST_WEBHOOK.THUMBNAIL");
        DISCORD_REQUEST_AUTHOR = configs.getConfig().getString("DISCORD_WEBHOOK.REQUEST_WEBHOOK.AUTHOR");
        DISCORD_REQUEST_AUTHOR_URL = configs.getConfig().getString("DISCORD_WEBHOOK.REQUEST_WEBHOOK.AUTHOR_URL");
        DISCORD_REQUEST_AUTHOR_ICON = configs.getConfig().getString("DISCORD_WEBHOOK.REQUEST_WEBHOOK.AUTHOR_ICON");

        DISCORD_REPORT_WEBHOOKURL = configs.getConfig().getString("DISCORD_WEBHOOK.REPORT_WEBHOOK.WEBHOOK_URL");
        DISCORD_REPORT_CONTENT = configs.getConfig().getString("DISCORD_WEBHOOK.REPORT_WEBHOOK.CONTENT");
        DISCORD_REPORT_DESCRIPTION = configs.getConfig().getString("DISCORD_WEBHOOK.REPORT_WEBHOOK.DESCRIPTION");
        DISCORD_REPORT_COLOR = configs.getConfig().getUntranslatedString("DISCORD_WEBHOOK.REPORT_WEBHOOK.COLOR");
        DISCORD_REPORT_FOOTER = configs.getConfig().getString("DISCORD_WEBHOOK.REPORT_WEBHOOK.FOOTER");
        DISCORD_REPORT_FOOTER_ICON = configs.getConfig().getString("DISCORD_WEBHOOK.REPORT_WEBHOOK.FOOTER_ICON");
        DISCORD_REPORT_THUMBNAIL = configs.getConfig().getString("DISCORD_WEBHOOK.REPORT_WEBHOOK.THUMBNAIL");
        DISCORD_REPORT_AUTHOR = configs.getConfig().getString("DISCORD_WEBHOOK.REPORT_WEBHOOK.AUTHOR");
        DISCORD_REPORT_AUTHOR_URL = configs.getConfig().getString("DISCORD_WEBHOOK.REPORT_WEBHOOK.AUTHOR_URL");
        DISCORD_REPORT_AUTHOR_ICON = configs.getConfig().getString("DISCORD_WEBHOOK.REPORT_WEBHOOK.AUTHOR_ICON");

        DISCORD_KOTH_WEBHOOKURL = configs.getConfig().getString("DISCORD_WEBHOOK.KOTH_WEBHOOK.WEBHOOK_URL");
        DISCORD_KOTH_CONTENT = configs.getConfig().getString("DISCORD_WEBHOOK.KOTH_WEBHOOK.CONTENT");
        DISCORD_KOTH_DESCRIPTION = configs.getConfig().getString("DISCORD_WEBHOOK.KOTH_WEBHOOK.DESCRIPTION");
        DISCORD_KOTH_COLOR = configs.getConfig().getUntranslatedString("DISCORD_WEBHOOK.KOTH_WEBHOOK.COLOR");
        DISCORD_KOTH_FOOTER = configs.getConfig().getString("DISCORD_WEBHOOK.KOTH_WEBHOOK.FOOTER");
        DISCORD_KOTH_FOOTER_ICON = configs.getConfig().getString("DISCORD_WEBHOOK.KOTH_WEBHOOK.FOOTER_ICON");
        DISCORD_KOTH_THUMBNAIL = configs.getConfig().getString("DISCORD_WEBHOOK.KOTH_WEBHOOK.THUMBNAIL");
        DISCORD_KOTH_AUTHOR = configs.getConfig().getString("DISCORD_WEBHOOK.KOTH_WEBHOOK.AUTHOR");
        DISCORD_KOTH_AUTHOR_URL = configs.getConfig().getString("DISCORD_WEBHOOK.KOTH_WEBHOOK.AUTHOR_URL");
        DISCORD_KOTH_AUTHOR_ICON = configs.getConfig().getString("DISCORD_WEBHOOK.KOTH_WEBHOOK.AUTHOR_ICON");

        DISCORD_RESTORE_WEBHOOKURL = configs.getConfig().getString("DISCORD_WEBHOOK.RESTORE_WEBHOOK.WEBHOOK_URL");
        DISCORD_RESTORE_CONTENT = configs.getConfig().getString("DISCORD_WEBHOOK.RESTORE_WEBHOOK.CONTENT");
        DISCORD_RESTORE_DESCRIPTION = configs.getConfig().getString("DISCORD_WEBHOOK.RESTORE_WEBHOOK.DESCRIPTION");
        DISCORD_RESTORE_COLOR = configs.getConfig().getUntranslatedString("DISCORD_WEBHOOK.RESTORE_WEBHOOK.COLOR");
        DISCORD_RESTORE_FOOTER = configs.getConfig().getString("DISCORD_WEBHOOK.RESTORE_WEBHOOK.FOOTER");
        DISCORD_RESTORE_FOOTER_ICON = configs.getConfig().getString("DISCORD_WEBHOOK.RESTORE_WEBHOOK.FOOTER_ICON");
        DISCORD_RESTORE_THUMBNAIL = configs.getConfig().getString("DISCORD_WEBHOOK.RESTORE_WEBHOOK.THUMBNAIL");
        DISCORD_RESTORE_AUTHOR = configs.getConfig().getString("DISCORD_WEBHOOK.RESTORE_WEBHOOK.AUTHOR");
        DISCORD_RESTORE_AUTHOR_URL = configs.getConfig().getString("DISCORD_WEBHOOK.RESTORE_WEBHOOK.AUTHOR_URL");
        DISCORD_RESTORE_AUTHOR_ICON = configs.getConfig().getString("DISCORD_WEBHOOK.RESTORE_WEBHOOK.AUTHOR_ICON");

        ABILITY_ACTION_BAR_SYMBOL = configs.getAbilitiesConfig().getString("ACTION_BAR_COOLDOWN.SYMBOL");
        ABILITY_ACTION_BAR_NO_COLOR = configs.getAbilitiesConfig().getString("ACTION_BAR_COOLDOWN.NON_COMPLETE_COLOR");
        ABILITY_ACTION_BAR_YES_COLOR = configs.getAbilitiesConfig().getString("ACTION_BAR_COOLDOWN.COMPLETE_COLOR");
        ABILITY_ACTION_BAR_STRING = configs.getAbilitiesConfig().getString("ACTION_BAR_COOLDOWN.ACTION_BAR");

        FALLTRAP_ACTION_BAR_SYMBOL = configs.getTeamConfig().getString("FALLTRAP_CONFIG.ACTION_BAR_COOLDOWN.SYMBOL");
        FALLTRAP_ACTION_BAR_NO_COLOR = configs.getTeamConfig().getString("FALLTRAP_CONFIG.ACTION_BAR_COOLDOWN.NON_COMPLETE_COLOR");
        FALLTRAP_ACTION_BAR_YES_COLOR = configs.getTeamConfig().getString("FALLTRAP_CONFIG.ACTION_BAR_COOLDOWN.COMPLETE_COLOR");
        FALLTRAP_ACTION_BAR_STRING = configs.getTeamConfig().getString("FALLTRAP_CONFIG.ACTION_BAR_COOLDOWN.ACTION_BAR");
        FALLTRAP_SOUND = configs.getTeamConfig().getString("FALLTRAP_CONFIG.SOUND");
        FALLTRAP_PARTICLE = ParticleType.of(configs.getTeamConfig().getString("FALLTRAP_CONFIG.PARTICLE"));

        BASE_ACTION_BAR_SYMBOL = configs.getTeamConfig().getString("BASE_CONFIG.ACTION_BAR_COOLDOWN.SYMBOL");
        BASE_ACTION_BAR_NO_COLOR = configs.getTeamConfig().getString("BASE_CONFIG.ACTION_BAR_COOLDOWN.NON_COMPLETE_COLOR");
        BASE_ACTION_BAR_YES_COLOR = configs.getTeamConfig().getString("BASE_CONFIG.ACTION_BAR_COOLDOWN.COMPLETE_COLOR");
        BASE_ACTION_BAR_STRING = configs.getTeamConfig().getString("BASE_CONFIG.ACTION_BAR_COOLDOWN.ACTION_BAR");
        BASE_SOUND = configs.getTeamConfig().getString("BASE_CONFIG.SOUND");
        BASE_PARTICLE = ParticleType.of(configs.getTeamConfig().getString("BASE_CONFIG.PARTICLE"));

        KILL_HEADER = configs.getConfig().getString("STATS_TRACKER.KILLS_PLACEHOLDER");
        DEATH_HEADER = configs.getConfig().getString("STATS_TRACKER.DEATHS_PLACEHOLDER");
        KILL_STAT = configs.getConfig().getString("STATS_TRACKER.KILL_STAT");
        DEATH_STAT = configs.getConfig().getString("STATS_TRACKER.DEATH_STAT");

        TABLIST_REPLACE_KOTH_NAME = configs.getTablistConfig().getString("TABLIST_INFO.REPLACE_SCHEDULE_NAME");
        TABLIST_REPLACE_KOTH_TIME = configs.getTablistConfig().getString("TABLIST_INFO.REPLACE_SCHEDULE_TIME");

        ANTICLEAN_STATS_HITS_MESSAGE = configs.getLanguageConfig().getString("ANTICLEAN_TIMER.STATS_MESSAGE.HITS_MESSAGE");
        ANTICLEAN_STATS_EMPTY = configs.getLanguageConfig().getString("ANTICLEAN_TIMER.STATS_MESSAGE.EMPTY_HITS");

        TABLIST_TEAM_CLAIM_SAFEZONE = new TablistSkin(
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.SAFEZONE.VALUE"),
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.SAFEZONE.SIGNATURE"));
        TABLIST_TEAM_CLAIM_WARZONE = new TablistSkin(
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.WARZONE.VALUE"),
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.WARZONE.SIGNATURE"));
        TABLIST_TEAM_CLAIM_WILDERNESS = new TablistSkin(
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.WILDERNESS.VALUE"),
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.WILDERNESS.SIGNATURE"));
        TABLIST_TEAM_CLAIM_PLAYER_TEAM = new TablistSkin(
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.TEAM_FRIENDLY.VALUE"),
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.TEAM_FRIENDLY.SIGNATURE"));
        TABLIST_TEAM_CLAIM_PLAYER_ALLY = new TablistSkin(
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.TEAM_ALLY.VALUE"),
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.TEAM_ALLY.SIGNATURE"));
        TABLIST_TEAM_CLAIM_PLAYER_FOCUS = new TablistSkin(
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.TEAM_FOCUS.VALUE"),
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.TEAM_FOCUS.SIGNATURE"));
        TABLIST_TEAM_CLAIM_PLAYER_ENEMY = new TablistSkin(
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.TEAM_ENEMY.VALUE"),
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.TEAM_ENEMY.SIGNATURE"));
        TABLIST_TEAM_CLAIM_MOUNTAIN = new TablistSkin(
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.MOUNTAIN.VALUE"),
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.MOUNTAIN.SIGNATURE"));
        TABLIST_TEAM_CLAIM_ROAD = new TablistSkin(
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.ROAD.VALUE"),
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.ROAD.SIGNATURE"));
        TABLIST_TEAM_CLAIM_EVENT = new TablistSkin(
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.EVENT.VALUE"),
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.EVENT.SIGNATURE"));
        TABLIST_TEAM_CLAIM_CITADEL = new TablistSkin(
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.CITADEL.VALUE"),
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.CITADEL.SIGNATURE"));
        TABLIST_TEAM_CLAIM_CONQUEST = new TablistSkin(
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.CONQUEST.VALUE"),
                configs.getTablistConfig().getString("TEAM_FORMAT.CLAIM_SKIN_CHANGE.CONQUEST.SIGNATURE"));

        TABLIST_TEAM_SKIN_SKIN = new TablistSkin(
                configs.getTablistConfig().getString("TEAM_FORMAT.TEAM_SKIN_CHANGE.VALUE"),
                configs.getTablistConfig().getString("TEAM_FORMAT.TEAM_SKIN_CHANGE.SIGNATURE"));

        MAX_KILL_STAT = configs.getConfig().getInt("STATS_TRACKER.MAX_KILLS_STATS");
        MAX_DEATH_STAT = configs.getConfig().getInt("STATS_TRACKER.MAX_DEATHS_STATS");

        TABLIST_MEMBER_FORMAT = configs.getTablistConfig().getString("TEAM_FORMAT.MEMBER_FORMAT");
        TABLIST_LIST_FORMAT_DTR = configs.getTablistConfig().getString("TEAM_FORMAT.LIST_FORMAT.DTR");
        TABLIST_LIST_FORMAT_ONLINE = configs.getTablistConfig().getString("TEAM_FORMAT.LIST_FORMAT.ONLINE");

        DEATHBAN_STRING = configs.getLanguageConfig().getString("TEAM_LISTENER.CLAIM_MESSAGE.DEATHBAN_FORMAT.DEATHBAN");
        NON_DEATHBAN_STRING = configs.getLanguageConfig().getString("TEAM_LISTENER.CLAIM_MESSAGE.DEATHBAN_FORMAT.NON_DEATHBAN");

        BLOCK_DIG_DENIED = configs.getLanguageConfig().getString("TEAM_LISTENER.BLOCK_DIG");
        BLOCK_PLACE_DENIED = configs.getLanguageConfig().getString("TEAM_LISTENER.BLOCK_PLACE");
        BLOCK_INTERACT_DENIED = configs.getLanguageConfig().getString("TEAM_LISTENER.BLOCK_INTERACT");

        DISPLAY_NAME_WARZONE = configs.getTeamConfig().getString("SYSTEM_TEAMS.WARZONE");
        DISPLAY_NAME_WILDERNESS = configs.getTeamConfig().getString("SYSTEM_TEAMS.WILDERNESS");
        DISPLAY_NAME_SAFEZONE = configs.getTeamConfig().getString("SYSTEM_TEAMS.SAFEZONE");
        DISPLAY_NAME_ROAD = configs.getTeamConfig().getString("SYSTEM_TEAMS.ROADS");
        DISPLAY_NAME_CITADEL = configs.getTeamConfig().getString("SYSTEM_TEAMS.CITADEL");
        DISPLAY_NAME_CONQUEST = configs.getTeamConfig().getString("SYSTEM_TEAMS.CONQUEST");
        DISPLAY_NAME_EVENT = configs.getTeamConfig().getString("SYSTEM_TEAMS.EVENT");

        LUNAR_PREFIXES_ONE = configs.getLunarConfig().getString("LUNAR_PREFIXES.PLAYER.ONE");
        LUNAR_PREFIXES_TWO = configs.getLunarConfig().getString("LUNAR_PREFIXES.PLAYER.TWO");
        LUNAR_PREFIXES_THREE = configs.getLunarConfig().getString("LUNAR_PREFIXES.PLAYER.THREE");

        NAMETAGS_TEAM_TOP = configs.getLunarConfig().getString("NAMETAGS.TEAM_TOP");
        NAMETAGS_NORMAL = configs.getLunarConfig().getString("NAMETAGS.NORMAL");

        HOLOGRAM_EMPTY = configs.getConfig().getString("HOLOGRAM_PLACEHOLDER.NONE");
        HOLOGRAM_FTOP = configs.getConfig().getString("HOLOGRAM_PLACEHOLDER.FTOP");
        HOLOGRAM_FTOP_RAIDABLE = configs.getConfig().getString("HOLOGRAM_PLACEHOLDER.FTOP_RAIDABLE");
        HOLOGRAM_KILLS = configs.getConfig().getString("HOLOGRAM_PLACEHOLDER.KILLS_TOP");
        HOLOGRAM_DEATHS = configs.getConfig().getString("HOLOGRAM_PLACEHOLDER.DEATHS_TOP");
        HOLOGRAM_KDR = configs.getConfig().getString("HOLOGRAM_PLACEHOLDER.KDR_TOP");
        HOLOGRAM_KILLSTREAK = configs.getConfig().getString("HOLOGRAM_PLACEHOLDER.KILLSTREAK_TOP");

        LAST_KILL_STRING = configs.getConfig().getString("LAST_KILLS.MESSAGE");
        LAST_DEATH_STRING = configs.getConfig().getString("LAST_DEATHS.MESSAGE");

        MEMBER_ONLINE = configs.getLanguageConfig().getString("TEAM_COMMAND.TEAM_INFO.MEMBER_FORMAT.ONLINE");
        MEMBER_DEATHBANNED = configs.getLanguageConfig().getString("TEAM_COMMAND.TEAM_INFO.MEMBER_FORMAT.DEATHBANNED");
        MEMBER_OFFLINE = configs.getLanguageConfig().getString("TEAM_COMMAND.TEAM_INFO.MEMBER_FORMAT.OFFLINE");

        ALLY_FORMAT = configs.getLanguageConfig().getString("TEAM_COMMAND.TEAM_INFO.ALLY_FORMAT");

        LUNAR_ONE_COLOR = configs.getLunarConfig().getString("LUNAR_PREFIXES.TEAMS.ONE.COLOR");
        LUNAR_ONE_PREFIX = configs.getLunarConfig().getString("LUNAR_PREFIXES.TEAMS.ONE.PREFIX");
        LUNAR_TWO_COLOR = configs.getLunarConfig().getString("LUNAR_PREFIXES.TEAMS.TWO.COLOR");
        LUNAR_TWO_PREFIX = configs.getLunarConfig().getString("LUNAR_PREFIXES.TEAMS.TWO.PREFIX");
        LUNAR_THREE_COLOR = configs.getLunarConfig().getString("LUNAR_PREFIXES.TEAMS.THREE.COLOR");
        LUNAR_THREE_PREFIX = configs.getLunarConfig().getString("LUNAR_PREFIXES.TEAMS.THREE.PREFIX");

        SCOREBOARD_TITLE = configs.getScoreboardConfig().getString("SCOREBOARD_INFO.TITLE");
        DONOR_SOUND = configs.getConfig().getString("ONLINE_DONOR.SOUND");
        FOUND_DIAMOND = configs.getLanguageConfig().getString("DIAMOND_LISTENER.FD_MESSAGE");

        LAST_KILLS_LIMIT = configs.getConfig().getInt("LAST_KILLS.LIMIT");
        LAST_DEATHS_LIMIT = configs.getConfig().getInt("LAST_DEATHS.LIMIT");

        REPAIR_ALL_COST = configs.getConfig().getInt("REPAIR_BLOCK.REPAIR_ALL_COST");
        REPAIR_HAND_COST = configs.getConfig().getInt("REPAIR_BLOCK.REPAIR_HAND_COST");
        ANTICLEAN_RADIUS = configs.getConfig().getInt("ANTI_CLEAN.RADIUS");
        ANTICLEAN_KOTH_RADIUS = configs.getConfig().getInt("ANTI_CLEAN.KOTH_RADIUS");

        LUNAR_PREFIXES_ENABLED = configs.getLunarConfig().getBoolean("LUNAR_PREFIXES.ENABLED");
        SCOREBOARD_CHANGER_ENABLED = configs.getScoreboardConfig().getBoolean("TITLE_CONFIG.CHANGER_ENABLED");
        DEATH_STAT_ENABLED = configs.getConfig().getBoolean("STATS_TRACKER.ENABLED");
        ANTICLEAN_ENABLED = configs.getConfig().getBoolean("ANTI_CLEAN.ENABLED");
        TABLIST_MEMBER_SKIN = configs.getTablistConfig().getBoolean("TEAM_FORMAT.MEMBER_SHOW_SKIN");
        TABLIST_TEAM_SKIN = configs.getTablistConfig().getBoolean("TEAM_FORMAT.CHANGE_TEAM_SKIN");
        TABLIST_CLAIM_SKIN = configs.getTablistConfig().getBoolean("TEAM_FORMAT.CHANGE_CLAIM_SKIN");
        TABLIST_REPLACE_KOTH = configs.getTablistConfig().getBoolean("TABLIST_INFO.REPLACE_KOTH_ACTIVE");

        // Join Items
        JOIN_ITEMS_ENABLED = configs.getConfig().getBoolean("JOIN_ITEMS.ENABLED");
        JOIN_ITEMS_BOOK_ENABLED = configs.getConfig().getBoolean("JOIN_ITEMS.BOOK_ITEM.ENABLED");
        JOIN_COMMANDS_ENABLED = configs.getConfig().getBoolean("JOIN_COMMANDS.ENABLED");
        JOIN_TEAM_INFO_ENABLED = configs.getConfig().getBoolean("TEAM_INFO_JOIN");
        JOIN_MOTD_ENABLED = configs.getConfig().getBoolean("MOTD_MESSAGE.ENABLED");

        COBWEB_PLACE_WARZONE = configs.getConfig().getBoolean("COBWEBS_CONFIG.PLACE_WARZONE");
        COBWEB_PLACE_ROAD = configs.getConfig().getBoolean("COBWEBS_CONFIG.PLACE_ROAD");
        COBWEB_PLACE_WILDERNESS = configs.getConfig().getBoolean("COBWEBS_CONFIG.PLACE_WILDERNESS");
        COBWEB_DESPAWN_TIME = configs.getConfig().getInt("COBWEBS_CONFIG.DESPAWN_TIME");

        SCOREBOARD_ENABLED = configs.getScoreboardConfig().getBoolean("SCOREBOARD_INFO.ENABLED");
        TABLIST_ENABLED = configs.getTablistConfig().getBoolean("TABLIST_INFO.ENABLED");
        MILK_FIX = configs.getConfig().getBoolean("MILK_ONLY_REMOVE_NEGATIVE");
        DISABLE_ENDER_EYE = configs.getConfig().getBoolean("DISABLE_ENDER_EYE");

        NATURAL_MOB_SPAWN = configs.getConfig().getBoolean("MOB_NATURAL_SPAWN");
        DEFAULT_CLAIM_SCOREBOARD = configs.getConfig().getBoolean("DEFAULT_CLAIM_SCOREBOARD");
        DEFAULT_DEATH_MESSAGES = configs.getConfig().getBoolean("DEFAULT_DEATH_MESSAGES");

        TITLE_CHANGER_ENABLED = configs.getScoreboardConfig().getBoolean("TITLE_CONFIG.CHANGER_ENABLED");
        WEBHOOKS_ENABLED = configs.getConfig().getBoolean("DISCORD_WEBHOOK.ENABLED");

        GIVE_EXP_MINE = configs.getConfig().getBoolean("GIVE_EXP_MINE");
        GIVE_EXP_KILL = configs.getConfig().getBoolean("GIVE_EXP_KILL");
        GIVE_ITEM_ON_KILL = configs.getConfig().getBoolean("GIVE_ITEM_ON_KILL");
        GIVE_SMELT_ON_MINE = configs.getConfig().getBoolean("SMELT_ON_MINE");
        GIVE_BLOCKS_ON_MINE = configs.getConfig().getBoolean("GIVE_BLOCK_ON_MINE");
        ALLOW_ORES_MINE_SPAWN = configs.getConfig().getBoolean("ALLOW_ORE_MINE_SPAWN");
        ALLOW_BOW_BOOSTING = configs.getConfig().getBoolean("ALLOW_BOW_BOOSTING");
        ALLOW_LUNAR_WALLS = configs.getConfig().getBoolean("WALLS.LUNAR_WALLS_ENABLED");

        STRENGTH_NERF = configs.getConfig().getBoolean("STRENGTH_FIX.ENABLED");
        ENDERMEN_HOSTILE = configs.getConfig().getBoolean("ENDERMEN_HOSTILE");

        // Team Name
        TEAM_NAME_MIN_LENGTH = configs.getTeamConfig().getInt("TEAMS.NAME_MIN_LENGTH");
        TEAM_NAME_MAX_LENGTH = configs.getTeamConfig().getInt("TEAMS.NAME_MAX_LENGTH");
        TEAM_ANNOUNCEMENT_MAX = configs.getTeamConfig().getInt("TEAMS.ANNOUNCEMENT_MAX_LENGTH");
        TEAM_EVENT_ENTER_LIMIT = configs.getConfig().getInt("LIMIT_ENTRY_PER_TEAM.EVENTS");
        TEAM_CITADEL_ENTER_LIMIT = configs.getConfig().getInt("LIMIT_ENTRY_PER_TEAM.CITADEL");
        TEAM_CONQUEST_ENTER_LIMIT = configs.getConfig().getInt("LIMIT_ENTRY_PER_TEAM.CONQUEST");

        ABILITY_DISABLED_RADIUS = configs.getAbilitiesConfig().getInt("GLOBAL_ABILITY.DISABLE_IN_RADIUS");

        WARZONE_NORMAL = configs.getTeamConfig().getInt("WARZONE.WARZONE_NORMAL");
        WARZONE_NETHER = configs.getTeamConfig().getInt("WARZONE.WARZONE_NETHER");

        WARZONE_BREAK = configs.getTeamConfig().getInt("WARZONE.WARZONE_BREAK");
        WARZONE_BREAK_NETHER = configs.getTeamConfig().getInt("WARZONE.WARZONE_BREAK_NETHER");

        // Misc
        DONOR_INTERVAL = configs.getConfig().getInt("ONLINE_DONOR.INTERVAL");
        RESTORE_LIMIT = configs.getConfig().getInt("INVENTORY_RESTORE.RESTORES_LIMIT");

        // Multipliers
        NETHER_MULTIPLIER = configs.getConfig().getInt("MULTIPLIERS.NETHER_MULTIPLIER");
        SMELT_MULTIPLIER = configs.getConfig().getInt("MULTIPLIERS.SMELT_MULTIPLIER");
        DEFAULT_BAL = configs.getConfig().getInt("STARTING_BALANCES.DEFAULT_MEMBER");

        // Conquest
        CONQUEST_SECONDS = configs.getConfig().getInt("CONQUEST.SECONDS_PER_CAPZONE");
        CONQUEST_POINTS_CAPTURE = configs.getConfig().getInt("CONQUEST.POINTS_CAPTURE");
        CONQUEST_POINTS_DEATH = configs.getConfig().getInt("CONQUEST.POINTS_DEATH");

        TABLIST_PING = configs.getTablistConfig().getInt("TABLIST_INFO.PING");
        ABILITY_ACTION_BAR_BARS = configs.getAbilitiesConfig().getInt("ACTION_BAR_COOLDOWN.BARS_AMOUNT");
        FALLTRAP_ACTION_BAR_BARS = configs.getTeamConfig().getInt("FALLTRAP_CONFIG.ACTION_BAR_COOLDOWN.BARS_AMOUNT");
        BASE_ACTION_BAR_BARS = configs.getTeamConfig().getInt("BASE_CONFIG.ACTION_BAR_COOLDOWN.BARS_AMOUNT");
        FALLTRAP_PARTICLE_AMOUNT = configs.getTeamConfig().getInt("FALLTRAP_CONFIG.PARTICLE_AMOUNT");
        BASE_PARTICLE_AMOUNT = configs.getTeamConfig().getInt("BASE_CONFIG.PARTICLE_AMOUNT");

        // Team Stuff
        DTR_REGEN_PER_MIN = configs.getTeamConfig().getDouble("TEAM_DTR.REGEN_PER_INTERVAL");
        DTR_TAKE_DEATH_NORMAL = configs.getTeamConfig().getDouble("TEAM_DTR.DTR_LOSS.NORMAL");
        DTR_TAKE_DEATH_END = configs.getTeamConfig().getDouble("TEAM_DTR.DTR_LOSS.END");
        DTR_TAKE_DEATH_NETHER = configs.getTeamConfig().getDouble("TEAM_DTR.DTR_LOSS.NETHER");
        DTR_TAKE_DEATH_EVENTS = configs.getTeamConfig().getDouble("TEAM_DTR.DTR_LOSS.EVENTS");
        DTR_TAKE_DEATH_CITADEL = configs.getTeamConfig().getDouble("TEAM_DTR.DTR_LOSS.CITADEL");

        STRENGTH_NERF_LEVEL1 = configs.getConfig().getDouble("STRENGTH_FIX.MULTIPLIER_LEVEL_1");
        STRENGTH_NERF_LEVEL2 = configs.getConfig().getDouble("STRENGTH_FIX.MULTIPLIER_LEVEL_2");
        STRENGTH_NERF_LEVEL3PLUS = configs.getConfig().getDouble("STRENGTH_FIX.MULTIPLIER_LEVEL_3+");

        LOW_DTR = configs.getTeamConfig().getDouble("TEAM_DTR.LOW_DTR");

        DTR_PER_MEMBERS = configs.getTeamConfig().getStringList("TEAM_DTR.DTR_PER_MEMBER")
                .stream().map(Double::valueOf).collect(Collectors.toList());
        JOIN_MOTD = configs.getConfig().getStringList("MOTD_MESSAGE.MOTD");
        CLAIM_CHANGE = configs.getLanguageConfig().getStringList("TEAM_LISTENER.CLAIM_MESSAGE.MESSAGE");
        DONOR_MESSAGE = configs.getConfig().getStringList("ONLINE_DONOR.MESSAGE");

        ANTICLEAN_STATS_MESSAGE = configs.getLanguageConfig().getStringList("ANTICLEAN_TIMER.STATS_MESSAGE.LINES");

        NAMETAG_MOD_MODE = configs.getLunarConfig().getStringList("NAMETAGS.FORMAT.MOD_MODE");
        NAMETAG_IN_TEAM = configs.getLunarConfig().getStringList("NAMETAGS.FORMAT.IN_TEAM");
        NAMETAG_NO_TEAM = configs.getLunarConfig().getStringList("NAMETAGS.FORMAT.NO_TEAM");
        TAB_SKIN_CACHE_LEFT = configs.getTablistConfig().getStringList("LEFT").stream().map(s -> s.split(";")[0]).collect(Collectors.toList());
        TAB_SKIN_CACHE_MIDDLE = configs.getTablistConfig().getStringList("MIDDLE").stream().map(s -> s.split(";")[0]).collect(Collectors.toList());
        TAB_SKIN_CACHE_RIGHT = configs.getTablistConfig().getStringList("RIGHT").stream().map(s -> s.split(";")[0]).collect(Collectors.toList());
        TAB_SKIN_CACHE_FAR_RIGHT = configs.getTablistConfig().getStringList("FAR_RIGHT").stream().map(s -> s.split(";")[0]).collect(Collectors.toList());

        TEAM_INFO_FANCY_MESSAGES = configs.getLanguageConfig().getStringList("TEAM_COMMAND.TEAM_INFO.FANCY_MESSAGES")
                .stream().map(s -> s.split(", "))
                .collect(Collectors.toMap(s -> s[0], s -> new FancyMessageData(s[1], s[2], s[3])));

        TEAM_INFO_SYSTEM = Serializer.loadFancyMessages(configs.getLanguageConfig().getStringList("TEAM_COMMAND.TEAM_INFO.FORMAT_SYSTEM_TEAM"));
        TEAM_INFO_FRIENDLY = Serializer.loadFancyMessages(configs.getLanguageConfig().getStringList("TEAM_COMMAND.TEAM_INFO.FORMAT_FRIENDLIES"));
        TEAM_INFO_ENEMIES = Serializer.loadFancyMessages(configs.getLanguageConfig().getStringList("TEAM_COMMAND.TEAM_INFO.FORMAT_ENEMIES"));
        TEAM_INFO_STAFF = Serializer.loadFancyMessages(configs.getLanguageConfig().getStringList("TEAM_COMMAND.TEAM_INFO.FORMAT_STAFF"));

        ARCHER_TAG_DAMAGE = configs.getClassesConfig().getDouble("ARCHER_CLASS.ARCHER_TAG_DAMAGE") * 2.0;
        ARCHER_HALF_FORCE_DAMAGE = configs.getClassesConfig().getDouble("ARCHER_CLASS.ARCHER_HALF_FORCE") * 2.0;
        ARCHER_TAGGED_DAMAGE = configs.getClassesConfig().getDouble("ARCHER_CLASS.ALREADY_TAGGED_DAMAGE") * 2.0;
        ARCHER_TAGGED_MULTIPLIER = configs.getClassesConfig().getDouble("ARCHER_CLASS.TAGGED_DAMAGE_MULTIPLIER");

        PVP_TIMER_WALL_DATA = (byte) configs.getConfig().getInt("WALLS.PVP_TIMER.DATA");
        COMBAT_TAG_WALL_DATA = (byte) configs.getConfig().getInt("WALLS.COMBAT_TAG.DATA");
        INVINCIBILITY_WALL_DATA = (byte) configs.getConfig().getInt("WALLS.INVINCIBILITY.DATA");
        EVENT_DENIED_WALL_DATA = (byte) configs.getConfig().getInt("WALLS.EVENT_DENIED.DATA");
        CITADEL_DENIED_WALL_DATA = (byte) configs.getConfig().getInt("WALLS.CITADEL_DENIED.DATA");
        CONQUEST_DENIED_WALL_DATA = (byte) configs.getConfig().getInt("WALLS.CONQUEST_DENIED.DATA");
        LOCKED_CLAIM_WALL_DATA = (byte) configs.getConfig().getInt("WALLS.LOCKED_CLAIM.DATA");

        PVP_TIMER_WALL_MATERIAL = ItemUtils.getMat(configs.getConfig().getString("WALLS.PVP_TIMER.TYPE"));
        COMBAT_TAG_WALL_MATERIAL = ItemUtils.getMat(configs.getConfig().getString("WALLS.COMBAT_TAG.TYPE"));
        INVINCIBILITY_WALL_MATERIAL = ItemUtils.getMat(configs.getConfig().getString("WALLS.INVINCIBILITY.TYPE"));
        EVENT_DENIED_WALL_MATERIAL = ItemUtils.getMat(configs.getConfig().getString("WALLS.EVENT_DENIED.TYPE"));
        CITADEL_DENIED_WALL_MATERIAL = ItemUtils.getMat(configs.getConfig().getString("WALLS.CITADEL_DENIED.TYPE"));
        CONQUEST_DENIED_WALL_MATERIAL = ItemUtils.getMat(configs.getConfig().getString("WALLS.CONQUEST_DENIED.TYPE"));
        LOCKED_CLAIM_WALL_MATERIAL = ItemUtils.getMat(configs.getConfig().getString("WALLS.LOCKED_CLAIM.TYPE"));

        PVP_TIMER_WALL_COLOR = Color.decode(configs.getConfig().getUntranslatedString("WALLS.PVP_TIMER.COLOR_LUNAR"));
        COMBAT_TAG_WALL_COLOR = Color.decode(configs.getConfig().getUntranslatedString("WALLS.COMBAT_TAG.COLOR_LUNAR"));
        INVINCIBILITY_WALL_COLOR = Color.decode(configs.getConfig().getUntranslatedString("WALLS.INVINCIBILITY.COLOR_LUNAR"));
        EVENT_DENIED_WALL_COLOR = Color.decode(configs.getConfig().getUntranslatedString("WALLS.EVENT_DENIED.COLOR_LUNAR"));
        CITADEL_DENIED_WALL_COLOR = Color.decode(configs.getConfig().getUntranslatedString("WALLS.CITADEL_DENIED.COLOR_LUNAR"));
        CONQUEST_DENIED_WALL_COLOR = Color.decode(configs.getConfig().getUntranslatedString("WALLS.CONQUEST_DENIED.COLOR_LUNAR"));
        LOCKED_CLAIM_WALL_COLOR = Color.decode(configs.getConfig().getUntranslatedString("WALLS.LOCKED_CLAIM.COLOR_LUNAR"));

        REFILL_SIGN = BukkitSerialization.itemStackArrayFromBase64(configs.getMiscConfig().getString("REFILL_SIGN"));
        QUICK_REFILL_SIGN = BukkitSerialization.itemStackArrayFromBase64(configs.getMiscConfig().getString("QUICK_REFILL"));

        CROWBAR = new ItemBuilder(ItemUtils.getMat(configs.getConfig().getString("CROWBARS.MATERIAL")))
                .setName(configs.getConfig().getString("CROWBARS.NAME"))
                .addLoreLine(configs.getConfig().getString("CROWBARS.SPAWNER_LINE").replace("%spawner%", String.valueOf(configs.getConfig().getInt("CROWBARS.SPAWNER_AMOUNT"))))
                .addLoreLine(configs.getConfig().getString("CROWBARS.END_FRAME_LINE").replace("%endframe%", String.valueOf(configs.getConfig().getInt("CROWBARS.END_FRAME_AMOUNT"))))
                .toItemStack();

        for (TitlesConfig title : TitlesConfig.values()) {
            title.load(configs);
        }
    }
}