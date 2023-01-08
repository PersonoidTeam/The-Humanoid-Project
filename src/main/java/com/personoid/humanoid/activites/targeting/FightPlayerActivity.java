package com.personoid.humanoid.activites.targeting;

import com.personoid.api.activities.GoToLocationActivity;
import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.ai.looking.Target;
import com.personoid.api.utils.Result;
import com.personoid.api.utils.types.HandEnum;
import com.personoid.api.utils.types.Priority;
import com.personoid.humanoid.Humanoid;
import com.personoid.humanoid.utils.LocationUtils;
import com.personoid.humanoid.utils.MathUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class FightPlayerActivity extends Activity {
    private Player player;
    private final AttackType attackType;
    private final Strategy strategy;
    private int attackCooldown;
    private boolean retreating;
    private double lastHealth;
    private double highestDamageTaken;

    public FightPlayerActivity(Player player, AttackType attackType, Strategy strategy) {
        super(ActivityType.FIGHTING, Priority.HIGHEST);
        this.player = player;
        this.attackType = attackType;
        this.strategy = strategy;
    }

    @Override
    public void onStart(StartType startType) {
        getNPC().getLookController().addTarget("fight_target", new Target(player, Priority.HIGHEST));
        getNPC().getInventory().addItem(new ItemStack(Material.STONE_AXE));
        getNPC().getInventory().setOffhand(new ItemStack(Material.SHIELD));
        switchToItem(getBestWeapon());
        attackCooldown = FightingUtils.getAttackSpeed(getNPC().getInventory().getSelectedItem());
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
        for (ItemStack itemStack : getNPC().getInventory().getHotbar()) {
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
            ItemStack item = getNPC().getInventory().getHotbar()[i];
            if (item != null && FightingUtils.isSimilar(itemStack, item)) {
                getNPC().getInventory().select(i);
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

    private final int lowHealthValue = MathUtils.random(2, 5);
    private final double highHealthMod = MathUtils.random(0.5, 0.8);
    private final int retreatEndCooldown = MathUtils.random(3 * 20, 5 * 20);
    private int retreatEndTimer;

    private boolean shouldRetreat() {
        double maxHealth = getNPC().getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double health = getNPC().getEntity().getHealth() - Math.min(highestDamageTaken, maxHealth - 1);

        boolean tooClose = getNPC().getLocation().distance(player.getLocation()) < 3;
        boolean lowHealth = health < lowHealthValue;
        boolean highHealth = health > maxHealth * highHealthMod;

/*        Bukkit.broadcastMessage("highestDamageTaken: " + MathUtils.round(highestDamageTaken, 2));
        Bukkit.broadcastMessage("lowHealth: " + lowHealth);
        Bukkit.broadcastMessage("highHealth: " + highHealth);
        Bukkit.broadcastMessage("tooClose: " + tooClose);
        Bukkit.broadcastMessage("retreat end timer: " + retreatEndTimer + " / " + retreatEndCooldown);*/

        if (retreating) {
            if (highHealth) {
                return false;
            } else {
                return tooClose || retreatEndTimer < retreatEndCooldown;
            }
        } else return lowHealth;
    }

    private int offset = MathUtils.random(-10, 0);

    @Override
    public void onUpdate() {
        if (checkWinStatus()) return;
        player = LocationUtils.getClosestPlayer(getNPC().getLocation());

        if (lastHealth - getNPC().getEntity().getHealth() > highestDamageTaken) {
            highestDamageTaken = lastHealth - getNPC().getEntity().getHealth();
        }
        double distance = getNPC().getLocation().distance(player.getLocation());
        boolean retreating = strategy != Strategy.OFFENSIVE && shouldRetreat();
        if (retreating) retreatEndTimer = this.retreating ? retreatEndTimer + 1 : 1;
        this.retreating = retreating;
        //Bukkit.broadcastMessage("retreating: " + retreating);
        Vector direction = player.getLocation().toVector().subtract(getNPC().getLocation().toVector()).normalize();
        Location retreatLoc = getNPC().getLocation().clone().add(direction.multiply(-5));
        Location targetLoc = retreating ? retreatLoc : player.getLocation();

        GoToLocationActivity.MovementType movementType = distance > 4 || retreating ?
                GoToLocationActivity.MovementType.SPRINT_JUMP : GoToLocationActivity.MovementType.SPRINT;
        getNPC().getLookController().addTarget("fight_target", new Target(targetLoc, Priority.HIGHEST));
        GoToLocationActivity goTo = new GoToLocationActivity(targetLoc, movementType);
        goTo.getOptions().setStoppingDistance(0.5F);
        goTo.getOptions().setFaceLocation(false);
        run(goTo);

        int attackSpeed = FightingUtils.getAttackSpeed(getNPC().getInventory().getSelectedItem());
        if (attackCooldown <= offset) { // TODO: is entity not occluded by blocks?
            if (!retreating && distance < 3.25) {
                getNPC().swingHand(HandEnum.RIGHT);
                double damage = getNPC().getEntity().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
                if (getNPC().getMoveController().getVelocity().getY() < 0) {
                    damage *= 1.5F;
                    if (FightingUtils.inSurvival(player)) {
                        FightingUtils.playCriticalHitEffect(player.getLocation());
                    }
                }
                player.damage(damage, getNPC().getEntity());
            }
            attackCooldown = attackSpeed;
            offset = MathUtils.random(-10, 0);
        } else {
            if (attackCooldown < 10 + offset) getNPC().getMoveController().jump();
            boolean shieldDisabled = getNPC().getItemCooldown(Material.SHIELD) > 0;
            if (attackCooldown > 2 + offset && attackCooldown < attackSpeed - 5 && !shieldDisabled && distance < 4) getNPC().startUsingItem(HandEnum.LEFT);
            else getNPC().stopUsingItem();
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
