package com.floodeer.hybrid.game;


import com.floodeer.hybrid.Hybrid;
import com.floodeer.hybrid.database.data.GamePlayer;
import com.floodeer.hybrid.game.payload.GameObjective;
import com.floodeer.hybrid.game.payload.GamePayload;
import com.floodeer.hybrid.utils.Runner;
import com.floodeer.hybrid.utils.TimeUtils;
import com.floodeer.hybrid.utils.Util;
import com.floodeer.hybrid.utils.update.UpdateEvent;
import com.floodeer.hybrid.utils.update.UpdateType;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.*;

public class Game implements Listener {

    @Getter private final String name;
    @Getter Game game = this;

    //If using @Getter, GameArena has to be set using @Setter
    @Getter @Setter private GameArena arena;

    @Getter @Setter GameObjective gameObjective;
    @Getter @Setter GameState state;

    private GamePoint point;

    @Getter @Setter private int timer = 0;
    @Getter @Setter private int startCountdown;

    @Getter private List<GamePlayer> players;
    @Getter private List<GameTeam> teams;

    public Game(String name, boolean load) {
        this.name = name;
        setArena(new GameArena(name));

        if (load) {
            setState(GameState.PRE_GAME);
            getArena().load();
            Hybrid.get().getServer().getScheduler().runTaskLater(Hybrid.get(), this::loadGame, 20L);
        }
    }

    private void loadGame() {
        players = Lists.newArrayList();
        teams = Lists.newArrayList();
        teams.add(new GameTeam(this, "Attackers", ChatColor.BLUE, getArena().getLocation("BLUE_SPAWN")));
        teams.add(new GameTeam(this, "Defenders", ChatColor.RED, getArena().getLocation("RED_SPAWN")));
        point = new  GamePoint(this, "Alpha", getArena().getLocation("POINT"));
        Hybrid.get().getServer().getPluginManager().registerEvents(this, Hybrid.get());
    }

    public void unlockNextObjective() {
        setGameObjective(new GamePayload(this));
        GamePayload payload = (GamePayload) getGameObjective();
        payload.unlockPayload();
    }

    public void addPlayer(GamePlayer gp) {
        gp.setGame(this);
        getPlayers().add(gp);
        getAttackers().addPlayer(gp.getPlayer());
        gp.getPlayer().teleportAsync(getArena().getLocation(GameArena.LocationType.LOBBY.toString()));

        Runner.make(Hybrid.get()).delay(5).run(() -> {
            gp.clearInventory(true);
            gp.getPlayer().setGameMode(GameMode.ADVENTURE);
        });
    }

    public void removePlayer(GamePlayer gp, boolean force, boolean leave) {
        gp.clearInventory(false);
        gp.restoreInventory();

        gp.setGame(null);
        gp.setSpectator(false);
        gp.getPlayer().setGameMode(GameMode.SURVIVAL);
        gp.getPlayer().setAllowFlight(false);
        gp.getPlayer().setFlying(false);

        if(!force && getState() == GameState.IN_GAME && !gp.isSpectator()) {
            gp.setGamesPlayed(gp.getGamesPlayed()+1);
        }

        if(!force) {
            getPlayers().remove(gp);
        }

        GameScoreboard.removeScore(gp.getPlayer());
        gp.getPlayer().setScoreboard(Bukkit.getServer().getScoreboardManager().getMainScoreboard());
    }

    public void start() {
        setState(GameState.IN_GAME);

        getAttackers().getPlayers().forEach(cur -> {
            cur.teleportAsync(getArena().getLocation("BLUE_SPAWN"));
        });

        getDefenders().getPlayers().forEach(cur -> {
            cur.teleportAsync(getArena().getLocation("RED_SPAWN"));
        });

        setGameObjective(point);
    }


    @EventHandler
    public void onGameTick(UpdateEvent event) {
        if (event.getType() == UpdateType.FAST) {
            updateScoreboard();
        }

        if(event.getType() == UpdateType.TICK && getState() == GameState.IN_GAME) {
            if(getGameObjective() != null)
                getGameObjective().update();
        }
    }

    public GameTeam getTeam(ChatColor color) {
        return getTeams().stream().filter(cur -> cur.getColor().equals(color)).findFirst().orElse(getTeams().get(0));
    }


    public void endGame(boolean winner) {
        setState(GameState.ENDING);

        resetArena(false);
    }

    public void shutdown(boolean recreate) {
        if(!getPlayers().isEmpty()) {
            getPlayers().forEach(gp ->  {
                removePlayer(gp, true, false);
                if(getState() == GameState.IN_GAME) {
                    broadcast("&cPartida cancelada!");
                }
            });
        }
        getPlayers().clear();
        setState(GameState.RESTORING);
        HandlerList.unregisterAll(this);

        if(recreate)
            Hybrid.get().getGameManager().recreateGame(this);
    }


    private void resetArena(boolean shutdown) {
        Bukkit.getScheduler().runTaskLater(Hybrid.get(), () -> {
            getPlayers().forEach(gp -> removePlayer(gp, true, false));
            if(!shutdown)
                Runner.make(Hybrid.get()).delay(35).run(() -> Hybrid.get().getGameManager().recreateGame(this));
        }, 10 * 20L);
    }

    private void updateScoreboard() {
        getPlayers().forEach(player -> {
            GameScoreboard scoreboard;
            if (GameScoreboard.hasScore(player.getPlayer()))
                scoreboard = GameScoreboard.getByPlayer(player.getPlayer());
            else {
                scoreboard = GameScoreboard.createScore(player.getPlayer());
                scoreboard.setTitle("&6&lHYBRID");
            }
            if (getState() == GameState.IN_GAME || getState() == GameState.ENDING) {
                scoreboard.setSlotsFromList(Lists.newArrayList(
                        Util.createSpacer(),
                        Util.color("&fTempo: &a" + TimeUtils.formatScoreboard(getTimer())),
                        Util.createSpacer(),
                        Util.color("&fMapa: &a" + getName()),
                        Util.color("&fPlayers: &a" + getPlayers().size())));
            } else {
                scoreboard.setSlotsFromList(Lists.newArrayList(
                        Util.createSpacer(),
                        Util.color("&fMapa: &a" + getName()),
                        Util.color("&fPlayers: &b" + getPlayers().size() + "/" + getArena().getMaxPlayers()),
                        Util.color("&fNecessários: &b" + getArena().getMinPlayers()),
                        Util.createSpacer(),
                        Util.color("&fEstado: &f" + getState().toString()),
                        Util.createSpacer(),
                        Util.color("&fSaldo: &b" + player.getBalance())));
            }
        });
    }


    private void sendActionBar(String text) {
        getPlayers().stream().map(GamePlayer::getPlayer).forEach(cur -> cur.sendActionBar(Component.text(text)));
    }

    private void sendTitle(String title, String subtitle) {
        getPlayers().stream().map(GamePlayer::getPlayer).forEach(cur -> cur.showTitle(Title.title(Component.text(title), Component.text(subtitle))));
    }

    private void playSound(Sound sound, float volume, float pitch) {
        getPlayers().stream().map(GamePlayer::getPlayer).forEach(cur -> cur.playSound(cur.getLocation(), sound, volume, pitch));
    }

    public GameTeam getAttackers() {
        return getTeams().get(0);
    }

    public GameTeam getDefenders() {
        return getTeams().get(1);
    }

    private void broadcast(String msg) {
        getPlayers().forEach(cur -> cur.msg(msg));
    }

    public enum GameState {
        PRE_GAME("&aAguardando"),
        STARTING("&eIniciando"),
        IN_GAME("&cEm jogo"),
        ENDING("&4Encerrando"),
        RESTORING("&bReiniciando");

        String state;

        GameState(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return Util.color(state);
        }
    }

}
