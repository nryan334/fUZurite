package me.keano.azurite.modules.masks;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.commands.Command;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class MaskCommand extends Command {

    private MaskGUIConfig guiConfig;

    public MaskCommand(CommandManager manager) {
        super(manager, "masks");
        this.setPermissible(null);
        this.completions.add(new TabCompletion(Arrays.asList("list", "give"), 0));
    }

    public void setGuiConfig(MaskGUIConfig guiConfig) {
        this.guiConfig = guiConfig;
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList("mask");
    }

    @Override
    public List<String> usage() {
        return Arrays.asList(
                CC.t("&7&m-------------------------"),
                CC.t("&cMask Commands:"),
                CC.t("&e/masks &7- Open the Masks GUI"),
                CC.t("&e/masks list &7- List all available masks"),
                CC.t("&e/masks give <player> <mask_name> <quantity> &7- Give mask items to a player"),
                CC.t("&7&m-------------------------")
        );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sendMessage(sender, Config.PLAYER_ONLY);
                return;
            }
            Player player = (Player) sender;
            if (guiConfig == null) {
                guiConfig = getInstance().getMaskManager() != null ?
                        getInstance().getMaskManager().getMaskGUIConfig() : null;
            }
            if (guiConfig == null) {
                sendMessage(sender, "&cMask GUI is not loaded!");
                return;
            }
            new MaskGUI(getInstance().getMenuManager(), player,
                    getInstance().getMaskManager(), guiConfig).open();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                listMasks(sender);
                return;

            case "give":
                if (!sender.hasPermission("azurite.masks.give")) {
                    sendMessage(sender, Config.INSUFFICIENT_PERM);
                    return;
                }
                giveMask(sender, args);
                return;
        }

        sendUsage(sender);
    }

    private void listMasks(CommandSender sender) {
        MaskManager maskManager = getInstance().getMaskManager();
        if (maskManager == null) {
            sendMessage(sender, "&cMask system is not loaded!");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("&7Available masks: ");
        List<String> names = maskManager.getMaskNames();
        for (int i = 0; i < names.size(); i++) {
            sb.append("&e").append(names.get(i));
            if (i < names.size() - 1) sb.append("&7, ");
        }
        sendMessage(sender, sb.toString());
    }

    private void giveMask(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sendMessage(sender, CC.t("&cUsage: /masks give <player> <mask_name> <quantity>"));
            return;
        }

        String playerName = args[1];
        String maskName = args[2];
        Integer amount = getInt(args[3]);

        MaskManager maskManager = getInstance().getMaskManager();
        if (maskManager == null) {
            sendMessage(sender, "&cMask system is not loaded!");
            return;
        }

        if (maskManager.getMask(maskName) == null) {
            sendMessage(sender, CC.t("&cMask '" + maskName + "' not found! Use /masks list to see available masks."));
            return;
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sendMessage(sender, Config.PLAYER_NOT_FOUND.replace("%player%", playerName));
            return;
        }

        if (amount == null || amount <= 0) {
            sendMessage(sender, Config.NOT_VALID_NUMBER.replace("%number%", args[3]));
            return;
        }

        ItemStack maskItem = maskManager.createMaskItem(maskName);
        if (maskItem == null) {
            sendMessage(sender, CC.t("&cFailed to create mask item!"));
            return;
        }

        int remaining = amount;
        while (remaining > 0) {
            int stackSize = Math.min(remaining, maskItem.getMaxStackSize());
            ItemStack toGive = maskItem.clone();
            toGive.setAmount(stackSize);
            ItemUtils.giveItem(target, toGive, target.getLocation());
            remaining -= stackSize;
        }

        target.sendMessage(CC.t("&aYou received &e" + amount + "x &c" +
                maskManager.getMask(maskName).getDisplayName() + "&a!"));
        sendMessage(sender, CC.t("&aGave &e" + amount + "x &c" +
                maskManager.getMask(maskName).getDisplayName() + " &ato &e" + target.getName() + "&a!"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        MaskManager maskManager = getInstance().getMaskManager();

        if (args.length == 1) {
            return super.tabComplete(sender, args);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            String prefix = args[2].toLowerCase();
            if (maskManager == null) return Collections.emptyList();
            return maskManager.getMaskNames().stream()
                    .filter(s -> s.startsWith(prefix))
                    .collect(Collectors.toList());
        }

        return super.tabComplete(sender, args);
    }
}
