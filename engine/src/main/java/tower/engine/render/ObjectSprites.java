package tower.engine.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
import static java.util.logging.Level.WARNING;

public class ObjectSprites {

    private static final Logger LOG = Logger.getLogger(ObjectSprites.class.getName());

    private static final Map<String, String> PATHS = Map.of(
            "lantern", "images/lantern.png",
            "old_chest", "images/objects/obj_old_chest_closed.png",
            "broken_chandelier", "images/objects/obj_broken_chandelier.png",
            "closed_door", "images/objects/obj_door_closed.png",
            "ancient_altar", "images/objects/obj_stone_altar.png",
            "map_table", "images/objects/obj_map_table.png");

    private final Map<String, Texture> textures = new LinkedHashMap<>();

    public ObjectSprites() {
        for (var entry : PATHS.entrySet()) {
            Texture texture = load(entry.getKey(), entry.getValue());
            if (texture != null) {
                textures.put(entry.getKey(), texture);
            }
        }
    }

    public Texture textureFor(String objectId) {
        return textures.get(objectId);
    }

    public void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
    }

    private static Texture load(String objectId, String path) {
        if (Gdx.files == null) {
            warn(path + " is unavailable; drawing placeholder", null);
            return null;
        }
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists()) {
            warn(path + " is missing; drawing placeholder", null);
            return null;
        }
        try {
            var texture = new Texture(file);
            texture.setFilter(Nearest, Nearest);
            if (Gdx.app != null) {
                Gdx.app.log("ObjectSprites", "Loaded " + path + " for " + objectId);
            }
            return texture;
        } catch (RuntimeException ex) {
            warn(path + " failed to load; drawing placeholder", ex);
            return null;
        }
    }

    private static void warn(String message, RuntimeException error) {
        if (Gdx.app != null) {
            String text = "WARNING: " + message;
            if (error == null) {
                Gdx.app.log("ObjectSprites", text);
            } else {
                Gdx.app.log("ObjectSprites", text, error);
            }
            return;
        }
        LOG.log(WARNING, message, error);
    }
}
