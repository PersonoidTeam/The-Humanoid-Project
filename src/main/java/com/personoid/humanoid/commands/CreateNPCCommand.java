package com.personoid.humanoid.commands;

import com.personoid.api.PersonoidAPI;
import com.personoid.api.npc.NPC;
import com.personoid.api.utils.bukkit.Message;
import com.personoid.humanoid.activites.gathering.MineTreeActivity;
import com.personoid.humanoid.activites.location.FollowEntityActivity;
import com.personoid.humanoid.activites.location.WanderActivity;
import com.personoid.humanoid.handlers.CommandHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CreateNPCCommand extends CommandHandler.Command {
    public CreateNPCCommand() {
        super("npc", "create", CommonRequirements.player);
    }

    @Override
    public boolean onCommand(@NotNull Player sender, String[] args) {
        NPC npc;
        if (args.length == 0) {
            npc = PersonoidAPI.getRegistry().createNPCInstance(sender.getWorld(), "Ham and Cheese");
        } else if (args.length == 1) {
            npc = PersonoidAPI.getRegistry().createNPCInstance(sender.getWorld(), args[0]);
        } else return false;
        PersonoidAPI.getRegistry().spawnNPC(npc, sender.getLocation());
        npc.getNavigation().getPathfinder().getOptions().setUseChunking(false);
        npc.getNPCBrain().getActivityManager().register(
                //new FollowEntityActivity(sender, 2),
                new WanderActivity(),
                new MineTreeActivity(),
                new FollowEntityActivity(sender)
        );
        new Message("&aCreated NPC: &e" + npc.getEntity().getName()).send(sender);
        return true;
    }
}
