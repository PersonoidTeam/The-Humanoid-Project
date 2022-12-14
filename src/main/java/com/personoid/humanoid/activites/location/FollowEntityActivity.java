package com.personoid.humanoid.activites.location;

import com.personoid.api.activities.GoToLocationActivity;
import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.ai.movement.MovementType;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.types.Priority;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.logging.LoggingPermission;

public class FollowEntityActivity extends Activity {
    private final Entity entity;
    private final double stoppingDistance;

    public FollowEntityActivity(Entity entity) {
        super(ActivityType.FOLLOWING, Priority.LOW, new BoredomSettings(MathUtils.random(600, 2400), MathUtils.random(2400, 12000)));
        this.entity = entity;
        this.stoppingDistance = 3;
    }

    public FollowEntityActivity(Entity entity, double stoppingDistance) {
        super(ActivityType.FOLLOWING, Priority.LOW, new BoredomSettings(MathUtils.random(600, 2400), MathUtils.random(2400, 12000)));
        this.entity = entity;
        this.stoppingDistance = stoppingDistance;
    }

    @Override
    public void onStart(StartType startType) {

    }

    @Override
    public void onUpdate() {
        if (getCurrentDuration() % 5 == 0) {
            GoToLocationActivity.Options options = new GoToLocationActivity.Options();
            options.setStoppingDistance(stoppingDistance);
            run(new GoToLocationActivity(entity.getLocation(), MovementType.SPRINT_JUMPING, options));
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
}
