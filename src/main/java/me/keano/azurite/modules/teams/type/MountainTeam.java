package me.keano.azurite.modules.teams.type;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.enums.MountainType;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.modules.teams.extra.LootData;
import me.keano.azurite.modules.teams.extra.LootItem;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public class MountainTeam extends Team {

    private final MountainType mountainType;
    private final LootData lootData;
    private final Map<Location, Material> resets;
    private final List<LootItem> randomItems;
    private final List<Material> allowedBreak;
    private final List<Location> chests;

    public MountainTeam(TeamManager manager, Map<String, Object> map) {
        super(
                manager,
                map,
                true,
                TeamType.MOUNTAIN
        );
        this.mountainType = MountainType.valueOf((String) map.get("mountainType"));
        this.lootData = new LootData(getManager(), Utils.createList(map.get("lootData"), String.class));
        this.resets = Serializer.fetchMountainBlocks(map.get("resets"));
        this.randomItems = new ArrayList<>();
        this.allowedBreak = Serializer.fetchMaterials(getConfig().getStringList("MOUNTAINS." + mountainType.name() + ".ALLOWED_BREAK"));
        this.chests = Serializer.fetchLocations(map.get("chests"));
    }

    public MountainTeam(TeamManager manager, String name, MountainType mountainType) {
        super(
                manager,
                name,
                UUID.randomUUID(),
                true,
                TeamType.MOUNTAIN
        );
        this.mountainType = mountainType;
        this.lootData = new LootData(getManager());
        this.resets = new HashMap<>();
        this.chests = new ArrayList<>();
        this.randomItems = new ArrayList<>();
        this.allowedBreak = Serializer.fetchMaterials(getConfig().getStringList("MOUNTAINS." + mountainType.name() + ".ALLOWED_BREAK"));
    }

    @Override
    public String getDisplayName(Player player) {
        String name = super.getDisplayName(player);
        return (mountainType == MountainType.GLOWSTONE ?
                Config.SYSTEAM_COLOR_GLOWSTONE.replace("%team%", name) :
                Config.SYSTEAM_COLOR_ORE_MOUNTAIN.replace("%team%", name));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();

        map.put("mountainType", typeName());
        map.put("lootData", lootData.serialize());
        map.put("resets", Serializer.serializeMountainBlocks(resets));
        map.put("chests", Serializer.serializeLocations(chests));

        return map;
    }

    public String typeName() {
        return mountainType.name();
    }

    public void resetBlocks() {
        Iterator<Location> iterator = chests.iterator();
        int min = getConfig().getInt("MOUNTAINS." + typeName() + ".MIN_ITEM_AMOUNT");
        int max = getConfig().getInt("MOUNTAINS." + typeName() + ".MAX_ITEM_AMOUNT");

        while (iterator.hasNext()) {
            Location location = iterator.next();

            if (!(location.getBlock().getState() instanceof Chest)) {
                iterator.remove();
                continue;
            }

            Chest chest = (Chest) location.getBlock().getState();
            lootData.addRandomItem(chest, min, max);
        }

        for (Map.Entry<Location, Material> entry : resets.entrySet()) {
            Block block = entry.getKey().getBlock();
            Material material = entry.getValue();

            // don't set the type again if it hasn't been mined yet
            if (block.getType() != material) {
                block.setType(material);
            }
        }
    }

    public void saveBlocks() {
        chests.clear();
        resets.clear(); // Override the old ones.

        for (Claim claim : claims) {
            // loop through blocks
            for (Block block : claim) {
                Material type = block.getType();
                Location location = block.getLocation();

                if (type == Material.AIR) continue; // no use checking List#contains

                if (type.name().contains("CHEST")) {
                    chests.add(location);
                    continue;
                }

                // Store the blocks that are allowed to be broken, e.g. glowstone
                if (allowedBreak.contains(type)) {
                    resets.put(location, type);
                }
            }
        }

        this.save();
    }
}