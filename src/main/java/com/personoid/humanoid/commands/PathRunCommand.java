package com.personoid.humanoid.commands;

import com.personoid.api.PersonoidAPI;
import com.personoid.api.activities.FollowPathActivity;
import com.personoid.api.npc.NPC;
import com.personoid.api.pathfinding.Path;
import com.personoid.humanoid.Humanoid;
import com.personoid.humanoid.handlers.CommandHandler;
import com.personoid.humanoid.listeners.DebugEvents;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class PathRunCommand extends CommandHandler.Command {
    public PathRunCommand() {
        super("path", "run", CommonRequirements.PLAYER);
    }

    @Override
    public boolean onCommand(@NotNull Player sender, String[] args) {
        ItemStack item = sender.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(Humanoid.getPlugin(), "pathwand");
        if (meta != null && meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
            int pathId = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
            Path path = DebugEvents.paths.get(pathId);
            if (path != null) {
                path = new Path(path.getNodes());
                NPC npc = PersonoidAPI.getRegistry().createNPCInstance("pathy");
                npc.setInvulnerable(true);
                npc.getNavigation().getOptions().setShowPath(true);
                Location startLocation = path.getNode(0).getPos().toLocation(sender.getWorld());
                PersonoidAPI.getRegistry().spawnNPC(npc, startLocation.add(0.5, 0, 0.5));
                npc.getBrain().getActivityManager().register(new FollowPathActivity(path, FollowPathActivity.MovementType.SPRINT) {
                    @Override
                    public void onStop(StopType stopType) {
                        super.onStop(stopType);
                        if (stopType == StopType.SUCCESS) {
                            PersonoidAPI.getRegistry().removeNPC(npc);
                            sender.sendMessage(ChatColor.GREEN + "Path test passed");
                        }
                    }

                    @Override
                    public void onStuck() {
                        super.onStuck();
                        PersonoidAPI.getRegistry().removeNPC(npc);
                        sender.sendMessage(ChatColor.RED + "Path test failed");
                    }
                });
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You must be holding a path wand to perform a path test");
        }
        return true;
    }
}
