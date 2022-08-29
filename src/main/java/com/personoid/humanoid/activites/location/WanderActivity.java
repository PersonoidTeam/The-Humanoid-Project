package com.personoid.humanoid.activites.location;

import com.personoid.api.activities.GoToLocationActivity;
import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.ai.movement.MovementType;
import com.personoid.api.utils.math.Range;
import com.personoid.api.utils.types.Priority;
import com.personoid.humanoid.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class WanderActivity extends Activity {
    private final Range range;

    public WanderActivity() {
        super(ActivityType.WANDERING, Priority.LOWEST);
        range = new Range(10, 25);
    }

    public WanderActivity(Range range) {
        super(ActivityType.WANDERING, Priority.LOWEST);
        this.range = range;
    }

    @Override
    public void onStart(StartType startType) {
        goToNewLocation();
    }

    private void goToNewLocation() {
        Location loc = LocationUtils.validRandom(getNPC().getLocation(), range, 0.85F);
        Bukkit.broadcastMessage("Going to " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        GoToLocationActivity.Options options = new GoToLocationActivity.Options();
        options.setStoppingDistance(3);
        run(new GoToLocationActivity(loc, MovementType.SPRINTING, options).onFinished(result -> goToNewLocation()));
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
