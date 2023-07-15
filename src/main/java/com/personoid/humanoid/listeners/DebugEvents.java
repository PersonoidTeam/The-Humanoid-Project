package com.personoid.humanoid.listeners;

import com.personoid.api.pathfinding.calc.Path;
import com.personoid.api.pathfinding.calc.goal.BlockGoal;
import com.personoid.api.pathfinding.calc.pathfinder.LongRangePathFinder;
import com.personoid.api.pathfinding.calc.pathfinder.PathFinder;
import com.personoid.api.pathfinding.calc.utils.BlockPos;
import com.personoid.api.utils.bukkit.Message;
import com.personoid.api.utils.bukkit.Task;
import com.personoid.humanoid.Humanoid;
import com.personoid.humanoid.utils.LocationUtils;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class DebugEvents implements Listener {
    public static final HashMap<Integer, Path> paths = new HashMap<>();
    private static Task pathTask;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getItemMeta() == null) return;
        ItemMeta meta = event.getItem().getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey pathwandKey = new NamespacedKey(Humanoid.getPlugin(), "pathwand");
        if (data.has(pathwandKey, PersistentDataType.INTEGER)) {
            event.setCancelled(true);
            NamespacedKey loc1Key = new NamespacedKey(Humanoid.getPlugin(), "pathwand_loc_1");
            NamespacedKey loc2Key = new NamespacedKey(Humanoid.getPlugin(), "pathwand_loc_2");
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                Location loc = event.getClickedBlock().getLocation();
                String locStr = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
                data.set(loc1Key, PersistentDataType.STRING, locStr);
                event.getItem().setItemMeta(meta);
                new Message("&b[PathWand] &aLocation 1 set: " + LocationUtils.toStringBasic(loc)).send(event.getPlayer());
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Location loc = event.getClickedBlock().getLocation();
                String locStr = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
                data.set(loc2Key, PersistentDataType.STRING, locStr);
                event.getItem().setItemMeta(meta);
                new Message("&b[PathWand] &aLocation 2 set: " + LocationUtils.toStringBasic(loc)).send(event.getPlayer());
            } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                if (!data.has(loc1Key, PersistentDataType.STRING)) {
                    new Message("&b[PathWand] &cFirst position not set!").send(event.getPlayer());
                    return;
                }
                if (!data.has(loc2Key, PersistentDataType.STRING)) {
                    new Message("&b[PathWand] &cSecond position not set!").send(event.getPlayer());
                    return;
                }
                Location loc1 = LocationUtils.fromString(data.get(loc1Key, PersistentDataType.STRING));
                Location loc2 = LocationUtils.fromString(data.get(loc2Key, PersistentDataType.STRING));
                int id = data.get(pathwandKey, PersistentDataType.INTEGER);
                if (paths.containsKey(id)) {
                    Path path = paths.get(id);
                    if (path.size() > 0 && path.getNode(0).getPos().toLocation(loc1.getWorld()).equals(loc1) &&
                            path.getNode(path.size() - 1).getPos().toLocation(loc2.getWorld()).equals(loc2)) {
                        new Message("&b[PathWand] &cPath is already in memory!").send(event.getPlayer());
                        return;
                    }
                }
                paths.remove(id);
                new Message("&b[PathWand] &6Generating path...").send(event.getPlayer());
                PathFinder pathfinder = new LongRangePathFinder();
                BlockPos loc2AbovePos = BlockPos.fromLocation(loc2).above();
                Path path = pathfinder.findPath(BlockPos.fromLocation(loc1).above(), new BlockGoal(loc2AbovePos), loc2.getWorld());
                if (path == null) {
                    new Message("&b[PathWand] &cPath not found!").send(event.getPlayer());
                    return;
                }
                new Message("&b[PathWand] &aPath found! &2Size: " + path.size()).send(event.getPlayer());
                paths.put(id, path);
                if (pathTask == null) {
                    pathTask = new Task(() -> {
                        for (Map.Entry<Integer, Path> entry : paths.entrySet()) {
                            Bukkit.getOnlinePlayers().forEach(player -> {
                                ItemStack hand = player.getInventory().getItemInMainHand();
                                if (hand.getType() == Material.AIR || hand.getItemMeta() == null) return;
                                if (hand.getItemMeta().getPersistentDataContainer().has(pathwandKey, PersistentDataType.INTEGER)) {
                                    int handId = hand.getItemMeta().getPersistentDataContainer().get(pathwandKey, PersistentDataType.INTEGER);
                                    if (handId == entry.getKey()) {
                                        if (entry.getValue().size() > 0) {
                                            for (int i = 0; i < entry.getValue().size(); i++) {
                                                player.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, entry.getValue().getNode(i)
                                                        .getPos().toLocation(player.getWorld()).clone().add(0.5, 0, 0.5), 5,
                                                        new Particle.DustTransition(Color.RED, Color.ORANGE, 1));
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }, Humanoid.getPlugin()).repeat(0, 2).run();
                }
            } else if (event.getAction() == Action.LEFT_CLICK_AIR) {
                if (!data.has(pathwandKey, PersistentDataType.INTEGER)) return;
                int id = data.get(pathwandKey, PersistentDataType.INTEGER);
                if (paths.containsKey(id)) {
                    paths.remove(id);
                    new Message("&b[PathWand] &aPath removed!").send(event.getPlayer());
                } else {
                    new Message("&b[PathWand] &cNo path to remove!").send(event.getPlayer());
                }
            }
        }
    }
}
