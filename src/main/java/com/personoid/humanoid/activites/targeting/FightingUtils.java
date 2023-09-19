package com.personoid.humanoid.activites.targeting;

import com.google.common.collect.Multimap;
import com.personoid.api.utils.Parameter;
import com.personoid.api.utils.bukkit.Logger;
import com.personoid.humanoid.values.ArmorItemValues;
import com.personoid.nms.NMS;
import com.personoid.nms.mappings.NMSClass;
import com.personoid.nms.mappings.NMSMethod;
import com.personoid.nms.packet.Package;
import com.personoid.nms.packet.Packages;
import com.personoid.nms.packet.NMSReflection;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;

public class FightingUtils {
    public static int getAttackSpeed(ItemStack item) {
        if (item == null) return 4;
        Object nmsItem = NMSReflection.getNMSItemStack(item);

        NMSClass equipmentSlotClass = Package.ENTITY.sub("EquipmentSlot").getMappedClass();
        Object equipmentSlot = equipmentSlotClass.getField("MAINHAND").getStaticValue();

        NMSClass itemClass = Package.ITEM.sub("ItemStack").getMappedClass();
        NMSMethod getAttrModMethod = itemClass.getMethod("getAttributeModifiers", equipmentSlotClass.getRawClass());
        Multimap<Object, Object> map = getAttrModMethod.invoke(nmsItem, equipmentSlot);

        NMSClass attributesClass = Package.ENTITY.sub("ai.attributes.Attributes").getMappedClass();
        Object attribute = attributesClass.getField("ATTACK_SPEED").getStaticValue();

        NMSClass attributeClass = Package.ENTITY.sub("ai.attributes.AttributeModifier").getMappedClass();
        NMSMethod getAmountMethod = attributeClass.getMethod("getAmount");

        double speed = map.get(attribute).stream().mapToDouble(getAmountMethod::invoke).sum();
        //Bukkit.broadcastMessage(item.getType().name() + " attack damage: " + damage);
        return (int) (1 / (4 + speed) * 20);
    }

    public static double getAttackDamage(ItemStack item) {
        if (item == null) return 0.5F;
        Object nmsItem = NMSReflection.getNMSItemStack(item);

        NMSClass equipmentSlotClass = Package.ENTITY.sub("EquipmentSlot").getMappedClass();
        Object equipmentSlot = equipmentSlotClass.getField("MAINHAND").getStaticValue();

        NMSClass itemClass = Package.ITEM.sub("ItemStack").getMappedClass();
        NMSMethod getAttrModMethod = itemClass.getMethod("getAttributeModifiers", equipmentSlotClass.getRawClass());
        Multimap<Object, Object> map = getAttrModMethod.invoke(nmsItem, equipmentSlot);

        NMSClass attributesClass = Package.ENTITY.sub("ai.attributes.Attributes").getMappedClass();
        Object attribute = attributesClass.getField("ATTACK_DAMAGE").getStaticValue();

        NMSClass attributeClass = Package.ENTITY.sub("ai.attributes.AttributeModifier").getMappedClass();
        NMSMethod getAmountMethod = attributeClass.getMethod("getAmount");

        double damage = map.get(attribute).stream().mapToDouble(getAmountMethod::invoke).sum();
        //Bukkit.broadcastMessage(item.getType().name() + " attack damage: " + damage);
        return damage;
    }

    public static void playCriticalHitEffect(Location location) {
        // particle type, ?, position, offset/range, speed, number of particles
        NMSClass particleTypesClass = Package.minecraft("core.particles.ParticleTypes").getMappedClass();
        NMSClass particleParamClass = Package.minecraft("core.particles.ParticleOptions").getMappedClass();

        Object critParticle = particleTypesClass.getField("CRIT").getStaticValue();

        Parameter param1 = new Parameter(particleParamClass.getRawClass(), critParticle);
        Parameter param2 = new Parameter(boolean.class, false);
        Parameter param3 = new Parameter(double.class, location.getX());
        Parameter param4 = new Parameter(double.class, location.getY());
        Parameter param5 = new Parameter(double.class, location.getZ());
        Parameter param6 = new Parameter(float.class, .5F);
        Parameter param7 = new Parameter(float.class, .4F);
        Parameter param8 = new Parameter(float.class, .5F);
        Parameter param9 = new Parameter(float.class, 0F);
        Parameter param10 = new Parameter(int.class, 15);

        NMS.createPacket("PacketPlayOutWorldParticles", param1, param2, param3, param4, param5,
                param6, param7, param8, param9, param10).send();
        location.getWorld().playSound(location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
    }

    public static boolean isSimilar(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) return false;
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
