package me.keano.azurite.modules.commands.type.essential;

import lombok.Getter;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class EnchantCommand extends Command {

    private final Map<String, Enchantment> enchants;

    public EnchantCommand(CommandManager manager) {
        super(
                manager,
                "enchant"
        );
        this.enchants = new HashMap<>();
        this.load();
        this.setPermissible("azurite.enchant");
    }

    private void load() {
        for (Enchantment enchantment : Enchantment.values()) {
            enchants.put(enchantment.getName().toUpperCase(), enchantment);
        }

        for (AzuriteEnchants azuriteEnchant : AzuriteEnchants.values()) {
            for (String azuriteEnchantName : azuriteEnchant.getNames()) {
                enchants.put(azuriteEnchantName.toUpperCase(), azuriteEnchant.getEnchantment());
            }
        }

        completions.add(new TabCompletion(new ArrayList<>(enchants.keySet()), 0));
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList(
                "addenchant",
                "addench",
                "ench"
        );
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("ENCHANT_COMMAND.USAGE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        Player target = null;

        // Check if we have a target first
        if (args.length == 3) {
            target = Bukkit.getPlayer(args[2]);

            if (target == null) {
                sendMessage(sender, Config.PLAYER_NOT_FOUND
                        .replace("%player%", args[2])
                );
                return;
            }

        } else if (sender instanceof Player) {
            target = (Player) sender;
        }

        if (target == null) {
            sendUsage(sender);
            return;
        }

        Enchantment enchantment = enchants.get(args[0].toUpperCase());
        Integer level = getInt(args[1]);
        ItemStack toEnchant = getManager().getItemInHand(target);

        if (enchantment == null) {
            sendMessage(sender, getLanguageConfig().getString("ENCHANT_COMMAND.ENCHANT_NOT_FOUND")
                    .replace("%enchant%", args[0])
            );
            return;
        }

        if (toEnchant == null || toEnchant.getType() == Material.AIR) {
            sendMessage(sender, getLanguageConfig().getString("ENCHANT_COMMAND.EMPTY_HAND"));
            return;
        }

        if (level == null) {
            sendMessage(sender, Config.NOT_VALID_NUMBER
                    .replace("%number%", args[1])
            );
            return;
        }

        if (sender != target) {
            sendMessage(sender, getLanguageConfig().getString("ENCHANT_COMMAND.ENCHANTED_TARGET")
                    .replace("%player%", target.getName())
                    .replace("%enchant%", enchantment.getName())
            );
        }

        target.sendMessage(getLanguageConfig().getString("ENCHANT_COMMAND.ENCHANTED")
                .replace("%enchant%", enchantment.getName())
        );
        toEnchant.addUnsafeEnchantment(enchantment, level);
        getManager().setItemInHand(target, toEnchant);
    }

    @Getter
    private enum AzuriteEnchants {
        DAMAGE_ALL(Enchantment.DAMAGE_ALL, "sharp", "sharpness"),
        DAMAGE_UNDEAD(Enchantment.DAMAGE_UNDEAD, "smite"),
        DAMAGE_ARTHROPODS(Enchantment.DAMAGE_ARTHROPODS, "baneofarthropods"),
        DURABILITY(Enchantment.DURABILITY, "unbreaking", "durability", "unb"),
        PROTECTION_EXPLOSIONS(Enchantment.PROTECTION_EXPLOSIONS, "blastprotection", "blastprot"),
        PROTECTION_PROJECTILE(Enchantment.PROTECTION_PROJECTILE, "projectileprotection", "projectileprot"),
        PROTECTION_ENVIRONMENTAL(Enchantment.PROTECTION_ENVIRONMENTAL, "protection", "prot"),
        PROTECTION_FIRE(Enchantment.PROTECTION_FIRE, "fireprotection", "fireprot"),
        PROTECTION_FALL(Enchantment.PROTECTION_FALL, "featherfalling"),
        FIRE_ASPECT(Enchantment.FIRE_ASPECT, "fireaspect", "fire"),
        LOOT_BONUS_MOBS(Enchantment.LOOT_BONUS_MOBS, "looting", "loot"),
        LOOT_BONUS_BLOCKS(Enchantment.LOOT_BONUS_BLOCKS, "fortune"),
        ARROW_DAMAGE(Enchantment.ARROW_DAMAGE, "power"),
        ARROW_KNOCKBACK(Enchantment.ARROW_KNOCKBACK, "punch"),
        ARROW_INFINITE(Enchantment.ARROW_INFINITE, "infinity"),
        DIG_SPEED(Enchantment.DIG_SPEED, "efficiency"),
        SILK_TOUCH(Enchantment.SILK_TOUCH, "silktouch", "silk"),
        KNOCKBACK(Enchantment.KNOCKBACK, "knockback", "knock"),
        ARROW_FIRE(Enchantment.ARROW_FIRE, "flame");

        private final Enchantment enchantment;
        private final String[] names;

        AzuriteEnchants(Enchantment enchantment, String... names) {
            this.enchantment = enchantment;
            this.names = names;
        }
    }
}