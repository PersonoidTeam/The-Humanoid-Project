package com.personoid.humanoid.activites.location;

import com.personoid.api.activities.GoToLocationActivity;
import com.personoid.api.ai.activity.Activity;
import com.personoid.api.utils.math.Range;
import com.personoid.api.utils.types.Priority;
import com.personoid.humanoid.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class WanderActivity extends Activity {
    private final Range range;

    public WanderActivity() {
        range = new Range(10, 25);
    }

    public WanderActivity(Range range) {
        this.range = range;
    }

    @Override
    public void onStart(StartType startType) {
        goToNewLocation();
    }

    private void goToNewLocation() {
        Location loc = LocationUtils.validRandom(getNPC().getLocation(), range, 0.85F);
        Bukkit.broadcastMessage("Going to " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        GoToLocationActivity goTo = new GoToLocationActivity(loc, GoToLocationActivity.MovementType.SPRINT_JUMP);
        goTo.onFinished(result -> goToNewLocation());
        goTo.getOptions().setStoppingDistance(3);
        run(goTo);
        //getNPC().getLookController().getTargets().forEach((key, target) -> Bukkit.broadcastMessage(key + ": " + target.getLocation()));
    }

    @Override
    public void onUpdate() {
        Bukkit.broadcastMessage("On Ground: " + getNPC().isOnGround());
        Bukkit.broadcastMessage("In Water: " + getNPC().isInWater());
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
        return Priority.LOWEST;
    }

    @Override
    public BoredomSettings getBoredomSettings() {
        return null;
    }
}
