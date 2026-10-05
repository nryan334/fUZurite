package me.keano.azurite.modules.killstreaks;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class KillstreakManager extends Manager {

    private final Map<Integer, Killstreak> killstreaks;

    public KillstreakManager(HCF instance) {
        super(instance);
        this.killstreaks = new LinkedHashMap<>(); // keep it in order
        this.load();
    }

    @Override
    public void reload() {
        killstreaks.clear();
        this.load();
    }

    private void load() {
        for (String key : getKillstreakConfig().getConfigurationSection("KILLSTREAKS.PER_KILL").getKeys(false)) {
            String path = "KILLSTREAKS.PER_KILL." + key + ".";
            List<String> effects = getKillstreakConfig().getStringList(path + "EFFECTS");
            List<String> items = getKillstreakConfig().getStringList(path + "ITEMS");
            killstreaks.put(Integer.parseInt(key), new Killstreak(this,
                    getKillstreakConfig().getString(path + "NAME"),
                    getKillstreakConfig().getStringList(path + "COMMANDS"),
                    effects.stream().map(Serializer::getEffect).collect(Collectors.toList()),
                    items.stream().map(this::fromString).collect(Collectors.toList()))
            );
        }
    }

    private ItemStack fromString(String string) {
        String[] split = string.split(", ");
        ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(split[0]), Integer.parseInt(split[1]))
                .data(this, Short.parseShort(split[2]));

        if (!split[3].equalsIgnoreCase("NONE")) {
            builder.setName(split[3]);
        }

        if (!split[4].equalsIgnoreCase("NONE")) {
            String[] lore = split[4].split(";");
            builder.setLore(String.join("", lore));
        }

        if (!split[5].equalsIgnoreCase("NONE")) {
            String[] enchants = split[5].split(";");

            for (String enchant : enchants) {
                String[] further = enchant.split(":");
                builder.addUnsafeEnchantment(Enchantment.getByName(further[0]), Integer.parseInt(further[1]));
            }
        }

        return builder.toItemStack();
    }

    public void checkKills(Player player, int kill) {
        if (!getKillstreakConfig().getBoolean("KILLSTREAKS.ENABLED")) return;

        Killstreak killstreak = killstreaks.get(kill);

        if (killstreak != null) {
            killstreak.handle(player);
            Bukkit.broadcastMessage(getLanguageConfig().getString("DEATH_LISTENER.KILLSTREAK")
                    .replace("%player%", player.getName())
                    .replace("%killstreak%", killstreak.getName())
            );
        }
    }
}