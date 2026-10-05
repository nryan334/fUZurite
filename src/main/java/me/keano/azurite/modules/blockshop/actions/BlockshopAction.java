package me.keano.azurite.modules.blockshop.actions;

import lombok.Getter;
import me.keano.azurite.modules.blockshop.BlockshopManager;
import me.keano.azurite.modules.framework.Module;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public abstract class BlockshopAction extends Module<BlockshopManager> {

    protected final String path;
    protected final List<ClickType> clickTypes;

    public BlockshopAction(BlockshopManager manager, String path) {
        super(manager);
        this.path = path;
        this.clickTypes = fetchClickType(path);
    }

    private List<ClickType> fetchClickType(String path) {
        if (getBlockshopConfig().contains(path + "CLICK")) {
            String click = getBlockshopConfig().getString(path + "CLICK");

            if (click.contains(";")) {
                String[] split = click.split(";");
                List<ClickType> clickTypes = new ArrayList<>();

                for (String string : split) {
                    clickTypes.add(ClickType.valueOf(string));
                }

                return clickTypes;
            }
            return Collections.singletonList(ClickType.valueOf(click));
        }
        return Collections.emptyList();
    }

    public abstract boolean handleClick(Player player, InventoryClickEvent event);
}