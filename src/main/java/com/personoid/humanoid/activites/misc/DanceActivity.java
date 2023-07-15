package com.personoid.humanoid.activites.misc;

import com.personoid.api.ai.activity.Activity;
import com.personoid.api.ai.looking.target.LocationTarget;
import com.personoid.api.npc.Pose;
import com.personoid.api.utils.math.MathUtils;
import com.personoid.api.utils.types.HandEnum;
import com.personoid.api.utils.types.Priority;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class DanceActivity extends Activity {
    private int nextToggleCrouchTick;
    private int nextPunchTick;
    private int nextLookTick;

    @Override
    public void onStart(StartType startType) {

    }

    @Override
    public void onUpdate() {
        if (nextToggleCrouchTick <= 0) {
            nextToggleCrouchTick = MathUtils.random(2, 10);
            getNPC().setPose(getNPC().getPose() == Pose.SNEAKING ? Pose.STANDING : Pose.SNEAKING);
        }
        if (nextPunchTick <= 0) {
            nextPunchTick = MathUtils.random(2, 10);
            getNPC().swingHand(HandEnum.RIGHT);
        }
        if (nextLookTick <= 0) {
            nextLookTick = MathUtils.random(5, 15);
            Vector randomVec = new Vector(MathUtils.random(-5, 5), MathUtils.random(-5, 5), MathUtils.random(-5, 5));
            Location facing = getNPC().getLocation().clone().add(randomVec);
            getNPC().getLookController().addTarget("dance_activity", new LocationTarget(facing));
        }
        nextToggleCrouchTick--;
        nextPunchTick--;
        nextLookTick--;
    }

    @Override
    public void onStop(StopType stopType) {
        getNPC().setPose(Pose.STANDING);
        getNPC().getLookController().removeTarget("dance_activity");
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
        return new BoredomSettings(MathUtils.random(200, 1200), MathUtils.random(2400, 12000));
    }
}
