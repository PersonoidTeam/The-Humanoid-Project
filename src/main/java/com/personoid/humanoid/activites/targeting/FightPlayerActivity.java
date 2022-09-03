package com.personoid.humanoid.activites.targeting;

import com.google.common.collect.Multimap;
import com.personoid.api.activities.GoToLocationActivity;
import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.ai.looking.Target;
import com.personoid.api.ai.movement.MovementType;
import com.personoid.api.utils.Result;
import com.personoid.api.utils.packet.ReflectionUtils;
import com.personoid.api.utils.types.HandEnum;
import com.personoid.api.utils.types.Priority;
import com.personoid.humanoid.Humanoid;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FightPlayerActivity extends Activity {
    private final Player player;
    private int attackCooldown;

    public FightPlayerActivity(Player player) {
        super(ActivityType.FIGHTING, Priority.HIGHEST);
        this.player = player;
    }

    @Override
    public void onStart(StartType startType) {
        getNPC().getLookController().addTarget("fight_target", new Target(player, Priority.HIGHEST));
        getNPC().getNPCInventory().addItem(new ItemStack(Material.NETHERITE_AXE));
        switchToItem(Material.NETHERITE_AXE);
        attackCooldown = getAttackSpeed(getNPC().getNPCInventory().getSelectedItem());
        player.hidePlayer(Humanoid.getPlugin(), getNPC().getEntity());
        player.showPlayer(Humanoid.getPlugin(), getNPC().getEntity());
    }

    private int switchToItem(Material material) {
        for (int i = 0; i < 9; i++) {
            ItemStack item = getNPC().getNPCInventory().getHotbar().get(i);
            if (item != null && item.getType() == material) {
                getNPC().getNPCInventory().select(i);
                return i;
            }
        }
        return -1;
    }

    private int getAttackSpeed(ItemStack item) {
        try {
            Class<?> craftClass = ReflectionUtils.getCraftClass("inventory", "CraftItemStack");
            Object nmsItem = craftClass.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            Method method = nmsItem.getClass().getMethod("a", EquipmentSlot.class);
            Multimap<Attribute, AttributeModifier> map = (Multimap<Attribute, AttributeModifier>) method.invoke(nmsItem, EquipmentSlot.MAINHAND);
            double itemSpeed = map.get(Attributes.ATTACK_SPEED).stream().mapToDouble(AttributeModifier::getAmount).sum();
            //Bukkit.broadcastMessage(item.getType().name() + " attack speed: " + (int) (1 / (4 + itemSpeed) * 20));
            return (int) (1 / (4 + itemSpeed) * 20);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        return 0;
    }

    private boolean checkWinStatus() {
        if (player.isDead()) {
            markAsFinished(new Result<>(Result.Type.SUCCESS, player));
            return true;
        }
        return false;
    }

    @Override
    public void onUpdate() {
        if (checkWinStatus()) return;
        run(new GoToLocationActivity(player.getLocation(), MovementType.SPRINTING));
        if (attackCooldown <= 0) {
            getNPC().getEntity().resetPlayerTime();
            if (getNPC().getLocation().distance(player.getLocation()) < 5) {
                getNPC().swingHand(HandEnum.RIGHT);
/*                ServerPlayer npcServerPlayer = ((CraftPlayer)getNPC().getEntity()).getHandle();
                ServerPlayer playerServerPlayer = ((CraftPlayer)player).getHandle();

                ChunkMap tracker = ((ServerLevel)playerServerPlayer.level).getChunkSource().chunkMap;
                ChunkMap.TrackedEntity entry = tracker.entityMap.get(getNPC().getEntity().getEntityId());
                if (entry != null) {
                    entry.removePlayer(npcServerPlayer);
                    Packets.removePlayer(getNPC().getEntity()).send();
                    entry.updatePlayer(npcServerPlayer);
                    Packets.addPlayer(getNPC().getEntity()).send();
                }*/
/*                    Class<?> nmsPlayerClass = npc.getClass().getMethod("getHandle").invoke(npc).getClass();
                    Bukkit.broadcastMessage("NMS player: " + nmsPlayerClass);
                    MethodHandle handle = MethodHandles.lookup().findSpecial()
                    Class<?> attribute = nmsPlayerClass.getMethod("a").invoke(Attributes.ATTACK_DAMAGE).getClass();
                    Bukkit.broadcastMessage("Attribute: " + attribute);
                    double damage = (double) attribute.getMethod("f").invoke(attribute);*/
                //double damage = getNPC().getEntity().getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE).getValue();
                getNPC().getEntity().attack(player);
                double x = player.getLocation().getX();
                double y = player.getLocation().getY() + 2;
                double z = player.getLocation().getZ();
                ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.CRIT, false, x, y, z, .5F, .4F, .5F, 0, 15);
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    ServerPlayer serverPlayer = ((CraftPlayer)onlinePlayer).getHandle();
                    serverPlayer.connection.send(packet);
                }
                getNPC().getLocation().getWorld().playSound(getNPC().getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
            }
            attackCooldown = getAttackSpeed(getNPC().getNPCInventory().getSelectedItem());
        } else {
            if (attackCooldown < 10) getNPC().getMoveController().jump();
            attackCooldown--;
        }
    }

    @Override
    public void onStop(StopType stopType) {
        getNPC().getLookController().removeTarget("fight_target");
    }

    @Override
    public boolean canStart(StartType startType) {
        return shouldAttack() && !checkWinStatus();
    }

    @Override
    public boolean canStop(StopType stopType) {
        return true;
    }

    // TODO: should attack entity based on their gear?
    private boolean shouldAttack() {
        return true;
    }
}
