package me.keano.azurite.modules.spawners;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class Spawner extends Module<SpawnerManager> {

    private EntityType type;
    private String name;
    private ItemStack itemStack;

    public Spawner(SpawnerManager manager, EntityType type, String name) {
        super(manager);
        this.type = type;
        this.name = name;
        this.itemStack = getSpawnerItem();
    }

    private ItemStack getSpawnerItem() {
        ItemBuilder builder = new ItemBuilder(ItemUtils.getMat("MOB_SPAWNER")).setName(getConfig().getString("SPAWNERS_CONFIG.ITEM.NAME"));

        for (String s : getConfig().getStringList("SPAWNERS_CONFIG.ITEM.LORE")) {
            builder.addLoreLine(s
                    .replace("%spawner%", name)
            );
        }

        return builder.toItemStack();
    }
}