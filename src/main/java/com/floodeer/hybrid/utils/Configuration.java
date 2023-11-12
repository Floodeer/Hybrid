package com.floodeer.hybrid.utils;

import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Configuration {

    private static final transient char DEFAULT_SEPARATOR = '_';
    private static final transient String LINE_SEPARATOR = System.lineSeparator();
    private static final transient String TEMP_CONFIG_SECTION = "temp";
    public static final transient HashMap<Class<?>, Class<?>> PRIMITIVES_CLASS = new HashMap<Class<?>, Class<?>>() {
        private static final long serialVersionUID = 1L;

        {
            put(int.class, Integer.class);
            put(long.class, Long.class);
            put(double.class, Double.class);
            put(float.class, Float.class);
            put(boolean.class, Boolean.class);
            put(byte.class, Byte.class);
            put(void.class, Void.class);
            put(short.class, Short.class);
        }
    };

    private transient File configFile;
    private transient List<String> header;

    protected Configuration(final File configFile) {
        this.configFile = configFile;
    }

    protected Configuration(final File configFile, final List<String> header) {
        this.configFile = configFile;
        this.header = header;
    }

    public final void load() throws InvalidConfigurationException {
        try {
            final YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            for (final Field field : getClass().getFields()) {
                loadField(field, getFieldName(field), config);
            }
            saveConfig(config);
        } catch (final Exception ex) {
            throw new InvalidConfigurationException(ex);
        }
    }

    public final void save() throws InvalidConfigurationException {
        try {
            final YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            for (final Field field : getClass().getFields()) {
                saveField(field, getFieldName(field), config);
            }
            saveConfig(config);
        } catch (final Exception ex) {
            throw new InvalidConfigurationException(ex);
        }
    }

    private final String getFieldName(final Field field) {
        final ConfigOptions options = field.getAnnotation(ConfigOptions.class);
        return (options == null ? field.getName().replace(DEFAULT_SEPARATOR, '.') : options.name());
    }

    private final void saveConfig(final YamlConfiguration config) throws IOException {
        if (header != null && header.size() > 0) {
            config.options().header(Joiner.on(LINE_SEPARATOR).join(header));
        }
        config.save(configFile);
    }

    private final void loadField(final Field field, final String name, final YamlConfiguration config)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ParseException {
        if (Modifier.isTransient(field.getModifiers())) {
            return;
        }
        final Object configValue = config.get(getFieldName(field));
        if (configValue == null) {
            saveField(field, name, config);
        } else {
            field.set(this, deserializeObject(field.getType(), configValue));
        }
    }

    private final void saveField(final Field field, final String name, final YamlConfiguration config)
            throws IllegalAccessException {
        if (Modifier.isTransient(field.getModifiers())) {
            return;
        }
        config.set(name, serializeObject(field.get(this), config));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final Object deserializeObject(final Class<?> clazz, final Object object)
            throws ParseException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (PRIMITIVES_CLASS.containsValue(clazz) || clazz.isPrimitive()) {
            return PRIMITIVES_CLASS.get(clazz).getMethod("valueOf", String.class).invoke(this, object.toString());
        }
        if (clazz.isEnum() || object instanceof Enum<?>) {
            return Enum.valueOf((Class<? extends Enum>) clazz, object.toString());
        }
        if (Map.class.isAssignableFrom(clazz) || object instanceof Map) {
            final ConfigurationSection section = (ConfigurationSection) object;
            final Map<Object, Object> map = new HashMap<Object, Object>();
            for (final String key : section.getKeys(false)) {
                final Object value = section.get(key);
                map.put(key, deserializeObject(value.getClass(), value));
            }
            return map;
        }
        if (List.class.isAssignableFrom(clazz) || object instanceof List) {
            final List<Object> result = new ArrayList<Object>();
            for (final Object value : (List<?>) object) {
                result.add(deserializeObject(value.getClass(), value));
            }
            return result;
        }
        if (Location.class.isAssignableFrom(clazz) || object instanceof Location) {
            final JSONObject jsonObject = (JSONObject) new JSONParser().parse(object.toString());
            return new Location(Bukkit.getWorld(jsonObject.get("world").toString()),
                    Double.parseDouble(jsonObject.get("x").toString()),
                    Double.parseDouble(jsonObject.get("y").toString()),
                    Double.parseDouble(jsonObject.get("z").toString()),
                    Float.parseFloat(jsonObject.get("yaw").toString()),
                    Float.parseFloat(jsonObject.get("pitch").toString()));
        }
        if (Vector.class.isAssignableFrom(clazz) || object instanceof Vector) {
            final JSONObject jsonObject = (JSONObject) new JSONParser().parse(object.toString());
            return new Vector(Double.parseDouble(jsonObject.get("x").toString()),
                    Double.parseDouble(jsonObject.get("y").toString()),
                    Double.parseDouble(jsonObject.get("z").toString()));
        }
        return object.toString();
    }

    @SuppressWarnings("unchecked")
    private final Object serializeObject(final Object object, final YamlConfiguration config) {
        if (object instanceof Enum) {
            return ((Enum<?>) object).name();
        }
        if (object instanceof Map) {
            final ConfigurationSection section = config.createSection(TEMP_CONFIG_SECTION);
            for (final Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                section.set(entry.getKey().toString(), serializeObject(entry.getValue(), config));
            }
            config.set(TEMP_CONFIG_SECTION, null);
            return section;
        }
        if (object instanceof List) {
            final List<Object> result = new ArrayList<Object>();
            for (final Object value : (List<?>) object) {
                result.add(serializeObject(value, config));
            }
            return result;
        }
        if (object instanceof Location) {
            final Location location = (Location) object;
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("world", location.getWorld().getName());
            jsonObject.put("x", location.getX());
            jsonObject.put("y", location.getY());
            jsonObject.put("z", location.getZ());
            jsonObject.put("yaw", location.getYaw());
            jsonObject.put("pitch", location.getPitch());
            return jsonObject.toJSONString();
        }
        if (object instanceof Vector) {
            final Vector vector = (Vector) object;
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("x", vector.getX());
            jsonObject.put("y", vector.getY());
            jsonObject.put("z", vector.getZ());
            return jsonObject.toJSONString();
        }
        return object.toString();
    }

    public final List<String> getHeader() {
        return header;
    }

    public final File getFile() {
        return configFile;
    }

    public final void setHeader(final List<String> header) {
        this.header = header;
    }

    public final void setFile(final File configFile) {
        this.configFile = configFile;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected @interface ConfigOptions {

        String name();

    }
}