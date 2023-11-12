package com.floodeer.hybrid.database.data;

import com.floodeer.hybrid.Hybrid;
import com.floodeer.hybrid.game.Game;
import com.floodeer.hybrid.game.GameTeam;
import com.floodeer.hybrid.utils.Util;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class GamePlayer {

    @Getter private final Player player;
    @Getter private final UUID UUID;
    @Getter private final String name;

    @Getter @Setter private int wins;
    @Getter @Setter private int gamesPlayed;
    @Getter @Setter private int waveRecord;
    @Setter @Getter private int kills;
    @Getter @Setter private int exp;
    @Getter @Setter private int balance;
    @Getter @Setter private String rank = "Level-1";
    @Getter @Setter private double damageCaused;
    @Getter @Setter private String kit;
    @Getter @Setter private List<String> kits;

    @Getter @Setter private GameTeam team;
    @Getter @Setter private Game game;

    @Getter @Setter private boolean spectator;

    @Getter @Setter private boolean loaded;

    private PlayerInventory playerInventory;

    public GamePlayer(UUID uuid) {
        this.UUID = uuid;
        this.player = Bukkit.getPlayer(uuid);
        this.name = player.getName();

        kits = Lists.newArrayList();
    }

    public GamePlayer(Player player) {
        this.player = player;
        this.UUID = player.getUniqueId();
        this.name = player.getName();

        kits = Lists.newArrayList();
    }

    public static GamePlayer get(Player player) {
        return Hybrid.get().getPlayerManager().getPlayer(player);
    }

    public static GamePlayer get(UUID uuid) {
        return Hybrid.get().getPlayerManager().getPlayer(uuid);
    }

    public void msg(String msg) {
        player.sendMessage(Util.color(msg));
    }

    public void addMoney(int amount) {
        setBalance(getBalance()+amount);
    }

    public void removeMoney(int amount) {
        if(getBalance() - amount < 0)
            setBalance(0);

        setBalance(getBalance()-amount);
    }

    public boolean isInGame() {
        return game != null;
    }

    public void clearInventory(boolean save) {
        if(save) {
            playerInventory = new PlayerInventory(getPlayer());
        }

        getPlayer().getInventory().clear();
        getPlayer().getInventory().setArmorContents(null);
        for (PotionEffect potions : getPlayer().getActivePotionEffects()) {
            getPlayer().removePotionEffect(potions.getType());
        }
        getPlayer().setLevel(0);
        getPlayer().setFireTicks(0);
        getPlayer().setExp(0);
        getPlayer().setFoodLevel(20);
        getPlayer().setHealth(getPlayer().getMaxHealth());
        getPlayer().updateInventory();
    }

    public void restoreInventory() {
        if (playerInventory != null) {
            for (PotionEffect potions : getPlayer().getActivePotionEffects()) {
                getPlayer().removePotionEffect(potions.getType());
            }
            playerInventory.restore();
            playerInventory = null;
        }
    }

    protected class PlayerInventory {

        @Getter private final ItemStack[] content;
        @Getter private final ItemStack[] armor;
        @Getter private final float exp;
        @Getter private final GameMode gameMode;
        @Getter private final int level;
        @Getter private final int foodLevel;
        @Getter private final Collection<PotionEffect> effects;
        @Getter private final Player p;

        public PlayerInventory(Player p) {
            this.p = p;
            this.content = p.getInventory().getContents();
            this.armor = p.getInventory().getArmorContents();
            this.level = p.getLevel();
            this.exp = p.getExp();
            this.gameMode = p.getGameMode();
            this.foodLevel = p.getFoodLevel();
            this.effects = p.getActivePotionEffects();
        }

        public void restore() {
            p.getInventory().clear();
            p.setGameMode(gameMode);
            p.setLevel(level);
            p.setExp(exp);
            p.setFoodLevel(foodLevel);
            p.getInventory().setArmorContents(armor);
            p.getInventory().setContents(content);
            p.updateInventory();
            for (PotionEffect pf : getEffects()) {
                p.addPotionEffect(pf);
            }
        }
    }
}
