package com.personoid.humanoid;

import com.personoid.api.PersonoidAPI;
import com.personoid.api.utils.bukkit.Task;
import com.personoid.humanoid.handlers.CommandHandler;
import com.personoid.humanoid.listeners.DebugEvents;
import com.personoid.humanoid.listeners.Events;
import com.personoid.humanoid.utils.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Humanoid extends JavaPlugin {
    private static JavaPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        CommandHandler.registerCommands();
        getServer().getPluginManager().registerEvents(new Events(), this);
        getServer().getPluginManager().registerEvents(new DebugEvents(), this);
        Config.reload();
        initReloader();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        PersonoidAPI.getRegistry().purgeNPCs();
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public void initReloader() {
        File file = new File("plugins/Humanoid-1.0.0.jar");
        long lastModified = file.lastModified();
        new Task(() -> {
            if (file.lastModified() != lastModified && Config.isAutoReload()) {
                Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "[Humanoid] " + ChatColor.GREEN + "Plugin modified, reloading...");
                Bukkit.reload();
            }
        }, this).repeat(0, 20);
    }
}
