package com.personoid.humanoid.activites.gathering;

import com.personoid.api.activities.BreakBlockActivity;
import com.personoid.api.activities.GoToLocationActivity;
import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.ai.movement.MovementType;
import com.personoid.api.utils.Result;
import com.personoid.humanoid.activites.location.FindStructureActivity;
import com.personoid.humanoid.utils.LocationUtils;
import com.personoid.humanoid.values.StructureType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class MineTreeActivity extends Activity {
    public MineTreeActivity() {
        super(ActivityType.GATHERING);
    }

    @Override
    public void onStart(StartType startType) {
        if (startType == StartType.START) tryFindTree();
    }

    private void tryFindTree() {
        run(new FindStructureActivity(StructureType.TREE, 50, 20, 8).onFinished((result) -> {
            if (result.getType() == Result.Type.SUCCESS) {
                Block tree = result.getResult(Block.class);
                Bukkit.broadcastMessage("Found tree at: " + LocationUtils.toStringBasic(tree.getLocation()));
                mineTree(tree);
            }
        }));
    }

    private void mineTree(Block block) {
        Location pathableLoc = LocationUtils.getPathableLocation(block.getLocation(), getNPC().getLocation());
        GoToLocationActivity.Options options = new GoToLocationActivity.Options();
        options.setStoppingDistance(3);
        Bukkit.broadcastMessage("Going to target location: " + LocationUtils.toStringBasic(pathableLoc));
        run(new GoToLocationActivity(pathableLoc, MovementType.SPRINTING, options).onFinished((result) -> {
            Bukkit.broadcastMessage("Mining tree at: " + LocationUtils.toStringBasic(block.getLocation()));
            if (result.getType() == Result.Type.SUCCESS) {
                run(new BreakBlockActivity(block).onFinished((result1) -> {
                    if (result1.getType() == Result.Type.SUCCESS) {
                        Bukkit.broadcastMessage("Mined tree successfully!");
                        markAsFinished(new Result<>(Result.Type.SUCCESS, block));
                    } else {
                        Bukkit.broadcastMessage("Too far away from tree");
                        markAsFinished(new Result<>(Result.Type.FAILURE, block));
                    }
                }));
            } else tryFindTree();
        }));
    }

    @Override
    public void onUpdate() {

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
}
