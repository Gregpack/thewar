package ru.gregpack.thewar.view;

import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gregpack.thewar.model.entities.composite.units.UnitType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GraphicsLibrary {
    private static final Logger logger = LogManager.getLogger(GraphicsLibrary.class.getName());
    private static Image barrackImage = null;
    private static final int supportedOwnersAmount = 2;
    private static final Map<String, Image> unitTypeAndOwnerImages = new HashMap<>();
    private static final Map<UnitType, Image> unitImages = new HashMap<>();
    private static final Map<UnitType, Image> unitWeapons = new HashMap<>();
    private static final Map<UnitType, String> unitToProp = Map.of(
            UnitType.FOOTMAN, "sword",
            UnitType.ARCHER, "arrow",
            UnitType.ASSASSIN, "dagger",
            UnitType.CAVALRY, "pike",
            UnitType.ALCHEMIST, "flask");

    static {
        try (InputStream barrack = Image.class.getClassLoader().getResourceAsStream("barrack.png")) {
            if (barrack != null) {
                barrackImage = new Image(barrack);
            }
        } catch (IOException e) {
            logger.info("Failed to load barrack image: {}", e.getMessage());
        }
        for (UnitType value : UnitType.values()) {
            String name = value.toString().toLowerCase(Locale.ROOT);
            for (int i = 0; i < supportedOwnersAmount; i++) {
                String unitWithOwner = name + "_" + i;
                try (InputStream unit = Image.class.getClassLoader().getResourceAsStream(unitWithOwner + ".png")) {
                    if (unit != null) {
                        unitTypeAndOwnerImages.put(unitWithOwner, new Image(unit));
                    }
                } catch (IOException e) {
                    logger.info("Failed to load {} image: {}", name, e.getMessage());
                }
            }
            try (InputStream unit = Image.class.getClassLoader().getResourceAsStream(name + ".png");
                 InputStream unitWeapon = Image.class.getClassLoader().getResourceAsStream(unitToProp.get(value) + ".png")) {
                if (unit != null) {
                    unitImages.put(value, new Image(unit));
                }
                if (unitWeapon != null) {
                    unitWeapons.put(value, new Image(unitWeapon));
                }
            } catch (IOException e) {
                logger.info("Failed to load {} image: {}", name, e.getMessage());
            }
        }
    }

    public static Image getBarrackImage() {
        return barrackImage;
    }

    public static Image getUnitImage(UnitType unitType, int ownerId) {
        String imageName = unitType.toString().toLowerCase(Locale.ROOT) + "_" + ownerId;
        if (unitTypeAndOwnerImages.containsKey(imageName)) {
            return unitTypeAndOwnerImages.get(imageName);
        }
        return getUnitImage(unitType);
    }

    public static Image getUnitImage(UnitType unitType) {
        return unitImages.get(unitType);
    }

    public static Image getUnitWeapon(UnitType unitType) {
        return unitWeapons.get(unitType);
    }
}
