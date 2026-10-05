package me.keano.azurite.modules.storage;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.storage.json.JsonStorage;
import me.keano.azurite.modules.storage.mongo.MongoStorage;
import me.keano.azurite.modules.storage.task.StorageSaveTask;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class StorageManager extends Manager {

    private Storage storage;
    private StorageSaveTask storageSaveTask;

    public StorageManager(HCF instance) {
        super(instance);
        this.storage = null;
        this.storageSaveTask = null;
        this.load();
    }

    @Override
    public void enable() {
        storage.load();
    }

    @Override
    public void disable() {
        if (storage != null) {
            storage.close();
        }
    }

    @Override
    public void reload() {
        this.storageSaveTask.cancel();
        this.storageSaveTask = new StorageSaveTask(this);
    }

    private void load() {
        this.storageSaveTask = new StorageSaveTask(this);

        if (getConfig().getString("STORAGE_TYPE").equalsIgnoreCase("MONGO")) {
            String URI;
            String configURI = getConfig().getString("MONGO.URI");

            if (!configURI.isEmpty()) {
                URI = configURI;

            } else {
                URI = "mongodb://" + (getConfig().getBoolean("MONGO.AUTH.ENABLED") ?
                        getConfig().getString("MONGO.AUTH.USERNAME") + ":" + getConfig().getString("MONGO.AUTH.PASSWORD") + "@" : "") +
                        getConfig().getString("MONGO.SERVER_IP");
            }

            MongoClient client = MongoClients.create(URI);
            storage = new MongoStorage(this, client, client.getDatabase(getConfig().getString("MONGO.DATABASE")));

        } else {
            storage = new JsonStorage(this);
        }
    }
}