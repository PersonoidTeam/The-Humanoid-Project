package com.personoid.humanoid.handlers;

import com.personoid.api.utils.bukkit.Message;
import com.personoid.humanoid.Humanoid;
import com.personoid.humanoid.commands.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHandler implements CommandExecutor {
    private static final List<Command> commands = new ArrayList<>();

    public static void registerCommands() {
        CommandHandler instance = new CommandHandler();

        Humanoid.getPlugin().getCommand("npc").setExecutor(instance);
        Humanoid.getPlugin().getCommand("profiler").setExecutor(instance);
        Humanoid.getPlugin().getCommand("debugger").setExecutor(instance);

        registerCommand(new CreateNPCCommand());
        registerCommand(new RemoveNPCCommand());
        registerCommand(new TestLocationCommand());
        registerCommand(new ProfilerMessageCommand());
        registerCommand(new PathWandCommand());
    }

    public static boolean registerCommand(Command command) {
        for (Command cmd : commands) {
            if (cmd.getName().equalsIgnoreCase(command.getName())) {
                return false;
            }
        }
        commands.add(command);
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        for (Command cmd : commands) {
            if (label.trim().equalsIgnoreCase(cmd.parent.trim()) && args[0].trim().equalsIgnoreCase(cmd.getName().trim())) {
                String[] shiftedArgs = Arrays.copyOfRange(args, 1, args.length);
                if (cmd.getRequirements().size() > 0) {
                    for (Command.Requirement requirement : cmd.getRequirements()) {
                        if (requirement.getType() == Command.Requirement.Type.PLAYER) {
                            if (sender instanceof Player) {
                                Player player = (Player) sender;
                                return cmd.onCommand(player, shiftedArgs);
                            } else {
                                cmd.getRequirements(requirement.getType()).forEach(r -> sender.sendMessage(Message.toColor(r.onFailure())));
                                return true;
                            }
                        } else if (requirement.getType() == Command.Requirement.Type.CONSOLE) {
                            if (sender instanceof ConsoleCommandSender) {
                                ConsoleCommandSender console = (ConsoleCommandSender) sender;
                                return cmd.onCommand(console, shiftedArgs);
                            } else {
                                cmd.getRequirements(requirement.getType()).forEach(r -> sender.sendMessage(Message.toColor(r.onFailure())));
                                return true;
                            }
                        }
                    }
                } else {
                    return cmd.onCommand(sender, shiftedArgs);
                }
            }
        }
        return false;
    }

    public static class Command {
        private final String parent;
        private final String name;
        private final Requirement[] requirements;

        public Command(String parent, String name, Requirement... requirements) {
            this.parent = parent;
            this.name = name;
            this.requirements = requirements;
        }

        public String getName() {
            return name;
        }

        public List<Requirement> getRequirements(Requirement.Type... types) {
            if (types.length == 0) return Arrays.asList(requirements);
            List<Requirement> requirements = new ArrayList<>();
            for (Requirement.Type type : types) {
                for (Requirement requirement : this.requirements) {
                    if (requirement.getType() == type) {
                        requirements.add(requirement);
                    }
                }
            }
            return requirements;
        }

        public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
            return false;
        }

        public boolean onCommand(@NotNull Player sender, @NotNull String[] args) {
            return false;
        }

        public boolean onCommand(@NotNull ConsoleCommandSender sender, @NotNull String[] args) {
            return false;
        }

        public static class Requirement {
            private final Type type;

            public Requirement(Type type) {
                this.type = type;
            }

            public String onFailure() {
                return null;
            }

            public Type getType() {
                return type;
            }

            public enum Type {
                PLAYER,
                CONSOLE,
            }
        }
    }
}
