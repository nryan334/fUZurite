package me.keano.azurite.modules.framework.commands;

import lombok.Getter;
import lombok.Setter;
import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.framework.Config;
import me.keano.azurite.modules.framework.Module;
import me.keano.azurite.modules.framework.commands.extra.TabCompletion;
import me.keano.azurite.utils.CC;
import me.keano.azurite.utils.ReflectionUtils;
import me.keano.azurite.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
@Getter
@Setter
@SuppressWarnings("unchecked")
public abstract class Command extends Module<CommandManager> {

    private static final Field KNOWN_COMMANDS = ReflectionUtils.accessField(SimpleCommandMap.class, "knownCommands");

    protected String name;
    protected String permissible;
    protected BukkitCommand bukkitCommand;
    protected boolean async;

    protected Map<String, Argument> arguments;
    protected List<TabCompletion> completions;
    protected List<String> usage;

    public Command(CommandManager manager, String name) {
        super(manager);
        this.name = name;

        this.permissible = null;
        this.async = false;

        this.arguments = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.completions = new ArrayList<>();
    }

    public abstract List<String> aliases();

    public abstract List<String> usage();

    public BukkitCommand asBukkitCommand() {
        if (bukkitCommand != null) return bukkitCommand;

        BukkitCommand command = new BukkitCommand(name) {
            @Override
            public boolean execute(CommandSender sender, String s, String[] args) {
                if (permissible != null && !permissible.isEmpty() && !sender.hasPermission(permissible)) {
                    sendMessage(sender, Config.INSUFFICIENT_PERM);
                    return true;
                }

                if (async) {
                    Tasks.executeAsync(getManager(), () -> Command.this.execute(sender, args));
                    return true;
                }

                Command.this.execute(sender, args);
                return true;
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
                List<String> tabComplete = Command.this.tabComplete(sender, args);
                if (tabComplete != null) return tabComplete;

                List<String> toTab = super.tabComplete(sender, alias, args);
                Iterator<String> iterator = toTab.iterator();

                while (iterator.hasNext()) {
                    String next = iterator.next();
                    Player player = Bukkit.getPlayer(next);

                    if (player != null && getInstance().getStaffManager().isVanished(player)) {
                        iterator.remove();
                    }
                }

                return toTab;
            }
        };

        if (!aliases().isEmpty()) {
            command.setAliases(aliases());
        }

        this.bukkitCommand = command; // Cache bukkit command
        return command;
    }

    public void unregister() {
        if (name.equalsIgnoreCase("azurite")) return;
        if (bukkitCommand == null) return;

        CommandMap commandMap = getInstance().getVersionManager().getVersion().getCommandMap();
        HandlerList.unregisterAll(this);

        for (Argument argument : arguments.values()) {
            HandlerList.unregisterAll(argument);
        }

        bukkitCommand.unregister(commandMap);

        try {

            ((Map<String, org.bukkit.command.Command>) KNOWN_COMMANDS.get(commandMap))
                    .values()
                    .removeIf(cmd -> cmd.getName().equals(bukkitCommand.getName()));

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleArguments(Argument... arguments) {
        for (Argument argument : arguments) {
            argument.getNames().forEach(s -> this.arguments.put(s, argument));
        }
    }

    public void handleArguments(List<Argument> arguments) {
        for (Argument argument : arguments) {
            argument.getNames().forEach(s -> this.arguments.put(s, argument));
        }
    }

    public void sendMessage(CommandSender sender, String... s) {
        for (String msg : s) {
            sender.sendMessage(CC.t(msg));
        }
    }

    public void sendUsage(CommandSender sender) {
        if (usage == null) usage = usage();

        for (String string : usage) {
            sender.sendMessage(CC.t(string));
        }
    }

    public Integer getInt(String string) {
        try {

            return Integer.parseInt(string);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Double getDouble(String string) {
        try {

            return Double.parseDouble(string);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        String[] array = Arrays.copyOfRange(args, 1, args.length);

        if (arguments.containsKey(args[0])) {
            Argument arg = arguments.get(args[0]);

            if (permissible != null && !permissible.isEmpty() && !sender.hasPermission(permissible)) {
                sendMessage(sender, Config.INSUFFICIENT_PERM);
                return;
            }

            if (arg.permissible != null && !arg.permissible.isEmpty() && !sender.hasPermission(arg.permissible)) {
                sendMessage(sender, Config.INSUFFICIENT_PERM);
                return;
            }

            if (arg.isAsync()) {
                Tasks.executeAsync(getManager(), () -> arg.execute(sender, array));
                return;
            }

            arg.execute(sender, array);
            return;
        }

        sendUsage(sender);
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        String string = args[args.length - 1];

        if (args.length == 1 && !arguments.isEmpty()) {
            List<String> toComplete = new ArrayList<>();

            for (Argument arg : arguments.values()) {
                if (hasPerm(sender, arg)) {
                    toComplete.addAll(arg.getNames());
                }
            }

            return toComplete
                    .stream()
                    .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                    .collect(Collectors.toList());
        }

        if (!arguments.isEmpty()) {
            String[] array = Arrays.copyOfRange(args, 1, args.length);
            Argument arg = arguments.get(args[0]);

            if (arg == null) return null;

            List<String> tabComplete = arg.tabComplete(sender, array);

            if (hasPerm(sender, arg) && tabComplete != null && !tabComplete.isEmpty()) {
                return tabComplete;
            }
        }

        if (!completions.isEmpty()) {
            // Check permission first
            if (permissible != null && !sender.hasPermission(permissible)) return null;

            List<String> toComplete = new ArrayList<>();

            for (TabCompletion completion : completions) {
                if (completion.getArg() != args.length - 1) continue;
                if (completion.getPermission() != null && !sender.hasPermission(completion.getPermission())) continue;

                toComplete.addAll(completion.getNames());
            }

            if (!toComplete.isEmpty())
                return toComplete
                        .stream()
                        .filter(s -> s.regionMatches(true, 0, string, 0, string.length()))
                        .collect(Collectors.toList());
        }

        return null;
    }

    private boolean hasPerm(CommandSender sender, Argument arg) {
        if (arg.getPermissible() == null && permissible == null) return true; // no perm set
        if (arg.getPermissible() != null && sender.hasPermission(arg.getPermissible())) return true;
        if (permissible != null && sender.hasPermission(permissible) && arg.getPermissible() != null &&
                !sender.hasPermission(arg.getPermissible())) return false; // don't override argument perms

        return permissible != null && sender.hasPermission(permissible);
    }
}