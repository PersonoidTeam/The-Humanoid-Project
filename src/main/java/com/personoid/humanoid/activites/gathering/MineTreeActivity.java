package com.personoid.humanoid.activites.gathering;

import com.personoid.api.activities.BreakBlockActivity;
import com.personoid.api.activities.GoToLocationActivity;
import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.utils.Result;
import com.personoid.humanoid.activites.location.FindStructureActivity;
import com.personoid.humanoid.utils.GenericMaterial;
import com.personoid.humanoid.utils.LocationUtils;
import com.personoid.humanoid.utils.Structure;
import com.personoid.humanoid.values.StructureType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;

public class MineTreeActivity extends Activity {
    private List<Block> logs;
    private int currentLogIndex = 0;

    public MineTreeActivity() {
        super(ActivityType.GATHERING);
    }

    @Override
    public void onStart(StartType startType) {
        if (startType == StartType.START) tryFindTree();
    }

    private void tryFindTree() {
        run(new FindStructureActivity(StructureType.TREE, 50, 20,
                FindStructureActivity.SearchType.CLOSEST, 8).onFinished((result) -> {
            if (result.getType() == Result.Type.SUCCESS) {
                Structure tree = result.getResult(Structure.class);
                Bukkit.broadcastMessage("Found tree at: " + LocationUtils.toStringBasic(tree.getOrigin().getLocation()));
                logs = tree.getBlocksFrom(new GenericMaterial("log"), Structure.Direction.UP);
                currentLogIndex = 0;
                mineLogFromTree(logs.get(currentLogIndex));
                Bukkit.broadcastMessage("mining tree");
            }
        }));
    }

    private void mineLogFromTree(Block block) {
        Location pathableLoc = LocationUtils.getPathableLocation(block.getLocation(), 5);
        Bukkit.broadcastMessage("--- MINELOGFROMTREE - currentLogIndex: " + currentLogIndex);
        Bukkit.broadcastMessage("--- MINELOGFROMTREE - logs.size(): " + logs.size());
        Bukkit.broadcastMessage("--- MINELOGFROMTREE - Mining log at: " + LocationUtils.toStringBasic(block.getLocation()));
        Bukkit.broadcastMessage("--- MINELOGFROMTREE - Pathable loc: " + LocationUtils.toStringBasic(pathableLoc));
        GoToLocationActivity goTo = new GoToLocationActivity(pathableLoc, GoToLocationActivity.MovementType.SPRINT) {
            @Override
            public void onStart(StartType startType) {
                super.onStart(startType);
                Bukkit.broadcastMessage("--- MINELOGFROMTREE - onStart");
            }
        };
        goTo.onFinished((result) -> {
            //Bukkit.broadcastMessage("Mining log at: " + LocationUtils.toStringBasic(block.getLocation()));
            if (result.getType() == Result.Type.SUCCESS) {
                Bukkit.broadcastMessage("--- MINELOGFROMTREE - successfully travelled to log");
                run(new BreakBlockActivity(block).onFinished((result1) -> {
                    if (result1.getType() == Result.Type.SUCCESS) {
                        Bukkit.broadcastMessage("Mined log successfully!");
                        currentLogIndex++;
                        if (currentLogIndex < logs.size()) {
                            Bukkit.broadcastMessage("Going to next log...");
                            mineLogFromTree(logs.get(currentLogIndex));
                        } else {
                            Bukkit.broadcastMessage("Finished mining tree!");
                            markAsFinished(new Result<>(Result.Type.SUCCESS, block));
                            tryFindTree();
                        }
                    } else {
                        Bukkit.broadcastMessage("Too far away from log");
                        markAsFinished(new Result<>(Result.Type.FAILURE, block));
                    }
                }));
            } else tryFindTree();
        });
        //goTo.getOptions().setFaceLocation(false);
        //getNPC().getLookController().addTarget("mine_tree_log", new Target(block, Priority.HIGH));
        goTo.getOptions().setStoppingDistance(1F);
        Bukkit.broadcastMessage("Going to target location: " + LocationUtils.toStringBasic(pathableLoc));
        run(goTo);
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
