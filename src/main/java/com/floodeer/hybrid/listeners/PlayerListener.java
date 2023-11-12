package com.floodeer.hybrid.listeners;

import com.floodeer.hybrid.Hybrid;
import com.floodeer.hybrid.database.data.GamePlayer;
import com.floodeer.hybrid.game.payload.PayloadVehicle;
import com.floodeer.hybrid.game.payload.PlanePayload;
import com.floodeer.hybrid.game.payload.VehicleLoader;
import com.floodeer.hybrid.utils.MathUtils;
import com.floodeer.hybrid.utils.Runner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {
    
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        new Runner(Hybrid.get()).delay(8).run(() -> Hybrid.get().getPlayerManager().addPlayer(e.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Hybrid.get().getPlayerManager().removePlayer(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        Hybrid.get().getPlayerManager().removePlayer(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if(GamePlayer.get(e.getPlayer()).isInGame())
            e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if(GamePlayer.get(e.getPlayer()).isInGame())
            e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(FoodLevelChangeEvent e) {
        if(GamePlayer.get(e.getEntity().getUniqueId()).isInGame())
            e.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if(e.getMessage().equalsIgnoreCase("test")) {
            Player player = e.getPlayer();
            e.setCancelled(true);
            new BukkitRunnable() {

                @Override
                public void run() {
                    PayloadVehicle v = VehicleLoader.load("car", player.getLocation(), PayloadVehicle.PayloadType.CAR, player.getUniqueId());
                    v.attemptSit(e.getPlayer());
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            v.update();

                        }
                    }.runTaskTimer(Hybrid.get(), 0, 1);
                }
            }.runTaskLater(Hybrid.get(), 1);
        }else if(e.getMessage().equalsIgnoreCase("test2")) {
            Player player = e.getPlayer();
            e.setCancelled(true);
            new BukkitRunnable() {

                @Override
                public void run() {
                    PlanePayload v = (PlanePayload) VehicleLoader.load("plane", player.getLocation(), PayloadVehicle.PayloadType.PLANE, player.getUniqueId());
                    v.attemptSit(e.getPlayer());
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            v.update();

                        }
                    }.runTaskTimer(Hybrid.get(), 0, 1);
                }
            }.runTaskLater(Hybrid.get(), 1);
        }else if(e.getMessage().equalsIgnoreCase("test3")) {
            Player player = e.getPlayer();
            e.setCancelled(true);
            new BukkitRunnable() {

                @Override
                public void run() {
                    PlanePayload v = (PlanePayload) VehicleLoader.load("biplane", player.getLocation(), PayloadVehicle.PayloadType.BIPLANE, player.getUniqueId());
                    v.attemptSit(e.getPlayer());
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            v.update();

                        }
                    }.runTaskTimer(Hybrid.get(), 0, 1);
                }
            }.runTaskLater(Hybrid.get(), 1);
        }
    }
}
