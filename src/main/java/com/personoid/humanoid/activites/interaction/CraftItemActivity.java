package com.personoid.humanoid.activites.interaction;

import com.personoid.api.ai.activity.Activity;
import com.personoid.api.utils.types.Priority;
import com.personoid.humanoid.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;

public class CraftItemActivity extends Activity {
    Location currentCraftingTable = null;

    // Recurse through materials in a crafting recipe for a given item.
    //
    // Count materials of the same type and group them together.
    // Look at each separate material and preform the same action.
    // Make list storing crafting instruction.
    // Instruction class format will be as follows.
    // Hashmap of material and count, and desired material to craft.

    @Override
    public void onStart(StartType startType) {
        Location placeLoc = LocationUtils.getRandomPlaceableSpot(getNPC().getLocation(), 3);
        if (placeLoc != null){
            currentCraftingTable = placeLoc.clone();
            getNPC().getBlocker().place(placeLoc.getBlock(), Material.CRAFTING_TABLE);
        }
    }

    @Override
    public void onUpdate() {

    }

    @Override
    public void onStop(StopType stopType) {

    }

    @Override
    public boolean canStart(StartType startType) {
        return getNPC().getInventory().contains(Material.CRAFTING_TABLE);
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
        return null;
    }
}
