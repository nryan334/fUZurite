package me.keano.azurite.modules.teams.type;

import lombok.Getter;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.TeamManager;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.modules.teams.extra.LootItem;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class CitadelTeam extends Team {

    private final List<LootItem> randomItems;
    private final List<Location> chests;

    public CitadelTeam(TeamManager manager, Map<String, Object> map) {
        super(
                manager,
                map,
                true,
                TeamType.CITADEL
        );

        this.randomItems = new ArrayList<>();
        this.chests = Utils.createList(map.get("chests"), String.class)
                .stream().map(Serializer::fetchLocation).collect(Collectors.toList());

        this.load();
    }

    public CitadelTeam(TeamManager manager, String name) {
        super(
                manager,
                name,
                UUID.randomUUID(),
                true,
                TeamType.CITADEL
        );

        this.randomItems = new ArrayList<>();
        this.chests = new ArrayList<>();

        this.load();
    }

    @Override
    public String getDisplayName(Player player) {
        return Config.DISPLAY_NAME_CITADEL.replace("%team%", super.getDisplayName(player));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();

        map.put("chests", chests
                .stream()
                .map(Serializer::serializeLoc)
                .collect(Collectors.toList()));

        return map;
    }

    private void load() {
        for (String s : getConfig().getStringList("CITADEL.CHEST_CONFIG.RANDOM_ITEMS")) {
            String[] split = s.split(", ");

            if (s.startsWith("ABILITY")) {
                String abilityName = split[0].split(":")[1].toUpperCase();
                ItemStack ability = getInstance().getAbilityManager().getAbility(abilityName).getItem().clone();

                ability.setAmount(Integer.parseInt(split[1]));
                randomItems.add(new LootItem(
                        ability,
                        Double.parseDouble(split[2].replaceAll("%", "")))
                );
                continue;
            }

            ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(split[0]), Integer.parseInt(split[1]))
                    .data(getManager(), Short.parseShort(split[2]));

            if (!split[3].equalsIgnoreCase("NONE")) {
                builder.setName(split[3]);
            }

            if (!split[4].equalsIgnoreCase("NONE")) {
                String[] furtherSplit = split[4].split(";"); // split the enchants

                for (String value : furtherSplit) {
                    String[] levelSplit = value.split(":"); // split to enchant:level

                    builder.addUnsafeEnchantment(Enchantment.getByName(levelSplit[0]), Integer.parseInt(levelSplit[1]));
                }
            }

            if (!split[5].equalsIgnoreCase("NONE")) {
                String[] furtherSplit = split[5].split(";"); // split the lore

                for (String value : furtherSplit) {
                    builder.addLoreLine(value);
                }
            }

            randomItems.add(new LootItem(
                    builder.toItemStack(),
                    Double.parseDouble(split[6].replaceAll("%", "")))
            );
        }
    }

    private void addRandomItem(Chest chest) {
        if (randomItems.isEmpty()) return;

        chest.getInventory().clear(); // clear old contents.

        int min = getConfig().getInt("CITADEL.CHEST_CONFIG.MIN_ITEM_AMOUNT");
        int max = getConfig().getInt("CITADEL.CHEST_CONFIG.MAX_ITEM_AMOUNT");

        // the amount of items we are adding.
        int amount = ThreadLocalRandom.current().nextInt(max);
        int amountFinal = Math.max(amount, min);

        List<LootItem> counted = new ArrayList<>();

        // Add them all together with the percentage amount
        for (LootItem randomItem : randomItems) {
            // so when we use the random - for example 30% will have 30 items in it thus 30% of a chance.
            for (int i = 0; i < randomItem.getPercentage(); i++) {
                counted.add(randomItem);
            }
        }

        // now just add it all with a random position and random item.
        for (int i = 0; i < amountFinal; i++) {
            int random = ThreadLocalRandom.current().nextInt(counted.size());
            int position = ThreadLocalRandom.current().nextInt(chest.getInventory().getSize());
            ItemStack atPos = chest.getInventory().getItem(position);

            if (atPos != null) {
                i -= 1; // we loop again.
                continue;
            }

            chest.getInventory().setItem(position, counted.get(random).getItem());
        }

        // clear to decrease ram
        counted.clear();
        chest.update(true);
    }

    public void resetBlocks() {
        Iterator<Location> iterator = chests.iterator();

        while (iterator.hasNext()) {
            Location location = iterator.next();

            if (!(location.getBlock().getState() instanceof Chest)) {
                iterator.remove();
                continue;
            }

            Chest chest = (Chest) location.getBlock().getState();
            this.addRandomItem(chest);
        }
    }

    public void saveBlocks() {
        for (Claim claim : claims) {
            // loop through blocks
            for (Block block : claim) {
                if (block.getType() == Material.AIR) continue; // no use checking List#contains

                if (block.getType().name().contains("CHEST") && !chests.contains(block.getLocation())) {
                    chests.add(block.getLocation());
                }
            }

            this.save(); // save everything when done checking the claim - not inside the block loop.
        }
    }
}