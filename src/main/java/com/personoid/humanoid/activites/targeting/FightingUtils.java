package com.personoid.humanoid.activites.targeting;

import com.google.common.collect.Multimap;
import com.personoid.api.utils.packet.ReflectionUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FightingUtils {
    public static int getAttackSpeed(ItemStack item) {
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

    public static double getAttackDamage(ItemStack item) {
        try {
            Class<?> craftClass = ReflectionUtils.getCraftClass("inventory", "CraftItemStack");
            Object nmsItem = craftClass.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            Method method = nmsItem.getClass().getMethod("a", EquipmentSlot.class);
            Multimap<Attribute, AttributeModifier> map = (Multimap<Attribute, AttributeModifier>) method.invoke(nmsItem, EquipmentSlot.MAINHAND);
            double itemDamage = map.get(Attributes.ATTACK_DAMAGE).stream().mapToDouble(AttributeModifier::getAmount).sum();
            //Bukkit.broadcastMessage(item.getType().name() + " attack damage: " + damage);
            return itemDamage;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        return 0;
    }

    public static void playCriticalHitEffect(Location location) {
        // particle type, ?, position, offset/range, speed, number of particles
        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.CRIT, false,
                location.getX(), location.getY(), location.getZ(), .5F, .4F, .5F, 0, 15);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            ServerPlayer serverPlayer = ((CraftPlayer)onlinePlayer).getHandle();
            serverPlayer.connection.send(packet);
        }
        location.getWorld().playSound(location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
    }
}
