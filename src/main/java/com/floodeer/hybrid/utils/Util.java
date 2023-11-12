package com.floodeer.hybrid.utils;

import com.floodeer.hybrid.Hybrid;
import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

    private static String healthString = "";


    private static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    public static void moveEntity(Entity entity, Location destination) {
        try {

            Class<?> craftEntityClass = Class.forName("org.bukkit.craftbukkit." + getVersion() + ".entity.CraftEntity");

            Object craftEntity = craftEntityClass.cast(entity);

            if (craftEntity == null) {
                return;
            }

            Method getHandleMethod = craftEntityClass.getMethod("getHandle");

            Object entityHandle = getHandleMethod.invoke(craftEntity);

            Class<?> entityHandleClass = entityHandle.getClass();

            Method b = entityHandleClass.getMethod("b", double.class, double.class, double.class, float.class, float.class);
            b.invoke(entityHandle, destination.getX(), destination.getY(), destination.getZ(), destination.getYaw(), destination.getPitch());

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertToString(Vector vector){
        return vector.getX() + ";" + vector.getY() + ";" + vector.getZ();
    }

    public static String convertToString(EulerAngle eulerAngle){
        return eulerAngle.getX() + ";" + eulerAngle.getY() + ";" + eulerAngle.getZ();
    }

    public static Vector convertToVector(String string){
        String[] split = string.split(";");
        return new Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
    }

    public static void playInstantFirework(Location loc, FireworkEffect effect) {

        Entity localEntity = loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        Firework firework = (Firework) localEntity;
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(effect);
        meta.setPower(1);
        firework.setFireworkMeta(meta);
        Runner.make(Hybrid.get()).delay(1).run(firework::detonate);
    }
    
    public static boolean isURL(String str) {
        Pattern p = Pattern.compile(URL_REGEX);
        Matcher m = p.matcher(str);
        return m.find();
    }

    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static int getPercent(int number) {
        return (number / 100) * 100;
    }

    public static List<String> colorList(List<String> list) {
        ArrayList<String> strings = new ArrayList<String>();
        for (String str : list) {
            strings.add(ChatColor.translateAlternateColorCodes('&', color(str)));
        }

        return strings;
    }

    public static String getHealth(double health, double maxHealth) {
        if(healthString.equalsIgnoreCase("")) {
            for (int i = 0; i < 10; i++) {
                healthString = healthString + StringEscapeUtils.unescapeJava("▌");
            }
        }

        if (health == maxHealth)
            return ChatColor.GREEN + healthString;
        int i = (int)(health / maxHealth * 10.0D);
        if (i < 1)
            return ChatColor.GRAY + healthString;
        return ChatColor.GREEN + healthString.substring(0, i) + ChatColor.GRAY + healthString.substring(i, healthString.length());
    }

    public static String createSpacer() {
        String build = "";

        for (int i = 0; i < 15; i++) {
            build = add(build);
        }

        return build;
    }
    private static String add(String build) {
        Random random = new Random();

        int r = random.nextInt(7) + 1;

        build = build + ChatColor.values()[r];

        return build;
    }

    public static void createFakeExplosion(Location loc, int amount, float speed, boolean huge) {
        loc.getWorld().spawnParticle(huge ? Particle.EXPLOSION_HUGE : Particle.EXPLOSION_NORMAL, loc, amount, 0, 0, 0, speed);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.5F, 1F);
    }

    public static void createFakeNormalExplosion(Location loc, int amount, float speed) {
        loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, amount, 0, 0, 0, speed);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.5F, 1F);
    }

    public static String saveLocation(Location location, boolean yawPitch) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("world", location.getWorld().getName());
        jsonObject.put("x", location.getX());
        jsonObject.put("y", location.getY());
        jsonObject.put("z", location.getZ());
        if (yawPitch) {
            jsonObject.put("yaw", location.getYaw());
            jsonObject.put("pitch", location.getPitch());
        }
        return jsonObject.toJSONString();
    }

    public static Location getLocation(JSONObject fromJson, boolean yawpitch) {
        double x = (double) fromJson.get("x");
        double y = (double) fromJson.get("y");
        double z = (double) fromJson.get("z");
        String world = (String) fromJson.get("world");
        if (yawpitch) {
            double pitch = (double) fromJson.get("pitch");
            double yaw = (double) fromJson.get("yaw");
            return new Location(Bukkit.getWorld(world), Math.floor(x), Math.floor(y), Math.floor(z), (float) yaw,
                    (float) pitch);
        } else {
            return new Location(Bukkit.getWorld(world), Math.floor(x), Math.floor(y), Math.floor(z));
        }
    }

    public static Location getLocationExact(JSONObject fromJson, boolean yawpitch) {
        double x = (double) fromJson.get("x");
        double y = (double) fromJson.get("y");
        double z = (double) fromJson.get("z");
        String world = (String) fromJson.get("world");
        if (yawpitch) {
            double pitch = (double) fromJson.get("pitch");
            double yaw = (double) fromJson.get("yaw");
            return new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
        } else {
            return new Location(Bukkit.getWorld(world), x, y, z);
        }
    }

    public static String getStringFromLocation(Location loc, boolean center) {
        return loc.getWorld().getName() + ", " + (loc.getBlockX() + (center ? 0.5D : 0.0D)) + ", " + loc.getBlockY() + ", " + (loc.getBlockZ() + (center ? 0.5D : 0.0D)) + ", " + loc.getYaw() + ", " + loc.getPitch();
    }

    public static Location getLocationFromString(String paramString) {
        String[] locationData = paramString.split(", ");
        World world = Bukkit.getWorld(locationData[0]);
        double x = Double.parseDouble(locationData[1]), y = Double.parseDouble(locationData[2]), z = Double.parseDouble(locationData[3]);
        float pitch = Float.parseFloat(locationData[4]), yaw = Float.parseFloat(locationData[5]);
        return new Location(world, x, y, z, pitch, yaw);
    }

    public static ArrayList<Location> getCircleAt(Location center, double radius, int amount) {
        World world = center.getWorld();
        double increment = 6.283185307179586D / amount;
        ArrayList<Location> locations = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            double angle = i * increment;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            locations.add(new Location(world, x, center.getY(), z));
        }
        return locations;
    }

    public static ArrayList<Location> getCircleReverse(Location center, double radius, int amount) {
        World world = center.getWorld();
        double increment = 6.283185307179586D / amount;
        ArrayList<Location> locations = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            double angle = i * increment;
            double x = center.getX() - radius * Math.cos(angle);
            double z = center.getZ() - radius * Math.sin(angle);
            locations.add(new Location(world, x, center.getY(), z));
        }
        return locations;
    }

    public static List<Location> getCircleBlocks(Location loc, int range) {
        List<Location> locations = Lists.newArrayList();

        int x = loc.getBlockX() - range / 2;
        int y = loc.getBlockY() - range / 2;
        int z = loc.getBlockZ() - range / 2;
        for (int m = x; m < x + range; m++) {
            for (int n = y; n < y + range; n++) {
                for (int i1 = z; i1 < z + range; i1++) {
                    locations.add(loc.getWorld().getBlockAt(m, n, i1).getLocation());
                }
            }
        }
        return locations;
    }

    public static List<Location> getCircle(Location loc, double angle, int range) {
        ArrayList<Location> localArrayList = new ArrayList<>();
        double d1 = 6.283185307179586D / range;
        for (int i = 0; i < range; i++) {
            double d2 = i * d1;
            double d3 = loc.getX() + angle * Math.cos(d2);
            double d4 = loc.getZ() + angle * Math.sin(d2);

            localArrayList.add(new Location(loc.getWorld(), d3, loc.getY(), d4));
        }
        return localArrayList;
    }

    public static Location getLocation(Location loc, double x, double y, double z) {
        return new Location(loc.getWorld(), loc.getX() + x,
                loc.getY() + y, loc.getZ() + z);
    }

    public static void displayProgress(String prefix, double amount, String suffix, boolean progressDirectionSwap, Player... players) {
        if (progressDirectionSwap)
            amount = 1 - amount;

        int bars = 24;
        String progressBar = ChatColor.GREEN + "";
        boolean colorChange = false;
        for (int i = 0; i < bars; i++) {
            if (!colorChange && (float) i / (float) bars >= amount) {
                progressBar += ChatColor.RED;
                colorChange = true;
            }

            progressBar += "▌";
        }
        sendActionBar((prefix == null ? "" : prefix + ChatColor.RESET + " ") + progressBar
                + (suffix == null ? "" : ChatColor.RESET + " " + suffix), players);
    }

    public static void sendActionBar(String msg, Player... players) {
        for(Player p : players) {
            p.sendActionBar(Component.text(Util.color(msg)));
        }
    }
}
