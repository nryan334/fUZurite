package me.keano.azurite.modules.listeners.type;

import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.framework.extra.TitlesConfig;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.modules.teams.type.PlayerTeam;
import me.keano.azurite.modules.users.User;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class MainListener extends Module<ListenerManager> {

    private final List<ItemStack> joinItems;
    private final List<String> joinCommands;
    private final ItemStack bookItem;

    public MainListener(ListenerManager manager) {
        super(manager);

        this.joinItems = new ArrayList<>();
        this.joinCommands = getConfig().getStringList("JOIN_COMMANDS.COMMANDS");
        this.bookItem = loadBook();

        this.load();
    }

    private ItemStack loadBook() {
        if (Config.JOIN_ITEMS_BOOK_ENABLED) {
            ItemStack bookItem = new ItemBuilder(Material.WRITTEN_BOOK).toItemStack();

            BookMeta meta = (BookMeta) bookItem.getItemMeta();
            meta.setTitle(getConfig().getString("JOIN_ITEMS.BOOK_ITEM.TITLE"));
            meta.setPages(getConfig().getStringList("JOIN_ITEMS.BOOK_ITEM.PAGES"));
            meta.setAuthor(getConfig().getString("JOIN_ITEMS.BOOK_ITEM.AUTHOR"));
            bookItem.setItemMeta(meta); // make sure we set the meta
            joinItems.add(bookItem);

            return bookItem;

        } else return new ItemStack(Material.AIR);
    }

    private void load() {
        for (String s : getConfig().getStringList("JOIN_ITEMS.NORMAL_ITEMS")) {
            String[] split = s.split(", ");

            ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(split[0]), Integer.parseInt(split[1]));

            if (!split[2].equals("NONE")) {
                // split the enchantment and level
                String[] furtherSplit = split[2].split(":");
                Enchantment enchantment = Enchantment.getByName(furtherSplit[0]);
                builder.addEnchant(enchantment, Integer.parseInt(furtherSplit[1]));
            }

            joinItems.add(builder.toItemStack());
        }
    }

    @EventHandler // if they drop the book remove it.
    public void onDrop(PlayerDropItemEvent e) {
        Item drop = e.getItemDrop();

        if (drop.getItemStack().isSimilar(bookItem)) {
            drop.remove();
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent e) {
        if (Config.ENDERMEN_HOSTILE) return;
        if (!(e.getEntity() instanceof Enderman)) return;
        if (!(e.getTarget() instanceof Player)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        e.setJoinMessage(null);
        Tasks.executeLater(getManager(), 20 * 3, () -> TitlesConfig.WELCOME_BACK.sendTitle(player));

        if (!player.hasPlayedBefore()) {
            User user = getInstance().getUserManager().getByUUID(player.getUniqueId());
            user.setBalance(getInstance().getUserManager().getStartingBalance(player));
            user.save();

            if (Config.JOIN_ITEMS_ENABLED) {
                for (ItemStack joinItem : joinItems) {
                    player.getInventory().addItem(joinItem.clone());
                }
            }

            if (Config.JOIN_COMMANDS_ENABLED) {
                for (String command : joinCommands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                            .replace("%player%", player.getName())
                    );
                }
            }

            player.teleport(getInstance().getWaypointManager().getWorldSpawn());
        }

        if (Config.JOIN_MOTD_ENABLED) {
            for (String s : Config.JOIN_MOTD)
                player.sendMessage(getInstance().getPlaceholderHook().replace(player, s)
                        .replace("%player%", player.getName())
                );
        }

        if (Config.JOIN_TEAM_INFO_ENABLED) {
            Tasks.executeLater(getManager(), 5L, () -> player.chat("/t info"));
        }

        Tasks.executeLater(getManager(), 20 * 7, () -> {
            PlayerTeam pt = getInstance().getTeamManager().getByPlayer(player.getUniqueId());

            if (pt == null) {
                TitlesConfig.JOIN_NO_TEAM.sendTitle(player);

            } else if (pt.getClaims().isEmpty()) {
                TitlesConfig.JOIN_NO_CLAIMS.sendTitle(player);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
    }
}