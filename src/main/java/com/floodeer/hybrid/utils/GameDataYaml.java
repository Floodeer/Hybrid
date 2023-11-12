package com.floodeer.hybrid.utils;

import com.floodeer.hybrid.Hybrid;

import java.io.File;

public class GameDataYaml {

    public static GameDataFile getMap(String mapName) {
        return new GameDataFile(Hybrid.get().getDataFolder().getAbsolutePath() + File.separator + "maps" + File.separator + mapName + File.separator + mapName + ".yml");
    }
}