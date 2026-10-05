package me.keano.azurite.modules.masks;

import lombok.Getter;
import me.keano.azurite.HCF;
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
public class MaskGUIConfig {

    private final HCF instance;
    private ConfigYML guiConfig;

    private String guiTitle;
    private int guiRows;
    private int guiSize;
    private final Map<String, Integer> maskSlots;
    private final List<DecorativeItem> decorativeItems;

    public MaskGUIConfig(HCF instance) {
        this.instance = instance;
        this.maskSlots = new LinkedHashMap<>();
        this.decorativeItems = new ArrayList<>();
        this.load();
    }

    public void load() {
        this.guiConfig = new ConfigYML(instance, "maskcommand");
        this.maskSlots.clear();
        this.decorativeItems.clear();

        String path = "gui";
        this.guiTitle = guiConfig.contains(path + ".title") ?
                CC.t(guiConfig.getString(path + ".title")) : CC.t("&8Masks");
        this.guiRows = guiConfig.contains(path + ".rows") ?
                guiConfig.getInt(path + ".rows") : 6;
        this.guiSize = guiRows * 9;

        ConfigurationSection maskSlotsSection = guiConfig.getConfigurationSection(path + ".mask_slots");
        if (maskSlotsSection != null) {
            for (String key : maskSlotsSection.getKeys(false)) {
                int slot = guiConfig.getInt(path + ".mask_slots." + key);
                maskSlots.put(key.toLowerCase(), slot);
            }
        }

        List<Map<?, ?>> decList = guiConfig.getMapList(path + ".decorative_items");
        for (Map<?, ?> map : decList) {
            Object slotObj = map.get("slot");
            Object matObj = map.get("material");
            Object dataObj = map.get("data");
            Object nameObj = map.get("name");

            int slot = slotObj instanceof Integer ? (Integer) slotObj : 0;
            String matName = matObj != null ? matObj.toString() : "STAINED_GLASS_PANE";
            int data = dataObj instanceof Integer ? (Integer) dataObj : 0;
            String name = nameObj != null ? nameObj.toString() : " ";

            decorativeItems.add(new DecorativeItem(slot, matName, data, name));
        }
    }

    public ItemStack createDecorativeItem(DecorativeItem dec) {
        Material material = ItemUtils.getMat(dec.material);
        ItemStack item = new ItemStack(material, 1, (short) dec.data);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(CC.t(dec.name != null ? dec.name : ""));
            item.setItemMeta(meta);
        }
        return item;
    }

    public Set<Integer> getMaskSlotValues() {
        return new HashSet<>(maskSlots.values());
    }

    public static class DecorativeItem {
        @Getter
        private final int slot;
        @Getter
        private final String material;
        @Getter
        private final int data;
        @Getter
        private final String name;

        public DecorativeItem(int slot, String material, int data, String name) {
            this.slot = slot;
            this.material = material;
            this.data = data;
            this.name = name;
        }
    }
}
