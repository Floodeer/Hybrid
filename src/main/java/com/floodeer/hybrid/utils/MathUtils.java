package com.floodeer.hybrid.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collector;

public class MathUtils {

    public static float nanoToSec = 1 / 1000000000f;

    public static float FLOAT_ROUNDING_ERROR = 0.000001f;

    public static float PI = 3.141592653589793238462643383279f;

    public static float PI2 = PI * 2;

    public static float SQRT_3 = 1.73205080757f;

    public static float E = 2.7182818284590452354f;

    private static int SIN_BITS = 14;

    private static int SIN_MASK = ~(-1 << SIN_BITS);

    private static int SIN_COUNT = SIN_MASK + 1;

    private static float radFull = PI * 2;

    private static float degFull = 360;

    private static float radToIndex = SIN_COUNT / radFull;

    private static float degToIndex = SIN_COUNT / degFull;

    public static float radiansToDegrees = 180f / PI;

    public static float radDeg = radiansToDegrees;

    public static float degreesToRadians = PI / 180;

    public static float degRad = degreesToRadians;

    private static int BIG_ENOUGH_INT = 16 * 1024;

    private static double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;

    private static double CEIL = 0.9999999;

    private static double BIG_ENOUGH_CEIL = 16384.999999999996;

    private static double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5f;

    private static int ATAN2_BITS = 7;

    private static int ATAN2_BITS2 = ATAN2_BITS << 1;

    private static int ATAN2_MASK = ~(-1 << ATAN2_BITS2);

    private static int ATAN2_COUNT = ATAN2_MASK + 1;

    static int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);

    private static float INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);

    public static int floor(double d1) {
        int i = (int) d1;
        return d1 >= i ? i : i - 1;
    }

    public static int d(float f1) {
        int i = (int) f1;
        return f1 >= i ? i : i - 1;
    }

    public static byte toPackedByte(float f) {
        return (byte) ((int) (f * 256.0F / 360.0F));
    }

    static public byte rotationToByte(float f) {
        return (byte) ((int) (f* 256.0F / 360.0F));
    }

    public static double trim(int degree, double d) {
        String format = "#.#";
        for (int i=1 ; i<degree ; i++)
            format += "#";
        DecimalFormatSymbols symb = new DecimalFormatSymbols(Locale.US);
        DecimalFormat twoDForm = new DecimalFormat(format, symb);
        return Double.valueOf(twoDForm.format(d));
    }

    public static double randomRange(double paramDouble1, double paramDouble2) {
        return Math.random() < 0.5D ? (1.0D - Math.random()) * (paramDouble2 - paramDouble1) + paramDouble1
                : Math.random() * (paramDouble2 - paramDouble1) + paramDouble1;
    }

    public static <T> Collector<T, List<T>, T> streamRandom() {
        Random RANDOM = new Random();
        return Collector.of(() -> new ArrayList<T>(),
                (acc, elem) -> acc.add(elem),
                (list1, list2) -> union(list1, list2),
                list -> list.isEmpty() ? null : list.get(RANDOM.nextInt(list.size())));
    }

    public static <T> T random(List<T> list) {
        if (list.isEmpty())
            return null;

        return list.get(r(list.size()));
    }

    public static <E> List<E> union(final List<? extends E> list1, List<? extends E> list2) {
        ArrayList<E> result = new ArrayList<E>(list1);
        result.addAll(list2);
        return result;
    }

    public static Random random = new Random();

    public static int r(int paramInt) {
        return random.nextInt(paramInt);
    }

    public static double offset2d(Entity paramEntity1, Entity paramEntity2) {
        return offset2d(paramEntity1.getLocation().toVector(), paramEntity2.getLocation().toVector());
    }

    public static double offset2d(Location paramLocation1, Location paramLocation2) {
        return offset2d(paramLocation1.toVector(), paramLocation2.toVector());
    }

    public static double offset2d(Vector paramVector1, Vector paramVector2) {
        paramVector1.setY(0);
        paramVector2.setY(0);
        return paramVector1.subtract(paramVector2).length();
    }

    public static Vector rotate(Vector v, Location loc) {
        double yaw = (loc.getYaw() / 180.0F) * Math.PI;
        double pitch = (loc.getPitch() / 180.0F) * Math.PI;
        v = VelocityUtils.rotateAroundAxisX(v, pitch);
        v = VelocityUtils.rotateAroundAxisY(v, -yaw);
        return v;
    }

    private static class Sin {

        static float[] table = new float[SIN_COUNT];

        static {
            for (int i = 0; i < SIN_COUNT; i++) {
                table[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
            }
            for (int i = 0; i < 360; i += 90) {
                table[(int) (i * degToIndex) & SIN_MASK] = (float) Math.sin(i * degreesToRadians);
            }
        }
    }

    public static float sin(float radians) {
        return Sin.table[(int) (radians * radToIndex) & SIN_MASK];
    }

    public static float cos(float radians) {
        return Sin.table[(int) ((radians + PI / 2) * radToIndex) & SIN_MASK];
    }

    public static float sinDeg(float degrees) {
        return Sin.table[(int) (degrees * degToIndex) & SIN_MASK];
    }

    public static float cosDeg(float degrees) {
        return Sin.table[(int) ((degrees + 90) * degToIndex) & SIN_MASK];
    }

    private static class Atan2 {

        static float[] table = new float[ATAN2_COUNT];

        static {
            for (int i = 0; i < ATAN2_DIM; i++) {
                for (int j = 0; j < ATAN2_DIM; j++) {
                    float x0 = (float) i / ATAN2_DIM;
                    float y0 = (float) j / ATAN2_DIM;
                    table[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
                }
            }
        }
    }

    public static boolean isInteger(Object object) {
        try {
            Integer.parseInt(object.toString());
            return true;
        } catch (Exception exc) {
            return false;
        }
    }

    public static boolean isBoolean(Object object) {
        try {
            Boolean.parseBoolean(object.toString());
            return true;
        } catch (Exception exc) {
            return false;
        }
    }

    public static boolean isDouble(Object object) {
        try {
            Double.parseDouble(object.toString());
            return true;
        } catch (Exception exc) {
            return false;
        }
    }

    public static boolean isFloat(Object object) {
        try {
            Float.parseFloat(object.toString());
            return true;
        } catch (Exception exc) {
            return false;
        }
    }

    public static float atan2(float y, float x) {
        float add, mul;
        if (x < 0) {
            if (y < 0) {
                y = -y;
                mul = 1;
            } else {
                mul = -1;
            }
            x = -x;
            add = -PI;
        } else {
            if (y < 0) {
                y = -y;
                mul = -1;
            } else {
                mul = 1;
            }
            add = 0;
        }
        float invDiv = 1 / ((x < y ? y : x) * INV_ATAN2_DIM_MINUS_1);

        if (invDiv == Float.POSITIVE_INFINITY) {
            return ((float) Math.atan2(y, x) + add) * mul;
        }

        int xi = (int) (x * invDiv);
        int yi = (int) (y * invDiv);
        return (Atan2.table[yi * ATAN2_DIM + xi] + add) * mul;
    }

    public static int random(int range) {
        return random.nextInt(range + 1);
    }

    public static int random(int start, int end) {
        return start + random.nextInt(end - start + 1);
    }

    public static boolean randomBoolean() {
        return random.nextBoolean();
    }

    public static boolean randomBoolean(float chance) {
        return random(100) < chance;
    }

    public static float random() {
        return random.nextFloat();
    }

    public static float random(float range) {
        return random.nextFloat() * range;
    }

    public static float random(float start, float end) {
        return start + random.nextFloat() * (end - start);
    }

    public static int nextPowerOfTwo(int value) {
        if (value == 0) {
            return 1;
        }
        value--;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        value |= value >> 16;
        return value + 1;
    }

    public static boolean isPowerOfTwo(int value) {
        return value != 0 && (value & value - 1) == 0;
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static short clamp(short value, short min, short max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static int floor(float x) {
        return (int) (x + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
    }

    public static int floorPositive(float x) {
        return (int) x;
    }

    public static int ceil(float x) {
        return (int) (x + BIG_ENOUGH_CEIL) - BIG_ENOUGH_INT;
    }

    public static int ceilPositive(float x) {
        return (int) (x + CEIL);
    }

    public static int round(float x) {
        return (int) (x + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
    }

    public static int roundPositive(float x) {
        return (int) (x + 0.5f);
    }

    public static boolean isZero(float value) {
        return Math.abs(value) <= FLOAT_ROUNDING_ERROR;
    }

    public static boolean isZero(float value, float tolerance) {
        return Math.abs(value) <= tolerance;
    }

    public static boolean isEqual(float a, float b) {
        return Math.abs(a - b) <= FLOAT_ROUNDING_ERROR;
    }

    public boolean isEqual(float a, float b, float tolerance) {
        return Math.abs(a - b) <= tolerance;
    }

    public static Location locMerge(Location a, Location b){
        a.setX(b.getX());
        a.setY(b.getY());
        a.setZ(b.getZ());
        return a;
    }

    public static Vector rotateAroundAxisX(Vector v, double angle) {
        double y, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        y = v.getY() * cos - v.getZ() * sin;
        z = v.getY() * sin + v.getZ() * cos;
        return v.setY(y).setZ(z);
    }

    public static Vector rotateAroundAxisY(Vector v, double angle) {
        double x, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos + v.getZ() * sin;
        z = v.getX() * -sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }

    public static Vector rotateAroundAxisZ(Vector v, double angle) {
        double x, y, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos - v.getY() * sin;
        y = v.getX() * sin + v.getY() * cos;
        return v.setX(x).setY(y);
    }

    public static Vector rotateVector(Vector v, double angleX, double angleY, double angleZ) {
        rotateAroundAxisX(v, angleX);
        rotateAroundAxisY(v, angleY);
        rotateAroundAxisZ(v, angleZ);
        return v;
    }

    public static double angleToXAxis(Vector vector) {
        return Math.atan2(vector.getX(), vector.getY());
    }

    public static Vector getRandomVector() {
        double x = random.nextDouble() * 2.0D - 1.0D;
        double y = random.nextDouble() * 2.0D - 1.0D;
        double z = random.nextDouble() * 2.0D - 1.0D;

        return new Vector(x, y, z).normalize();
    }

    public static Vector getRandomCircleVector() {
        double rnd = random.nextDouble() * 2.0D * 3.141592653589793D;
        double x = Math.cos(rnd);
        double z = Math.sin(rnd);

        return new Vector(x, 0.0D, z);
    }

    public static Material material(Material[] materials) {
        return materials[random.nextInt(materials.length)];
    }

    public static <T> T getRandomT(T[] t) {
        return t[random.nextInt(t.length)];
    }

    public static double getRandomAngle() {
        return random.nextDouble() * 2 * Math.PI;
    }

    public static double randomDouble(double min, double max) {
        return Math.random() < 0.5 ? ((1 - Math.random()) * (max - min) + min) : (Math.random() * (max - min) + min);
    }

    public static float randomRangeFloat(float min, float max) {
        return (float) (Math.random() < 0.5 ? ((1 - Math.random()) * (max - min) + min)
                : (Math.random() * (max - min) + min));
    }

    public static byte randomByte(int max) {
        return (byte) random.nextInt(max + 1);
    }


    public static int randomRangeInt(int min, int max) {
        return (int) (Math.random() < 0.5 ? ((1 - Math.random()) * (max - min) + min)
                : (Math.random() * (max - min) + min));
    }

    public static double offset(Entity a, Entity b) {
        return offset(a.getLocation().toVector(), b.getLocation().toVector());
    }

    public static double offset(Location a, Location b) {
        return offset(a.toVector(), b.toVector());
    }

    public static double offset(Vector a, Vector b) {
        return a.subtract(b).length();
    }

    public static boolean isInteger(String s, int radix) {
        if (s.isEmpty())
            return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1)
                    return false;
                else
                    continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0)
                return false;
        }
        return true;
    }

    public static float getLookAtYaw(Vector v) {
        double x = v.getX();
        double z = v.getZ();
        double r = 0.0D;
        if (x != 0.0D) {
            if (x < 0.0D) {
                r = 4.71238898038469D;
            } else {
                r = 1.5707963267948966D;
            }
            r -= Math.atan(z / x);
        } else if (z < 0.0D) {
            r = 3.141592653589793D;
        }
        return (float)(-r * 180.0D / 3.141592653589793D - 90.0D);
    }

    public static int absolute(double d) {
        return (int)(d>= 0.0D ? d : -d+ 1.0D);
    }

    public static double arrondi(double paramDouble, int paramInt) {
        return (int) (paramDouble * Math.pow(10.0D, paramInt) + 0.5D) / Math.pow(10.0D, paramInt);
    }

    public static Range<Double, Double> getLargestArcRange(Double[] angles) {
        Set<Set<Double>> combinations = getCombinationsFor(Arrays.asList(angles), 2);
        Range<Double, Double> largestRange = null;
        Double largest = null;
        for (Set<Double> combo : combinations) {
            Double[] array = combo.toArray(new Double[2]);
            double arc = distance(array[0], array[1]);

            if (largest == null || arc > largest) {
                largest = arc;
                largestRange = new Range<>(array[0], array[1]);
            }
        }
        if (largestRange != null && largestRange.getLowest() > largestRange.getHighest())
            largestRange = new Range<>(largestRange.getHighest(), largestRange.getLowest());
        return largestRange;
    }

    public static <T> Set<Set<T>> getCombinationsFor(List<T> group, int k) {

        Set<Set<T>> allCombos = new HashSet<>();
        if (k == 0) {
            allCombos.add(new HashSet<T>());
            return allCombos;
        }
        if (k > group.size())
            return allCombos;

        List<T> groupWithoutX = new ArrayList<>(group);
        T x = groupWithoutX.remove(groupWithoutX.size() - 1);

        Set<Set<T>> combosWithoutX = getCombinationsFor(groupWithoutX, k);
        Set<Set<T>> combosWithX = getCombinationsFor(groupWithoutX, k - 1);
        for (Set<T> combo : combosWithX)
            combo.add(x);
        allCombos.addAll(combosWithoutX);
        allCombos.addAll(combosWithX);
        return allCombos;
    }

    private static double distance(double r1, double r2) {
        double d = Math.abs(r2 - r1);
        if (d <= Math.PI)
            return d;
        else
            return 2 * Math.PI - d;
    }

    public static double toPolar(double x, double y) {
        double theta = Math.atan2(y, x);
        if (theta < 0)
            return 2 * Math.PI + theta;
        else
            return theta;
    }

    public static boolean inside(double theta, double small, double large) {
        if (large - small > Math.PI)
            return theta > large || theta < small;
        else
            return theta < large && theta > small;
    }

    public static class Range<L extends Number, H extends Number> {
        L lowest;
        H highest;

        Range(L lowest, H highest) {
            this.lowest = lowest;
            this.highest = highest;
        }

        public L getLowest() {
            return lowest;
        }

        public H getHighest() {
            return highest;
        }

        public void setKey(L lowest) {
            this.lowest = lowest;
        }

        public void setValue(H highest) {
            this.highest = highest;
        }
    }
}