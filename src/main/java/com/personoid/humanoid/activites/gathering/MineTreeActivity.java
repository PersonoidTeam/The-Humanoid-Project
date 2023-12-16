package com.personoid.humanoid.activites.gathering;

import com.personoid.api.activities.BreakBlockActivity;
import com.personoid.api.activities.GoToLocationActivity;
import com.personoid.api.ai.activity.Activity;
import com.personoid.api.utils.Result;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.math.Range;
import com.personoid.api.utils.types.Priority;
import com.personoid.humanoid.activites.location.FindStructureActivity;
import com.personoid.humanoid.material.GenericMaterial;
import com.personoid.humanoid.material.filters.NameMaterialFilter;
import com.personoid.humanoid.structure.Structure;
import com.personoid.humanoid.structure.StructureRef;
import com.personoid.humanoid.structure.detection.StructureLocator;
import com.personoid.humanoid.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class MineTreeActivity extends Activity {
    private final StructureLocator locator;
    private final int retryTime;

    private Structure tree;
    private List<Block> logs;
    private int currentLogIndex;
    private final List<Structure> exclusions = new ArrayList<>();
    private int retryTimer;
    private boolean findingTree;

    public MineTreeActivity(StructureRef type, int retryTime) {
        locator = new StructureLocator(type, StructureLocator.SearchType.CLOSEST, new Range(-20, 20));
        this.retryTime = retryTime;
    }

    @Override
    public void onStart(StartType startType) {
        if (startType == StartType.START) tryFindTree();
    }

    private void tryFindTree() {
        findingTree = true;
        run(new FindStructureActivity(locator, exclusions, 50, 8).onFinished((result) -> {
            if (result.getType() == Result.Type.SUCCESS) {
                tree = result.getResult(Structure.class);
                Bukkit.broadcastMessage("Found tree at: " + LocationUtils.toStringBasic(tree.getOrigin().getLocation()));
                logs = tree.getBlocksFrom(new GenericMaterial("log", new NameMaterialFilter("log")), Structure.Direction.UP);
                currentLogIndex = 0;
                mineLogFromTree(logs.get(currentLogIndex));
            }
        }));
    }

    private void mineLogFromTree(Block block) {
        Location pathableLoc = LocationUtils.getPathableLocation(getNPC().getLocation(), block.getLocation(), 5);
/*        Bukkit.broadcastMessage("--- MINELOGFROMTREE - currentLogIndex: " + currentLogIndex);
        Bukkit.broadcastMessage("--- MINELOGFROMTREE - logs.size(): " + logs.size());
        Bukkit.broadcastMessage("--- MINELOGFROMTREE - Mining log at: " + LocationUtils.toStringBasic(block.getLocation()));
        Bukkit.broadcastMessage("--- MINELOGFROMTREE - Pathable loc: " + LocationUtils.toStringBasic(pathableLoc));*/
        if (pathableLoc == null) {
            Bukkit.broadcastMessage("Can't path to tree. Waiting before retrying...");
            exclusions.add(tree);
            retryTimer = retryTime;
            findingTree = false;
            return;
        }
        GoToLocationActivity goTo = new GoToLocationActivity(pathableLoc, GoToLocationActivity.MovementType.SPRINT);
        goTo.onFinished((result) -> {
            Bukkit.broadcastMessage("Mining log at: " + LocationUtils.toStringBasic(block.getLocation()));
            if (result.getType() == Result.Type.SUCCESS) {
                Bukkit.broadcastMessage("Successfully travelled to log");
                getNPC().getBlocker().mine(block, true, result1 -> {
                    if (result1.getType() == Result.Type.SUCCESS) {
                        Bukkit.broadcastMessage("Mined log successfully!");
                        currentLogIndex++;
                        if (currentLogIndex < logs.size()) {
                            Bukkit.broadcastMessage("Going to next log...");
                            mineLogFromTree(logs.get(currentLogIndex));
                        } else {
                            Bukkit.broadcastMessage("Finished mining tree!");
                            markAsFinished(new Result<>(Result.Type.SUCCESS, block));
                        }
                    } else {
                        Bukkit.broadcastMessage("Too far away from log");
                        exclusions.add(tree);
                        tryFindTree();
                    }
                });
            } else {
                Bukkit.broadcastMessage("Couldn't travel to log");
                exclusions.add(tree);
                tryFindTree();
            }
        });
        goTo.getOptions().setFaceLocation(true, Priority.NORMAL);
        goTo.getOptions().setStoppingDistance(1);
        //getNPC().getLookController().addTarget("mine_tree_log", new Target(block, Priority.HIGH));
        Bukkit.broadcastMessage("Going to target location: " + LocationUtils.toStringBasic(pathableLoc));
        run(goTo);
    }

    @Override
    public void onUpdate() {
        if (retryTimer > 0) {
            retryTimer--;
        } else if (!findingTree) {
            Bukkit.broadcastMessage("Retrying to find tree...");
            tryFindTree();
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
        return Priority.NORMAL;
    }

    @Override
    public BoredomSettings getBoredomSettings() {
        return new BoredomSettings(MathUtils.random(1200, 6000), MathUtils.random(3600, 12000));
    }
}
