package com.personoid.humanoid.activites.location;

import com.personoid.api.activities.GoToLocationActivity;
import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.utils.Result;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.humanoid.structure.Structure;
import com.personoid.humanoid.structure.detection.StructureLocator;
import com.personoid.humanoid.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class FindStructureActivity extends Activity {
    private final StructureLocator locator;
    private final List<Structure> exclusions;
    private final int maxAttempts;
    private final int travelRadius;

    private final List<Location> attempted = new ArrayList<>();

    public FindStructureActivity(StructureLocator locator, List<Structure> exclusions, int travelRadius, int maxAttempts) {
        super(ActivityType.SEARCHING);
        this.locator = locator;
        this.exclusions = exclusions;
        this.travelRadius = travelRadius;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public void onStart(StartType startType) {
        attempted.clear(); // acts like a "try again after a break"
        if (startType == StartType.START) checkLocation();
    }

    private void checkLocation() {
        if (attempted.size() >= maxAttempts) markAsFinished(new Result<>(Result.Type.FAILURE, attempted));
        // search for structure that matches the reference
        Structure structure = locator.locateStructure(getNPC().getLocation(), exclusions);
        if (structure != null) {
            markAsFinished(new Result<>(Result.Type.SUCCESS, structure)); // found the structure -> return result
            return;
        }
        Location npcLoc = getNPC().getLocation().clone();
        Location travelLoc = npcLoc.add(MathUtils.random(-travelRadius, travelRadius), 0, MathUtils.random(-travelRadius, travelRadius));
        double yLoc = LocationUtils.getAirInDir(travelLoc.subtract(0, 1, 0), BlockFace.UP).getLocation().getY();
        travelLoc = new Location(travelLoc.getWorld(), travelLoc.getX(), yLoc, travelLoc.getZ());
        Bukkit.broadcastMessage("Couldn't find " + locator.getReference().getId() + ", going to " + LocationUtils.toStringBasic(travelLoc));
        attempted.add(travelLoc);
        GoToLocationActivity goTo = new GoToLocationActivity(travelLoc, GoToLocationActivity.MovementType.SPRINT);
        goTo.onFinished((result) -> {
            if (result.getType() == Result.Type.FAILURE) Bukkit.broadcastMessage("Couldn't get to target destination");
            checkLocation();
        });
        goTo.getOptions().setStoppingDistance(3);
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
