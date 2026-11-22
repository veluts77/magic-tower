package tower.engine.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import tower.domain.data.ObjectDefinition;
import tower.domain.state.GameState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
import static java.util.Map.entry;
import static java.util.logging.Level.WARNING;
import static tower.domain.state.RoomState.LIT;

public class ObjectSprites {

    private static final Logger LOG = Logger.getLogger(ObjectSprites.class.getName());

    private static final Map<String, String> PATHS = Map.ofEntries(
            entry("lantern", "images/objects/obj_magic_lantern.png"),
            entry("lantern:lit", "images/objects/obj_magic_lantern_lit.png"),
            entry("old_chest", "images/objects/obj_old_chest_closed.png"),
            entry("old_chest:open", "images/objects/obj_old_chest_open.png"),
            entry("broken_chandelier", "images/objects/obj_broken_chandelier.png"),
            entry("dusty_floor", "images/objects/obj_dusty_floor.png"),
            entry("closed_door", "images/objects/obj_door_closed.png"),
            entry("closed_door_left", "images/objects/obj_door_left.png"),
            entry("closed_door_right", "images/objects/obj_door_right.png"),
            entry("ancient_altar", "images/objects/obj_stone_altar.png"),
            entry("stairs_up", "images/objects/obj_stairs_up.png"),
            entry("map_table", "images/objects/obj_map_table.png"),
            entry("ancient_folio", "images/objects/obj_ancient_folio.png"),
            entry("ancient_rune", "images/objects/obj_hidden_rune.png"),
            entry("push_page", "images/items/item_spell_scroll_push.png"),
            entry("seeker_page", "images/items/item_spell_scroll_seeker.png"),
            entry("quiet_ring", "images/items/item_artifact_ring_fire.png"),
            entry("ash_slug", "images/creatures/creature_ash_slug.png"),
            entry("ash_slug:neutralized", "images/creatures/creature_ash_slug_neutralized.png"),
            entry("player", "images/player/player_idle.png"),
            entry("seeker", "images/spirits/spirit_seeker.png"));

    private final Map<String, Texture> textures = new LinkedHashMap<>();

    public ObjectSprites() {
        for (var entry : PATHS.entrySet()) {
            Texture texture = load(entry.getKey(), entry.getValue());
            if (texture != null) {
                textures.put(entry.getKey(), texture);
            }
        }
    }

    public Texture textureFor(ObjectDefinition object, GameState state) {
        if (object == null) {
            return null;
        }
        return textures.get(resolveKey(object, state));
    }

    public Texture textureFor(String key) {
        return textures.get(key);
    }

    public void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();
    }

    private static String resolveKey(ObjectDefinition object, GameState state) {
        String objectId = object.id();
        if (objectId == null) {
            return "";
        }
        if ("lantern".equals(objectId) && state != null && LIT.equals(state.objectCondition(objectId))) {
            return "lantern:lit";
        }
        if ("old_chest".equals(objectId) && chestOpen(object, state)) {
            return "old_chest:open";
        }
        if ("ash_slug".equals(objectId) && slugNeutralized(object, state)) {
            return "ash_slug:neutralized";
        }
        return object.sprite();
    }

    private static boolean chestOpen(ObjectDefinition object, GameState state) {
        if (state == null) {
            return false;
        }
        String flag = object.grantsFlag();
        return flag != null && state.hasFlag(flag);
    }

    private static boolean slugNeutralized(ObjectDefinition object, GameState state) {
        if (state == null) {
            return false;
        }
        String creatureId = object.creatureId();
        if (creatureId == null || creatureId.isBlank()) {
            creatureId = object.id();
        }
        return state.creatureNeutralized(creatureId);
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
