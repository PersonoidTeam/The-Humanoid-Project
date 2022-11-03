package com.personoid.humanoid.commands;

import com.personoid.api.PersonoidAPI;
import com.personoid.api.npc.NPC;
import com.personoid.api.npc.Skin;
import com.personoid.api.utils.bukkit.Message;
import com.personoid.humanoid.activites.location.WanderActivity;
import com.personoid.humanoid.activites.targeting.FightPlayerActivity;
import com.personoid.humanoid.features.TestFeature;
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
            npc = PersonoidAPI.getRegistry().createNPCInstance("Ham and Cheese", Skin.get("cvjk"));
        } else if (args.length == 1) {
            npc = PersonoidAPI.getRegistry().createNPCInstance(args[0], Skin.get("cvjk"));
        } else return false;
        //npc.getProfile().setTabVisibility(false);
        PersonoidAPI.getRegistry().spawnNPC(npc, sender.getLocation());
        //npc.getNavigation().getPathfinder().getOptions().setUseChunking(false);
        npc.getBrain().getActivityManager().register(
                new WanderActivity(),
                //new MineTreeActivity()
                //new FollowEntityActivity(sender)
                new FightPlayerActivity(sender, FightPlayerActivity.AttackType.ALL, FightPlayerActivity.Strategy.OFFENSIVE)
        );
        npc.addFeature(new TestFeature());
        new Message("&aCreated NPC: &e" + npc.getEntity().getName()).send(sender);
/*        AtomicInteger count = new AtomicInteger(1);
        Location loc = sender.getLocation();
        new Task(() -> {
            NPC npc = PersonoidAPI.getRegistry().createNPCInstance(Bukkit.getWorld("world"), "Ham and Cheese");
            PersonoidAPI.getRegistry().spawnNPC(npc, loc);
            npc.getNPCBrain().getActivityManager().register(
                    new FightPlayerActivity(Bukkit.getPlayer("DefineDoddy"), FightPlayerActivity.AttackType.ALL, FightPlayerActivity.Strategy.MIXED)
            );
            Bukkit.broadcastMessage("Spawned NPC " + count);
            count.getAndIncrement();
        }, Humanoid.getPlugin()).repeat(0, 8 * 20);*/
        return true;
    }
}
