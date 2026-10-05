package me.keano.azurite.utils;

import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CC {

    private static final Function<String, String> REPLACER;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final char COLOR_CHAR = ChatColor.COLOR_CHAR;
    public static final String MENU_BAR;
    public static final String CHAT_BAR;
    public static final String SB_BAR;
    public static final String TAB_BAR;
    public static final String BLUE;
    public static final String AQUA;
    public static final String YELLOW;
    public static final String RED;
    public static final String GRAY;
    public static final String GOLD;
    public static final String GREEN;
    public static final String WHITE;
    public static final String BLACK;
    public static final String BOLD;
    public static final String ITALIC;
    public static final String UNDER_LINE;
    public static final String STRIKE_THROUGH;
    public static final String RESET;
    public static final String MAGIC;
    public static final String DARK_BLUE;
    public static final String DARK_AQUA;
    public static final String DARK_GRAY;
    public static final String DARK_GREEN;
    public static final String DARK_PURPLE;
    public static final String DARK_RED;
    public static final String PINK;
    static {
        if (Utils.isModernVer()) {
            REPLACER = s -> {
                Matcher matcher = HEX_PATTERN.matcher(s);
                StringBuffer buffer = new StringBuffer(s.length() + 4 * 8);
                while (matcher.find()) {
                    String group = matcher.group(1);
                    matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                            + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                            + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                            + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
                    );
                }
                return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
            };

        } else {
            REPLACER = s -> ChatColor.translateAlternateColorCodes('&', s);
        }


        BLUE = ChatColor.BLUE.toString();
        AQUA = ChatColor.AQUA.toString();
        YELLOW = ChatColor.YELLOW.toString();
        RED = ChatColor.RED.toString();
        GRAY = ChatColor.GRAY.toString();
        GOLD = ChatColor.GOLD.toString();
        GREEN = ChatColor.GREEN.toString();
        WHITE = ChatColor.WHITE.toString();
        BLACK = ChatColor.BLACK.toString();
        BOLD = ChatColor.BOLD.toString();
        ITALIC = ChatColor.ITALIC.toString();
        UNDER_LINE = ChatColor.UNDERLINE.toString();
        STRIKE_THROUGH = ChatColor.STRIKETHROUGH.toString();
        RESET = ChatColor.RESET.toString();
        MAGIC = ChatColor.MAGIC.toString();
        DARK_BLUE = ChatColor.DARK_BLUE.toString();
        DARK_AQUA = ChatColor.DARK_AQUA.toString();
        DARK_GRAY = ChatColor.DARK_GRAY.toString();
        DARK_GREEN = ChatColor.DARK_GREEN.toString();
        DARK_PURPLE = ChatColor.DARK_PURPLE.toString();
        DARK_RED = ChatColor.DARK_RED.toString();
        PINK = ChatColor.LIGHT_PURPLE.toString();
        MENU_BAR = ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------";
        CHAT_BAR = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------------------------------";
        SB_BAR = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------";
        TAB_BAR = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-----------------";
    }

    public static String LINE = t("&7&m-------------------------");

    public static String t(String t) {
        return REPLACER.apply(t);
    }
    public static String translate(String p) {return ChatColor.translateAlternateColorCodes('&', p);}

    public static List<String> t(List<String> t) {
        return t.stream().map(REPLACER).collect(Collectors.toList());
    }
}