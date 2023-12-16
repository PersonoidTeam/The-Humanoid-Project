package com.personoid.humanoid.activites.location;

import com.personoid.api.activities.GoToLocationActivity;
import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.looking.target.EntityTarget;
import com.personoid.api.pathfinding.calc.Path;
import com.personoid.api.pathfinding.calc.goal.BlockGoal;
import com.personoid.api.pathfinding.calc.node.Node;
import com.personoid.api.pathfinding.calc.pathfinder.PathFinder;
import com.personoid.api.pathfinding.calc.utils.BlockPos;
import com.personoid.api.utils.LocationUtils;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.types.Priority;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class FollowEntityActivity extends Activity {
    private static final int CHECK_RATE = 5;
    private static final int CHECK_DISTANCE = 1;

    private final Entity entity;
    private final double stoppingDistance;

    private Location lastCheckLoc;

    public FollowEntityActivity(Entity entity) {
        this.entity = entity;
        this.stoppingDistance = 2;
    }

    public FollowEntityActivity(Entity entity, double stoppingDistance) {
        this.entity = entity;
        this.stoppingDistance = stoppingDistance;
    }

    @Override
    public void onStart(StartType startType) {
        getNPC().getLookController().addTarget("follow_target", new EntityTarget(entity));
    }

    @Override
    public void onUpdate() {
        if (getCurrentDuration() % CHECK_RATE == 0) {
            //Location endLoc = LocationUtils.getBlockInDir(entity.getLocation().clone(), BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
            GoToLocationActivity goTo = new GoToLocationActivity(entity.getLocation(), GoToLocationActivity.MovementType.SPRINT_JUMP);
            goTo.getOptions().setStoppingDistance(stoppingDistance);
            goTo.getOptions().setFaceLocation(false, Priority.NORMAL);
            lastCheckLoc = entity.getLocation();
            run(goTo);
/*            boolean deltaDistanceCheck = lastCheckLoc == null || lastCheckLoc.distance(entity.getLocation()) > CHECK_DISTANCE;
            boolean stoppingDistanceCheck = getNPC().getLocation().distance(entity.getLocation()) > this.stoppingDistance;
            Vector vel = getNPC().getMoveController().getVelocity();
            boolean movingCheck = Math.abs(vel.getX()) > 0.2 || Math.abs(vel.getY()) > 0.2 || Math.abs(vel.getZ()) > 0.2;
            if (deltaDistanceCheck || (stoppingDistanceCheck && movingCheck)) {
                Location endLoc = LocationUtils.getBlockInDir(entity.getLocation().clone(), BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
                GoToLocationActivity goTo = new GoToLocationActivity(endLoc, GoToLocationActivity.MovementType.SPRINT_JUMP);
                goTo.getOptions().setStoppingDistance(stoppingDistance);
                goTo.getOptions().setFaceLocation(false, Priority.NORMAL);
                lastCheckLoc = entity.getLocation();
                run(goTo);
            }
            Location startLoc = LocationUtils.getBlockInDir(getNPC().getLocation().clone(), BlockFace.DOWN).getRelative(BlockFace.UP).getLocation();
            PathFinder pathfinder = getNPC().getNavigation().getPathfinder();
            Path currentPath = getNPC().getNavigation().getCurrentPath();
            Path newPath = pathfinder.findPath(BlockPos.fromLocation(startLoc), new BlockGoal(BlockPos.fromLocation(entity.getLocation())), entity.getWorld());

            boolean samePath = false;
            if (currentPath != null) {
                boolean firstNode = true;
                for (Node node : newPath.getNodes()) {
                    if (currentPath.getNextNodeIndex() >= currentPath.size()) continue;
                    if (node.equals(currentPath.getNode(currentPath.getNextNodeIndex())) && !firstNode) {
                        samePath = true;
                        break;
                    }
                    firstNode = false;
                }
            }

            if (!samePath) {
                GoToLocationActivity goTo = new GoToLocationActivity(entity.getLocation(), newPath, GoToLocationActivity.MovementType.SPRINT_JUMP){
                    @Override
                    public void onStuck() {
                        super.onStuck();
                        Bukkit.broadcastMessage("Stuck! Teleporting to entity...");
                    }
                };
                goTo.getOptions().setStoppingDistance(stoppingDistance);
                goTo.getOptions().setFaceLocation(false, Priority.NORMAL);
                //goTo.getOptions().setStuckAction(GoToLocationActivity.StuckAction.TELEPORT);
                run(goTo);
            }*/
        }
    }

    @Override
    public void onStop(StopType stopType) {

    }

    @Override
    public boolean canStart(StartType startType) {
        return true;
    }

    @Override
    public boolean canStop(StopType stopType) {
        return true;
    }

    @Override
    public Priority getPriority() {
        return Priority.LOW;
    }

    @Override
    public BoredomSettings getBoredomSettings() {
        return new BoredomSettings(MathUtils.random(600, 2400), MathUtils.random(2400, 12000));
    }
}
