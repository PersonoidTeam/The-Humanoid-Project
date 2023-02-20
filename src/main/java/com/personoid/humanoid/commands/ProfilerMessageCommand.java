package com.personoid.humanoid.commands;

import com.personoid.api.utils.bukkit.Message;
import com.personoid.api.utils.debug.Profiler;
import com.personoid.humanoid.handlers.CommandHandler;
import com.personoid.humanoid.utils.Config;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class ProfilerMessageCommand extends CommandHandler.Command {
    public ProfilerMessageCommand() {
        super("profiler", "message");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, String[] args) {
        if (args.length == 1) {
            for (Profiler profilerType : Profiler.values()) {
                if (profilerType.name().equalsIgnoreCase(args[0].trim())) {
                    if (Config.getEnabledProfilers().contains(profilerType)) {
                        profilerType.disable();
                        Config.removeEnabledProfiler(Collections.singletonList(profilerType));
                        new Message("&cDisabled &6" + profilerType.name() + "&c profiling messages").send(sender);
                    } else {
                        profilerType.enable();
                        Config.addEnabledProfiler(Collections.singletonList(profilerType));
                        new Message("&aEnabled &6" + profilerType.name() + "&a profiling messages").send(sender);
                    }
                    return true;
                }
            }
            new Message("&cInvalid profiler type").send(sender);
        } else return false;
        return true;
    }
}
