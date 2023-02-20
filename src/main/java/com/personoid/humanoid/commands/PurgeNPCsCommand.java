package com.personoid.humanoid.commands;

import com.personoid.api.PersonoidAPI;
import com.personoid.api.utils.bukkit.Message;
import com.personoid.humanoid.handlers.CommandHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PurgeNPCsCommand extends CommandHandler.Command {
    public PurgeNPCsCommand() {
        super("npc", "purge", CommonRequirements.PLAYER);
    }

    @Override
    public boolean onCommand(@NotNull Player sender, String[] args) {
        if (args.length != 0) return false;
        if (PersonoidAPI.getRegistry().getNPCs().size() == 0) {
            new Message("&cNo NPCs found").send(sender);
            return true;
        }
        PersonoidAPI.getRegistry().purgeNPCs();
        new Message("&aPurged all NPCs").send(sender);
        return true;
    }
}
