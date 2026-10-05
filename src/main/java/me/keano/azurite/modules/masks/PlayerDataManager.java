package me.keano.azurite.modules.masks;

import me.keano.azurite.HCF;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class PlayerDataManager {

    private final HCF instance;
    private final File playersDir;

    public PlayerDataManager(HCF instance) {
        this.instance = instance;
        this.playersDir = new File(instance.getDataFolder(), "players");
        if (!playersDir.exists()) {
            playersDir.mkdirs();
        }
    }

    public File getPlayerFile(UUID uuid) {
        return new File(playersDir, uuid.toString() + ".yml");
    }

    public YamlConfiguration loadPlayerData(UUID uuid) {
        File file = getPlayerFile(uuid);
        YamlConfiguration config = new YamlConfiguration();
        if (file.exists()) {
            try {
                config.load(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return config;
    }

    public void savePlayerData(UUID uuid, YamlConfiguration config) {
        File file = getPlayerFile(uuid);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getEquippedMask(UUID uuid) {
        YamlConfiguration config = loadPlayerData(uuid);
        String mask = config.getString("equipped_mask");
        if (mask == null || mask.isEmpty() || mask.equalsIgnoreCase("none")) {
            return null;
        }
        return mask;
    }

    public void setEquippedMask(UUID uuid, String maskName) {
        YamlConfiguration config = loadPlayerData(uuid);
        if (maskName == null || maskName.isEmpty()) {
            config.set("equipped_mask", "none");
        } else {
            config.set("equipped_mask", maskName);
        }
        savePlayerData(uuid, config);
    }

    public void clearEquippedMask(UUID uuid) {
        setEquippedMask(uuid, null);
    }

    public void savePlayerData(Player player) {
        // Called on quit - data is already saved when mask state changes
    }
}
