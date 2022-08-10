package ru.gregpack.thewar.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gregpack.thewar.model.entities.basic.Coordinate;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class PropertyUtil {
    private static final Logger logger = LogManager.getLogger(PropertyUtil.class.getName());

    private static final Properties properties;

    private static final Map<String, Map<String, String>> propertyMapCache = new HashMap<>();

    static {
        properties = new Properties();
        try {
            properties.load(PropertyUtil.class.getClassLoader().getResourceAsStream("game.properties"));
        } catch (IOException | NullPointerException e) {
            logger.error("Can't load properties from file!");
        }
    }

    public static Integer getIntProperty(String name, int defValue) {
        String property = properties.getProperty(name);
        return property == null ? defValue : Integer.parseInt(property);
    }

    public static String getProperty(String name, String defValue) {
        return properties.getProperty(name, defValue);
    }

    public static List<Coordinate> getCoordinateList(String name) {
        String prop = getProperty(name, "");
        String[] coords = prop.split(",");
        if (coords.length == 1 && coords[0].equals("")) {
            return Collections.emptyList();
        }
        List<Coordinate> coordinates = new ArrayList<>();
        for (String coord : coords) {
            String[] xy = coord.split(";");
            coordinates.add(new Coordinate(Integer.parseInt(xy[0]), Integer.parseInt(xy[1])));
        }
        return coordinates;
    }

    public static <T> T getProperty(String name, T defValue, Function<String, T> parser) {
        String property = properties.getProperty(name);
        return property == null ? defValue : parser.apply(property);
    }

    public static Map<String, String> getPropertyMap(String prefix) {
        if (propertyMapCache.containsKey(prefix)) {
            return propertyMapCache.get(prefix);
        }
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<Object, Object> objectObjectEntry : properties.entrySet()) {
            String propName = (String) objectObjectEntry.getKey();
            if (!propName.contains(prefix)) {
                continue;
            }
            String propValue = (String) objectObjectEntry.getValue();
            propName = propName.replace(prefix + ".", "");
            map.put(propName, propValue);
        }
        propertyMapCache.put(prefix, map);
        return map;
    }

}
