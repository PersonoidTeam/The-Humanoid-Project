package com.personoid.humanoid.activites.targeting;

import com.google.common.collect.Multimap;
import com.personoid.api.utils.Parameter;
import com.personoid.humanoid.values.ArmorItemValues;
import com.personoid.nms.packet.Packages;
import com.personoid.nms.packet.ReflectionUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;

public class FightingUtils {
    public static int getAttackSpeed(ItemStack item) {
        try {
            Class<?> craftClass = ReflectionUtils.getCraftClass("inventory", "CraftItemStack");
            Object nmsItem = craftClass.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            Object equipmentSlot = ReflectionUtils.getEquipmentSlot(EquipmentSlot.HAND);
            Multimap<Object, Object> map = (Multimap<Object, Object>) ReflectionUtils.invoke(nmsItem, "a", equipmentSlot);
            Class<?> attributesClass = ReflectionUtils.findClass(Packages.ATTRIBUTES, "GenericAttributes");
            Object attackSpeedAttribute = ReflectionUtils.getField(attributesClass, "h"); // ATTACK_SPEED
            double speed = map.get(attackSpeedAttribute).stream().mapToDouble(o -> (double) ReflectionUtils.invoke(o, "d")).sum(); // getAmount
            //Bukkit.broadcastMessage(item.getType().name() + " attack damage: " + damage);
            return (int) (1 / (4 + speed) * 20);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        return 0;
    }

    public static double getAttackDamage(ItemStack item) {
        try {
            Class<?> craftClass = ReflectionUtils.getCraftClass("inventory", "CraftItemStack");
            Object nmsItem = craftClass.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            Object equipmentSlot = ReflectionUtils.getEquipmentSlot(EquipmentSlot.HAND);
            Multimap<Object, Object> map = (Multimap<Object, Object>) ReflectionUtils.invoke(nmsItem, "a", equipmentSlot);
            Class<?> attributesClass = ReflectionUtils.findClass(Packages.ATTRIBUTES, "GenericAttributes");
            Object attackDamageAttribute = ReflectionUtils.getField(attributesClass, "f"); // ATTACK_DAMAGE
            double damage = map.get(attackDamageAttribute).stream().mapToDouble(o -> (double) ReflectionUtils.invoke(o, "d")).sum(); // getAmount
            //Bukkit.broadcastMessage(item.getType().name() + " attack damage: " + damage);
            return damage;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        return 0;
    }

    public static void playCriticalHitEffect(Location location) {
        // particle type, ?, position, offset/range, speed, number of particles
        Class<?> particleTypesClass = ReflectionUtils.findClass(Packages.PARTICLE_TYPES, "Particles");
        Class<?> particleParamClass = ReflectionUtils.findClass(Packages.PARTICLE_TYPES, "ParticleParam");
        Object critParticle = ReflectionUtils.getField(particleTypesClass, "g"); // CRIT

        Parameter param1 = new Parameter(particleParamClass, critParticle);
        Parameter param2 = new Parameter(boolean.class, false);
        Parameter param3 = new Parameter(double.class, location.getX());
        Parameter param4 = new Parameter(double.class, location.getY());
        Parameter param5 = new Parameter(double.class, location.getZ());
        Parameter param6 = new Parameter(float.class, .5F);
        Parameter param7 = new Parameter(float.class, .4F);
        Parameter param8 = new Parameter(float.class, .5F);
        Parameter param9 = new Parameter(float.class, 0F);
        Parameter param10 = new Parameter(int.class, 15);

        try {
            ReflectionUtils.createPacket("PacketPlayOutWorldParticles", param1, param2, param3, param4, param5,
                    param6, param7, param8, param9, param10).send();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        location.getWorld().playSound(location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
    }

    public static boolean isSimilar(ItemStack item1, ItemStack item2) {
        boolean baseSimilar = item1.isSimilar(item2);
        if (!baseSimilar) {
            String item1Type = item1.getType().name();
            String item2Type = item2.getType().name();
            if (item1Type.contains("LEGACY_")) {
                item1Type = item1Type.replace("LEGACY_", "");
            }
            if (item2Type.contains("LEGACY_")) {
                item2Type = item2Type.replace("LEGACY_", "");
            }
            return item1Type.equals(item2Type);
        } else return true;
    }

    public static boolean inSurvival(Player player) {
        GameMode gameMode = player.getGameMode();
        return gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE;
    }

    public static float getTotalDefence(Player player) {
        // TODO: improve calculations
        float defence = 0;
        defence += ArmorItemValues.from(player.getInventory().getHelmet()).getHealthMultiplier();
        defence += ArmorItemValues.from(player.getInventory().getChestplate()).getHealthMultiplier();
        defence += ArmorItemValues.from(player.getInventory().getLeggings()).getHealthMultiplier();
        defence += ArmorItemValues.from(player.getInventory().getBoots()).getHealthMultiplier();
/*        for (ItemStack armour : player.getInventory().getArmorContents()) {
            if (armour != null) {
                defence += armour.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
            }
        }*/
        return defence;
    }

    public static float getTotalAttack(Player player) {
        float attack = 0;
        ItemStack item = player.getInventory().getItemInMainHand();
        attack += item.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
        return attack;
    }
}
