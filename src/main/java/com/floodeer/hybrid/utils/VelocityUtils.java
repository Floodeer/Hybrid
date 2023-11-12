package com.floodeer.hybrid.utils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class VelocityUtils {

    public static Vector getRandomVector() {
        double u = MathUtils.random.nextDouble();
        double v = MathUtils.random.nextDouble();

        double theta = u * 2 * Math.PI;
        double phi = Math.acos(2 * v - 1);

        double sinTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);
        double sinPhi = Math.sin(phi);
        double cosPhi = Math.cos(phi);

        double x = sinPhi * cosTheta;
        double y = sinPhi * sinTheta;
        double z = cosPhi;

        return new Vector(x, y, z);
    }
    
    public static Vector getTrajectory(Entity entity, Entity entity2) {
        return getTrajectory(entity.getLocation().toVector(), entity2.getLocation().toVector());
    }

    public static Vector getTrajectory(Entity entity, Player player) {
        return getTrajectory(entity.getLocation().toVector(), player.getLocation().toVector());
    }

    public static Vector getTrajectory(Location loc, Player player) {
        return getTrajectory(loc.toVector(), player.getLocation().toVector());
    }

    public static Vector getTrajectory(Location loc1, Location loc2) {
        return getTrajectory(loc1.toVector(), loc2.toVector());
    }

    public static Vector getTrajectory(Vector vector, Vector vector2) {
        return vector2.subtract(vector).normalize();
    }

    public static Vector getTrajectory2d(Entity entity, Entity entity2) {
        return getTrajectory2d(entity.getLocation().toVector(), entity2.getLocation().toVector());
    }

    public static Vector getTrajectory2d(Location loc1, Location loc2) {
        return getTrajectory2d(loc1.toVector(), loc2.toVector());
    }

    public static Vector getTrajectory2d(Vector vector, Vector vector2) {
        return vector2.subtract(vector).setY(0).normalize();
    }

    public static float getPitch(Vector vector) {
        double d1 = vector.getX();
        double d2 = vector.getY();
        double d3 = vector.getZ();
        double d4 = Math.sqrt(d1 * d1 + d3 * d3);

        double d5 = Math.toDegrees(Math.atan(d4 / d2));
        if (d2 <= 0.0D) {
            d5 += 90.0D;
        } else {
            d5 -= 90.0D;
        }
        return (float) d5;
    }

    public static float getYaw(Vector vector) {
        double d1 = vector.getX();
        double d2 = vector.getZ();

        double d3 = Math.toDegrees(Math.atan(-d1 / d2));
        if (d2 < 0.0D) {
            d3 += 180.0D;
        }
        return (float) d3;
    }

    public static Vector normalize(Vector vector) {
        if (vector.length() > 0.0D) {
            vector.normalize();
        }
        return vector;
    }

    public static Vector clone(Vector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vector getBumpVector(Entity entity, Location loc, double d) {
        Vector localVector = entity.getLocation().toVector().subtract(loc.toVector()).normalize();
        localVector.multiply(d);
        return localVector;
    }

    public static void knockback(Player p, Location loc, double multiply, double y, boolean zero) {
        Location l = p.getLocation();
        l.setPitch(0.0F);
        loc.setPitch(0.0F);
        Vector v = l.toVector().subtract(loc.toVector()).normalize();
        if (zero) {
            v = loc.toVector().subtract(l.toVector()).normalize();
        }

        v.setY(y);
        p.setVelocity(v.multiply(multiply));
    }

    public static Vector getRandomVectorLine() {
        int min = -5;
        int max = 5;
        int rz = (int) (Math.random() * (max - min) + min);
        int rx = (int) (Math.random() * (max - min) + min);

        double miny = -5.0D;
        double maxy = -1.0D;
        double ry = Math.random() * (maxy - miny) + miny;

        return new Vector(rx, ry, rz).normalize();
    }

    public static Vector getPullVector(Entity entity, Location loc, double d) {
        Vector localVector = loc.toVector().subtract(entity.getLocation().toVector()).normalize();
        localVector.multiply(d);
        return localVector;
    }

    public static void bumpEntity(Entity entity, Location loc, double d) {
        entity.setVelocity(getBumpVector(entity, loc, d));
    }

    public static void bumpEntity(Entity entity, Location loc, double d1, double d2) {
        Vector localVector = getBumpVector(entity, loc, d1);
        localVector.setY(d2);
        entity.setVelocity(localVector);
    }

    public static void pullEntity(Entity entity, Location loc, double d) {
        entity.setVelocity(getPullVector(entity, loc, d));
    }

    public static void pullEntity(Entity entity, Location loc, double d1,
                                  double d2) {
        Vector localVector = getPullVector(entity, loc, d1);
        localVector.setY(d2);
        entity.setVelocity(localVector);
    }

    public static void velocity(Entity entity, double d1, double d2, double d3) {
        velocity(entity, entity.getLocation().getDirection(), d1, false, 0.0D, d2,
                d3);
    }

    public static void velocity(Entity entity, Vector vector, double d1, boolean paramBoolean,
                                double d2, double d3, double d4) {
        if ((Double.isNaN(vector.getX())) || (Double.isNaN(vector.getY()))
                || (Double.isNaN(vector.getZ())) || (vector.length() == 0.0D)) {
            return;
        }
        if (paramBoolean) {
            vector.setY(d2);
        }
        vector.normalize();
        vector.multiply(d1);

        vector.setY(vector.getY() + d3);
        if (vector.getY() > d4) {
            vector.setY(d4);
        }
        entity.setFallDistance(0.0F);
        entity.setVelocity(vector);
    }

    public static final Vector rotateAroundAxisX(Vector vector, double d) {
        double d3 = Math.cos(d);
        double d4 = Math.sin(d);
        double d1 = vector.getY() * d3 - vector.getZ() * d4;
        double d2 = vector.getY() * d4 + vector.getZ() * d3;
        return vector.setY(d1).setZ(d2);
    }

    public static final Vector rotateAroundAxisY(Vector vector, double d) {
        double d3 = Math.cos(d);
        double d4 = Math.sin(d);
        double d1 = vector.getX() * d3 + vector.getZ() * d4;
        double d2 = vector.getX() * -d4 + vector.getZ() * d3;
        return vector.setX(d1).setZ(d2);
    }

    public static final Vector rotateAroundAxisZ(Vector vector, double d) {
        double d3 = Math.cos(d);
        double d4 = Math.sin(d);
        double d1 = vector.getX() * d3 - vector.getY() * d4;
        double d2 = vector.getX() * d4 + vector.getY() * d3;
        return vector.setX(d1).setY(d2);
    }

    public static final Vector rotateVector(Vector vector, double d1, double d2,
                                            double d3) {
        rotateAroundAxisX(vector, d1);
        rotateAroundAxisY(vector, d2);
        rotateAroundAxisZ(vector, d3);
        return vector;
    }

    public static final double angleToXAxis(Vector vector) {
        return Math.atan2(vector.getX(), vector.getY());
    }

    public static void velocity(Entity entity, double d1, double d2, double d3,
                                boolean paramBoolean) {
        velocity(entity, entity.getLocation().getDirection(), d1, false, 0.0D, d2,
                d3, paramBoolean);
    }

    public static void velocity(Entity entity, Vector vector, double d1, boolean paramBoolean1,
                                double d2, double d3, double d4, boolean paramBoolean2) {
        if ((Double.isNaN(vector.getX())) || (Double.isNaN(vector.getY()))
                || (Double.isNaN(vector.getZ())) || (vector.length() == 0.0D)) {
            return;
        }
        if (paramBoolean1) {
            vector.setY(d2);
        }
        vector.normalize();
        vector.multiply(d1);

        vector.setY(vector.getY() + d3);
        if (vector.getY() > d4) {
            vector.setY(d4);
        }
        if (paramBoolean2) {
            vector.setY(vector.getY() + 0.2D);
        }
        entity.setFallDistance(0.0F);
        entity.setVelocity(vector);
    }

    public static Vector getBackVector(Location loc) {
        final float newZ = (float) (loc.getZ() + (1 * Math.sin(Math.toRadians(loc.getYaw() + 90 * 1))));
        final float newX = (float) (loc.getX() + (1 * Math.cos(Math.toRadians(loc.getYaw() + 90 * 1))));
        return new Vector(newX - loc.getX(), 0, newZ - loc.getZ());
    }

    public static Vector rotateX(Vector v, double a) {
        double y = Math.cos(a) * v.getY() - Math.sin(a) * v.getZ();
        double z = Math.sin(a) * v.getY() + Math.cos(a) * v.getZ();
        return v.setY(y).setZ(z);
    }

    public static Vector rotateY(Vector v, double b) {
        double x = Math.cos(b) * v.getX() + Math.sin(b) * v.getZ();
        double z = -Math.sin(b) * v.getX() + Math.cos(b) * v.getZ();
        return v.setX(x).setY(z);
    }

    public static final Vector rotateZ(Vector v, double c) {
        double x = Math.cos(c) * v.getX() - Math.sin(c) * v.getY();
        double y = Math.sin(c) * v.getX() + Math.cos(c) * v.getY();
        return v.setX(x).setY(y);
    }
}