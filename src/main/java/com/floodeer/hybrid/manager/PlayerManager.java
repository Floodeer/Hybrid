package com.floodeer.hybrid.manager;

import com.floodeer.hybrid.Hybrid;
import com.floodeer.hybrid.database.data.GamePlayer;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final Map<UUID, GamePlayer> onlinePlayers = Maps.newHashMap();

    public PlayerManager() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            addPlayer(player.getUniqueId());
        }
    }

    public void addPlayer(UUID uuid) {
        if (!this.onlinePlayers.containsKey(uuid)) {
            final GamePlayer gamePlayer = new GamePlayer(uuid);
            onlinePlayers.put(uuid, gamePlayer);
        }
    }

    public void addPlayer(Player player) {
        boolean present = getAll().stream().map(GamePlayer::getPlayer).anyMatch(cur -> cur.equals(player));
        if (!present) {
            final GamePlayer gamePlayer = new GamePlayer(player);
            onlinePlayers.put(gamePlayer.getUUID(), gamePlayer);
        }
    }

    public void removePlayer(UUID uuid) {
        GamePlayer gp = getPlayer(uuid);
        if(gp.isInGame())
            gp.getGame().removePlayer(gp, false, true);

        updateAsync(gp);
        onlinePlayers.remove(uuid);
    }

    public GamePlayer getPlayer(UUID uuid) {
        return onlinePlayers.get(uuid);
    }

    public GamePlayer getPlayer(Player player) {
        return onlinePlayers.values().stream().filter(cur -> cur.getPlayer().equals(player)).findAny().orElse(null);
    }

    public void shutdown() {
        for(GamePlayer gp : getAll()) {
            Hybrid.get().getDataManager().updatePlayer(gp);
        }
        onlinePlayers.clear();
    }

    public void restart() {
        for(GamePlayer gp : getAll()) {
            Hybrid.get().getDataManager().updatePlayer(gp);
        }
        onlinePlayers.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            addPlayer(player.getUniqueId());
        }
    }

    public void updateAll() {
        for(GamePlayer gp : getAll()) {
            Hybrid.get().getDataManager().updatePlayer(gp);
        }
    }

    public void updateAllAsync() {
        for(GamePlayer gp : getAll()) {
            Hybrid.get().getDataManager().updatePlayerAsync(gp);
        }
    }

    public void update(GamePlayer gp) {
        Hybrid.get().getDataManager().updatePlayer(gp);
    }

    public void updateAsync(GamePlayer gp) {
        Hybrid.get().getDataManager().updatePlayerAsync(gp);
    }
    
    
    public GamePlayer getPlayer(String name) {
        for (GamePlayer gPlayer: onlinePlayers.values()) {
            if (gPlayer.getName().equals(name)) {
                return gPlayer;
            }
        }
        return null;
    }

    public Collection<GamePlayer> getAll() {
        return onlinePlayers.values();
    }

}