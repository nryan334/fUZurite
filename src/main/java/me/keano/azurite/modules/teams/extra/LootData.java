package me.keano.azurite.modules.teams.extra;

import lombok.Getter;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.teams.TeamManager;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class LootData extends Module<TeamManager> {

    private final List<LootItem> lootItems;

    public LootData(TeamManager manager) {
        super(manager);
        this.lootItems = new ArrayList<>();
    }

    public LootData(TeamManager manager, List<String> list) {
        super(manager);
        this.lootItems = list.stream().map(LootItem::new).collect(Collectors.toList());
    }

    public List<String> serialize() {
        return lootItems.stream().map(LootItem::serialize).collect(Collectors.toList());
    }

    public void addRandomItem(Chest chest, int min, int max) {
        if (lootItems.isEmpty()) return;

        chest.getInventory().clear(); // clear old contents.

        // the amount of items we are adding.
        int amount = ThreadLocalRandom.current().nextInt(max);
        int amountFinal = Math.max(amount, min);

        List<LootItem> counted = new ArrayList<>();

        // Add them all together with the percentage amount
        for (LootItem randomItem : lootItems) {

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
}