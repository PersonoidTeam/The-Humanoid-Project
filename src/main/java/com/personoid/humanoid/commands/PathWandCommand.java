package com.personoid.humanoid.commands;

import com.personoid.api.utils.math.MathUtils;
import com.personoid.humanoid.Humanoid;
import com.personoid.humanoid.handlers.CommandHandler;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class PathWandCommand extends CommandHandler.Command {
    public PathWandCommand() {
        super("debugger", "pathwand", CommonRequirements.PLAYER);
    }

    @Override
    public boolean onCommand(@NotNull Player sender, String[] args) {
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName("Path Wand");
        meta.getPersistentDataContainer().set(new NamespacedKey(Humanoid.getPlugin(), "pathwand"),
                PersistentDataType.INTEGER, MathUtils.random(111, 999));
        wand.setItemMeta(meta);
        sender.getInventory().addItem(wand);
        return true;
    }
}
