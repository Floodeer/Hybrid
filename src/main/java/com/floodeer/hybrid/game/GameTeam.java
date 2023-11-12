package com.floodeer.hybrid.game;

import com.floodeer.hybrid.Hybrid;
import com.floodeer.hybrid.utils.Util;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generic GameTeam class from my other resources, may change later
 */
public class GameTeam {

    @Getter
    private Game game;
    private String name;
    private ChatColor color;
    private final HashMap<Player, PlayerState> players = Maps.newHashMap();
    private List<Location> spawns;
    private int capturedPoints;

    public GameTeam(Game game, String name, ChatColor color2, List<Location> list) {
        this.setGame(game);
        this.name = name;
        this.color = color2;
        this.spawns = list;
        this.capturedPoints = 0;
    }


    public GameTeam(Game game, String name, ChatColor color2, Location spawn) {
        this.setGame(game);
        this.name = name;
        this.color = color2;
        this.spawns = Lists.newLinkedList();
        this.capturedPoints = 0;

        spawns.add(spawn);
    }

    public String getName() {
        return this.name;
    }

    public ChatColor getColor() {
        return this.color;
    }

    public List<Location> getSpawns() {
        return this.spawns;
    }

    public void addPlayer(Player player) {
        this.players.put(player, PlayerState.IN);
        Bukkit.getOnlinePlayers().stream().filter((cur) -> !cur.equals(player)).forEach((other) -> {
            other.hidePlayer(player);
            other.showPlayer(player);
        });
        Hybrid.get().getPlayerManager().getPlayer(player.getUniqueId()).setTeam(this);
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
        Hybrid.get().getPlayerManager().getPlayer(player.getUniqueId()).setTeam(null);
        Bukkit.getOnlinePlayers().stream().filter((cur) -> !cur.equals(player)).forEach((other) -> {
            other.hidePlayer(player);
            other.showPlayer(player);
        });
    }

    public Player getPlayer(String name) {
        for (Player player : this.players.keySet()) {
            if (player.getName().equals(name)) return player;
        }
        return null;
    }

    public boolean hasPlayer(Player player) {
        return this.players.containsKey(player);
    }

    public boolean hasPlayer(String name, boolean alive) {
        for (Player player : this.players.keySet()) {
            if ((player.getName().equals(name)) && (
                    (!alive) || ((alive) && (this.players.get(player) == PlayerState.IN))))
                return true;
        }
        return false;
    }

    public int getSize() {
        return this.players.size();
    }

    public void setPlayerState(Player player, PlayerState state) {
        if (player == null) {
            return;
        }
        this.players.put(player, state);
        if(state == PlayerState.OUT) {
            Bukkit.getOnlinePlayers().stream().filter((cur) -> !cur.equals(player)).forEach((other) -> {
                other.hidePlayer(player);
            });
        }else {
            Bukkit.getOnlinePlayers().stream().filter((cur) -> !cur.equals(player)).forEach((other) -> {
                other.hidePlayer(player);
                other.showPlayer(player);
            });
        }
    }

    public boolean isTeamAlive() {
        for (PlayerState state : this.players.values()) {
            if (state == PlayerState.IN)
                return true;
        }
        return false;
    }


    public List<Player> getAlivePlayers() {
        return players.keySet().stream().filter((player) -> players.get(player) == PlayerState.IN).collect(Collectors.toList());
    }

    public List<Player> getDeathPlayers() {
        return players.keySet().stream().filter((player) -> players.get(player) == PlayerState.OUT).collect(Collectors.toList());
    }

    public List<Player> getPlayers() {
        return Lists.newArrayList(players.keySet());
    }

    public String getFormattedName() {
        return getColor() + "§l" + getName();
    }


    public boolean isAlive(Player player) {
        if (!this.players.containsKey(player)) {
            return false;
        }
        return this.players.get(player) == PlayerState.IN;
    }

    public void getColor(ChatColor color) {
        this.color = color;
    }

    public void getName(String name) {
        this.name = name;
    }

    public void sendTeamMessage(String msg) {
        getPlayers().forEach(cur -> cur.sendMessage(Util.color(msg)));
    }

    public byte getColorData() {
        if (getColor() == ChatColor.WHITE) return 0;
        if (getColor() == ChatColor.GOLD) return 1;
        if (getColor() == ChatColor.LIGHT_PURPLE) return 2;
        if (getColor() == ChatColor.AQUA) return 3;
        if (getColor() == ChatColor.YELLOW) return 4;
        if (getColor() == ChatColor.GREEN) {
            return 5;
        }
        if (getColor() == ChatColor.DARK_GRAY) return 7;
        if (getColor() == ChatColor.GRAY) return 8;
        if (getColor() == ChatColor.DARK_AQUA) return 9;
        if (getColor() == ChatColor.DARK_GRAY) return 10;
        if (getColor() == ChatColor.BLUE) return 11;
        if (getColor() == ChatColor.DARK_BLUE) {
            return 11;
        }
        if (getColor() == ChatColor.DARK_GREEN) return 13;
        if (getColor() == ChatColor.RED) return 14;
        return 15;
    }

    public Color getColorBase() {
        if (getColor() == ChatColor.WHITE) return Color.WHITE;
        if (getColor() == ChatColor.GOLD) return Color.ORANGE;
        if (getColor() == ChatColor.DARK_GREEN) return Color.PURPLE;
        if (getColor() == ChatColor.AQUA) return Color.AQUA;
        if (getColor() == ChatColor.YELLOW) return Color.YELLOW;
        if (getColor() == ChatColor.GREEN) return Color.GREEN;
        if (getColor() == ChatColor.DARK_GREEN) return Color.GRAY;
        if (getColor() == ChatColor.GRAY) return Color.GRAY;
        if (getColor() == ChatColor.DARK_AQUA) return Color.AQUA;
        if (getColor() == ChatColor.DARK_PURPLE) return Color.PURPLE;
        if (getColor() == ChatColor.BLUE) return Color.BLUE;
        if (getColor() == ChatColor.DARK_BLUE) return Color.BLUE;
        if (getColor() == ChatColor.DARK_GREEN) return Color.GREEN;
        if (getColor() == ChatColor.RED) return Color.RED;
        return Color.WHITE;
    }

    public void setSpawns(ArrayList<Location> spawns) {
        this.spawns = spawns;
    }

    public void setGame(Game IGame) {
        this.game = IGame;
    }

    public int getCapturedPoints() {
        return capturedPoints;
    }

    public void setCapturedPoints(int capturedPoints) {
        this.capturedPoints = capturedPoints;
    }

    public enum PlayerState {
        IN("In", ChatColor.GRAY),
        OUT("Out", ChatColor.RED);

        private final String name;
        private final ChatColor color;

        PlayerState(String name, ChatColor color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return this.name;
        }

        public ChatColor getColor() {
            return this.color;
        }
    }
}
