package com.floodeer.hybrid.game.payload;

import com.floodeer.hybrid.Hybrid;
import com.floodeer.hybrid.game.Game;
import com.floodeer.hybrid.utils.Util;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class PayloadVehicle {

    private final List<UUID> probablyTires = Lists.newArrayList();
    @Getter
    private final Map<UUID, Location> relativeLocations = Maps.newHashMap();
    @Getter
    private final Map<UUID, Vector> originalOffsets = Maps.newHashMap();
    @Getter
    private final UUID id;
    @Getter
    private final UUID seat;
    @Getter
    private final double groundDistance = 0.1;
    @Getter
    private final double frictionRate = 0.02;
    @Getter
    private final double gravityUpdateRate = 5;
    private final double turnMultiplier = 0;
    @Getter
    private List<UUID> armorStands = Lists.newArrayList();
    @Getter
    private List<UUID> otherSeats = Lists.newArrayList();
    @Getter
    private Location origin;
    @Getter
    @Setter
    private Location previousPosition;
    @Getter
    @Setter
    private Location currentLocation;
    @Getter
    @Setter
    private Vector forwards;
    @Getter
    private String name;
    @Getter
    private double speed = 0.4;
    @Getter
    @Setter
    private float speedMultiplier = 0;
    @Getter
    private double gainRate = 0.01;
    @Getter
    private double turningSpeed = 0.15;
    @Getter
    private double stepHeight = 1;
    private boolean crashed = false;
    private boolean checkedForTires = false;
    @Getter
    private String color = null;
    @Getter
    private String enumName = "";

    @Getter
    @Setter
    private UUID owner;

    private PayloadType type;

    public PayloadVehicle(UUID seat, List<UUID> armorStands, Vector forwards, Location origin, String name, List<UUID> extraSeats, PayloadType type, UUID owner, UUID vehicleId) {
        this.id = vehicleId;
        this.otherSeats = extraSeats;
        this.seat = seat;
        this.name = name;
        this.armorStands = armorStands;
        this.forwards = forwards;
        this.origin = origin.clone();
        if (type != null) {
            this.enumName = type.name();
            this.speed = type.speed;
            this.name = type.name;
            this.gainRate = type.gainRate;
            this.turningSpeed = type.turnRate;
            this.stepHeight = type.stepHeight;
            this.type = type;
        }


        for (UUID uuid : extraSeats) {
            relativeLocations.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().subtract(origin));
            PersistentDataContainer persistentDataContainer = Bukkit.getEntity(uuid).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(Hybrid.get(), "vehicleId"), PersistentDataType.STRING, id.toString());
            originalOffsets.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().getDirection());
        }

        for (UUID uuid : armorStands) {
            relativeLocations.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().subtract(origin));
            PersistentDataContainer persistentDataContainer = Bukkit.getEntity(uuid).getPersistentDataContainer();
            persistentDataContainer.set(new NamespacedKey(Hybrid.get(), "vehicleId"), PersistentDataType.STRING, id.toString());
            originalOffsets.put(uuid, Bukkit.getEntity(uuid).getLocation().clone().getDirection());
        }
        relativeLocations.put(seat, Bukkit.getEntity(seat).getLocation().clone().subtract(origin));
        originalOffsets.put(seat, Bukkit.getEntity(seat).getLocation().clone().getDirection());

        for (int i = 0; i < 360; i++) {
            rotate(0.1);
        }
    }

    public void dye(String color) {
        this.color = color;
        if (!checkedForTires) {
            for (UUID uuid : relativeLocations.keySet()) {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity != null) {
                    if (!isSeat(uuid)) {
                        ArmorStand armorStand = (ArmorStand) entity;
                        if (armorStand.getEquipment().getHelmet() != null) {
                            if (armorStand.getEquipment().getHelmet().getType().equals(Material.BLACK_CONCRETE)) {
                                probablyTires.add(uuid);
                            }
                        }
                    }
                }
            }
            checkedForTires = true;
        }
        Map<Material, Integer> materialCount = Maps.newHashMap();
        for (UUID uuid : relativeLocations.keySet()) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
                if (!isSeat(uuid) && !probablyTires.contains(uuid)) {
                    ArmorStand armorStand = (ArmorStand) entity;
                    if (armorStand.getEquipment().getHelmet() != null) {
                        materialCount.putIfAbsent(armorStand.getEquipment().getHelmet().getType(), 0);
                        materialCount.put(armorStand.getEquipment().getHelmet().getType(), materialCount.get(armorStand.getEquipment().getHelmet().getType()) + 1);
                    }
                }
            }
        }
        Material majorityConcrete = null;
        int highestConcrete = -1;
        Material majorityCarpet = null;
        int highestCarpet = -1;
        for (Material material : materialCount.keySet()) {
            if (materialCount.get(material) > highestConcrete && material.toString().toLowerCase().contains("concrete") && !material.toString().toLowerCase().contains("powder")) {
                majorityConcrete = material;
                highestConcrete = materialCount.get(material);
            }
            if (materialCount.get(material) > highestCarpet && material.toString().toLowerCase().contains("carpet")) {
                majorityCarpet = material;
                highestCarpet = materialCount.get(material);
            }
        }

        for (UUID uuid : relativeLocations.keySet()) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
                if (!isSeat(uuid) && !probablyTires.contains(uuid)) {
                    ArmorStand armorStand = (ArmorStand) entity;
                    if (armorStand.getEquipment().getHelmet() != null) {
                        Material material = armorStand.getEquipment().getHelmet().getType();
                        if (material.equals(majorityConcrete)) {
                            armorStand.getEquipment().setHelmet(new ItemStack(Material.valueOf(color.toUpperCase() + "_CONCRETE")));
                        } else if (material.equals(majorityCarpet)) {
                            armorStand.getEquipment().setHelmet(new ItemStack(Material.valueOf(color.toUpperCase() + "_CARPET")));
                        }
                        if (majorityConcrete != null) {
                            String carpetCheck = majorityConcrete.toString().toLowerCase().replace("_concrete", "_carpet");
                            if (material.equals(Material.valueOf(carpetCheck.toUpperCase()))) {
                                armorStand.getEquipment().setHelmet(new ItemStack(Material.valueOf(color.toUpperCase() + "_CARPET")));
                            }
                        }
                    }
                }
            }
        }
    }

    public void remove(boolean clearId) {
        for (UUID uuid : relativeLocations.keySet()) {
            if (Bukkit.getEntity(uuid) != null) {
                Bukkit.getEntity(uuid).remove();
            }
        }
    }

    public void attemptSit(Player player) {
        if (getSeat().getPassengers().isEmpty()) {
            getSeat().addPassenger(player);
        } else {
            if (otherSeats.isEmpty()) {
                return;
            }
            for (UUID uuid : otherSeats) {
                if (Bukkit.getEntity(uuid).getPassengers().isEmpty()) {
                    Bukkit.getEntity(uuid).addPassenger(player);
                }
            }
        }
    }

    public Entity getSeat() {
        return Bukkit.getEntity(seat);
    }

    public UUID getSeatUID() {
        return seat;
    }

    public void rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        Vector desiredForwards = forwards.clone().rotateAroundY(-angle);
        forwards = desiredForwards.clone();

        for (UUID uuid : relativeLocations.keySet()) {
            Entity entity = Bukkit.getEntity(uuid);
            Location originalRelative = relativeLocations.get(uuid).clone().add(origin);
            Location relativeToOrigin = originalRelative.clone().subtract(origin);
            double x = relativeToOrigin.getX();
            double y = relativeToOrigin.getY();
            double z = relativeToOrigin.getZ();
            double newX = x * cos - z * sin;
            double newZ = x * sin + z * cos;
            Location destination = origin.clone().add(newX, y, newZ);

            Util.moveEntity(entity, destination);
            relativeLocations.put(uuid, entity.getLocation().clone().subtract(origin));
        }
    }

    private boolean isSeat(UUID uuid) {
        if (otherSeats.contains(uuid)) {
            return true;
        }
        return uuid.equals(seat);
    }

    public void move(boolean negative, boolean modify) {
        double desiredSpeed = speed;
        desiredSpeed *= speedMultiplier;
        Location floorCheck = origin.clone().add(forwards.clone().multiply(desiredSpeed));
        Location aboveCheck = floorCheck.clone().add(0, stepHeight, 0);
        if (floorCheck.getBlock().getType().isSolid()) {
            if (aboveCheck.getBlock().getType().isSolid()) {
                if (!crashed) {
                    if (speedMultiplier > 0.9) {
                        crashed = true;
                        origin.getWorld().playSound(origin, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                        origin.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, origin, 3);
                    }
                }
                speedMultiplier = 0;
            } else {
                origin = aboveCheck;
                for (UUID uuid : armorStands) {
                    ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(uuid);
                    armorStand.teleport(armorStand.getLocation().clone().add(forwards.clone().normalize().multiply(desiredSpeed)).add(0, stepHeight, 0));
                }
                for (UUID uuid : otherSeats) {
                    ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(uuid);
                    Location destination = armorStand.getLocation().clone().add(forwards.clone().normalize().multiply(desiredSpeed)).add(0, stepHeight, 0);
                    Util.moveEntity(armorStand, destination);
                }
                Entity seat = Bukkit.getEntity(this.seat);
                Location seatDestination = seat.getLocation().clone().add(forwards.clone().normalize().multiply(desiredSpeed)).add(0, stepHeight, 0);
                seatDestination.setDirection(forwards);
                if (!seat.getPassengers().isEmpty()) {
                    Util.moveEntity(seat, seatDestination);
                } else {
                    seat.teleport(seatDestination);
                }
                if (modify) {
                    speedMultiplier += (float) gainRate;
                }
            }
            return;
        }
        for (UUID uuid : armorStands) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(uuid);
            if (armorStand != null) {
                armorStand.teleport(armorStand.getLocation().clone().add(forwards.clone().normalize().multiply(desiredSpeed)));
            }

        }
        for (UUID uuid : otherSeats) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(uuid);
            if (armorStand != null) {
                Location destination = armorStand.getLocation().clone().add(forwards.clone().normalize().multiply(desiredSpeed));
                Util.moveEntity(armorStand, destination);
            }
        }
        Entity seat = Bukkit.getEntity(this.seat);
        if (seat == null) {
            return;
        }
        Location seatDestination = seat.getLocation().clone().add(forwards.clone().normalize().multiply(desiredSpeed));
        seatDestination.setDirection(forwards);
        if (!seat.getPassengers().isEmpty()) {
            Util.moveEntity(seat, seatDestination);
        } else {
            seat.teleport(seatDestination);
        }
        origin.add(forwards.clone().normalize().multiply(desiredSpeed));
        if (modify) {
            if (negative) {
                speedMultiplier -= (float) gainRate;
            } else {
                speedMultiplier += (float) gainRate;
            }
        }
        crashed = false;
    }

    public void runDelayedActions() {
    }

    public void update(Game game, int pathIndex) {
        previousPosition = origin.clone();

        boolean moved = false;
        boolean turned = false;

        // Verifique se há um próximo ponto no caminho
        if (pathIndex < game.getArena().getPath().size()) {
            Location targetLocation = game.getArena().getPath().get(pathIndex);

            // Verifique se o veículo chegou perto o suficiente do próximo ponto no caminho
            double distanceSquared = origin.distanceSquared(targetLocation);
            if (distanceSquared < 1.5) {
                pathIndex++;

                // Verifique se há mais pontos no caminho
                if (pathIndex < game.getArena().getPath().size()) {
                    targetLocation = game.getArena().getPath().get(pathIndex);
                } else {
                    // Caso contrário, o veículo atingiu o final do caminho
                    // Adicione a lógica de finalização do jogo aqui se necessário
                    return;
                }
            }

            // Calcule a direção em que o veículo deve se mover
            Vector directionToTarget = targetLocation.toVector().subtract(origin.toVector()).normalize();

            // Atualize a posição do veículo
            updateEntityPositions(speedMultiplier * 0.03, false);

            // Atualize a direção do veículo para apontar para o próximo ponto no caminho
            setForwards(directionToTarget);

            // Marque que o veículo se moveu e virou
            moved = true;
            turned = true;
        }

        // Resto do código permanece inalterado, remova a lógica do jogador

        // ...

        // Atualize a posição atual do veículo
        currentLocation = origin.clone();

        // Adicione o restante do código para visualização, como partículas
        origin.getWorld().spawnParticle(Particle.REDSTONE, origin, 0, new Particle.DustOptions(Color.fromBGR(100, 100, 100), 2 * speedMultiplier));
        origin.getWorld().spawnParticle(Particle.REDSTONE, origin.clone().add(forwards.clone().normalize().multiply(0.25)), 0, new Particle.DustOptions(Color.fromBGR(0, 0, 0), 1 * speedMultiplier));
    }

    public void move(Location target) {
        forwards = target.clone().add(0, stepHeight, 0).toVector().subtract(origin.toVector()).normalize();
        double desiredSpeed = speed;
        desiredSpeed *= speedMultiplier;
        Location floorCheck = origin.clone().add(forwards.clone().multiply(desiredSpeed));
        Location aboveCheck = floorCheck.clone().add(0, stepHeight, 0);

        if (floorCheck.getBlock().getType().isSolid() || aboveCheck.getBlock().getType().isSolid()) {
            handleCollision(desiredSpeed, aboveCheck.getBlock().getType().isSolid());
        } else {
            updateEntityPositions(desiredSpeed, false);
        }

        Vector directionToTarget = target.toVector().subtract(origin.toVector()).normalize();
        setForwards(directionToTarget);

    }
    private void handleCollision(double desiredSpeed, boolean moveUp) {
        if (!crashed && speedMultiplier > 0.9) {
            crashed = true;
            origin.getWorld().playSound(origin, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
            origin.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, origin, 3);
        }
        speedMultiplier = 0;

        if (moveUp) {
            origin = origin.clone().add(0, stepHeight, 0);
        }

        updateEntityPositions(desiredSpeed, moveUp);
    }

    private void updateEntityPositions(double desiredSpeed, boolean moveUp) {
        for (UUID uuid : armorStands) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(uuid);
            if (armorStand != null) {
                Location destination = armorStand.getLocation().clone().add(forwards.clone().normalize().multiply(desiredSpeed));
                if (moveUp) {
                    destination.add(0, stepHeight, 0);
                }
                Util.moveEntity(armorStand, destination);
            }
        }

        for (UUID uuid : otherSeats) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(uuid);
            if (armorStand != null) {
                Location destination = armorStand.getLocation().clone().add(forwards.clone().normalize().multiply(desiredSpeed));
                if (moveUp) {
                    destination.add(0, stepHeight, 0);
                }
                Util.moveEntity(armorStand, destination);
            }
        }

        Entity seat = Bukkit.getEntity(this.seat);
        if (seat != null) {
            Location seatDestination = seat.getLocation().clone().add(forwards.clone().normalize().multiply(desiredSpeed));
            if (moveUp) {
                seatDestination.add(0, stepHeight, 0);
            }
            seatDestination.setDirection(forwards);
            if (!seat.getPassengers().isEmpty()) {
                Util.moveEntity(seat, seatDestination);
            } else {
                seat.teleport(seatDestination);
            }
        }

        origin.add(forwards.clone().normalize().multiply(desiredSpeed));
        if (moveUp) {
            origin.add(0, stepHeight, 0);
        }

        speedMultiplier += (float) gainRate;
        crashed = false;
    }


    public void update() {
        previousPosition = origin.clone();
        Entity seat = getSeat();
        boolean moved = false;
        boolean turned = false;
        if (seat == null) {
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
                rotate(turningSpeed * speedMultiplier);
                speedMultiplier *= 0.999F;
                moved = true;
                turned = true;
            } else if (forwardsLeftTurnDot > 0.9) {
                move(false, true);
                rotate(-turningSpeed * speedMultiplier);
                speedMultiplier *= 0.999F;
                moved = true;
                turned = true;
            } else if (forwardsLeftTurnDot < -0.9) {
                move(true, true);
                rotate(-turningSpeed * speedMultiplier);
                speedMultiplier *= 0.999F;
                moved = true;
                turned = true;
            } else if (forwardsRightTurnDot < -0.9) {
                move(true, true);
                rotate(turningSpeed * speedMultiplier);
                speedMultiplier *= 0.999F;
                moved = true;
                turned = true;
            } else if (turnDot > 0.8) {
                rotate(-turningSpeed * speedMultiplier);
                turned = true;
            } else if (turnDot < -0.8) {
                rotate(turningSpeed * speedMultiplier);
                turned = true;
            } else if (dot > 0.9 && dot < 1) {
                move(false, true);
                moved = true;
            } else if (dot < -0.9 && dot > -1) {
                if (speedMultiplier <= 0) {
                    move(true, true);
                    moved = true;
                }
            }

        }
        if (!moved) {
            if (speedMultiplier >= 0) {
                move(false, false);
                speedMultiplier -= (float) frictionRate;
                if (speedMultiplier < 0.001) {
                    speedMultiplier = 0;
                }
            } else {
                move(true, false);
                speedMultiplier += (float) frictionRate;
                if (speedMultiplier > -0.001) {
                    speedMultiplier = 0;
                }
            }
        }
        if (speedMultiplier > 1) {
            speedMultiplier *= 0.9;
        }
        if (speedMultiplier < -0.5) {
            speedMultiplier *= 0.9;
        }
        if (!origin.clone().subtract(0, groundDistance, 0).getBlock().getType().isSolid()) {
            int times = 0;
            Location testLoc = origin.clone().subtract(0, groundDistance, 0);
            while (!testLoc.clone().getBlock().getType().isSolid()) {
                testLoc.add(0, -groundDistance, 0);
                times++;
                if (times > gravityUpdateRate) {
                    break;
                }
            }
            for (UUID uuid : relativeLocations.keySet()) {
                Entity entity = Bukkit.getEntity(uuid);
                Location destination = entity.getLocation().clone().subtract(0, groundDistance * times, 0);
                Util.moveEntity(entity, destination);
            }
            origin.subtract(0, groundDistance * times, 0);
        }
        currentLocation = origin.clone();
        origin.getWorld().spawnParticle(Particle.REDSTONE, origin, 0, new Particle.DustOptions(Color.fromBGR(100, 100, 100), 2 * speedMultiplier));
        origin.getWorld().spawnParticle(Particle.REDSTONE, origin.clone().add(forwards.clone().normalize().multiply(0.25)), 0, new Particle.DustOptions(Color.fromBGR(0, 0, 0), 1 * speedMultiplier));
    }

    @Getter
    public enum PayloadType {
        CAR("car", 0.3, 0.1, 0.01, 1),
        FORMULA_CAR("formula_car", 20, 0.2, 0.01, 1),
        PLANE("plane", 1.0, 0.1, 0.1, 1, false, 0.4),
        BIPLANE("biplane", 0.9, 0.1, 0.003, 1, true, 0.2);

        private final String name;
        @Getter
        private final double speed;
        @Getter
        private final double turnRate;
        @Getter
        private final double gainRate;
        @Getter
        private final double stepHeight;
        @Getter
        private double raiseRate = 0;
        @Getter
        private boolean rotateX = false;

        PayloadType(String name, double speed, double turnRate, double gainRate, double stepHeight) {
            this.name = name;
            this.speed = speed;
            this.turnRate = turnRate;
            this.gainRate = gainRate;
            this.stepHeight = stepHeight;
        }

        PayloadType(String name, double speed, double turnRate, double gainRate, double stepHeight, boolean rotateX, double raiseRate) {
            this.name = name;
            this.speed = speed;
            this.turnRate = turnRate;
            this.gainRate = gainRate;
            this.stepHeight = stepHeight;
            this.rotateX = rotateX;
            this.raiseRate = raiseRate;
        }
    }
}
