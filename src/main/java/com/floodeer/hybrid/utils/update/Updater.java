package com.floodeer.hybrid.utils.update;

import org.bukkit.plugin.java.JavaPlugin;

public class Updater implements Runnable {

    private final JavaPlugin plugin;

    public Updater(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void run() {
        for (UpdateType types : UpdateType.values()) {
            if (types.elapsed())
                this.plugin.getServer().getPluginManager().callEvent(new UpdateEvent(types));
        }
	}
}
