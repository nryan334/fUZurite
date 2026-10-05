package me.keano.azurite.modules.masks;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.configs.ConfigYML;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class MaskManager extends Manager {

    private final Map<String, Mask> masks;
    private final List<String> maskNames;
    private final Map<String, Integer> maskSlots;

    private final PlayerDataManager playerDataManager;
    private MaskGUIConfig maskGUIConfig;

    private ConfigYML masksConfig;

    public MaskManager(HCF instance) {
        super(instance);
        this.masks = new LinkedHashMap<>();
        this.maskNames = new ArrayList<>();
        this.maskSlots = new LinkedHashMap<>();
        this.playerDataManager = new PlayerDataManager(instance);
        this.load();
        this.maskGUIConfig = new MaskGUIConfig(instance);
        new MaskFusionListener(this);
        new MaskEquipListener(this);
        new MaskAbilityListener(this);
    }

    private void load() {
        this.masksConfig = new ConfigYML(getInstance(), "masks");
        this.masks.clear();
        this.maskNames.clear();

        ConfigurationSection masksSection = masksConfig.getConfigurationSection("masks");
        if (masksSection == null) return;

        for (String key : masksSection.getKeys(false)) {
            String path = "masks." + key;
            String displayName = masksConfig.getString(path + ".display_name");
            String matName = masksConfig.getString(path + ".material");
            int data = masksConfig.getInt(path + ".data");
            String texture = masksConfig.contains(path + ".texture") ?
                    masksConfig.getUntranslatedString(path + ".texture") : "";
            List<String> lore = masksConfig.getStringList(path + ".lore");

            Mask mask = new Mask(key, displayName, matName, data, texture, lore);
            masks.put(key.toLowerCase(), mask);
            maskNames.add(key.toLowerCase());
        }
    }

    public Mask getMask(String name) {
        return masks.get(name.toLowerCase());
    }

    public ItemStack createMaskItem(String maskName) {
        Mask mask = getMask(maskName);
        if (mask == null) return null;

        Material material = ItemUtils.getMat(mask.getMaterial());
        ItemStack item = new ItemStack(material, 1, (short) mask.getData());

        if (material == ItemUtils.getMat("SKULL_ITEM") && mask.getTexture() != null && !mask.getTexture().isEmpty()) {
            item = SkullUtils.createSkull(mask.getTexture());
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(CC.t(mask.getDisplayName()));
            meta.setLore(CC.t(mask.getLore()));
            item.setItemMeta(meta);
        }

        item = NBTUtils.setMaskId(item, mask.getName().toLowerCase());
        return item;
    }

    public boolean isMaskItem(ItemStack item) {
        return item != null && NBTUtils.hasMaskId(item);
    }

    public String getMaskIdFromItem(ItemStack item) {
        return NBTUtils.getMaskId(item);
    }

    public static boolean isHelmet(Material material) {
        return material == Material.LEATHER_HELMET ||
                material == Material.IRON_HELMET ||
                material == Material.GOLD_HELMET ||
                material == Material.CHAINMAIL_HELMET ||
                material == Material.DIAMOND_HELMET;
    }

    public String getEquippedMask(UUID uuid) {
        return playerDataManager.getEquippedMask(uuid);
    }

    public void setEquippedMask(UUID uuid, String maskName) {
        playerDataManager.setEquippedMask(uuid, maskName);
    }

    public void clearEquippedMask(UUID uuid) {
        playerDataManager.clearEquippedMask(uuid);
    }

    @Override
    public void reload() {
        load();
        if (maskGUIConfig != null) maskGUIConfig.load();
    }

    public static class Mask {
        @Getter
        private final String name;
        @Getter
        private final String displayName;
        @Getter
        private final String material;
        @Getter
        private final int data;
        @Getter
        private final String texture;
        @Getter
        private final List<String> lore;

        public Mask(String name, String displayName, String material, int data, String texture, List<String> lore) {
            this.name = name;
            this.displayName = displayName;
            this.material = material;
            this.data = data;
            this.texture = texture;
            this.lore = lore;
        }
    }
}
