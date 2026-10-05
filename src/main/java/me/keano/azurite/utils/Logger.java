package me.keano.azurite.utils;

import org.bukkit.Bukkit;

public class Logger {

    public static final String LINE_CONSOLE = CC.t("&5=========================");

    public static void state(String state, int managers, int teams, int users, int kits, int koths) {
        print(LINE_CONSOLE);
        print("- &dLoading Azurite HCF...");
        print("- &dLoading modules: &f" + Utils.getNMSVer());
        print("- &d" + convert(state) + " &f" + managers + " &dmanagers.");
        print("- &d" + convert(state) + " &f" + teams + " &dteams.");
        print("- &d" + convert(state) + " &f" + users + " &dusers");
        print("- &d" + convert(state) + " &f" + koths + " &dkoths");
        print("- &d" + convert(state) + " &f" + kits + " &dkits");
        print(LINE_CONSOLE);
        print("- &dAuthor&f: Keqno");
        print("- &dVersion&f: " + Bukkit.getPluginManager().getPlugin("Azurite").getDescription().getVersion());
        print("- &dState&f: " + state);
        print(LINE_CONSOLE);
    }

    public static void print(String... message) {
        for (String s : message) {
            Bukkit.getServer().getConsoleSender().sendMessage(CC.t(s));
        }
    }

    private static String convert(String string) {
        return string.equalsIgnoreCase("enabled") ? "Loaded" : "Saved";
    }
}