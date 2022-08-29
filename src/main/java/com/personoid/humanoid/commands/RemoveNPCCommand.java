package com.personoid.humanoid.commands;

import com.personoid.api.PersonoidAPI;
import com.personoid.api.npc.NPC;
import com.personoid.api.utils.bukkit.Message;
import com.personoid.humanoid.handlers.CommandHandler;
import com.personoid.humanoid.utils.LocationUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RemoveNPCCommand extends CommandHandler.Command {
    public RemoveNPCCommand() {
        super("npc", "remove", CommonRequirements.player);
    }

    @Override
    public boolean onCommand(@NotNull Player sender, String[] args) {
        NPC npc;
        if (args.length == 0) {
            npc = LocationUtils.getClosestNPC(sender.getLocation());
            if (npc == null) {
                new Message("&cNo NPC found").send(sender);
                return true;
            }
        } else if (args.length == 1) {
            npc = PersonoidAPI.getRegistry().getNPC(args[0]);
            if (npc == null) {
                new Message("&cNPC not found").send(sender);
                return true;
            }
        } else return false;
        PersonoidAPI.getRegistry().removeNPC(npc);
        new Message("&aRemoved NPC: &e" + npc.getEntity().getName()).send(sender);
        return true;
    }
}
