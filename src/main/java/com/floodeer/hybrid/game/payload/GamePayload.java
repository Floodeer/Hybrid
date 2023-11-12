package com.floodeer.hybrid.game.payload;

import com.floodeer.hybrid.Hybrid;
import com.floodeer.hybrid.game.Game;
import com.floodeer.hybrid.game.GameArena;
import com.floodeer.hybrid.utils.MathUtils;
import com.floodeer.hybrid.utils.Util;
import com.floodeer.hybrid.utils.VelocityUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

@Getter
public class GamePayload implements GameObjective {

    private final Game game;

    @Getter private PayloadVehicle vehicle;
    @Getter private boolean canMove;
    @Getter private boolean started;
    @Getter private double traveledDistance;
    @Getter private int movingPlayers;
    @Getter @Setter private int pathIndex;

    private boolean announceEnding;

    public GamePayload(Game game) {
        this.game = game;
    }

    public void unlockPayload() {
        canMove = false;
        announceEnding = false;
        started = false;
        traveledDistance = 0.0;
        movingPlayers = 0;

        getGame().getAttackers().sendTeamMessage("&eDesbloqueando carga...");
        vehicle = VehicleLoader.load("formula_car", game.getGame().getArena().getLocation(GameArena.LocationType.POINT.toString()), PayloadVehicle.PayloadType.FORMULA_CAR, UUID.randomUUID());
        vehicle.setForwards(game.getArena().getPath().get(pathIndex).getDirection().normalize());
        vehicle.setCurrentLocation(vehicle.getOrigin());
        vehicle.rotate(180);
        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                ++step;
                if (step == 12 * 10) {
                    getGame().getAttackers().sendTeamMessage("&a&lCarga liberada! Fique perto para escoltar até o objetivo final!");
                    started = true;
                    cancel();
                }

            }
        }.runTaskTimer(Hybrid.get(), 0, 1);
    }

    @Override
    public void update() {
        if(!started)
            return;

        Location targetLocation = getGame().getArena().getPath().get(getPathIndex());
        double distanceSquared = vehicle.getCurrentLocation().distanceSquared(targetLocation);
        if(distanceSquared < 1.5) {
            setPathIndex(getPathIndex() + 1);
            if(getPathIndex() >= game.getArena().getPath().size()) {
                getGame().getAttackers().sendTeamMessage("&a&lBoom");
                //TODO end
                return;
            }
        }

        System.out.println(pathIndex);
        vehicle.move(targetLocation);
        vehicle.update();

    }
}
