package com.floodeer.hybrid.game.payload;

import com.floodeer.hybrid.Hybrid;
import com.floodeer.hybrid.utils.Util;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

public class VehicleLoader {

    private final List<UUID> selectedStands = Lists.newArrayList();
    private final Location origin;
    private final Vector forwards = new Vector(1, 0, 0);
    @Getter
    private final HashMap<Integer, UUID> allArmorStands = Maps.newHashMap();
    @Getter
    private final List<UUID> seats = Lists.newArrayList();
    @Getter
    private final List<UUID> blades = Lists.newArrayList();
    VehicleSettings creationSettings = new VehicleSettings();
    private Location seat;
    private int lastInt = 0;
    private int indexCycle = 0;
    @Getter
    private UUID bladeOrigin = null;

    public VehicleLoader(Location origin) {
        this.origin = origin;
        this.seat = origin.clone().add(0, 0.5, 0);
    }

    public static void loadCreation(String name, Location origin, Player owner) {
        List<UUID> seats = loadSeats(name, origin, null);
        List<UUID> armorStands = loadArmorStands(name, origin, null);
        List<UUID> blades = loadBlades(name, origin, null);
        UUID bladeOrigin = loadBladeOrigin(name, origin, null);
        Vector seatOffset = Util.convertToVector(Hybrid.get().getVehiclesConfig().getString(name + ".seat"));
        VehicleLoader armorStandCreation = new VehicleLoader(origin);
        armorStandCreation.addAll(armorStands);
        armorStandCreation.addAll(seats);
        armorStandCreation.addAll(blades);
        armorStandCreation.addAll(new ArrayList<>(Collections.singletonList(bladeOrigin)));
        armorStandCreation.seat = origin.clone().add(seatOffset);
        armorStandCreation.blades.addAll(blades);
        armorStandCreation.bladeOrigin = bladeOrigin;
        armorStandCreation.seats.addAll(seats);
    }

    public static List<UUID> loadSeats(String name, Location origin, UUID uuid) {
        int seatCount = 0;
        try {
            seatCount = Hybrid.get().getVehiclesConfig().getInt(name + ".extraseatcount");
        } catch (Exception e) {
            // ignore
        }
        List<UUID> extraSeats = new ArrayList<>();
        for (int i = 0; i < seatCount; i++) {
            String exs = Hybrid.get().getVehiclesConfig().getString(name + ".exs." + i);
            String[] exsSplit = exs.split(";");
            Vector exsVector = new Vector(Double.parseDouble(exsSplit[0]), Double.parseDouble(exsSplit[1]), Double.parseDouble(exsSplit[2]));
            ArmorStand extraSeat = (ArmorStand) origin.getWorld().spawnEntity(origin.clone().add(exsVector), EntityType.ARMOR_STAND);
            extraSeat.setGravity(false);
            extraSeat.setVisible(false);
            extraSeat.setBasePlate(false);
            extraSeat.setArms(true);
            extraSeat.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            extraSeat.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            extraSeats.add(extraSeat.getUniqueId());

            if (uuid != null) {
                PersistentDataContainer persistentDataContainer = extraSeat.getPersistentDataContainer();
                persistentDataContainer.set(new NamespacedKey(Hybrid.get(), "vehicleId"), PersistentDataType.STRING, uuid.toString());
            }
        }

        return extraSeats;
    }

    public static UUID loadBladeOrigin(String name, Location origin, UUID uuid) {
        UUID armorStandId = null;
        int count = Hybrid.get().getVehiclesConfig().getInt(name + ".count");
        for (int i = 0; i < count; i++) {
            String key = Hybrid.get().getVehiclesConfig().getString(name + ".am." + i);
            if (!key.endsWith("bladeOrigin")) {
                continue;
            }
            String[] split = key.split(";");
            Vector vector = new Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
            Vector direction = new Vector(Double.parseDouble(split[3]), Double.parseDouble(split[4]), Double.parseDouble(split[5]));
            EulerAngle leftArm = new EulerAngle(Double.parseDouble(split[6]), Double.parseDouble(split[7]), Double.parseDouble(split[8]));
            EulerAngle rightArm = new EulerAngle(Double.parseDouble(split[9]), Double.parseDouble(split[10]), Double.parseDouble(split[11]));
            ArmorStand armorStand = (ArmorStand) origin.getWorld().spawnEntity(origin.clone().add(vector), EntityType.ARMOR_STAND);
            Location location = armorStand.getLocation().clone();
            location.setDirection(direction);
            armorStand.teleport(location);
            armorStand.setLeftArmPose(leftArm);
            armorStand.setRightArmPose(rightArm);
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.setBasePlate(false);
            armorStand.setArms(true);
            armorStand.setCustomName("Part");
            armorStand.setCustomNameVisible(false);
            armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.getEquipment().setHelmet(new ItemStack(Material.valueOf(split[12])));
            armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.valueOf(split[13])));
            armorStand.getEquipment().setItemInOffHand(new ItemStack(Material.valueOf(split[14])));
            armorStandId = armorStand.getUniqueId();
            if (uuid != null) {
                PersistentDataContainer persistentDataContainer = armorStand.getPersistentDataContainer();
                persistentDataContainer.set(new NamespacedKey(Hybrid.get(), "vehicleId"), PersistentDataType.STRING, uuid.toString());
            }
        }
        return armorStandId;
    }

    public static List<UUID> loadBlades(String name, Location origin, UUID uuid) {
        List<UUID> armorStands = new ArrayList<>();
        int count = Hybrid.get().getVehiclesConfig().getInt(name + ".count");
        for (int i = 0; i < count; i++) {
            String key = Hybrid.get().getVehiclesConfig().getString(name + ".am." + i);
            if (!key.endsWith("blade")) {
                continue;
            }
            String[] split = key.split(";");
            Vector vector = new Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
            Vector direction = new Vector(Double.parseDouble(split[3]), Double.parseDouble(split[4]), Double.parseDouble(split[5]));
            EulerAngle leftArm = new EulerAngle(Double.parseDouble(split[6]), Double.parseDouble(split[7]), Double.parseDouble(split[8]));
            EulerAngle rightArm = new EulerAngle(Double.parseDouble(split[9]), Double.parseDouble(split[10]), Double.parseDouble(split[11]));
            ArmorStand armorStand = (ArmorStand) origin.getWorld().spawnEntity(origin.clone().add(vector), EntityType.ARMOR_STAND);
            Location location = armorStand.getLocation().clone();
            location.setDirection(direction);
            armorStand.teleport(location);
            armorStand.setLeftArmPose(leftArm);
            armorStand.setRightArmPose(rightArm);
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.setBasePlate(false);
            armorStand.setArms(true);
            armorStand.setCustomName("Part");
            armorStand.setCustomNameVisible(false);
            armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.getEquipment().setHelmet(new ItemStack(Material.valueOf(split[12])));
            armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.valueOf(split[13])));
            armorStand.getEquipment().setItemInOffHand(new ItemStack(Material.valueOf(split[14])));
            armorStands.add(armorStand.getUniqueId());

            if (uuid != null) {
                PersistentDataContainer persistentDataContainer = armorStand.getPersistentDataContainer();
                persistentDataContainer.set(new NamespacedKey(Hybrid.get(), "vehicleId"), PersistentDataType.STRING, uuid.toString());
            }
        }
        return armorStands;
    }

    public static List<UUID> loadArmorStands(String name, Location origin, UUID uuid) {
        List<UUID> armorStands = new ArrayList<>();
        int count = Hybrid.get().getVehiclesConfig().getInt(name + ".count");
        for (int i = 0; i < count; i++) {
            String key = Hybrid.get().getVehiclesConfig().getString(name + ".am." + i);
            if (key.equals("seat")) {
                continue;
            }
            if (key.endsWith("blade") || key.endsWith("bladeOrigin")) {
                continue;
            }
            String[] split = key.split(";");
            Vector vector = new Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
            Vector direction = new Vector(Double.parseDouble(split[3]), Double.parseDouble(split[4]), Double.parseDouble(split[5]));
            EulerAngle leftArm = new EulerAngle(Double.parseDouble(split[6]), Double.parseDouble(split[7]), Double.parseDouble(split[8]));
            EulerAngle rightArm = new EulerAngle(Double.parseDouble(split[9]), Double.parseDouble(split[10]), Double.parseDouble(split[11]));
            ArmorStand armorStand = (ArmorStand) origin.getWorld().spawnEntity(origin.clone().add(vector), EntityType.ARMOR_STAND);
            Location location = armorStand.getLocation().clone();
            location.setDirection(direction);
            armorStand.teleport(location);
            armorStand.setLeftArmPose(leftArm);
            armorStand.setRightArmPose(rightArm);
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.setBasePlate(false);
            armorStand.setArms(true);
            armorStand.setCustomName("Part");
            armorStand.setCustomNameVisible(false);
            armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.getEquipment().setHelmet(new ItemStack(Material.valueOf(split[12])));
            armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.valueOf(split[13])));
            armorStand.getEquipment().setItemInOffHand(new ItemStack(Material.valueOf(split[14])));
            armorStands.add(armorStand.getUniqueId());

            if (uuid != null) {
                PersistentDataContainer persistentDataContainer = armorStand.getPersistentDataContainer();
                persistentDataContainer.set(new NamespacedKey(Hybrid.get(), "vehicleId"), PersistentDataType.STRING, uuid.toString());
            }
        }
        return armorStands;
    }

    public static PayloadVehicle load(String name, Location origin, PayloadVehicle.PayloadType vehicle, UUID owner) {

        UUID uuid = UUID.randomUUID();
        String seat = Hybrid.get().getVehiclesConfig().getString(name + ".seat");
        String[] seatSplit = seat.split(";");
        Vector seatVector = new Vector(Double.parseDouble(seatSplit[0]), Double.parseDouble(seatSplit[1]), Double.parseDouble(seatSplit[2]));

        List<UUID> armorStands = loadArmorStands(name, origin, uuid);
        List<UUID> extraSeats = loadSeats(name, origin, uuid);
        List<UUID> blades = loadBlades(name, origin, uuid);
        UUID bladeOrigin = loadBladeOrigin(name, origin, uuid);

        ArmorStand center = origin.getWorld().spawn(origin.clone().add(seatVector.clone().subtract(new Vector(0, 0.7, 0))), ArmorStand.class);
        center.setGravity(false);
        center.setVisible(false);
        center.setBasePlate(false);
        center.setArms(true);
        center.getEquipment().setHelmet(new ItemStack(Material.AIR));
        center.setCustomName("Seat");
        center.setCustomNameVisible(false);
        center.setRemoveWhenFarAway(false);
        center.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
        center.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        center.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
        center.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
        center.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
        center.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
        center.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
        center.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
        center.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
        center.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
        center.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
        center.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
        PersistentDataContainer persistentDataContainer = center.getPersistentDataContainer();
        persistentDataContainer.set(new NamespacedKey(Hybrid.get(), "vehicleId"), PersistentDataType.STRING, uuid.toString());

        if (bladeOrigin != null) {
            return new PlanePayload(center.getUniqueId(), armorStands, new Vector(1, 0, 0), origin.clone(), name, extraSeats, vehicle, owner, uuid, blades, bladeOrigin, vehicle.isRotateX());
        }
        return new PayloadVehicle(center.getUniqueId(), armorStands, new Vector(1, 0, 0), origin.clone(), name, extraSeats, vehicle, owner, uuid);
    }

    public VehicleSettings getSettings() {
        return creationSettings;
    }

    public void moveAll(double x, double y, double z) {
        for (UUID uuid : allArmorStands.values()) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(uuid);
            armorStand.teleport(armorStand.getLocation().clone().add(x, y, z));
        }
    }

    public void makeBlade() {
        if (selectedStands.isEmpty()) {
            return;
        }
        for (UUID uuid : selectedStands) {
            if (uuid.equals(bladeOrigin)) {
                continue;
            }
            blades.add(uuid);
        }
    }

    public void makeBladeOrigin() {
        bladeOrigin = selectedStands.get(0);
    }

    public void moveSeat(double x, double y, double z) {
        seat.add(new Vector(x, y, z));
    }

    public void deselect(int id) {
        for (Map.Entry<Integer, UUID> entry : allArmorStands.entrySet()) {
            if (selectedStands.contains(entry.getValue())) {
                if (entry.getKey() == id) {
                    selectedStands.remove(entry.getValue());
                    return;
                }
            }
        }
    }

    public void select(int id) {
        for (Map.Entry<Integer, UUID> entry : allArmorStands.entrySet()) {
            if (entry.getKey() == id) {
                selectedStands.add(entry.getValue());
                return;
            }
        }
    }

    public void clearVisibleIds() {
        for (Map.Entry<Integer, UUID> entry : allArmorStands.entrySet()) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(entry.getValue());
            armorStand.setCustomName(entry.getKey() + "#");
            armorStand.setCustomNameVisible(false);
        }
    }

    public void displayIds(String block) {
        clearVisibleIds();
        for (Map.Entry<Integer, UUID> entry : allArmorStands.entrySet()) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(entry.getValue());
            if (armorStand.getEquipment().getHelmet().getType().toString().toLowerCase().contains(block.toLowerCase())) {
                armorStand.setCustomName(entry.getKey() + "#");
                armorStand.setCustomNameVisible(true);
            }
        }
    }

    public void addAll(List<UUID> uuids) {
        for (UUID uuid : uuids) {
            allArmorStands.put(lastInt, uuid);
            lastInt++;
        }
        selectedStands.clear();
        selectedStands.add(uuids.get(0));
    }

    public void cycle() {
        selectedStands.clear();
        selectedStands.add(allArmorStands.get(indexCycle));
        indexCycle++;
        if (indexCycle == allArmorStands.size()) {
            indexCycle = 0;
        }
    }

    public void cycle(String block) {
        List<Integer> availableIntegers = new ArrayList<>();
        for (Map.Entry<Integer, UUID> entry : allArmorStands.entrySet()) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(entry.getValue());
            if (armorStand.getEquipment().getHelmet().getType().toString().toLowerCase().contains(block.toLowerCase())) {
                availableIntegers.add(entry.getKey());
            }
        }
        availableIntegers.sort(Comparator.naturalOrder());
        for (int i = 0; i < availableIntegers.size(); i++) {
            int index = availableIntegers.get(i);
            if (indexCycle == index) {
                if (availableIntegers.size() > i + 1) {
                    index = availableIntegers.get(i + 1);
                } else {
                    indexCycle = 0;
                    index = availableIntegers.get(0);
                }
            }
            Bukkit.broadcastMessage("Comparing indexCycle: " + indexCycle + " to index: " + index);
            if (indexCycle < index) {
                Bukkit.broadcastMessage("IndexCycle is less than index");
                selectedStands.clear();
                selectedStands.add(allArmorStands.get(index));
                indexCycle = index;
                break;
            }
        }
        if (indexCycle >= availableIntegers.get(availableIntegers.size() - 1)) {
            indexCycle = -1;
        }
    }

    public void makeArmorStand() {
        ArmorStand armorStand = (ArmorStand) origin.getWorld().spawnEntity(origin.clone(), EntityType.ARMOR_STAND);
        armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.setRightArmPose(new EulerAngle(0, 0, 0));
        armorStand.setLeftArmPose(new EulerAngle(0, 0, 0));
        armorStand.teleport(armorStand.getLocation().clone().setDirection(forwards));
        selectedStands.clear();
        selectedStands.add(armorStand.getUniqueId());
        allArmorStands.put(lastInt, armorStand.getUniqueId());
        lastInt++;
        armorStand.setGravity(false);
        armorStand.setVisible(false);
        armorStand.setBasePlate(false);
        armorStand.setArms(true);
    }

    public void cloneStand() {
        for (ArmorStand selected : getSelectedStands()) {
            ArmorStand clone = (ArmorStand) selected.getWorld().spawnEntity(selected.getLocation(), EntityType.ARMOR_STAND);
            clone.getEquipment().setHelmet(selected.getEquipment().getHelmet());
            clone.getEquipment().setItemInMainHand(selected.getEquipment().getItemInMainHand());
            clone.getEquipment().setItemInOffHand(selected.getEquipment().getItemInOffHand());
            clone.setLeftArmPose(selected.getLeftArmPose());
            clone.setRightArmPose(selected.getRightArmPose());

            clone.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.ADDING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.ADDING_OR_CHANGING);
            clone.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);

            clone.setGravity(false);
            clone.setVisible(false);
            clone.setBasePlate(false);
            clone.setArms(true);

            selectedStands.add(clone.getUniqueId());
            allArmorStands.put(lastInt, clone.getUniqueId());
            lastInt++;
        }
    }

    public void selectAll(String block) {
        selectedStands.clear();
        for (Map.Entry<Integer, UUID> entry : allArmorStands.entrySet()) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(entry.getValue());
            if (armorStand.getEquipment().getHelmet().getType().toString().toLowerCase().contains(block.toLowerCase())) {
                selectedStands.add(entry.getValue());
            }
        }

    }

    public void deselectAll(String block) {
        for (Map.Entry<Integer, UUID> entry : allArmorStands.entrySet()) {
            ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(entry.getValue());
            if (armorStand.getEquipment().getHelmet().getType().toString().toLowerCase().contains(block.toLowerCase())) {
                selectedStands.remove(entry.getValue());
            }
        }
    }

    public void undo() {
        allArmorStands.remove(lastInt - 1);
        seats.remove(selectedStands.get(selectedStands.size() - 1));
        Bukkit.getEntity(selectedStands.get(selectedStands.size() - 1)).remove();
        selectedStands.remove(selectedStands.size() - 1);
        lastInt--;
        selectedStands.add(allArmorStands.get(lastInt - 1));
    }

    public void clear() {
        lastInt = 0;
        for (UUID uuid : allArmorStands.values()) {
            if (Bukkit.getEntity(uuid) != null) {
                Bukkit.getEntity(uuid).remove();
            }
        }
        allArmorStands.clear();
        selectedStands.clear();
    }

    public void move(double x, double y, double z) {
        for (ArmorStand armorStand : getSelectedStands()) {
            armorStand.teleport(armorStand.getLocation().clone().add(x, y, z));
        }
    }

    public void rotateTotal(double x, double y, double z) {
        for (ArmorStand armorStand : getSelectedStands()) {
            Location location = armorStand.getLocation().clone();
            Location desiredLocation = location.setDirection(location.getDirection().clone().rotateAroundY(x));
            desiredLocation.setDirection(desiredLocation.getDirection().clone().setX(Math.round(desiredLocation.getDirection().clone().getX())));
            desiredLocation.setDirection(desiredLocation.getDirection().clone().setY(Math.round(desiredLocation.getDirection().clone().getY())));
            desiredLocation.setDirection(desiredLocation.getDirection().clone().setZ(Math.round(desiredLocation.getDirection().clone().getZ())));
            armorStand.teleport(desiredLocation);
        }
    }

    public void rotateArm(boolean left, double x, double y, double z) {
        for (ArmorStand armorStand : getSelectedStands()) {
            if (left) {
                armorStand.setLeftArmPose(armorStand.getLeftArmPose().add(x, y, z));
                armorStand.setLeftArmPose(armorStand.getLeftArmPose().setX(Math.round(armorStand.getLeftArmPose().getX())));
                armorStand.setLeftArmPose(armorStand.getLeftArmPose().setY(Math.round(armorStand.getLeftArmPose().getY())));
                armorStand.setLeftArmPose(armorStand.getLeftArmPose().setZ(Math.round(armorStand.getLeftArmPose().getZ())));
            } else {
                armorStand.setRightArmPose(armorStand.getRightArmPose().add(x, y, z));
                armorStand.setRightArmPose(armorStand.getRightArmPose().setX(Math.round(armorStand.getRightArmPose().getX())));
                armorStand.setRightArmPose(armorStand.getRightArmPose().setY(Math.round(armorStand.getRightArmPose().getY())));
                armorStand.setRightArmPose(armorStand.getRightArmPose().setZ(Math.round(armorStand.getRightArmPose().getZ())));
            }
        }
    }

    public void update() {
        if (getSelectedStands() == null) {
            return;
        }
        for (ArmorStand armorStand : getSelectedStands()) {
            armorStand.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 1, true, false, false));
        }
        seat.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, seat.clone(), 0);
        for (UUID uuid : seats) {
            ArmorStand seat = (ArmorStand) Bukkit.getEntity(uuid);
            seat.getWorld().spawnParticle(Particle.FLAME, seat.getLocation().clone().add(0, 1.8, 0), 0);
            seat.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10, 1, true, false, false));
        }
        Vector direction = forwards.clone().normalize().multiply(5);
        Location start = origin.clone();
        int parts = 10;
        double percent = direction.length() / parts / direction.length();
        for (int i = 0; i < parts; i++) {
            start.add(direction.clone().multiply(percent));
            start.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, start.clone(), 0);
        }
    }

    public List<ArmorStand> getSelectedStands() {
        if (selectedStands.isEmpty()) {
            return null;
        }
        List<ArmorStand> armorStands = new ArrayList<>();
        for (UUID uuid : selectedStands) {
            armorStands.add((ArmorStand) Bukkit.getEntity(uuid));
        }
        return armorStands;
    }
}
