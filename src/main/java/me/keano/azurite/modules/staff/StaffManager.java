package me.keano.azurite.modules.staff;

import lombok.Getter;
import me.keano.azurite.HCF;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.framework.extra.TitlesConfig;
import me.keano.azurite.modules.staff.extra.StaffItem;
import me.keano.azurite.modules.staff.extra.StaffItemAction;
import me.keano.azurite.modules.staff.extra.StaffReport;
import me.keano.azurite.modules.staff.extra.StaffRequest;
import me.keano.azurite.modules.staff.listener.StaffListener;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Pair;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
public class StaffManager extends Manager {

    private final Map<UUID, Staff> staffMembers;
    private final Map<Pair<String, List<String>>, StaffItem> staffItems;

    private final Set<UUID> vanished;
    private final Set<UUID> frozen;
    private final Set<UUID> staffBuild;
    private final Set<UUID> hideStaff;

    private final List<StaffReport> reports;
    private final List<StaffRequest> requests;

    public StaffManager(HCF instance) {
        super(instance);

        this.staffMembers = new ConcurrentHashMap<>();
        this.staffItems = new HashMap<>();

        this.vanished = new HashSet<>();
        this.frozen = new HashSet<>();
        this.staffBuild = new HashSet<>();
        this.hideStaff = new HashSet<>();

        this.reports = new ArrayList<>();
        this.requests = new ArrayList<>();

        new StaffListener(this);

        this.load();
    }

    @Override
    public void reload() {
        staffItems.clear();
        this.load();
    }

    @Override
    public void disable() {
        for (Staff staff : staffMembers.values()) {
            Player player = staff.getPlayer();
            PlayerInventory inventory = player.getInventory();

            for (PotionEffect effect : staff.getEffects()) {
                player.addPotionEffect(effect);
            }

            inventory.setContents(staff.getContents());
            inventory.setArmorContents(staff.getArmorContents());
            player.updateInventory();
            player.setGameMode(staff.getGameMode());
        }
    }

    private void load() {
        for (String key : getConfig().getConfigurationSection("STAFF_MODE.STAFF_ITEMS").getKeys(false)) {
            String path = "STAFF_MODE.STAFF_ITEMS." + key + ".";
            String action = getConfig().getString(path + "ACTION");
            String replace = getConfig().getString(path + "REPLACE");
            String name = getConfig().getString(path + "NAME");
            List<String> list = getConfig().getStringList(path + "LORE");
            ItemStack item = new ItemBuilder(ItemUtils.getMatItem(getConfig().getString(path + "MATERIAL")))
                    .setName(name).setLore(list)
                    .data(this, getConfig().getInt(path + "DATA"))
                    .toItemStack();

            staffItems.put(new Pair<>(name, list), new StaffItem(
                    this, key,
                    action.isEmpty() ? null : StaffItemAction.valueOf(action),
                    replace.isEmpty() ? null : replace,
                    getConfig().getString(path + "COMMAND"), item,
                    getConfig().getInt(path + "SLOT"))
            );
        }
    }

    public void enableStaff(Player player, boolean title) {
        PlayerInventory inventory = player.getInventory();
        Staff staff = new Staff(this, player, player.getGameMode());

        inventory.clear();
        inventory.setArmorContents(null);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
            staff.getEffects().add(effect);
        }

        for (StaffItem item : staffItems.values()) {
            // They are always vanished when u enable staff mode.
            if (item.getAction() == StaffItemAction.VANISH_ON) continue;
            player.getInventory().setItem(item.getSlot() - 1, item.getItem());
        }

        getInstance().getWaypointManager().enableStaffModules(player);
        getInstance().getUserManager().getByUUID(player.getUniqueId()).updatePlaytime();

        player.updateInventory();
        player.setGameMode(GameMode.CREATIVE);

        enableVanish(player);
        staffMembers.put(player.getUniqueId(), staff);
        if (title) TitlesConfig.STAFF_ENABLED.sendTitle(player);
    }

    public void disableStaff(Player player, boolean title) {
        Staff staff = staffMembers.get(player.getUniqueId());

        if (staff != null) {
            PlayerInventory inventory = player.getInventory();

            inventory.setContents(staff.getContents());
            inventory.setArmorContents(staff.getArmorContents());

            for (PotionEffect effect : staff.getEffects()) {
                player.addPotionEffect(effect);
            }

            getInstance().getWaypointManager().disableStaffModules(player);
            if (staff.getActionBarTask() != null) staff.getActionBarTask().destroy();

            player.updateInventory();
            player.setGameMode(staff.getGameMode());

            staffMembers.remove(player.getUniqueId());
            staffBuild.remove(player.getUniqueId());
            disableVanish(player);
            if (title) TitlesConfig.STAFF_DISABLED.sendTitle(player);
        }
    }

    public void enableVanish(Player player) {
        vanished.add(player.getUniqueId());
        Utils.setCollidesWithEntities(player, false);

        getInstance().getTeamManager().checkTeamSorting(player.getUniqueId());

        if (isStaffEnabled(player)) {
            for (StaffItem item : staffItems.values()) {
                if (item.getAction() == StaffItemAction.VANISH_OFF) {
                    player.getInventory().setItem(item.getSlot() - 1, item.getItem());
                }
            }
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            // Add hide staff
            if (onlinePlayer.hasPermission("azurite.vanish") &&
                    !hideStaff.contains(onlinePlayer.getUniqueId())) continue;
            onlinePlayer.hidePlayer(player);
        }
    }

    public void disableVanish(Player player) {
        vanished.remove(player.getUniqueId());
        Utils.setCollidesWithEntities(player, true);

        getInstance().getTeamManager().checkTeamSorting(player.getUniqueId());

        if (isStaffEnabled(player)) {
            for (StaffItem item : staffItems.values()) {
                if (item.getAction() == StaffItemAction.VANISH_ON) {
                    player.getInventory().setItem(item.getSlot() - 1, item.getItem());
                }
            }
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(player);
        }
    }

    public void freezePlayer(Player player) {
        player.setWalkSpeed(0F);
        player.setFoodLevel(0);
        player.setSprinting(false);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));
        frozen.add(player.getUniqueId());
    }

    public void unfreezePlayer(Player player) {
        player.setWalkSpeed(0.2F);
        player.setFoodLevel(20);
        player.setSprinting(false);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.removePotionEffect(PotionEffectType.JUMP);
        frozen.remove(player.getUniqueId());
    }

    public StaffItem getItem(ItemStack item) {
        List<String> lore = Collections.emptyList();
        String name = "";

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            if (meta.hasLore()) {
                lore = meta.getLore();
            }

            if (meta.hasDisplayName()) {
                name = meta.getDisplayName();
            }
        }

        return staffItems.get(new Pair<>(name, lore));
    }

    public boolean isVanished(Player player) {
        return vanished.contains(player.getUniqueId());
    }

    public boolean isStaffEnabled(Player player) {
        return staffMembers.containsKey(player.getUniqueId());
    }

    public boolean isStaffBuild(Player player) {
        return staffBuild.contains(player.getUniqueId());
    }

    public boolean isHideStaff(Player player) {
        return hideStaff.contains(player.getUniqueId());
    }

    public boolean isFrozen(Player player) {
        return frozen.contains(player.getUniqueId());
    }
}