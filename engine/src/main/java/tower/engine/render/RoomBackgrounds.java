package tower.engine.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
import static java.util.logging.Level.WARNING;

public class RoomBackgrounds {

    private static final Logger LOG = Logger.getLogger(RoomBackgrounds.class.getName());

    private static final Map<String, String> PATHS = Map.of(
            "entrance_hall", "room_backgrounds/bg_entrance_hall.png",
            "map_hall", "room_backgrounds/bg_map_hall.png",
            "flooded_gallery", "room_backgrounds/bg_flooded_gallery.png",
            "gatekeeper_cell", "room_backgrounds/bg_gatekeeper_cell.png",
            "hidden_cell", "room_backgrounds/bg_hidden_cell.png",
            "empty_landing", "room_backgrounds/bg_empty_landing.png");

    private final Map<String, Texture> textures = new LinkedHashMap<>();

    public RoomBackgrounds() {
        for (var entry : PATHS.entrySet()) {
            Texture texture = load(entry.getKey(), entry.getValue());
            if (texture != null) {
                textures.put(entry.getKey(), texture);
            }
        }
    }

    public Texture textureFor(String roomId) {
        return textures.get(roomId);
    }

    public void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
    }

    private static Texture load(String roomId, String path) {
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
                Gdx.app.log("RoomBackgrounds", "Loaded " + path + " for " + roomId);
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
                Gdx.app.log("RoomBackgrounds", text);
            } else {
                Gdx.app.log("RoomBackgrounds", text, error);
            }
            return;
        }
        LOG.log(WARNING, message, error);
    }
}
