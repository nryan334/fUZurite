package me.keano.azurite.utils;

import me.keano.azurite.modules.deathban.Deathban;
import me.keano.azurite.modules.events.conquest.ConquestManager;
import me.keano.azurite.modules.events.conquest.extra.Capzone;
import me.keano.azurite.modules.events.conquest.extra.ConquestType;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Manager;
import me.keano.azurite.modules.framework.extra.FancyMessageData;
import me.keano.azurite.modules.teams.claims.Claim;
import me.keano.azurite.modules.teams.player.Member;
import me.keano.azurite.modules.teams.player.Role;
import me.keano.azurite.modules.teams.task.BaseTask;
import me.keano.azurite.modules.teams.task.FalltrapTask;
import me.keano.azurite.modules.users.extra.StoredInventory;
import me.keano.azurite.utils.cuboid.Cuboid;
import me.keano.azurite.utils.extra.Pair;
import me.keano.azurite.utils.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class Serializer {

    public static List<FancyMessage> loadFancyMessages(List<String> toSend) {
        List<FancyMessage> list = new ArrayList<>();

        for (String string : toSend) {
            if (!string.contains("<f>")) {
                list.add(new FancyMessage(string));
                continue;
            }

            FancyMessage current = new FancyMessage();

            while (string.contains("<f>")) {
                Pair<String, FancyMessage> pair = getFancy(current, string);
                string = pair.getKey();
                current = pair.getValue();
            }

            list.add(current);
        }

        return list;
    }

    private static Pair<String, FancyMessage> getFancy(FancyMessage fancyMessage, String string) {
        int first = string.indexOf("<f>");
        String firstPart = "";
        String lastPart = "";

        if (first > 0) {
            firstPart = string.substring(0, first);
        }

        int last = string.indexOf("<f>", first + 1);

        if (last == -1) {
            throw new IllegalArgumentException("Fancy message bracket started but not finished.");
        }

        String inside = string.substring(first + 3, last);
        FancyMessageData data = Config.TEAM_INFO_FANCY_MESSAGES.get(inside.toUpperCase());

        if (last + 3 < string.length()) {
            lastPart = string.substring(last + 3);
        }

        string = lastPart;
        fancyMessage.text(firstPart);

        if (data != null) {
            fancyMessage.then().text(data.getInside()).tooltip(data.getHover()).command(data.getCommand());
        }

        fancyMessage.then().text(lastPart);
        return new Pair<>(string, fancyMessage);
    }

    public static List<String> serializeClaims(List<Claim> claims) {
        return claims.stream().map(Serializer::serializeClaim).collect(Collectors.toList());
    }

    public static List<Claim> fetchClaims(Object object) {
        List<String> list = Utils.createList(object, String.class);
        return list.stream().map(Serializer::fetchClaim).collect(Collectors.toList());
    }

    public static String serializeClaim(Claim claim) {
        return String.join(", ", claim.getTeam().toString(), claim.getWorldName(),
                String.valueOf(claim.getX1()), String.valueOf(claim.getY1()), String.valueOf(claim.getZ1()),
                String.valueOf(claim.getX2()), String.valueOf(claim.getY2()), String.valueOf(claim.getZ2()));
    }

    public static Claim fetchClaim(String string) {
        String[] split = string.split(", ");
        World world = Bukkit.getWorld(split[1]);
        return new Claim(UUID.fromString(split[0]),
                new Location(world, parseInt(split[2]), parseInt(split[3]), parseInt(split[4])),
                new Location(world, parseInt(split[5]), parseInt(split[6]), parseInt(split[7]))
        );
    }

    public static String serializeCapzone(Capzone capzone) {
        return serializeCuboid(capzone.getZone()) + ":" + capzone.getType().toString();
    }

    public static Capzone fetchCapzone(ConquestManager manager, String string) {
        String[] split = string.split(":");
        return new Capzone(manager, fetchCuboid(split[0]), ConquestType.valueOf(split[1]));
    }

    public static String serializeCuboid(Cuboid cuboid) {
        return String.join(", ", cuboid.getWorldName(),
                String.valueOf(cuboid.getX1()), String.valueOf(cuboid.getY1()), String.valueOf(cuboid.getZ1()),
                String.valueOf(cuboid.getX2()), String.valueOf(cuboid.getY2()), String.valueOf(cuboid.getZ2()));
    }

    public static Cuboid fetchCuboid(String string) {
        String[] split = string.split(", ");
        return new Cuboid(
                new Location(Bukkit.getWorld(split[0]), parseInt(split[1]), parseInt(split[2]), parseInt(split[3])),
                new Location(Bukkit.getWorld(split[0]), parseInt(split[4]), parseInt(split[5]), parseInt(split[6]))
        );
    }

    public static List<String> serializeMountainBlocks(Map<Location, Material> map) {
        List<String> list = new ArrayList<>();

        for (Map.Entry<Location, Material> entry : map.entrySet()) {
            list.add(serializeLoc(entry.getKey()) + ";" + entry.getValue().name());
        }

        return list;
    }

    public static Map<Location, Material> fetchMountainBlocks(Object object) {
        List<String> list = Utils.createList(object, String.class);
        Map<Location, Material> map = new HashMap<>();

        for (String s : list) {
            String[] split = s.split(";");
            map.put(fetchLocation(split[0]), ItemUtils.getMat(split[1]));
        }

        return map;
    }

    public static List<Location> fetchLocations(Object object) {
        List<String> list = Utils.createList(object, String.class);
        return list.stream().map(Serializer::fetchLocation).collect(Collectors.toList());
    }

    public static List<String> serializeLocations(List<Location> locations) {
        return locations.stream().map(Serializer::serializeLoc).collect(Collectors.toList());
    }

    public static List<Material> fetchMaterials(Object object) {
        List<String> list = Utils.createList(object, String.class);
        return list.stream().map(ItemUtils::getMat).collect(Collectors.toList());
    }

    public static String serializeDeathban(Deathban deathban) {
        return String.join(";", deathban.getUniqueID().toString(), String.valueOf(deathban.getTime()),
                deathban.getReason(), serializeLoc(deathban.getLocation()), String.valueOf(deathban.getDate().getTime()));
    }

    public static Deathban fetchDeathban(Manager manager, Object object) {
        String[] split = ((String) object).split(";");
        Deathban deathban = new Deathban(
                manager.getInstance().getDeathbanManager(),
                UUID.fromString(split[0]), Long.parseLong(split[1]), split[2], fetchLocation(split[3]));
        deathban.setDate(new Date(Long.parseLong(split[4])));
        return deathban;
    }

    public static List<String> serializeInventories(List<StoredInventory> inventories) {
        return inventories.stream().map(StoredInventory::serialize).collect(Collectors.toList());
    }

    public static List<StoredInventory> fetchInventories(Object object) {
        List<String> list = Utils.createList(object, String.class);
        return list.stream().map(StoredInventory::fromString).collect(Collectors.toList());
    }

    public static List<String> serializeUUIDs(Collection<UUID> list) {
        return list.stream().map(UUID::toString).collect(Collectors.toList());
    }

    public static Set<UUID> fetchUUIDs(Object object) {
        List<String> list = Utils.createList(object, String.class);
        return list.stream().map(UUID::fromString).collect(Collectors.toSet());
    }

    public static List<String> serializeFalltrapTasks(Set<FalltrapTask> tasks) {
        List<String> list = new ArrayList<>();

        for (FalltrapTask task : tasks) {
            list.add(String.join(";",
                    serializeClaim(task.getClaim()), task.getPlayer().toString(),
                    BukkitSerialization.itemStackToBase64(task.getWall()),
                    String.valueOf(task.getOutlineIndex()),
                    String.valueOf(task.getWallIndex())));
        }

        return list;
    }

    public static Set<FalltrapTask> fetchFalltrapTasks(Manager manager, Object object) {
        List<String> list = Utils.createList(object, String.class);
        Set<FalltrapTask> falltrapTasks = new HashSet<>();

        for (String s : list) {
            String[] split = s.split(";");
            String[] secSplit = split[0].split(", ");
            ItemStack wall = BukkitSerialization.itemStackFromBase64(split[2]);
            Claim claim = fetchClaim(split[0]);
            claim.setY1(parseInt(secSplit[3]));
            claim.setY2(parseInt(secSplit[6]));
            falltrapTasks.add(new FalltrapTask(manager,
                    UUID.fromString(split[1]), claim, wall,
                    Integer.parseInt(split[3]), Integer.parseInt(split[4])));
        }

        return falltrapTasks;
    }

    public static List<String> serializeBaseTasks(Set<BaseTask> tasks) {
        List<String> list = new ArrayList<>();

        for (BaseTask task : tasks) {
            list.add(String.join(";",
                    serializeClaim(task.getClaim()), task.getPlayer().toString(),
                    BukkitSerialization.itemStackArrayToBase64(new ItemStack[]{task.getWall(), task.getOutline()}),
                    String.valueOf(task.getOutlineIndex()),
                    String.valueOf(task.getWallIndex())));
        }

        return list;
    }

    public static Set<BaseTask> fetchBaseTasks(Manager manager, Object object) {
        List<String> list = Utils.createList(object, String.class);
        Set<BaseTask> baseTasks = new HashSet<>();

        for (String s : list) {
            String[] split = s.split(";");
            String[] secSplit = split[0].split(", ");
            ItemStack[] array = BukkitSerialization.itemStackArrayFromBase64(split[2]);
            Claim claim = fetchClaim(split[0]);
            claim.setY1(parseInt(secSplit[3]));
            claim.setY2(parseInt(secSplit[6]));
            baseTasks.add(new BaseTask(manager, UUID.fromString(split[1]), claim, array[0], array[1],
                    Integer.parseInt(split[3]), Integer.parseInt(split[4])));
        }

        return baseTasks;
    }

    public static List<String> serializeMembers(Set<Member> members) {
        return members.stream().map(m -> m.getUniqueID().toString() + ", " + m.getRole().toString()).collect(Collectors.toList());
    }

    public static Set<Member> fetchMembers(Object object) {
        List<String> list = Utils.createList(object, String.class);
        Set<Member> members = new HashSet<>();

        for (String s : list) {
            String[] split = s.split(", ");
            members.add(new Member(UUID.fromString(split[0]), Role.valueOf(split[1])));
        }

        return members;
    }

    public static String serializeLoc(Location location) {
        if (location == null) return "null";
        return location.getWorld().getName() + ", " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() +
                ", " + location.getYaw() + ", " + location.getPitch();
    }

    public static Location fetchLocation(String string) {
        if (string.equals("null")) return null;
        String[] split = string.split(", ");
        return new Location(Bukkit.getWorld(split[0]),
                parseDouble(split[1]), parseDouble(split[2]), parseDouble(split[3]),
                parseFloat(split[4]), parseFloat(split[5]));
    }

    public static Location fetchLocationAddOffset(String string) {
        if (string.equals("null")) return null;
        String[] split = string.split(", ");
        return new Location(Bukkit.getWorld(split[0]),
                parseDouble(split[1]), parseDouble(split[2]), parseDouble(split[3]),
                parseFloat(split[4]), parseFloat(split[5])).clone().add(0.5, 1, 0.5);
    }

    public static PotionEffect getEffect(String string) {
        if (string.isEmpty()) {
            return null;
        }

        String[] split = string.split(", ");

        try {

            int duration = (split[1].equals("MAX_VALUE") ? (Utils.isModernVer() ? -1 : Integer.MAX_VALUE) : 20 * parseInt(split[1].replaceAll("s", "")));
            return new PotionEffect(PotionEffectType.getByName(split[0]), duration, parseInt(split[2]) - 1);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Effect " + split[0] + " does not exist!");
        }
    }

    private static Integer parseInt(String string) {
        return Integer.parseInt(string);
    }

    private static Double parseDouble(String string) {
        return Double.parseDouble(string);
    }

    private static Float parseFloat(String string) {
        return Float.parseFloat(string);
    }
}