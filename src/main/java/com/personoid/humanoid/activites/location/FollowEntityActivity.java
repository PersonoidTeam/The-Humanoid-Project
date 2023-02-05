package com.personoid.humanoid.activites.location;

import com.personoid.api.activities.GoToLocationActivity;
import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.activity.ActivityType;
import com.personoid.api.ai.looking.Target;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.types.Priority;
import org.bukkit.entity.Entity;

public class FollowEntityActivity extends Activity {
    private final Entity entity;
    private final double stoppingDistance;

    public FollowEntityActivity(Entity entity) {
        super(ActivityType.FOLLOWING, Priority.LOW, new BoredomSettings(MathUtils.random(600, 2400), MathUtils.random(2400, 12000)));
        this.entity = entity;
        this.stoppingDistance = 2;
    }

    public FollowEntityActivity(Entity entity, double stoppingDistance) {
        super(ActivityType.FOLLOWING, Priority.LOW, new BoredomSettings(MathUtils.random(600, 2400), MathUtils.random(2400, 12000)));
        this.entity = entity;
        this.stoppingDistance = stoppingDistance;
    }

    @Override
    public void onStart(StartType startType) {
        getNPC().getLookController().addTarget("follow_target", new Target(entity, Priority.HIGH));
    }

    @Override
    public void onUpdate() {
        if (getCurrentDuration() % 5 == 0) {
            GoToLocationActivity goTo = new GoToLocationActivity(entity.getLocation(), GoToLocationActivity.MovementType.SPRINT_JUMP);
            goTo.getOptions().setStoppingDistance(stoppingDistance);
            goTo.getOptions().setFaceLocation(false, Priority.NORMAL);
            run(goTo);
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
