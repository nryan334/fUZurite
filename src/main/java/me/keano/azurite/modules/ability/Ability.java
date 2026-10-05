package me.keano.azurite.modules.ability;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.ability.event.AbilityUseEvent;
import me.keano.azurite.modules.ability.extra.AbilityUseType;
import me.keano.azurite.modules.ability.task.CooldownBarTask;
import me.keano.azurite.modules.ability.type.PocketBardAbility;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.teams.type.CitadelTeam;
import me.keano.azurite.modules.teams.type.EventTeam;
import me.keano.azurite.modules.teams.type.SafezoneTeam;
import me.keano.azurite.modules.timers.listeners.playertimers.AbilityTimer;
import me.keano.azurite.utils.ItemBuilder;
import me.keano.azurite.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
public abstract class Ability extends Module<AbilityManager> {

    protected String name;
    protected String displayName;
    protected String nameConfig;
    protected AbilityUseType useType;
    protected AbilityTimer abilityCooldown;
    protected ItemStack item;
    protected boolean enabled;

    public Ability(AbilityManager manager, AbilityUseType useType, String name) {
        super(manager);

        this.name = name;
        this.useType = useType;
        this.nameConfig = name.replaceAll(" ", "_").toUpperCase();
        this.displayName = getAbilitiesConfig().getString(nameConfig + ".DISPLAY_NAME");
        this.abilityCooldown = new AbilityTimer(getInstance().getTimerManager(), this, "PLAYER_TIMERS.ABILITIES");
        this.item = loadItem();
        this.enabled = getAbilitiesConfig().getBoolean(nameConfig + ".ENABLED");

        manager.getAbilities().put(name.toUpperCase().replaceAll(" ", ""), this);
    }

    private ItemStack loadItem() {
        Material material = ItemUtils.getMat(getAbilitiesConfig().getString(nameConfig + ".MATERIAL"));
        ItemBuilder builder = new ItemBuilder(material);

        builder.setName(getAbilitiesConfig().getString(nameConfig + ".DISPLAY_NAME"));
        builder.setLore(getAbilitiesConfig().getStringList(nameConfig + ".LORE"));
        builder.data(getManager(), getAbilitiesConfig().getInt(nameConfig + ".DATA"));

        if (getAbilitiesConfig().contains(nameConfig + ".DURABILITY")) {
            short setting = (short) getAbilitiesConfig().getInt(nameConfig + ".DURABILITY");
            builder.setDurability(getManager(), (short) (material.getMaxDurability() - setting));
        }

        if (getAbilitiesConfig().contains(nameConfig + ".ENCHANTS")) {
            for (String s : getAbilitiesConfig().getStringList(nameConfig + ".ENCHANTS")) {
                String[] split = s.split(", ");
                builder.addUnsafeEnchantment(Enchantment.getByName(split[0]), Integer.parseInt(split[1]));
            }
        }

        if (getAbilitiesConfig().getBoolean(nameConfig + ".ADD_GLOW")) {
            return getInstance().getVersionManager().getVersion().addGlow(builder.toItemStack());
        }

        return builder.toItemStack();
    }

    public boolean hasAbilityInHand(Player player) {
        ItemStack hand = getManager().getItemInHand(player);
        return isSimilar(hand);
    }

    public boolean isSimilar(ItemStack hand) {
        if (hand == null) return false;
        if (!hand.hasItemMeta()) return false;
        if (!hand.getItemMeta().hasDisplayName()) return false;
        if (!hand.getItemMeta().hasLore()) return false;

        // Don't use ItemStack#isSimilar because that checks durability too
        ItemMeta handMeta = hand.getItemMeta();
        ItemMeta itemMeta = item.getItemMeta();
        return handMeta.getDisplayName().equals(itemMeta.getDisplayName()) && handMeta.getLore().equals(itemMeta.getLore());
    }

    public boolean hasCooldown(Player player) {
        if (getManager().getGlobalCooldown().hasTimer(player)) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.GLOBAL_COOLDOWN")
                    .replace("%time%", getManager().getGlobalCooldown().getRemainingString(player))
            );
            return true;

        } else if (abilityCooldown.hasTimer(player)) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.COOLDOWN")
                    .replace("%ability%", displayName)
                    .replace("%time%", abilityCooldown.getRemainingString(player))
            );
            return true;

        } else return false;
    }

    public void takeItem(Player player) {
        if (getAbilitiesConfig().getBoolean(nameConfig + ".TAKE_ITEM")) {
            getManager().takeItemInHand(player, 1);
        }
    }

    public void applyCooldown(Player player) {
        if (getAbilitiesConfig().getBoolean("GLOBAL_ABILITY.ENABLED")) {
            getManager().getGlobalCooldown().applyTimer(player);
        }

        long cooldownMillis = abilityCooldown.getSeconds() * 1000L;

        AbilityUseEvent event = new AbilityUseEvent(this, player, cooldownMillis);
        Bukkit.getPluginManager().callEvent(event);
        abilityCooldown.applyTimer(player, event.getCooldown()); // call event and update with the time if changed.
        new CooldownBarTask(player, this);
    }

    public boolean shouldSendCooldown(ItemStack item) {
        if (this instanceof PocketBardAbility) {
            return ((PocketBardAbility) this).isPocketBard(item);
        }

        return isSimilar(item);
    }

    public boolean cannotUse(Player damager) {
        if (!enabled) {
            damager.sendMessage(getLanguageConfig().getString("ABILITIES.DISABLED"));
            return true;
        }

        return checkUsage(damager);
    }

    private boolean checkUsage(Player player) {
        World.Environment environment = player.getWorld().getEnvironment();

        if (getAbilitiesConfig().getBoolean("GLOBAL_ABILITY.DISABLE_IN_END") && environment == World.Environment.THE_END) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.DISABLED_END"));
            return true;
        }

        if (getAbilitiesConfig().getBoolean("GLOBAL_ABILITY.DISABLE_IN_NETHER") && environment == World.Environment.NETHER) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.DISABLED_NETHER"));
            return true;
        }

        Team team = getInstance().getTeamManager().getClaimManager().getTeam(player.getLocation());

        if (getAbilitiesConfig().getBoolean("GLOBAL_ABILITY.DISABLE_IN_CITADEL") && team instanceof CitadelTeam) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.DISABLED_CITADEL"));
            return true;
        }

        if (getAbilitiesConfig().getBoolean("GLOBAL_ABILITY.DISABLE_IN_EVENTS") && team instanceof EventTeam) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.DISABLED_EVENT"));
            return true;
        }

        if (team instanceof SafezoneTeam) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.DISABLED_SPAWN"));
            return true;
        }

        if (!team.isAbilities()) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.DISABLED_TOGGLED"));
            return true;
        }

        if (checkRadius(player.getLocation()) && !player.hasPermission("azurite.abilityradius.bypass")) {
            player.sendMessage(getLanguageConfig().getString("ABILITIES.DISABLED_RADIUS")
                    .replace("%radius%", String.valueOf(Config.ABILITY_DISABLED_RADIUS))
            );
            return true;
        }

        return false;
    }

    private boolean checkRadius(Location location) {
        int x = location.getBlockX();
        int z = location.getBlockZ();
        return (x <= Config.ABILITY_DISABLED_RADIUS && x >= -Config.ABILITY_DISABLED_RADIUS
                && z <= Config.ABILITY_DISABLED_RADIUS && z >= -Config.ABILITY_DISABLED_RADIUS);
    }

    public void onClick(Player player) {
    }

    public void onHit(Player damager, Player damaged) {
    }
}