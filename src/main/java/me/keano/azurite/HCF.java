package me.keano.azurite;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.balance.BalanceManager;
import me.keano.azurite.modules.blockshop.BlockshopManager;
import me.keano.azurite.modules.board.BoardManager;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.deathban.DeathbanManager;
import me.keano.azurite.modules.events.conquest.ConquestManager;
import me.keano.azurite.modules.events.eotw.EOTWManager;
import me.keano.azurite.modules.events.king.KingManager;
import me.keano.azurite.modules.events.koth.KothManager;
import me.keano.azurite.modules.events.purge.PurgeManager;
import me.keano.azurite.modules.events.sotw.SOTWManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.framework.extra.Configs;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.hooks.abilities.AbilitiesHook;
import me.keano.azurite.modules.hooks.clients.ClientHook;
import me.keano.azurite.modules.hooks.pearls.PearlHook;
import me.keano.azurite.modules.hooks.placeholder.PlaceholderHook;
import me.keano.azurite.modules.hooks.ranks.RankHook;
import me.keano.azurite.modules.hooks.tags.TagHook;
import me.keano.azurite.modules.killstreaks.KillstreakManager;
import me.keano.azurite.modules.killtag.KilltagManager;
import me.keano.azurite.modules.kits.KitManager;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.modules.listeners.type.GlitchListener;
import me.keano.azurite.modules.loggers.LoggerManager;
import me.keano.azurite.modules.nametags.NametagManager;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.reclaims.ReclaimManager;
import me.keano.azurite.modules.scheduler.ScheduleManager;
import me.keano.azurite.modules.signs.CustomSignManager;
import me.keano.azurite.modules.spawners.SpawnerManager;
import me.keano.azurite.modules.staff.StaffManager;
import me.keano.azurite.modules.storage.StorageManager;
import me.keano.azurite.modules.tablist.TablistManager;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.editor.FactionEditorManager;
import me.keano.azurite.modules.timers.TimerManager;
import me.keano.azurite.modules.tips.TipManager;
import me.keano.azurite.modules.masks.MaskManager;
import me.keano.azurite.modules.users.UserManager;
import me.keano.azurite.modules.versions.VersionManager;
import me.keano.azurite.modules.walls.WallManager;
import me.keano.azurite.modules.waypoints.WaypointManager;
import me.keano.azurite.utils.Logger;
import me.keano.azurite.utils.configs.ConfigYML;
import me.keano.azurite.utils.extra.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public final class HCF extends JavaPlugin {
    @Getter private static HCF plugin;
    private List<Manager> managers;
    private List<ConfigYML> configs;

    private List<Cooldown> cooldowns;
    private List<TeamCooldown> teamCooldowns;
    private List<AzuriteDecimalFormat> decimalFormats;
    private List<AzuriteDateFormat> dateFormats;

    private Configs configsObject;

    private Gson gson; // used for serialization / deserialization
    private GlitchListener glitchListener; // Used to check glitch cooldown

    // Managers
    private UserManager userManager;
    private VersionManager versionManager;
    private TimerManager timerManager;
    private BalanceManager balanceManager;
    private WaypointManager waypointManager;
    private TeamManager teamManager;
    private WallManager wallManager;
    private SpawnerManager spawnerManager;
    private DeathbanManager deathbanManager;
    private ReclaimManager reclaimManager;
    private KitManager kitManager;
    private RankHook rankHook;
    private TagHook tagHook;
    private PlaceholderHook placeholderHook;
    private ClientHook clientHook;
    private AbilitiesHook abilitiesHook;
    private PvPClassManager classManager;
    private LoggerManager loggerManager;
    private AbilityManager abilityManager;
    private StorageManager storageManager;
    private ScheduleManager scheduleManager;
    private StaffManager staffManager;
    private NametagManager nametagManager;
    private KillstreakManager killstreakManager;
    private KilltagManager killtagManager;
    private CustomSignManager customSignManager;
    private BlockshopManager blockshopManager;
    private MenuManager menuManager;
    private ListenerManager listenerManager;
    private FactionEditorManager factionEditorManager;
    private MaskManager maskManager;

    // Events
    private KothManager kothManager;
    private KingManager kingManager;
    private SOTWManager sotwManager;
    private EOTWManager eotwManager;
    private PurgeManager purgeManager;
    private ConquestManager conquestManager;

    private boolean loaded = false;
    private boolean kits;
    public String load;

    @Override
    public void onEnable() {
        plugin = this;
        this.managers = new ArrayList<>();
        this.configs = new ArrayList<>();
        this.cooldowns = new ArrayList<>();
        this.teamCooldowns = new ArrayList<>();
        this.decimalFormats = new ArrayList<>();
        this.dateFormats = new ArrayList<>();

        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        (this.configsObject = new Configs()).load(this);

        this.userManager = new UserManager(this);
        this.versionManager = new VersionManager(this);
        this.timerManager = new TimerManager(this);
        this.balanceManager = new BalanceManager(this);
        this.waypointManager = new WaypointManager(this);
        this.teamManager = new TeamManager(this);
        this.wallManager = new WallManager(this);
        this.spawnerManager = new SpawnerManager(this);
        this.deathbanManager = new DeathbanManager(this);
        this.reclaimManager = new ReclaimManager(this);
        this.kitManager = new KitManager(this);
        this.rankHook = new RankHook(this);
        this.tagHook = new TagHook(this);
        this.abilitiesHook = new AbilitiesHook(this);
        this.placeholderHook = new PlaceholderHook(this);
        this.clientHook = new ClientHook(this);
        this.classManager = new PvPClassManager(this);
        this.loggerManager = new LoggerManager(this);
        this.abilityManager = new AbilityManager(this);
        this.storageManager = new StorageManager(this);
        this.scheduleManager = new ScheduleManager(this);
        this.staffManager = new StaffManager(this);
        this.nametagManager = new NametagManager(this);
        this.killstreakManager = new KillstreakManager(this);
        this.killtagManager = new KilltagManager(this);
        this.customSignManager = new CustomSignManager(this);
        this.blockshopManager = new BlockshopManager(this);
        this.menuManager = new MenuManager(this);
        this.listenerManager = new ListenerManager(this);
        this.factionEditorManager = new FactionEditorManager(this);
        this.maskManager = new MaskManager(this);

        this.kothManager = new KothManager(this);
        this.kingManager = new KingManager(this);
        this.sotwManager = new SOTWManager(this);
        this.eotwManager = new EOTWManager(this);
        this.purgeManager = new PurgeManager(this);
        this.conquestManager = new ConquestManager(this);

        this.glitchListener = new GlitchListener(listenerManager);
        this.kits = getConfig().getBoolean("KITMAP_MODE");

        new PearlHook(this);
        new CommandManager(this);
        new TipManager(this);
        new BoardManager(this);
        new MemoryClearTask(this);

        if (Config.TABLIST_ENABLED) new TablistManager(this);

        this.managers.forEach(Manager::enable);
        this.loaded = true;

        Logger.state("Enabled", managers.size(),
                teamManager.getTeams().size(), userManager.getUsers().size(),
                kitManager.getKits().size(), kothManager.getKoths().size());
    }

    @Override
    public void onDisable() {
        if (!loaded) {
            return;
        }

        this.managers.forEach(Manager::disable);

        if (teamManager != null && userManager != null && kitManager != null && kothManager != null) {
            Logger.state("Disabled", managers.size(),
                    teamManager.getTeams().size(), userManager.getUsers().size(),
                    kitManager.getKits().size(), kothManager.getKoths().size());
        }
    }
}