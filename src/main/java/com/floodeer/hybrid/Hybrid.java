package com.floodeer.hybrid;

import com.floodeer.hybrid.database.DatabaseProvider;
import com.floodeer.hybrid.database.SQLite;
import com.floodeer.hybrid.listeners.PlayerListener;
import com.floodeer.hybrid.manager.DataManager;
import com.floodeer.hybrid.manager.EnergyManager;
import com.floodeer.hybrid.manager.GameManager;
import com.floodeer.hybrid.manager.PlayerManager;
import com.floodeer.hybrid.utils.ConfigOptions;
import com.floodeer.hybrid.utils.IconCore;
import com.floodeer.hybrid.utils.Items;
import com.floodeer.hybrid.utils.update.Updater;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;


public final class Hybrid extends JavaPlugin {

    private static Hybrid main;
    @Getter private PlayerManager playerManager;
    @Getter private GameManager gameManager;
    @Getter private ConfigOptions configOptions;
    @Getter private DatabaseProvider<?> database;
    @Getter private DataManager dataManager;
    @Getter private EnergyManager energyManager;
    @Getter private IconCore iconCore;
    @Getter private Items items;

    public YamlConfiguration vehiclesConfig;

    public static Hybrid get() {
        return main;
    }

    public YamlConfiguration getVehiclesConfig() {
        return vehiclesConfig;
    }

    @Override
    public void onEnable() {
        main = this;

        getCommand("hybrid").setExecutor(new Commands());

        getServer().getScheduler().runTaskTimer(this, new Updater(this), 20, 1);

        this.playerManager = new PlayerManager();
        this.gameManager = new GameManager();
        this.energyManager = new EnergyManager();
        this.iconCore = new IconCore();
        this.items = new Items();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(iconCore, this);


        configOptions = new ConfigOptions(new File(getDataFolder(), "options.yml"));
        try {
            configOptions.load();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        saveResource("vehicles.yml", false);
        File file = new File(getDataFolder(), "vehicles.yml");
        this.vehiclesConfig = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void onDisable() {
        gameManager.shutdownGames();
        playerManager.shutdown();
    }

    private boolean setupDatabase()  {
        try {
            database = new SQLite();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        dataManager = new DataManager();
        return true;
    }
}
