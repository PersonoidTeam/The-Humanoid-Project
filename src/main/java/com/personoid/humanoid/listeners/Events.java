package com.personoid.humanoid.listeners;

import com.personoid.api.PersonoidAPI;
import com.personoid.api.events.NPCDeathEvent;
import com.personoid.api.npc.NPC;
import com.personoid.api.npc.Skin;
import com.personoid.humanoid.Humanoid;
import com.personoid.humanoid.utils.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Events implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PersonoidAPI.getRegistry().getNPCs().forEach(npc -> npc.setVisibilityTo(player, true));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PersonoidAPI.getRegistry().getNPCs().forEach(npc -> npc.setVisibilityTo(player, false));
    }

/*    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        NPC npc = LocationUtils.getClosestNPC(player.getLocation());
        MessageManager messageManager = npc.getNPCBrain().getMessageManager();
        // TODO: message delay based on message length
        new Task(() -> {
            String response = messageManager.getResponseFrom(event.getMessage(), event.getPlayer().getName());
            npc.sendMessage(response);
        }).async().run(20);
    }*/

/*    @EventHandler
    public void onNPCChat(NPCChatEvent event) {
        NPC npc = event.getNPC();
        NPC closest = LocationUtils.getClosestNPC(npc.getLocation(), List.of(npc));
        MessageManager messageManager = closest.getNPCBrain().getMessageManager();
        AtomicReference<String> response = new AtomicReference<>("");
        String name = npc.getName().getString().trim();
        if (name.equalsIgnoreCase(closest.getName().getString().trim())) name += 1;
        new Task(() -> response.set(messageManager.getResponseFrom(event.getMessage(), name))).async().run(20);
        // TODO: message delay based on message length
        closest.sendMessage(response.get());
    }*/

/*    @EventHandler
    public void onNPCPickupItem(NPCPickupItemEvent event) {
        if (event.getThrower() != null && event.getThrower() instanceof Player player) {
            ItemStack item = event.getItem().getItemStack();
            MessageManager messageManager = event.getNPC().getNPCBrain().getMessageManager();
            String npcName = event.getNPC().getName().getString();
            AtomicReference<String> response = new AtomicReference<>("");
            new Task(() -> {
                String msg = player.getDisplayName() + " gave " + npcName + " x" + item.getAmount() + " " +
                        item.getType().name().toLowerCase().replace("_", " ");
                response.set(messageManager.getResponse(msg));
            }).async();
            new Task(() -> event.getNPC().getBukkitEntity().performCommand("msg " + player.getDisplayName() + " " + response)).run(20);
            // TODO: message delay based on message length
        }
    }*/

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Bukkit.broadcastMessage(player.getDisplayName() + " took " + event.getDamage() + " damage");
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (PersonoidAPI.getRegistry().isNPC(event.getEntity())) {
            NPC npc = PersonoidAPI.getRegistry().getNPC(event.getEntity());
            Bukkit.getPluginManager().callEvent(new NPCDeathEvent(npc));
        }
    }

    @EventHandler
    public void onNPCDeath(NPCDeathEvent event) {
        NPC oldNPC = event.getNPC();
        new BukkitRunnable() {
            @Override
            public void run(){
                Location spawnLocation = oldNPC.getEntity().getBedSpawnLocation();
                if (spawnLocation == null){
                    spawnLocation = Bukkit.getServer().getWorlds().get(0).getSpawnLocation();
                }
                PersonoidAPI.getRegistry().removeNPC(oldNPC);
                //ServerboundClientCommandPacket packet = new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN);
                //((CraftPlayer)oldNPC.getEntity()).getHandle().connection.handleClientCommand(packet);
                NPC newNPC = PersonoidAPI.getRegistry().createNPCInstance("Ham and Cheese", Skin.randomDefault());
                oldNPC.getBrain().getActivityManager().getRegisteredActivities().forEach(activity -> {
                    newNPC.getBrain().getActivityManager().register(activity);
                });
                PersonoidAPI.getRegistry().spawnNPC(newNPC, spawnLocation);
            }
        }.runTaskLater(Humanoid.getPlugin(), MathUtils.random(2 * 20, 4 * 20));
    }
}
