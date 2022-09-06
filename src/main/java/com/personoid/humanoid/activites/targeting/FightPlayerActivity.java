package com.personoid.humanoid.activites.targeting;

import com.personoid.api.activities.GoToLocationActivity;
import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.ai.looking.Target;
import com.personoid.api.ai.movement.MovementType;
import com.personoid.api.utils.Result;
import com.personoid.api.utils.types.HandEnum;
import com.personoid.api.utils.types.Priority;
import com.personoid.humanoid.Humanoid;
import com.personoid.humanoid.utils.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class FightPlayerActivity extends Activity {
    private final Player player;
    private int attackCooldown;
    private boolean retreating;
    private double lastHealth;
    private double highestDamageTaken;

    public FightPlayerActivity(Player player, AttackType attackType, Strategy strategy) {
        super(ActivityType.FIGHTING, Priority.HIGHEST);
        this.player = player;
    }

    @Override
    public void onStart(StartType startType) {
        getNPC().getLookController().addTarget("fight_target", new Target(player, Priority.HIGHEST).track());
        getNPC().getNPCInventory().addItem(new ItemStack(Material.IRON_AXE));
        getNPC().getNPCInventory().setOffhand(new ItemStack(Material.SHIELD));
        switchToItem(getBestWeapon());
        attackCooldown = FightingUtils.getAttackSpeed(getNPC().getNPCInventory().getSelectedItem());
        player.hidePlayer(Humanoid.getPlugin(), getNPC().getEntity());
        player.showPlayer(Humanoid.getPlugin(), getNPC().getEntity());
    }

    public enum AttackType {
        MELEE,
        RANGED,
        MAGIC,
        ALL
    }

    public enum Strategy {
        OFFENSIVE,
        DEFENSIVE,
        MIXED
    }

    private ItemStack getBestWeapon() {
        ItemStack bestWeapon = null;
        double bestDamage = 0;
        for (ItemStack itemStack : getNPC().getNPCInventory().getHotbar()) {
            if (itemStack == null) continue;
            double damage = FightingUtils.getAttackDamage(itemStack);
            if (damage > bestDamage) {
                bestDamage = damage;
                bestWeapon = itemStack;
            }
        }
        return bestWeapon;
    }

    private int switchToItem(ItemStack itemStack) {
        for (int i = 0; i < 9; i++) {
            ItemStack item = getNPC().getNPCInventory().getHotbar()[i];
            if (item != null && item.isSimilar(itemStack)) {
                getNPC().getNPCInventory().select(i);
                return i;
            }
        }
        return -1;
    }

    private boolean checkWinStatus() {
        if (player.isDead()) {
            markAsFinished(new Result<>(Result.Type.SUCCESS, player));
            return true;
        }
        return false;
    }

    private final int lowHealthValue = MathUtils.random(2, 7);
    private final double highHealthMod = MathUtils.random(0.6, 1);
    private final int retreatEndCooldown = MathUtils.random(3 * 20, 7 * 20);
    private int retreatEndTimer;

    private boolean shouldRetreat() {
        // TODO: get best weapon used by player rather than one currently holding
        Bukkit.broadcastMessage("highestDamageTaken: " + highestDamageTaken);
        boolean tooClose = getNPC().getEntity().getLocation().distance(player.getLocation()) < 4.2;
        boolean lowHealth = getNPC().getEntity().getHealth() - Math.min(highestDamageTaken, 19) < lowHealthValue;
        boolean highHealth = getNPC().getEntity().getHealth() > getNPC().getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * highHealthMod;
        Bukkit.broadcastMessage("lowHealth: " + lowHealth);
        Bukkit.broadcastMessage("highHealth: " + highHealth);
        Bukkit.broadcastMessage("tooClose: " + tooClose);
        Bukkit.broadcastMessage("retreat end timer: " + MathUtils.round(retreatEndTimer, 2) + " / " + MathUtils.round(retreatEndCooldown, 2));
        return retreating ? !highHealth : lowHealth && (retreatEndTimer <= retreatEndCooldown || !tooClose);
    }

    @Override
    public void onUpdate() {
        if (checkWinStatus()) return;
        if (lastHealth - getNPC().getEntity().getHealth() > highestDamageTaken) {
            highestDamageTaken = lastHealth - getNPC().getEntity().getHealth();
        }
        double distance = getNPC().getLocation().distance(player.getLocation());
        boolean retreating = shouldRetreat();
        if (retreating) retreatEndTimer = this.retreating ? retreatEndTimer + 1 : 1;
        this.retreating = retreating;
        Bukkit.broadcastMessage("retreating: " + retreating);
        Vector direction = player.getLocation().toVector().subtract(getNPC().getLocation().toVector()).normalize();
        Location retreatLoc = getNPC().getLocation().clone().add(direction.multiply(-1));
        Location targetLoc = retreating ? retreatLoc : player.getLocation();

        MovementType movementType = distance > 4 || retreating ? MovementType.SPRINT_JUMPING : MovementType.SPRINTING;
        getNPC().getLookController().addTarget("fight_target", new Target(targetLoc, Priority.HIGHEST).track());
        run(new GoToLocationActivity(targetLoc, movementType));

        int attackSpeed = FightingUtils.getAttackSpeed(getNPC().getNPCInventory().getSelectedItem());
        if (attackCooldown <= 0) {
            if (!retreating && distance < 3.25) {
                getNPC().swingHand(HandEnum.RIGHT);
                double damage = getNPC().getEntity().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
                if (getNPC().getMoveController().getVelocity().getY() < 0) {
                    damage *= 1.5;
                    FightingUtils.playCriticalHitEffect(player.getLocation());
                }
                player.damage(damage, getNPC().getEntity());
            }
            attackCooldown = attackSpeed;
        } else {
            if (attackCooldown < 10) getNPC().getMoveController().jump();
            boolean shieldDisabled = getNPC().getItemCooldown(Material.SHIELD) > 0;
            if (attackCooldown > 2 && attackCooldown < attackSpeed - 5 && !shieldDisabled && distance < 4) getNPC().beginUsingItem(HandEnum.LEFT);
            else getNPC().endUsingItem();
            attackCooldown--;
        }
        lastHealth = getNPC().getEntity().getHealth();
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
