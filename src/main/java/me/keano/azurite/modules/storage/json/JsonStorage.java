package me.keano.azurite.modules.storage.json;

import me.keano.azurite.modules.events.sotw.SOTWManager;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.storage.Storage;
import me.keano.azurite.modules.storage.StorageManager;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.modules.teams.type.*;
import me.keano.azurite.modules.timers.type.CustomTimer;
import me.keano.azurite.modules.timers.type.PlayerTimer;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.configs.ConfigJson;
import me.keano.azurite.utils.configs.StorageJson;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@SuppressWarnings("unchecked")
public class JsonStorage extends Module<StorageManager> implements Storage {

    private final File usersParent;
    private final File teamsParent;
    private final File systemTeamsParent;
    private final ConfigJson timersJson;

    public JsonStorage(StorageManager manager) {
        super(manager);
        this.usersParent = new File(getInstance().getDataFolder().getPath() + File.separator + "data" + File.separator + "users");
        this.teamsParent = new File(getInstance().getDataFolder().getPath() + File.separator + "data" + File.separator + "playerteams");
        this.systemTeamsParent = new File(getInstance().getDataFolder().getPath() + File.separator + "data" + File.separator + "systemteams");
        this.timersJson = new ConfigJson(getInstance(), "data" + File.separator + "timers.json");
        this.mkdirs();
    }

    private void mkdirs() {
        if (!usersParent.exists()) {
            usersParent.mkdirs();
        }

        if (!teamsParent.exists()) {
            teamsParent.mkdirs();
        }

        if (!systemTeamsParent.exists()) {
            systemTeamsParent.mkdirs();
        }
    }

    @Override
    public void loadTeams() {
        File[] playerFiles = teamsParent.listFiles();
        File[] systemFiles = systemTeamsParent.listFiles();

        if (playerFiles == null || systemFiles == null) return;

        for (File playerFile : playerFiles) {
            fromFile(teamsParent, playerFile);
        }

        for (File systemFile : systemFiles) {
            fromFile(systemTeamsParent, systemFile);
        }
    }

    private void fromFile(File parent, File listFile) {
        StorageJson json = new StorageJson(getInstance(), parent, listFile.getName());
        TeamType type = TeamType.valueOf((String) json.getValues().get("teamType"));
        Team toSave = null;

        switch (type) {
            case PLAYER:
                toSave = new PlayerTeam(getInstance().getTeamManager(), json.getValues());
                break;

            case SAFEZONE:
                toSave = new SafezoneTeam(getInstance().getTeamManager(), json.getValues());
                break;

            case ROAD:
                toSave = new RoadTeam(getInstance().getTeamManager(), json.getValues());
                break;

            case MOUNTAIN:
                toSave = new MountainTeam(getInstance().getTeamManager(), json.getValues());
                break;

            case EVENT:
                toSave = new EventTeam(getInstance().getTeamManager(), json.getValues());
                break;

            case CITADEL:
                toSave = new CitadelTeam(getInstance().getTeamManager(), json.getValues());
                break;

            case CONQUEST:
                toSave = new ConquestTeam(getInstance().getTeamManager(), json.getValues());
                break;
        }

        // Should never happen
        if (toSave == null) {
            getInstance().getLogger().log(
                    Level.SEVERE,
                    "[Azurite] Error occurred while loading a team! Report immediately."
            );
            return;
        }

        getInstance().getTeamManager().getTeams().put(toSave.getUniqueID(), toSave);
        getInstance().getTeamManager().getStringTeams().put(toSave.getName(), toSave);

        for (Claim claim : toSave.getClaims()) {
            getInstance().getTeamManager().getClaimManager().saveClaim(claim);
        }
    }

    @Override
    public void saveTeams() {
        for (Team team : getInstance().getTeamManager().getTeams().values()) {
            saveTeam(team, false);
        }
    }

    @Override
    public void saveTeam(Team team, boolean async) {
        if (async) {
            Tasks.executeAsync(getManager(), () -> this.saveTeam(team, false));
            return;
        }

        // Cache the file, so we don't need to create it each time
        if (team.getStorageJson() == null) {
            team.setStorageJson(new StorageJson(getInstance(), (team.isSystemTeam() ? systemTeamsParent : teamsParent), team.getUniqueID().toString() + ".json"));
        }

        StorageJson storageJson = team.getStorageJson();
        storageJson.setValues(team.serialize());
        storageJson.save();
    }

    @Override
    public void deleteTeam(Team team) {
        Tasks.executeAsync(getManager(), () -> {
            File file = new File((team.isSystemTeam() ? systemTeamsParent : teamsParent), team.getUniqueID().toString() + ".json");
            file.delete();
        });
    }

    @Override
    public void deleteUser(User user) {
        Tasks.executeAsync(getManager(), () -> {
            File file = new File(usersParent, user.getUniqueID().toString() + ".json");
            file.delete();
        });
    }

    @Override
    public void loadUsers() {
        File[] files = usersParent.listFiles();

        if (files == null) return;

        for (File listFile : files) {
            StorageJson json = new StorageJson(getInstance(), usersParent, listFile.getName());
            new User(getInstance().getUserManager(), json.getValues());
            json.getValues().clear();
        }
    }

    @Override
    public void saveUsers() {
        for (User user : getInstance().getUserManager().getUsers().values()) {
            saveUser(user, false);
        }
    }

    @Override
    public void saveUser(User user, boolean async) {
        if (async) {
            Tasks.executeAsync(getManager(), () -> this.saveUser(user, false));
            return;
        }

        // Cache the file, so we don't need to create it each time
        if (user.getStorageJson() == null) {
            user.setStorageJson(new StorageJson(getInstance(), usersParent, user.getUniqueID().toString() + ".json"));
        }

        StorageJson storageJson = user.getStorageJson();
        storageJson.setValues(user.serialize());
        storageJson.save();
    }

    @Override
    public void loadTimers() {
        // TODO: clean

        for (PlayerTimer timer : getInstance().getTimerManager().getPlayerTimers().values()) {
            Map<String, Object> mainNormal = (Map<String, Object>) timersJson.getValues().get((timer.isPausable() ? "Normal:" : "") + timer.getName());
            Map<String, Object> mainPaused = (Map<String, Object>) timersJson.getValues().get("Paused:" + timer.getName());

            if (mainNormal != null) {
                timer.getTimerCache().putAll(mainNormal
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                entry -> UUID.fromString(entry.getKey()),
                                entry -> Long.parseLong((String) entry.getValue())))
                );
            }

            if (mainPaused != null) {
                timer.getPausedCache().putAll(mainPaused
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                entry -> UUID.fromString(entry.getKey()),
                                entry -> Long.parseLong((String) entry.getValue())))
                );
            }
        }

        List<String> enabled = (List<String>) timersJson.getValues().get("SOTW_ENABLED:");

        if (enabled != null) {
            getInstance().getSotwManager().getEnabled().addAll(enabled
                    .stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList()));
        }

        if (timersJson.getValues().containsKey("SOTW:")) {
            long time = Long.parseLong((String) timersJson.getValues().get("SOTW:"));

            if (time > 0L) {
                SOTWManager sotwManager = getInstance().getSotwManager();
                sotwManager.setActive(true);
                sotwManager.setRemaining(time);
            }
        }

        for (String s : timersJson.getValues().keySet()) {
            if (!s.contains("CTimer:")) continue;

            String string = (String) timersJson.getValues().get(s);
            String[] split = string.split(":");
            String name = s.split(":")[1];

            CustomTimer ct = new CustomTimer(getInstance().getTimerManager(), name, split[1], 0);
            ct.setRemaining(Long.parseLong(split[0]));
        }
    }

    @Override
    public void saveTimers() {
        Map<String, Object> values = timersJson.getValues();
        values.clear(); // we want to overwrite the old data.

        // TODO: clean.

        for (PlayerTimer timer : getInstance().getTimerManager().getPlayerTimers().values()) {
            values.put((timer.isPausable() ? "Normal:" : "") + timer.getName(), timer.getTimerCache()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString())));

            if (timer.isPausable()) {
                values.put("Paused:" + timer.getName(), timer.getPausedCache()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString())));
            }
        }

        values.put("SOTW:", getInstance().getSotwManager().getRemaining() + "");
        values.put("SOTW_ENABLED:", getInstance().getSotwManager().getEnabled()
                .stream()
                .map(UUID::toString).collect(Collectors.toList()));

        for (CustomTimer timer : getInstance().getTimerManager().getCustomTimers().values()) {
            values.put("CTimer:" + timer.getName(), timer.getRemaining().toString() + ":" + timer.getDisplayName());
        }

        timersJson.save();
    }

    @Override
    public void load() {
        this.loadTeams();
        this.loadUsers();
        this.loadTimers();
    }

    @Override
    public void close() {
        this.saveTeams();
        this.saveUsers();
        this.saveTimers();
    }
}