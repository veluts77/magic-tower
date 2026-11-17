package tower.engine.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

import java.util.logging.Logger;

import static com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
import static java.util.logging.Level.WARNING;

public class ObjectSprites {

    private static final Logger LOG = Logger.getLogger(ObjectSprites.class.getName());
    private static final String LANTERN_ID = "lantern";
    private static final String LANTERN_PATH = "images/lantern.png";

    private Texture lantern;

    public ObjectSprites() {
        lantern = load(LANTERN_PATH);
    }

    public Texture textureFor(String objectId) {
        if (LANTERN_ID.equals(objectId)) {
            return lantern;
        }
        return null;
    }

    public void dispose() {
        if (lantern == null) {
            return;
        }
        lantern.dispose();
        lantern = null;
    }

    private static Texture load(String path) {
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
                Gdx.app.log("ObjectSprites", "Loaded " + path + " for " + LANTERN_ID);
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
