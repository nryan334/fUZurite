package me.keano.azurite.utils.configs;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.utils.CC;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@SuppressWarnings("all")
public class ConfigYML extends YamlConfiguration {

    private final Map<String, Object> map;
    private final File file;

    public ConfigYML(HCF instance, String name) {
        this.file = new File(instance.getDataFolder(), name + ".yml");
        this.map = new HashMap<>();

        instance.getConfigs().add(this);

        if (!file.exists()) {
            instance.saveResource(name + ".yml", false);
        }

        this.reload();
    }

    /*
    These methods have cached variables stored for better performance.
     */

    @Override
    public double getDouble(String path) {
        Double cache = (Double) map.get(path);

        if (cache != null) {
            return cache;

        } else {
            if (!super.contains(path)) {
                throw new IllegalArgumentException("[Azurite] Error finding double! Config: " + file.getName() + " Path: " + path);
            }

            Double toCache = super.getDouble(path);
            map.put(path, toCache);
            return toCache;
        }
    }

    @Override
    public int getInt(String path) {
        Integer cache = (Integer) map.get(path);

        if (cache != null) {
            return cache;

        } else {
            if (!super.contains(path)) {
                throw new IllegalArgumentException("[Azurite] Error finding integer! Config: " + file.getName() + " Path: " + path);
            }

            Integer toCache = super.getInt(path);
            map.put(path, toCache);
            return toCache;
        }
    }

    @Override
    public long getLong(String path) {
        Long cache = (Long) map.get(path);

        if (cache != null) {
            return cache;

        } else {
            if (!super.contains(path)) {
                throw new IllegalArgumentException("[Azurite] Error finding long! Config: " + file.getName() + " Path: " + path);
            }

            Long toCache = super.getLong(path);
            map.put(path, toCache);
            return toCache;
        }
    }

    @Override
    public boolean getBoolean(String path) {
        Boolean cache = (Boolean) map.get(path);

        if (cache != null) {
            return cache;

        } else {
            if (!super.contains(path)) {
                throw new IllegalArgumentException("[Azurite] Error finding boolean! Config: " + file.getName() + " Path: " + path);
            }

            Boolean toCache = super.getBoolean(path);
            map.put(path, toCache);
            return toCache;
        }
    }

    @Override
    public String getString(String path) {
        String cache = (String) map.get(path);

        if (cache != null) {
            return cache;

        } else {
            if (!super.contains(path)) {
                throw new IllegalArgumentException("[Azurite] Error finding string! Config: " + file.getName() + " Path: " + path);
            }

            String raw = super.getString(path);
            if (raw == null) {
                return null;
            }

            String toCache = CC.t(raw);
            map.put(path, toCache);
            return toCache;
        }
    }

    @Override
    public List<String> getStringList(String path) {
        List<String> cache = (List<String>) map.get(path);

        if (cache != null) {
            return new ArrayList<>(cache);

        } else {
            if (!super.contains(path)) {
                throw new IllegalArgumentException("[Azurite] Error finding list! Config: " + file.getName() + " Path: " + path);
            }

            List<String> toCache = CC.t(super.getStringList(path));
            map.put(path, new ArrayList<>(toCache));
            return toCache;
        }
    }

    @Override
    public ConfigurationSection getConfigurationSection(String path) {
        ConfigurationSection cache = (ConfigurationSection) map.get(path);

        if (cache != null) {
            return cache;

        } else {
            if (!super.contains(path)) {
                throw new IllegalArgumentException("[Azurite] Error finding config section! Config: " + file.getName() + " Path: " + path);
            }

            ConfigurationSection toCache = super.getConfigurationSection(path);
            map.put(path, toCache);
            return toCache;
        }
    }

    @Override
    public void set(String path, Object value) {
        super.set(path, value);
        map.clear();
    }

    public String getUntranslatedString(String path) {
        return super.getString(path);
    }

    public void reloadCache() {
        map.clear(); // we just clear and if it's absent we'll get it from the file again.
    }

    public void reload() {
        try {

            this.load(file);

        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {

            this.save(file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}