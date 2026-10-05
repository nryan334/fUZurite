package me.keano.azurite.modules.ability.type;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.ability.Ability;
import me.keano.azurite.modules.ability.AbilityManager;
import me.keano.azurite.modules.pvpclass.type.bard.BardEffect;
import me.keano.azurite.utils.ItemUtils;
import me.keano.azurite.utils.Serializer;
import me.keano.azurite.utils.Utils;
import me.keano.azurite.utils.extra.Cooldown;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class PortableBardAbility extends Ability {

    private final Map<UUID, PortableBard> portableBards;
    private final Map<UUID, UUID> byEntity;
    private final List<PortableBardEffect> effects;

    private final Cooldown regenCooldown;
    private final EntityType entityType;
    private final String name;
    private final int radius;
    private final int timeUpgrade;
    private final int health;
    private final int timeAlive;

    public PortableBardAbility(AbilityManager manager) {
        super(
                manager,
                null,
                "Portable Bard"
        );
        this.portableBards = new HashMap<>();
        this.byEntity = new HashMap<>();
        this.effects = new ArrayList<>();

        this.regenCooldown = new Cooldown(getManager());
        this.entityType = EntityType.valueOf(getAbilitiesConfig().getString("PORTABLE_BARD.ENTITY_TYPE"));
        this.name = getAbilitiesConfig().getString("PORTABLE_BARD.NAME");
        this.radius = getAbilitiesConfig().getInt("PORTABLE_BARD.RADIUS");
        this.timeUpgrade = getAbilitiesConfig().getInt("PORTABLE_BARD.UPGRADE_EVERY");
        this.health = getAbilitiesConfig().getInt("PORTABLE_BARD.HEALTH");
        this.timeAlive = getAbilitiesConfig().getInt("PORTABLE_BARD.TIME_ALIVE");

        this.load();
    }

    private void load() {
        for (String key : getAbilitiesConfig().getConfigurationSection("PORTABLE_BARD.EFFECTS").getKeys(false)) {
            String path = "PORTABLE_BARD.EFFECTS." + key + ".";
            effects.add(new PortableBardEffect(
                    Serializer.getEffect(getAbilitiesConfig().getString(path + "EFFECT")),
                    Serializer.getEffect(getAbilitiesConfig().getString(path + "EFFECT_UPGRADE")),
                    ItemUtils.getMatItem(getAbilitiesConfig().getString(path + "MATERIAL"))
            ));
        }
    }

    public void disable() {
        for (PortableBard portableBard : portableBards.values()) {
            portableBard.getLivingEntity().remove();
            portableBard.getTickTask().cancel();
        }

        byEntity.clear();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT")) return;
        if (!e.hasItem()) return;

        Player player = e.getPlayer();
        Location toSpawn = (e.hasBlock() ? e.getClickedBlock().getLocation().add(0.5, 1.0, 0.5) : player.getLocation());

        if (hasAbilityInHand(player)) {
            e.setCancelled(true);

            if (cannotUse(player)) return;
            if (hasCooldown(player)) return;
            if (getInstance().getTimerManager().getPvpTimer().hasTimer(player)) return;
            if (getInstance().getTimerManager().getInvincibilityTimer().hasTimer(player)) return;

            PortableBard old = portableBards.remove(player.getUniqueId());

            if (old != null) {
                byEntity.remove(old.getLivingEntity().getUniqueId());
                old.delete();
            }

            PortableBard portableBard = new PortableBard(player, toSpawn, entityType);
            portableBards.put(player.getUniqueId(), portableBard);
            byEntity.put(portableBard.getLivingEntity().getUniqueId(), player.getUniqueId());

            takeItem(player);
            applyCooldown(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof LivingEntity)) return;

        Player damager = Utils.getDamager(e.getDamager());
        LivingEntity entity = (LivingEntity) e.getEntity();
        UUID playerBard = byEntity.get(entity.getUniqueId());

        if (playerBard != null) {
            PortableBard portableBard = portableBards.get(playerBard);

            if (portableBard != null) {
                if (damager.getUniqueId() == portableBard.getPlayer().getUniqueId() ||
                        !getInstance().getTeamManager().canHit(damager, portableBard.getPlayer(), false)) {
                    e.setCancelled(true);
                    return;
                }

                e.setDamage(0D);
                portableBard.setHealth(portableBard.getHealth() - 1);
                portableBard.updateName();

                if (portableBard.getHealth() <= 0) {
                    portableBard.delete();
                }
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (byEntity.containsKey(e.getEntity().getUniqueId())) {
            e.getDrops().clear();
            e.setDroppedExp(0);
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent e) {
        if (byEntity.containsKey(e.getEntity().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    public class PortableBardTask extends BukkitRunnable {

        private final PortableBard portableBard;
        private int i;

        public PortableBardTask(PortableBard portableBard) {
            this.portableBard = portableBard;
            this.i = 0;
        }

        @Override
        public void run() {
            if (portableBard.getLivingEntity().isDead() || i == timeAlive) {
                portableBard.delete();
                return;
            }

            Player player = portableBard.getPlayer();
            PortableBardEffect random = effects.get(ThreadLocalRandom.current().nextInt(effects.size()));

            for (PortableBardEffect effect : effects) {
                if (effect.getEffect() == null) continue;

                if (effect.getEffect().getEffect().getType().equals(PotionEffectType.REGENERATION)) {
                    if (regenCooldown.hasCooldown(player)) continue;
                    regenCooldown.applyCooldownTicks(player, 3000);
                }

                if (shouldEffectPlayer()) {
                    getInstance().getClassManager().addEffect(player, effect.getEffect().getEffect());
                }

                effect.getEffect().applyEffect(player);
            }

            if (i % timeUpgrade == 0 && i != 0) {
                if (shouldEffectPlayer()) {
                    getInstance().getClassManager().addEffect(player, random.getUpgrade().getEffect());
                }

                random.getUpgrade().applyEffect(player);
            }

            i++;
            portableBard.getLivingEntity().getEquipment().setItemInHand(random.getHand());
            portableBard.updateName();
        }

        public boolean shouldEffectPlayer() {
            return portableBard.getLivingEntity().getLocation().distance(portableBard.getPlayer().getLocation()) <= radius;
        }
    }

    @Getter
    public class PortableBardEffect {

        private final BardEffect effect;
        private final BardEffect upgrade;
        private final ItemStack hand;

        public PortableBardEffect(PotionEffect effect, PotionEffect upgrade, ItemStack hand) {
            this.effect = (effect != null ? new BardEffect(getInstance().getClassManager(), false, effect) : null);
            this.upgrade = (upgrade != null ? new BardEffect(getInstance().getClassManager(), false, upgrade) : null);
            this.hand = hand;
            if (effect != null && upgrade != null) load();
        }

        private void load() {
            effect.setEffectSelf(false);
            upgrade.setEffectSelf(false);
            effect.setBardDistance(radius);
            upgrade.setBardDistance(radius);
        }
    }

    @Getter
    @Setter
    public class PortableBard {

        private Player player;
        private LivingEntity livingEntity;
        private BukkitTask tickTask;
        private int health;

        public PortableBard(Player player, Location location, EntityType entityType) {
            if (!entityType.isAlive()) {
                throw new IllegalArgumentException("Portable bard has to be a living entity");
            }

            this.player = player;
            this.livingEntity = (LivingEntity) location.getWorld().spawnEntity(location, entityType);
            this.tickTask = new PortableBardTask(this).runTaskTimer(getInstance(), 0, 20);
            this.health = PortableBardAbility.this.health;

            // Equipment
            livingEntity.getEquipment().setHelmet(new ItemStack(ItemUtils.getMat("GOLD_HELMET")));
            livingEntity.getEquipment().setChestplate(new ItemStack(ItemUtils.getMat("GOLD_CHESTPLATE")));
            livingEntity.getEquipment().setLeggings(new ItemStack(ItemUtils.getMat("GOLD_LEGGINGS")));
            livingEntity.getEquipment().setBoots(new ItemStack(ItemUtils.getMat("GOLD_BOOTS")));

            // Name
            livingEntity.setCustomNameVisible(true);
            this.updateName();

            // So they can't move
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 100, false, false));
            livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 100, false, false));

            // Make sure Adult
            if (livingEntity instanceof Ageable) {
                Ageable ageable = (Ageable) livingEntity;
                ageable.setAdult();
                ageable.setBreed(false);
                ageable.setAgeLock(true);
            }
        }

        public void updateName() {
            livingEntity.setCustomName(name
                    .replace("%player%", player.getName())
                    .replace("%health%", String.valueOf(health)));
        }

        public void delete() {
            portableBards.remove(player.getUniqueId());
            byEntity.remove(livingEntity.getUniqueId());
            livingEntity.remove();
            tickTask.cancel();
        }
    }
}