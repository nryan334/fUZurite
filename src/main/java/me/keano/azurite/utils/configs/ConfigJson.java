package me.keano.azurite.utils.configs;

import lombok.Getter;
import me.keano.azurite.HCF;

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
@Getter
@SuppressWarnings("unchecked")
public class ConfigJson {

    private final Map<String, Object> values;

    private final HCF instance;
    private final String name;
    private final File file;

    public ConfigJson(HCF instance, String name) {
        this.file = new File(instance.getDataFolder(), name);
        this.values = new ConcurrentHashMap<>();
        this.instance = instance;
        this.name = name;
        this.load();
    }

    public void load() {
        try {

            if (!file.exists()) {
                instance.saveResource(name, false);
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
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(instance.getGson().toJson(values));
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}