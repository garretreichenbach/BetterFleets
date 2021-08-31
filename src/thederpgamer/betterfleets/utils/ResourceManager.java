package thederpgamer.betterfleets.utils;

import api.utils.textures.StarLoaderTexture;
import org.schema.schine.graphicsengine.forms.Sprite;
import thederpgamer.betterfleets.BetterFleets;
import java.util.HashMap;

/**
 * Manages mod files and resources.
 *
 * @author TheDerpGamer
 * @since 07/02/2021
 */
public class ResourceManager {

    private static final String[] textureNames = {
            "repair-paste-fabricator/repair-paste-fabricator-icon",
            "repair-paste-fabricator/repair-paste-fabricator-caps",
            "repair-paste-fabricator/repair-paste-fabricator-sides"
    };

    private static final String[] spriteNames = {
            "entity-indicator-unknown",
            "entity-indicator-ship-neutral", "entity-indicator-ship-friend", "entity-indicator-ship-enemy",
            "entity-indicator-ship-neutral-selected", "entity-indicator-ship-friend-selected", "entity-indicator-ship-enemy-selected",
            "entity-indicator-space-station-neutral", "entity-indicator-space-station-friend", "entity-indicator-space-station-enemy",
            "entity-indicator-space-station-neutral-selected", "entity-indicator-space-station-friend-selected", "entity-indicator-space-station-enemy-selected",
            "entity-indicator-pirate-station", "entity-indicator-trade-station",
            "entity-indicator-shop"
    };

    private static final HashMap<String, StarLoaderTexture> textureMap = new HashMap<>();
    private static final HashMap<String, Sprite> spriteMap = new HashMap<>();


    public static void loadResources(final BetterFleets instance) {
        StarLoaderTexture.runOnGraphicsThread(new Runnable() {
            @Override
            public void run() {
                //Load Textures
                for(String texturePath : textureNames) {
                    String textureName = texturePath.substring(texturePath.lastIndexOf('/') + 1);
                    try {
                        if(textureName.endsWith("icon")) {
                            textureMap.put(textureName, StarLoaderTexture.newIconTexture(instance.getJarBufferedImage("thederpgamer/betterfleets/resources/textures/" + texturePath + ".png")));
                        } else {
                            textureMap.put(textureName, StarLoaderTexture.newBlockTexture(instance.getJarBufferedImage("thederpgamer/betterfleets/resources/textures/" + texturePath + ".png")));
                        }
                    } catch(Exception exception) {
                        LogManager.logException("Failed to load texture \"" + texturePath + "\"", exception);
                    }
                }

                //Load Sprites
                for(String spriteName : spriteNames) {
                    try {
                        spriteMap.put(spriteName, StarLoaderTexture.newSprite(instance.getJarBufferedImage("thederpgamer/betterfleets/resources/sprites/" + spriteName + ".png"), BetterFleets.getInstance(), spriteName));
                    } catch(Exception exception) {
                        LogManager.logException("Failed to load sprite \"" + spriteMap + "\"", exception);
                    }
                }
            }
        });
    }

    public static StarLoaderTexture getTexture(String name) {
        return textureMap.get(name);
    }

    public static Sprite getSprite(String name) {
        return spriteMap.get(name);
    }
}
