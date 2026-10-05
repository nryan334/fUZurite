package me.keano.azurite.utils.configs;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.HCF;
import me.keano.azurite.utils.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@SuppressWarnings("unchecked")
@Getter
@Setter
public class StorageJson {

    private Map<String, Object> values;
    private final HCF instance;
    private final File file;

    public StorageJson(HCF instance, File parent, String name) {
        this.instance = instance;
        this.file = new File(parent, name);
        this.values = new ConcurrentHashMap<>();
        this.load();
    }

    public void load() {
        try {

            if (!file.exists()) {
                file.createNewFile();

                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write("{}");
                }
            }

            FileReader reader = new FileReader(file);
            Map<String, Object> map = instance.getGson().fromJson(reader, Map.class);
            values.putAll(map);
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        String toJson;

        try {

            toJson = instance.getGson().toJson(new ConcurrentHashMap<>(values));

        } catch (Exception e) {
            Logger.print("Error saving data. Aborted to avoid corruption: " + e.getMessage());
            return;
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(toJson);
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}