package com.personoid.humanoid.activites.targeting;

import com.google.common.collect.Multimap;
import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.ai.looking.Target;
import com.personoid.api.utils.Result;
import com.personoid.api.utils.packet.ReflectionUtils;
import com.personoid.api.utils.types.HandEnum;
import com.personoid.api.utils.types.Priority;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.Material;
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
        getNPC().getNPCInventory().addItem(new ItemStack(Material.NETHERITE_SWORD));
        switchToItem(Material.NETHERITE_SWORD);
        attackCooldown = getAttackSpeed(getNPC().getNPCInventory().getSelectedItem());
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
        if (attackCooldown <= 0) {
            getNPC().swingHand(HandEnum.RIGHT);
            attackCooldown = getAttackSpeed(getNPC().getNPCInventory().getSelectedItem());
        } else attackCooldown--;
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
