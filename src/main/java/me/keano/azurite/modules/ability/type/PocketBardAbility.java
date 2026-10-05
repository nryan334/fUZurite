package me.keano.azurite.modules.ability.type;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.pvpclass.PvPClassManager;
import me.keano.azurite.modules.pvpclass.type.bard.BardEffect;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class PocketBardAbility extends Ability {

    private final Map<String, PocketBard> pocketBards;

    public PocketBardAbility(AbilityManager manager) {
        super(
                manager,
                AbilityUseType.INTERACT,
                "PocketBard"
        );
        this.pocketBards = new HashMap<>();
        this.load();
    }

    private void load() {
        for (String key : getAbilitiesConfig().getConfigurationSection("POCKETBARD.TYPES").getKeys(false)) {
            String path = "POCKETBARD.TYPES." + key + ".";
            ItemStack item = new ItemBuilder(ItemUtils.getMat(getAbilitiesConfig().getString(path + "MATERIAL")))
                    .setName(getAbilitiesConfig().getString(path + "NAME"))
                    .setLore(getAbilitiesConfig().getStringList(path + "LORE"))
                    .data(getManager(), getAbilitiesConfig().getInt(path + "DATA"))
                    .toItemStack();

            item.setAmount(getAbilitiesConfig().getInt(path + "AMOUNT"));
            pocketBards.put(key, new PocketBard(
                    getInstance().getClassManager(),
                    Serializer.getEffect(getAbilitiesConfig().getString(path + "EFFECT")),
                    (getAbilitiesConfig().getBoolean(nameConfig + ".ADD_GLOW") ?
                            getInstance().getVersionManager().getVersion().addGlow(item) :
                            item))
            );
        }

        // This will handle the menu items.
        for (String key : getAbilitiesConfig().getConfigurationSection("POCKETBARD.POCKETBARD_MENU.ITEMS").getKeys(false)) {
            String path = "POCKETBARD.POCKETBARD_MENU.ITEMS." + key + ".";
            PocketBard pocketBard = pocketBards.get(getAbilitiesConfig().getString(path + "POCKET_BARD"));
            ItemBuilder builder = new ItemBuilder(ItemUtils.getMat(getAbilitiesConfig().getString(path + "MATERIAL")))
                    .setName(getAbilitiesConfig().getString(path + "NAME"))
                    .setLore(getAbilitiesConfig().getStringList(path + "LORE"))
                    .data(getManager(), getAbilitiesConfig().getInt(path + "DATA"));

            pocketBard.setMenuItem(builder.toItemStack());
            pocketBard.setSlot(getAbilitiesConfig().getInt(path + "SLOT"));
        }
    }

    @Override
    public void onClick(Player player) {
        new PocketBardMenu(getInstance().getMenuManager(), this, player).open();
    }

    @EventHandler
    public void onInteractPocket(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;

        Player player = e.getPlayer();
        PocketBard pocketBard = getPocketBardInHand(player);

        if (pocketBard != null) {
            if (cannotUse(player)) return;
            if (hasCooldown(player)) return;

            pocketBard.getEffect().applyEffect(player);
            takeItem(player);
            applyCooldown(player);
            e.setCancelled(true);
        }
    }

    public boolean isPocketBard(ItemStack item) {
        for (PocketBard pocketBard : pocketBards.values()) {
            if (!pocketBard.getItem().isSimilar(item)) continue;
            return true;
        }
        return false;
    }

    public PocketBard getPocketBardInHand(Player player) {
        ItemStack hand = getManager().getItemInHand(player);

        if (hand == null) return null;

        for (PocketBard pocketBard : pocketBards.values()) {
            if (!pocketBard.getItem().isSimilar(hand)) continue;
            return pocketBard;
        }

        return null;
    }

    @Getter
    @Setter
    public static class PocketBard {

        private BardEffect effect;
        private ItemStack item;
        private ItemStack menuItem;
        private int slot;

        public PocketBard(PvPClassManager manager, PotionEffect effect, ItemStack item) {
            this.effect = new BardEffect(manager, true, effect);
            this.item = item;
            this.menuItem = null;
            this.slot = 0;
        }
    }

    private static class PocketBardMenu extends Menu {

        private final PocketBardAbility ability;

        public PocketBardMenu(MenuManager manager, PocketBardAbility ability, Player player) {
            super(
                    manager,
                    player,
                    manager.getAbilitiesConfig().getString("POCKETBARD.POCKETBARD_MENU.TITLE"),
                    manager.getAbilitiesConfig().getInt("POCKETBARD.POCKETBARD_MENU.SIZE"),
                    false
            );
            this.ability = ability;
        }

        @Override
        public Map<Integer, Button> getButtons(Player player) {
            Map<Integer, Button> buttons = new HashMap<>();

            for (PocketBard pocketBard : ability.getPocketBards().values()) {
                buttons.put(pocketBard.getSlot(), new Button() {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        // Check first and then check after aswell.
                        if (!ability.hasAbilityInHand(player)) {
                            e.setCancelled(true);
                            player.closeInventory();
                            return;
                        }

                        e.setCancelled(true);
                        getManager().takeItemInHand(player, 1);
                        ItemUtils.giveItem(player, pocketBard.getItem(), player.getLocation());
                        if (!ability.hasAbilityInHand(player)) player.closeInventory();
                    }

                    @Override
                    public ItemStack getItemStack() {
                        return pocketBard.getMenuItem();
                    }
                });
            }

            return buttons;
        }
    }
}