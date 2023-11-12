package com.floodeer.hybrid.game;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Generic Scoreboard class from my other resources, may change later
 */
public class GameScoreboard {

    private static final int MAX_SIZE = 128;

    private static final HashMap<UUID, GameScoreboard> players = new HashMap<>();
    private final Scoreboard scoreboard;
    private final Objective sidebar;
    private Objective belowNames;

    private GameScoreboard(Player player) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        sidebar = scoreboard.registerNewObjective("sidebar", "dummy");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (int i = 1; i <= 15; i++) {
            Team team = scoreboard.registerNewTeam("SLOT_" + i);
            team.addEntry(genEntry(i));
        }
        player.setScoreboard(scoreboard);
        players.put(player.getUniqueId(), this);
    }

    public static HashMap<UUID, GameScoreboard> getScoreboards() {
        return players;
    }

    public static boolean hasScore(Player player) {
        return players.containsKey(player.getUniqueId());
    }

    public static GameScoreboard createScore(Player player) {
        return new GameScoreboard(player);
    }

    public static GameScoreboard getByPlayer(Player player) {
        return players.get(player.getUniqueId());
    }

    public static GameScoreboard getByUUID(UUID uuid) {
        return players.get(uuid);
    }

    public static GameScoreboard removeScore(Player player) {
        return players.remove(player.getUniqueId());
    }

    public void setTitle(String title) {
        title = ChatColor.translateAlternateColorCodes('&', title);
        sidebar.setDisplayName(title.length() > 32 ? title.substring(0, 32) : title);
    }

    public void setSlot(int slot, String text) {
        Team team = scoreboard.getTeam("SLOT_" + slot);
        String entry = genEntry(slot);
        if (!scoreboard.getEntries().contains(entry)) {
            sidebar.getScore(entry).setScore(slot);
        }
        String pre = getFirstSplit(text);
        String suf = getFirstSplit(ChatColor.getLastColors(pre) + getSecondSplit(text));
        team.setPrefix(pre);
        team.setSuffix(suf);
    }

    public void removeSlot(int slot) {
        String entry = genEntry(slot);
        if (scoreboard.getEntries().contains(entry)) {
            scoreboard.resetScores(entry);
        }
    }

    public void setSlotsFromList(List<String> list) {
        while (list.size() > 15) {
            list.remove(list.size() - 1);
        }

        int slot = list.size();

        if (slot < 15) {
            for (int i = (slot + 1); i <= 15; i++) {
                removeSlot(i);
            }
        }

        for (String line : list) {
            setSlot(slot, line);
            slot--;
        }
    }

    private String genEntry(int slot) {
        return ChatColor.values()[slot].toString();
    }

    private String getFirstSplit(String s) {
        return s.length() > MAX_SIZE ? s.substring(0, MAX_SIZE) : s;
    }

    private String getSecondSplit(String s) {
        if (s.length() > (MAX_SIZE)) {
            s = s.substring(0, MAX_SIZE);
        }
        return s.length() > MAX_SIZE ? s.substring(MAX_SIZE) : "";
    }

    public void setPlayerTeam(Player player, GameTeam gameTeam) {

        for (Team team : this.scoreboard.getTeams()) {
            team.removePlayer(player);
        }
        String teamName = gameTeam.getName();

        if (scoreboard.getTeam(gameTeam.getName()) == null) {
            Team team = scoreboard.registerNewTeam(gameTeam.getName());
            if (teamName.equalsIgnoreCase("RED")) {
                team.setColor(ChatColor.RED);
                team.setAllowFriendlyFire(true);
                team.addEntry(player.getName());
            } else {
                if (teamName.equalsIgnoreCase("BLUE")) {
                    team.setColor(ChatColor.BLUE);
                    team.setAllowFriendlyFire(true);
                    team.addEntry(player.getName());
                }
            }
        }
        this.scoreboard.getTeam(teamName).addPlayer(player);
    }

    public void removeFromTeam(Player player) {
        for (Team team : this.scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                team.removePlayer(player);
            }
        }
    }
}
