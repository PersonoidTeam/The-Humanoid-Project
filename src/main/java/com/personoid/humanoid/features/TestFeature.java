package com.personoid.humanoid.features;

import com.personoid.api.npc.injection.CallbackInfo;
import com.personoid.api.npc.injection.Feature;
import com.personoid.api.npc.injection.Hook;

public class TestFeature extends Feature {
    @Hook("damage")
    public void constantDamage(double damage, CallbackInfo<Double> ci) {
        //Bukkit.broadcastMessage("NPC took " + damage + " damage!");
        //ci.setReturnValue(10D);
    }

    @Hook("tick")
    public void tickInjection() {
        //Bukkit.broadcastMessage("NPC ticked!");
    }
}
