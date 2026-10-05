package me.keano.azurite.modules.listeners.type;

import lombok.Getter;
import lombok.SneakyThrows;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.listeners.ListenerManager;
import me.keano.azurite.utils.ReflectionUtils;
import me.keano.azurite.utils.Tasks;
import me.keano.azurite.utils.Utils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
Credits: https://www.spigotmc.org/resources/hcf-enchantlimiter-100-configurable.10215/
Credits: https://bukkit.org/threads/brewevent-help.70336/
- Uses the same ideas as above.
 */
public class LimiterListener extends Module<ListenerManager> {

    private static Method GET_POTION_DATA = null;

    private final Map<PotionEffectType, PotionLimit> potionLimits;
    private final Map<Enchantment, Integer> enchantmentLimits;

    public LimiterListener(ListenerManager manager) {
        super(manager);

        this.potionLimits = new HashMap<>();
        this.enchantmentLimits = new HashMap<>();

        this.load();
    }

    private void load() {
        for (Enchantment enchantment : Enchantment.values()) {
            if (enchantment == null) continue; // Just incase
            int maxLevel = getLimitersConfig().getInt("ENCHANTMENTS." + enchantment.getName());
            enchantmentLimits.put(enchantment, maxLevel);
        }

        for (PotionEffectType effect : PotionEffectType.values()) {
            if (effect == null) continue; // No clue why some are null?
            String name = effect.getName();
            if (name.startsWith("minecraft:")) name = name.replace("minecraft:", "").toUpperCase();
            boolean enabled = getLimitersConfig().getBoolean("POTIONS." + name + ".ENABLED");
            boolean upgradable = getLimitersConfig().getBoolean("POTIONS." + name + ".UPGRADABLE");
            boolean extended = getLimitersConfig().getBoolean("POTIONS." + name + ".EXTENDED");
            potionLimits.put(effect, new PotionLimit(enabled, upgradable, extended));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent e) {
        Map<Enchantment, Integer> enchants = e.getEnchantsToAdd();
        Iterator<Map.Entry<Enchantment, Integer>> iterator = enchants.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Enchantment, Integer> next = iterator.next();
            Enchantment enchantment = next.getKey();
            Integer level = next.getValue();
            Integer limit = enchantmentLimits.get(next.getKey());

            // We don't check below if it's -1 (No change)
            if (limit != null && limit != -1) {
                if (limit == 0) {
                    iterator.remove(); // Remove completely if disabled.

                } else if (level > limit) {
                    enchants.put(enchantment, limit); // Replace with the max limit.
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent e) {
        if (!(e.getCaught() instanceof Item)) return;

        ItemStack item = ((Item) e.getCaught()).getItemStack();

        if (item.hasItemMeta() && item.getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            cancelBookEnchant(meta);
            item.setItemMeta(meta); // Set the meta again
            return;
        }

        cancelItemEnchant(item);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAnvil(InventoryClickEvent e) {
        Inventory inventory = e.getClickedInventory();

        if (inventory == null || inventory.getType() != InventoryType.ANVIL) return;
        if (e.getSlotType() != InventoryType.SlotType.RESULT) return;

        ItemStack result = e.getCurrentItem();

        if (result.hasItemMeta() && result.getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
            cancelBookEnchant(meta);
            result.setItemMeta(meta); // Set the meta again
            return;
        }

        if (result.hasItemMeta() && result.getItemMeta().hasEnchants()) {
            cancelItemEnchant(result);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBrew(BrewEvent e) {
        // Execute a tick later to get the potion after its finished brewing.
        Tasks.execute(getManager(), () -> {
            for (int i = 0; i < 3; i++) {
                BrewerInventory inventory = e.getContents();
                ItemStack potion = inventory.getItem(i);

                if (potion == null) continue;

                if (cancelPotion(potion)) {
                    inventory.setItem(i, new ItemStack(Material.AIR));
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onEffect(PotionSplashEvent e) {
        ThrownPotion entity = e.getPotion();
        Player player = Utils.getDamager(entity);
        ItemStack item = entity.getItem();

        if (player == null) return;
        if (item == null) return; // Just incase

        if (!player.hasPermission("azurite.potionlimit.bypass") && cancelPotion(entity.getItem())) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("LIMITER_LISTENER.DENIED_POTION"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrink(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (!player.hasPermission("azurite.potionlimit.bypass") && cancelPotion(item)) {
            e.setCancelled(true);
            player.sendMessage(getLanguageConfig().getString("LIMITER_LISTENER.DENIED_POTION"));
        }
    }

    private void cancelBookEnchant(EnchantmentStorageMeta meta) {
        for (Map.Entry<Enchantment, Integer> entry : meta.getStoredEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer limit = enchantmentLimits.get(enchantment);
            Integer level = entry.getValue();

            // We don't check below if it's -1 (No change)
            if (limit != null && limit != -1) {
                if (limit == 0) {
                    meta.removeStoredEnchant(enchantment); // Remove completely if disabled.

                } else if (level > limit) {
                    meta.addStoredEnchant(enchantment, limit, true); // Replace with the max limit.
                }
            }
        }
    }

    private void cancelItemEnchant(ItemStack itemStack) {
        for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer level = entry.getValue();
            Integer maxLevel = enchantmentLimits.get(enchantment);

            // We don't check below if it's -1 (No change)
            if (maxLevel != null && maxLevel != -1) {
                if (maxLevel == 0) {
                    itemStack.removeEnchantment(enchantment); // Remove completely if disabled.

                } else if (level > maxLevel) {
                    itemStack.addUnsafeEnchantment(enchantment, maxLevel); // Replace with the max limit.
                }
            }
        }
    }

    @SneakyThrows
    public boolean cancelPotion(ItemStack itemStack) {
        if (!itemStack.getType().name().contains("POTION")) return false;

        // 1.16 has some stupid way of doing potions, so now we gotta do this...
        if (Utils.isModernVer()) {
            if (!itemStack.hasItemMeta() || !(itemStack.getItemMeta() instanceof PotionMeta)) return false;
            if (GET_POTION_DATA == null)
                GET_POTION_DATA = ReflectionUtils.accessMethod(PotionMeta.class, "getBasePotionData");

            PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
            PotionData data = (PotionData) GET_POTION_DATA.invoke(potionMeta);
            PotionLimit limit = potionLimits.get(data.getType().getEffectType());

            if (limit == null) return false;

            if (!limit.isEnabled()) {
                return true;

            } else if (!limit.isUpgradable() && data.isUpgraded()) {
                return true;

            } else return !limit.isExtended() && data.isExtended();

        } else {
            Potion potion = Potion.fromItemStack(itemStack);

            for (PotionEffect effect : potion.getEffects()) {
                PotionLimit limit = potionLimits.get(effect.getType());

                // No limit for this potion
                if (limit == null) continue;

                // It's completely disabled
                if (!limit.isEnabled()) {
                    return true;

                } else if (!limit.isUpgradable() && effect.getAmplifier() >= 1) {
                    return true;

                } else return !limit.isExtended() && potion.hasExtendedDuration();
            }
        }

        return false;
    }

    @Getter
    private static class PotionLimit {

        private final boolean enabled;
        private final boolean upgradable;
        private final boolean extended;

        public PotionLimit(boolean enabled, boolean upgradable, boolean extended) {
            this.enabled = enabled;
            this.upgradable = upgradable;
            this.extended = extended;
        }
    }
}