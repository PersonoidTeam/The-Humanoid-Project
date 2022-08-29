package com.personoid.humanoid.utils;

import com.personoid.api.utils.debug.Profiler;
import com.personoid.humanoid.Humanoid;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private static final JavaPlugin plugin = Humanoid.getPlugin(Humanoid.class);
    private static boolean autoReload;
    private static List<Profiler> enabledProfilers;

    public static void reset() {
        autoReload = false;
        enabledProfilers = new ArrayList<>();
    }

    public static boolean doesConfigExist() {
        return new File("plugins/Humanoid/config.yml").exists();
    }

    public static void reload() {
        if (!doesConfigExist()) reset();
        autoReload = plugin.getConfig().getBoolean("auto-reload");
        enabledProfilers = plugin.getConfig().getStringList("enabled-profilers")
                .stream().map(Profiler::valueOf).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public static boolean isAutoReload() {
        return autoReload;
    }

    public static void setAutoReload(boolean value) {
        plugin.getConfig().set("auto-reload", value);
        plugin.saveConfig();
        reload();
    }

    public static List<Profiler> getEnabledProfilers() {
        return enabledProfilers;
    }

    public static void addEnabledProfiler(List<Profiler> profilers) {
        enabledProfilers.addAll(profilers);
        plugin.getConfig().set("enabled-profilers", enabledProfilers.stream().map(Profiler::name)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
        plugin.saveConfig();
        reload();
    }

    public static void removeEnabledProfiler(List<Profiler> profilers) {
        enabledProfilers.removeAll(profilers);
        plugin.getConfig().set("enabled-profilers", enabledProfilers.stream().map(Profiler::name)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
        plugin.saveConfig();
        reload();
    }
}
