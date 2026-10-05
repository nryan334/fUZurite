package me.keano.azurite.modules.signs;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.framework.Module;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public abstract class CustomSign extends Module<CustomSignManager> {

    protected List<String> lines;

    public CustomSign(CustomSignManager manager, List<String> lines) {
        super(manager);
        this.lines = lines;
    }

    public abstract void onClick(Player player, Sign sign);

    public Integer getIndex(String string) {
        return lines.indexOf(lines.stream().filter(s -> s.toLowerCase().contains(string)).findFirst().orElse(null));
    }

    public boolean isPassable(Material material) {
        return material.isTransparent() || material.name().contains("SIGN") || material == Material.AIR;
    }

    public boolean equals(String[] array) {
        for (int i = 0; i < 4; i++) {
            if (!array[i].equals(lines.get(i))) {
                return false;
            }
        }

        return true;
    }
}