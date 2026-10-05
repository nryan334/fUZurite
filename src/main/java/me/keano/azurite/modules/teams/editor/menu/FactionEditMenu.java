package me.keano.azurite.modules.teams.editor.menu;

import me.keano.azurite.modules.framework.menu.Menu;
import me.keano.azurite.modules.framework.menu.MenuManager;
import me.keano.azurite.modules.framework.menu.button.Button;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.editor.FactionEditorManager;
import me.keano.azurite.modules.teams.enums.TeamType;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Per-faction edit menu - color editor, claims, metadata selector, and back button.
 */
public class FactionEditMenu extends Menu {

    private final Team team;

    // 1-indexed button slots (Menu framework subtracts 1)
    // Functional slots (0-indexed): 10, 11, 12, 14
    private static final int COLOR_SLOT = 11;       // 0-indexed 10
    private static final int CLAIMS_SLOT = 12;      // 0-indexed 11
    private static final int METADATA_SLOT = 13;    // 0-indexed 12
    private static final int BACK_SLOT = 15;        // 0-indexed 14

    // All 27 slots (3 rows), fill with decorative panes except functional slots
    private static final Set<Integer> FUNCTIONAL_SLOTS = new HashSet<>(Arrays.asList(10, 11, 12, 14));

    public FactionEditMenu(MenuManager manager, Player player, Team team) {
        super(manager, player, CC.t("&8Editor - " + team.getName()), 27, false);
        this.team = team;
    }

    private FactionEditorManager getEditorManager() {
        return getInstance().getFactionEditorManager();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        ItemStack pane = new ItemBuilder(Material.STAINED_GLASS_PANE)
                .data(getManager(), 15)
                .setName(" ")
                .toItemStack();

        for (int i = 0; i < 27; i++) {
            if (FUNCTIONAL_SLOTS.contains(i)) continue;

            int buttonSlot = i + 1;
            buttons.put(buttonSlot, new Button() {
                @Override
                public void onClick(InventoryClickEvent e) {
                    e.setCancelled(true);
                }

                @Override
                public ItemStack getItemStack() {
                    return pane;
                }
            });
        }

        // Slot 10 - Display Name / Color Editor
        buttons.put(COLOR_SLOT, createColorButton(player));

        // Slot 11 - Claims Editor
        buttons.put(CLAIMS_SLOT, createClaimsButton(player));

        // Slot 12 - Metadata / Claim Type Selector
        buttons.put(METADATA_SLOT, createMetadataButton(player));

        // Slot 14 - Go Back
        buttons.put(BACK_SLOT, new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
                Player p = (Player) e.getWhoClicked();
                p.closeInventory();
                getEditorManager().openMainMenu(p);
            }

            @Override
            public ItemStack getItemStack() {
                return new ItemBuilder(Material.BED)
                        .setName("&cGo Back")
                        .toItemStack();
            }
        });

        return buttons;
    }

    private Button createColorButton(Player player) {
        return new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
                Player p = (Player) e.getWhoClicked();
                p.closeInventory();

                p.sendMessage(CC.t("&aEnter a display name for " + team.getName()));

                getEditorManager().awaitChatInput(p, (message) -> {
                    // Parse the color code from input
                    String colorCode = message;
                    if (!colorCode.startsWith("&") && !colorCode.startsWith("\u00a7")) {
                        // Try to interpret as a color code character
                        if (colorCode.length() >= 1) {
                            colorCode = "&" + colorCode.charAt(0);
                        }
                    }

                    // Validate it's a valid color code
                    if (colorCode.length() >= 2) {
                        char c = Character.toLowerCase(colorCode.charAt(1));
                        if (c >= '0' && c <= '9' || c >= 'a' && c <= 'f') {
                            getEditorManager().setFactionColor(team, colorCode);
                            p.sendMessage(CC.t("&aColor set to " + colorCode + team.getName()));
                        } else {
                            p.sendMessage(CC.t("&cInvalid color code! Use format like &c, &d, &3, etc."));
                        }
                    } else {
                        p.sendMessage(CC.t("&cInvalid color code! Use format like &c, &d, &3, etc."));
                    }

                    getEditorManager().reopenEditMenu(p, team);
                });
            }

            @Override
            public ItemStack getItemStack() {
                String currentColor = team.getCustomColor();
                String currentDisplay = team.getDisplayName(player);

                List<String> lore = new ArrayList<>();
                lore.add("&7");
                if (currentColor != null && !currentColor.isEmpty()) {
                    lore.add("&eCurrent: " + currentDisplay);
                } else {
                    lore.add("&eCurrent: &cNone");
                }

                return new ItemBuilder(Material.ITEM_FRAME)
                        .setName("&6&lDisplay Name")
                        .setLore(lore)
                        .toItemStack();
            }
        };
    }

    private Button createClaimsButton(Player player) {
        return new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
                Player p = (Player) e.getWhoClicked();

                if (e.getClick() == ClickType.LEFT) {
                    p.closeInventory();
                    getEditorManager().giveClaimWand(p, team);
                    p.sendMessage(CC.t("&eYou have been given a claim wand. Right-click blocks to select corners, shift-left-click to claim, right-click air to cancel."));
                    return;
                }

                if (e.getClick() == ClickType.RIGHT) {
                    boolean success = getEditorManager().unclaimAtPlayer(p, team);
                    if (success) {
                        p.sendMessage(CC.t("&aUnclaimed successfully from &f" + team.getName() + "&a!"));
                    } else {
                        p.sendMessage(CC.t("&cYou are not standing in a claim belonging to " + team.getName() + "!"));
                    }

                    p.closeInventory();
                    getEditorManager().reopenEditMenu(p, team);
                    return;
                }
            }

            @Override
            public ItemStack getItemStack() {
                List<Claim> claims = team.getClaims();
                List<String> lore = new ArrayList<>();

                if (claims.isEmpty()) {
                    lore.add("&7This faction currently has no");
                    lore.add("&7claims, left click to add a claim.");
                } else {
                    lore.add("&7This faction has &a" + claims.size() + " &7claim(s).");
                }
                lore.add("&7");
                lore.add("&aLeft click to add a claim");
                lore.add("&cRight click to unclaim");

                return new ItemBuilder(Material.DIAMOND_HOE)
                        .setName("&6&lClaims")
                        .setLore(lore)
                        .toItemStack();
            }
        };
    }

    private Button createMetadataButton(Player player) {
        return new Button() {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
                Player p = (Player) e.getWhoClicked();

                TeamType applied = getEditorManager().getAppliedType(p);
                boolean typeLocked = (applied != null);

                if (e.getClick() == ClickType.SHIFT_LEFT) {
                    // Remove/reset the claim type
                    getEditorManager().removeAppliedType(p);
                    p.sendMessage(CC.t("&eClaim type has been reset to default."));
                    update();
                    return;
                }

                if (typeLocked) {
                    // Scrolling is disabled once a type is set
                    p.sendMessage(CC.t("&cA type is already set. Shift + Left Click to remove it first."));
                    return;
                }

                if (e.getClick() == ClickType.LEFT) {
                    // Scroll down (decrease index, wrap around)
                    int current = getEditorManager().getScrollIndex(p);
                    int newIndex = current - 1;
                    if (newIndex < 0) {
                        newIndex = getEditorManager().getClaimTypes().size() - 1;
                    }
                    getEditorManager().setScrollIndex(p, newIndex);
                    update();
                    return;
                }

                if (e.getClick() == ClickType.RIGHT) {
                    // Scroll up (increase index, wrap around)
                    int current = getEditorManager().getScrollIndex(p);
                    int newIndex = current + 1;
                    if (newIndex >= getEditorManager().getClaimTypes().size()) {
                        newIndex = 0;
                    }
                    getEditorManager().setScrollIndex(p, newIndex);
                    update();
                    return;
                }

                if (e.getClick() == ClickType.MIDDLE) {
                    // Apply the currently selected type
                    int current = getEditorManager().getScrollIndex(p);
                    TeamType selectedType = getEditorManager().getClaimTypes().get(current);
                    getEditorManager().setAppliedType(p, selectedType);

                    // Change the team type via the API
                    // Since Team.setType() exists via Lombok @Setter, we call it directly
                    team.setType(selectedType);
                    team.save();

                    p.sendMessage(CC.t("&aClaim type set to &f" + selectedType.name() + "&a!"));
                    update();
                    return;
                }
            }

            @Override
            public ItemStack getItemStack() {
                List<TeamType> types = getEditorManager().getClaimTypes();
                int current = getEditorManager().getScrollIndex(player);
                TeamType applied = getEditorManager().getAppliedType(player);

                List<String> lore = new ArrayList<>();
                lore.add("&7");

                for (int i = 0; i < types.size(); i++) {
                    String typeName = formatTypeName(types.get(i));

                    if (applied != null && applied == types.get(i)) {
                        // This type is permanently set - entire line green
                        lore.add("&a" + i + ". &a" + typeName);
                    } else if (i == current) {
                        // Currently highlighted - number in green
                        lore.add("&a" + i + ". &f" + typeName);
                    } else {
                        // Normal - white
                        lore.add("&f" + i + ". " + typeName);
                    }
                }

                lore.add("&7");
                lore.add("&eLeft and right click to scroll");
                lore.add("&eShift + Left Click to remove");
                lore.add("&bMiddle click to set the Claim Type");

                return new ItemBuilder(Material.COMMAND)
                        .setName("&6&lMetadata")
                        .setLore(lore)
                        .toItemStack();
            }
        };
    }

    private String formatTypeName(TeamType type) {
        switch (type) {
            case ROAD:
                return "Road";
            case SAFEZONE:
                return "Safe-Zone";
            case EVENT:
                return "Event";
            case CONQUEST:
                return "Conquest";
            case CITADEL:
                return "Citadel";
            default:
                return type.name();
        }
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void onClickOwn(InventoryClickEvent e) {
        e.setCancelled(true);
    }
}
