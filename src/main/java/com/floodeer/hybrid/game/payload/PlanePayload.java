package com.floodeer.hybrid.game.payload;

import com.floodeer.hybrid.Hybrid;
import com.floodeer.hybrid.utils.Util;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlanePayload extends PayloadVehicle {

    private final double gravityUpdateRate = 1;
    private double raiseRate = 0.15;
    private boolean drivable = true;
    private List<UUID> blades = new ArrayList<>();
    private UUID bladeOrigin = null;
    private boolean hasBlades = true;
    private boolean rotateX = false;

    public PlanePayload(UUID seat, List<UUID> armorStand, Vector forwards, Location origin, String name, List<UUID> extraSeats, PayloadType vehicleType, UUID owner, UUID vehicleId, List<UUID> blades, UUID bladeOrigin, boolean rotateX) {
        super(seat, armorStand, forwards, origin, name, extraSeats, vehicleType, owner, vehicleId);
        this.blades = blades;
        this.bladeOrigin = bladeOrigin;
        if (blades.isEmpty()) {
            hasBlades = false;
        }
        this.raiseRate = vehicleType.getRaiseRate();
        this.rotateX = rotateX;
        getArmorStands().addAll(blades);
        getArmorStands().add(bladeOrigin);
        blades.add(bladeOrigin);

        for (UUID uuid : blades) {
            getRelativeLocations().put(uuid, Bukkit.getEntity(uuid).getLocation().clone().subtract(getOrigin()));
            PersistentDataContainer persistentDataContainer = Bukkit.getEntity(uuid).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(Hybrid.get(), "vehicleId"), PersistentDataType.STRING, getId().toString());
            getOriginalOffsets().put(uuid, Bukkit.getEntity(uuid).getLocation().clone().getDirection());
        }

        getRelativeLocations().put(bladeOrigin, Bukkit.getEntity(bladeOrigin).getLocation().clone().subtract(getOrigin()));
        PersistentDataContainer persistentDataContainer = Bukkit.getEntity(bladeOrigin).getPersistentDataContainer();
        persistentDataContainer.set(new NamespacedKey(Hybrid.get(), "vehicleId"), PersistentDataType.STRING, getId().toString());
        getOriginalOffsets().put(bladeOrigin, Bukkit.getEntity(bladeOrigin).getLocation().clone().getDirection());
        fixBlades(0.1);
    }

    public void runDelayedActions() {
        for (UUID uuid : blades) {
            if (Bukkit.getEntity(uuid) == null) {
                continue;
            }
            getRelativeLocations().put(uuid, Bukkit.getEntity(uuid).getLocation().clone().subtract(getOrigin()));
            PersistentDataContainer persistentDataContainer = Bukkit.getEntity(uuid).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(Hybrid.get(), "vehicleId"), PersistentDataType.STRING, getId().toString());
            getOriginalOffsets().put(uuid, Bukkit.getEntity(uuid).getLocation().clone().getDirection());
        }
        if (Bukkit.getEntity(bladeOrigin) != null) {
            getRelativeLocations().put(bladeOrigin, Bukkit.getEntity(bladeOrigin).getLocation().clone().subtract(getOrigin()));
            PersistentDataContainer bladeContainer = Bukkit.getEntity(bladeOrigin).getPersistentDataContainer();
            bladeContainer.set(new NamespacedKey(Hybrid.get(), "vehicleId"), PersistentDataType.STRING, getId().toString());
            getOriginalOffsets().put(bladeOrigin, Bukkit.getEntity(bladeOrigin).getLocation().clone().getDirection());
        }

        for (UUID uuid : getOtherSeats()) {
            if (Bukkit.getEntity(uuid) == null) {
                continue;
            }
            getRelativeLocations().put(uuid, Bukkit.getEntity(uuid).getLocation().clone().subtract(getOrigin()));
            PersistentDataContainer persistentDataContainer = Bukkit.getEntity(uuid).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(Hybrid.get(), "vehicleId"), PersistentDataType.STRING, getId().toString());
            getOriginalOffsets().put(uuid, Bukkit.getEntity(uuid).getLocation().clone().getDirection());
        }
        for (UUID uuid : getArmorStands()) {
            if (Bukkit.getEntity(uuid) == null) {
                continue;
            }
            getRelativeLocations().put(uuid, Bukkit.getEntity(uuid).getLocation().clone().subtract(getOrigin()));
            PersistentDataContainer persistentDataContainer = Bukkit.getEntity(uuid).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(Hybrid.get(), "vehicleId"), PersistentDataType.STRING, getId().toString());
            getOriginalOffsets().put(uuid, Bukkit.getEntity(uuid).getLocation().clone().getDirection());
        }
        if (Bukkit.getEntity(getSeatUID()) != null) {
            getRelativeLocations().put(getSeatUID(), Bukkit.getEntity(getSeatUID()).getLocation().clone().subtract(getOrigin()));
            getOriginalOffsets().put(getSeatUID(), Bukkit.getEntity(getSeatUID()).getLocation().clone().getDirection());
        }
        for (int i = 0; i < 360; i++) {
            rotate(0.1);
        }
        if (getColor() != null) {
            dye(getColor());
        }
    }

    public void fixBlades(double angle) {
        for (int i = 0; i < 360; i++) {
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            for (UUID uuid : getRelativeLocations().keySet()) {
                if (!blades.contains(uuid)) {
                    continue;
                }
                Entity entity = Bukkit.getEntity(uuid);
                Location originalRelative = getRelativeLocations().get(uuid).clone().add(getOrigin());
                Location relativeToOrigin = originalRelative.clone().subtract(getOrigin());
                double x = relativeToOrigin.getX();
                double y = relativeToOrigin.getY();
                double z = relativeToOrigin.getZ();
                double newX = x * cos - z * sin;
                double newZ = x * sin + z * cos;
                Location destination = getOrigin().clone().add(newX, y, newZ);
                destination.setDirection(getRelativeLocations().get(uuid).clone().getDirection().rotateAroundY(-(angle)));
                Util.moveEntity(entity, destination);
                getRelativeLocations().put(uuid, entity.getLocation().clone().subtract(getOrigin()));
            }
        }
    }

    public void rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        Vector desiredForwards = getForwards().clone().rotateAroundY(-angle);
        setForwards(desiredForwards.clone());

        for (UUID uuid : getRelativeLocations().keySet()) {
            Entity entity = Bukkit.getEntity(uuid);
            Location originalRelative = getRelativeLocations().get(uuid).clone().add(getOrigin());
            Location relativeToOrigin = originalRelative.clone().subtract(getOrigin());
            double x = relativeToOrigin.getX();
            double y = relativeToOrigin.getY();
            double z = relativeToOrigin.getZ();
            double newX = x * cos - z * sin;
            double newZ = x * sin + z * cos;
            Location destination = getOrigin().clone().add(newX, y, newZ);
            destination.setDirection(getRelativeLocations().get(uuid).clone().getDirection().rotateAroundY(-(angle)));
            Util.moveEntity(entity, destination);
            getRelativeLocations().put(uuid, entity.getLocation().clone().subtract(getOrigin()));
        }
    }

    public void rotateBlades(double angle) {
        if (!hasBlades) {
            return;
        }
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        Entity entityOrigin = Bukkit.getEntity(bladeOrigin);
        for (UUID uuid : blades) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity == null) {
                return;
            }
            Location relativeToOrigin = entity.getLocation().clone().subtract(entityOrigin.getLocation().clone());
            Location destination;
            if (!rotateX) {
                double x = relativeToOrigin.getX();
                double y = relativeToOrigin.getY();
                double z = relativeToOrigin.getZ();
                double newX = x * cos - z * sin;
                double newZ = x * sin + z * cos;
                destination = entityOrigin.getLocation().clone().add(newX, y, newZ);
                destination.setDirection(getRelativeLocations().get(uuid).clone().getDirection().rotateAroundY(-(angle)));
            } else {
                destination = entityOrigin.getLocation().clone().add(relativeToOrigin.clone().toVector().clone().rotateAroundAxis(getForwards(), -angle));
            }
            Util.moveEntity(entity, destination);
            getRelativeLocations().put(uuid, entity.getLocation().clone().subtract(getOrigin()));
        }
    }

    public void update() {
        setPreviousPosition(getOrigin().clone());
        Entity seat = getSeat();
        boolean moved = false;
        boolean turned = false;
        boolean flew = false;
        if (seat == null) {
            return;
        }
        if (blades == null) {
            return;
        }
        if (!seat.getPassengers().isEmpty()) {

            Entity passenger = seat.getPassengers().get(0);
            double dot = passenger.getVelocity().setY(0).normalize().dot(passenger.getLocation().getDirection().setY(0).normalize());
            double turnDot = passenger.getVelocity().setY(0).normalize().dot(passenger.getLocation().getDirection().setY(0).rotateAroundY(90).normalize());
            double forwardsLeftTurnDot = passenger.getVelocity().setY(0).normalize().dot(passenger.getLocation().getDirection().setY(0).rotateAroundY(45).normalize());
            double forwardsRightTurnDot = passenger.getVelocity().setY(0).normalize().dot(passenger.getLocation().getDirection().setY(0).rotateAroundY(-45).normalize());

            if (forwardsRightTurnDot > 0.9) {
                move(false, true);
                rotate(getTurningSpeed() * getSpeed());
                setSpeedMultiplier(getSpeedMultiplier() * 0.9F);
                moved = true;
                turned = true;
            } else if (forwardsLeftTurnDot > 0.9) {
                move(false, true);
                rotate(-getTurningSpeed() * getSpeedMultiplier());
                setSpeedMultiplier(getSpeedMultiplier() * 0.9F);
                moved = true;
                turned = true;
            } else if (forwardsLeftTurnDot < -0.9) {
                move(true, true);
                rotate(-getTurningSpeed() * getSpeedMultiplier());
                setSpeedMultiplier(getSpeedMultiplier() * 0.9F);
                moved = true;
                turned = true;
            } else if (forwardsRightTurnDot < -0.9) {
                move(true, true);
                rotate(getTurningSpeed() * getSpeedMultiplier());
                setSpeedMultiplier(getSpeedMultiplier() * 0.9F);
                moved = true;
                turned = true;
            } else if (turnDot > 0.8) {
                rotate(-getTurningSpeed() * getSpeedMultiplier());
                turned = true;
            } else if (turnDot < -0.8) {
                rotate(getTurningSpeed() * getSpeedMultiplier());
                turned = true;
            } else if (dot > 0.9 && dot < 1) {
                move(false, true);
                moved = true;
            } else if (dot < -0.9 && dot > -1) {
                if (getSpeedMultiplier() <= 0) {
                    move(true, true);
                    moved = true;
                }
            }

            if (drivable) {
                if (passenger.getLocation().getPitch() < -20) {
                    double yAddition = raiseRate * getSpeedMultiplier();
                    if (getOrigin().clone().add(0, yAddition, 0).getBlock().getType().isAir()) {
                        for (UUID uuid : getRelativeLocations().keySet()) {
                            Entity entity = Bukkit.getEntity(uuid);
                            Location destination = entity.getLocation().clone().add(0, yAddition, 0);
                            Util.moveEntity(entity, destination);
                        }
                        getOrigin().add(0, yAddition, 0);
                    } else {
                        setSpeedMultiplier(getSpeedMultiplier() * 0.8F);
                    }
                } else if (passenger.getLocation().getPitch() < 20 && passenger.getLocation().getPitch() > -20) {
                    if (getSpeedMultiplier() > 0.8 && getOrigin().clone().subtract(0, getGroundDistance(), 0).getBlock().getType().isAir()) {
                        flew = true;
                    }
                }
            }
        } else {
            drivable = false;
        }
        rotateBlades(0.4 * getSpeedMultiplier());

        if (!moved) {
            if (getSpeedMultiplier() >= 0) {
                move(false, false);
                setSpeedMultiplier(getSpeedMultiplier() - (float) getFrictionRate());
                if (getSpeedMultiplier() < 0.001) {
                    setSpeedMultiplier(0);
                }
            } else {
                move(true, false);
                setSpeedMultiplier(getSpeedMultiplier() + (float) getFrictionRate());
                if (getSpeedMultiplier() < -0.001) {
                    setSpeedMultiplier(0);
                }
            }
        }
        if (getSpeedMultiplier() > 1) {
            setSpeedMultiplier(getSpeedMultiplier() * 0.9F);
        }
        if (getSpeedMultiplier() < -0.5) {
            setSpeedMultiplier(getSpeedMultiplier() * 0.9F);
        }
        if (!flew) {
            if (!getOrigin().clone().subtract(0, getGroundDistance(), 0).getBlock().getType().isSolid()) {
                int times = 0;
                double checks = gravityUpdateRate - (gravityUpdateRate * getSpeedMultiplier());
                Location testLoc = getOrigin().clone().subtract(0, getGroundDistance(), 0);
                while (!testLoc.clone().getBlock().getType().isSolid()) {
                    testLoc.add(0, -getGroundDistance(), 0);
                    times++;
                    if (times > checks) {
                        break;
                    }
                }
                for (UUID uuid : getRelativeLocations().keySet()) {
                    Entity entity = Bukkit.getEntity(uuid);
                    Location destination = entity.getLocation().clone().subtract(0, getGroundDistance() * times, 0);
                    Util.moveEntity(entity, destination);
                }
                getOrigin().subtract(0, getGroundDistance() * times, 0);
            }
        }

        setCurrentLocation(getOrigin().clone());
        getOrigin().getWorld().spawnParticle(Particle.REDSTONE, getOrigin(), 0, new Particle.DustOptions(Color.fromBGR(100, 100, 100), 2 * getSpeedMultiplier()));
        getOrigin().getWorld().spawnParticle(Particle.REDSTONE, getOrigin().clone().add(getForwards().clone().normalize().multiply(0.25)), 0, new Particle.DustOptions(Color.fromBGR(0, 0, 0), 1 * getSpeedMultiplier()));
    }
}
