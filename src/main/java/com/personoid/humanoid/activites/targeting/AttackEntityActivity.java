package com.personoid.humanoid.activites.targeting;

import com.google.common.collect.Multimap;
import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.utils.packet.NMSUtils;
import com.personoid.api.utils.types.Priority;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;

public class AttackEntityActivity extends Activity {
    private final LivingEntity entity;

    public AttackEntityActivity(LivingEntity entity) {
        super(ActivityType.FIGHTING, Priority.HIGHEST);
        this.entity = entity;
    }

    @Override
    public void onStart(StartType startType) {
        getNPC().getNPCInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
/*        for (ItemStack itemStack : getNPC().getNPCInventory().getContents()) {
            if (itemStack != null && itemStack.getType() == Material.DIAMOND_SWORD) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier("generic.attackDamage", 100, AttributeModifier.Operation.ADD_NUMBER));
                itemStack.setItemMeta(itemMeta);
            }
        }*/
        getNPC().getNPCInventory().select(0);
        for (int i = 0; i < getNPC().getNPCInventory().getHotbar().size(); i++) {
            Bukkit.broadcastMessage(i + ": " + getNPC().getNPCInventory().getHotbar().get(i).getType().name());
        }
        //getNPC().getEntity().getAttackCooldown();
        ItemStack hand = getNPC().getEntity().getEquipment().getItemInMainHand();
        Bukkit.broadcastMessage(hand.getType().name());
        Bukkit.broadcastMessage("Attack speed: " + getAttackSpeed(hand));
        ItemMeta meta = hand.getItemMeta();
        if (meta != null) {
            Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE);
            if (modifiers != null) modifiers.forEach(attributeModifier -> Bukkit.broadcastMessage(attributeModifier.getName()));
        }
    }

    private int getAttackSpeed(ItemStack item) {
        Multimap<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> attributes =
                NMSUtils.getItemStack(item).getAttributeModifiers(EquipmentSlot.MAINHAND);
        return attributes.get(Attributes.ATTACK_SPEED).stream().mapToInt(attributeModifier -> (int) (attributeModifier.getAmount())).sum();
    }

    @Override
    public void onUpdate() {

    }

    @Override
    public void onStop(StopType stopType) {

    }

    @Override
    public boolean canStart(StartType startType) {
        return shouldAttack();
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
