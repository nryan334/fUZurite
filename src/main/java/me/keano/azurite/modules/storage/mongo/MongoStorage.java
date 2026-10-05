package me.keano.azurite.modules.storage.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.storage.Storage;
import me.keano.azurite.modules.storage.StorageManager;
import me.keano.azurite.modules.storage.json.JsonStorage;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.modules.teams.type.*;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.Tasks;
import org.bson.Document;

import java.util.logging.Level;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class MongoStorage extends Module<StorageManager> implements Storage {

    private final MongoClient mongoClient;
    private final MongoCollection<Document> teamsCollection;
    private final MongoCollection<Document> usersCollection;
    private final JsonStorage timerStorage; // just store timers with json files instead of mongo.

    public MongoStorage(StorageManager manager, MongoClient mongoClient, MongoDatabase db) {
        super(manager);
        this.mongoClient = mongoClient;
        this.teamsCollection = db.getCollection("teams");
        this.usersCollection = db.getCollection("users");
        this.timerStorage = new JsonStorage(manager);
    }

    @Override
    public void loadTeams() {
        for (Document document : teamsCollection.find()) {

            Team toSave = null;

            switch (TeamType.valueOf(document.getString("teamType"))) {
                case PLAYER:
                    toSave = new PlayerTeam(getInstance().getTeamManager(), document);
                    break;

                case SAFEZONE:
                    toSave = new SafezoneTeam(getInstance().getTeamManager(), document);
                    break;

                case ROAD:
                    toSave = new RoadTeam(getInstance().getTeamManager(), document);
                    break;

                case MOUNTAIN:
                    toSave = new MountainTeam(getInstance().getTeamManager(), document);
                    break;

                case EVENT:
                    toSave = new EventTeam(getInstance().getTeamManager(), document);
                    break;

                case CITADEL:
                    toSave = new CitadelTeam(getInstance().getTeamManager(), document);
                    break;

                case CONQUEST:
                    toSave = new ConquestTeam(getInstance().getTeamManager(), document);
                    break;
            }

            // Should never happen
            if (toSave == null) {
                getInstance().getLogger().log(
                        Level.SEVERE,
                        "[Azurite] Error occurred while loading a team! Report immediately! (MONGO)"
                );
                continue;
            }

            getInstance().getTeamManager().getTeams().put(toSave.getUniqueID(), toSave);
            getInstance().getTeamManager().getStringTeams().put(toSave.getName(), toSave);

            for (Claim claim : toSave.getClaims()) {
                getInstance().getTeamManager().getClaimManager().saveClaim(claim);
            }
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
            Tasks.executeAsync(getManager(), () -> saveTeam(team, false));
            return;
        }

        Document document = new Document("_id", team.getUniqueID().toString());
        document.putAll(team.serialize());

        teamsCollection.replaceOne(
                Filters.eq("_id", team.getUniqueID().toString()),
                document,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public void deleteTeam(Team team) {
        teamsCollection.deleteOne(Filters.eq("_id", team.getUniqueID().toString()));
    }

    @Override
    public void deleteUser(User user) {
        usersCollection.deleteOne(Filters.eq("_id", user.getUniqueID().toString()));
    }

    @Override
    public void loadUsers() {
        for (Document document : usersCollection.find()) {
            new User(getInstance().getUserManager(), document);
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

        Document document = new Document("_id", user.getUniqueID().toString());
        document.putAll(user.serialize());

        usersCollection.replaceOne(
                Filters.eq("_id", user.getUniqueID().toString()),
                document,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public void loadTimers() {
        timerStorage.loadTimers();
    }

    @Override
    public void saveTimers() {
        timerStorage.saveTimers();
    }

    @Override
    public void load() {
        this.loadTeams();
        this.loadUsers();
        this.loadTimers();
    }

    @Override
    public void close() {
        this.saveTimers();
        this.saveTeams();
        this.saveUsers();
        this.mongoClient.close();
    }
}