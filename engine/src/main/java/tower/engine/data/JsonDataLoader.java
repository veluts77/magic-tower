package tower.engine.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import tower.domain.data.FloorDefinition;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.data.RoomExit;
import tower.domain.data.SpellDefinition;
import tower.domain.data.SpellTarget;
import tower.domain.service.DataLoader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Locale.ROOT;
import static tower.domain.data.SpellTarget.CREATURE;
import static tower.domain.data.SpellTarget.OBJECT;
import static tower.domain.data.SpellTarget.ROOM;

public class JsonDataLoader implements DataLoader {

    private List<RoomDefinition> loadedRooms = List.of();

    public List<RoomDefinition> loadedRooms() {
        return List.copyOf(loadedRooms);
    }

    public RoomDefinition firstLoadedRoom() {
        if (loadedRooms.isEmpty()) {
            Gdx.app.error("JsonDataLoader", "Loaded floor has no rooms");
            throw new IllegalStateException("Loaded floor has no rooms");
        }
        return loadedRooms.get(0);
    }

    @Override
    public FloorDefinition loadFloor(String floorId) {
        FileHandle file = Gdx.files.internal("data/floors/" + floorId + ".json");
        if (!file.exists()) {
            Gdx.app.error("JsonDataLoader", "Floor file not found: " + file.path());
            throw new IllegalArgumentException("Floor file not found: " + floorId);
        }
        return loadFloor(file);
    }

    public FloorDefinition loadFloor(FileHandle file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("Floor file not found");
        }
        // Domain DTOs have no no-arg constructor, so libGDX Json cannot construct them.
        JsonValue root = new JsonReader().parse(file);
        List<RoomDefinition> rooms = readRooms(root.get("rooms"));
        loadedRooms = rooms;

        var floor = FloorDefinition.create(text(root, "id"), text(root, "displayName"));
        var roomIds = new ArrayList<String>();
        for (RoomDefinition room : rooms) {
            roomIds.add(room.id());
        }
        floor.setRoomIds(roomIds);
        return floor;
    }

    @Override
    public List<SpellDefinition> loadSpells() {
        FileHandle directory = Gdx.files.internal("data/spells");
        if (!directory.exists() || !directory.isDirectory()) {
            Gdx.app.error("JsonDataLoader", "Spells directory not found: " + directory.path());
            throw new IllegalArgumentException("Spells directory not found");
        }
        FileHandle[] files = directory.list(".json");
        var spells = new ArrayList<SpellDefinition>();
        if (files != null) {
            for (FileHandle file : files) {
                spells.add(readSpell(file));
            }
        }
        spells.sort(Comparator.comparing(SpellDefinition::id));
        return List.copyOf(spells);
    }

    private static SpellDefinition readSpell(FileHandle file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("Spell file not found");
        }
        JsonValue root = new JsonReader().parse(file);
        var spell = SpellDefinition.create(
                text(root, "id"),
                text(root, "displayName"),
                text(root, "description"),
                spellTarget(root));
        if (spell.id().isBlank()) {
            throw new IllegalArgumentException("Spell id is missing: " + file.path());
        }
        return spell;
    }

    private static List<RoomDefinition> readRooms(JsonValue roomsJson) {
        var rooms = new ArrayList<RoomDefinition>();
        if (roomsJson == null) {
            return rooms;
        }
        for (JsonValue roomJson : roomsJson) {
            var room = RoomDefinition.create(
                    text(roomJson, "id"),
                    text(roomJson, "displayName"),
                    text(roomJson, "description"));
            room.setObjects(readObjects(roomJson.get("objects")));
            room.setExits(readExits(roomJson.get("exits")));
            rooms.add(room);
        }
        return rooms;
    }

    private static List<ObjectDefinition> readObjects(JsonValue objectsJson) {
        var objects = new ArrayList<ObjectDefinition>();
        if (objectsJson == null) {
            return objects;
        }
        for (JsonValue objectJson : objectsJson) {
            var object = ObjectDefinition.create(
                    text(objectJson, "id"),
                    text(objectJson, "displayName"),
                    text(objectJson, "type"),
                    text(objectJson, "description"));
            object.setSupportedSpells(readIds(objectJson.get("supportedSpells")));
            objects.add(object);
        }
        return objects;
    }

    private static List<RoomExit> readExits(JsonValue exitsJson) {
        var exits = new ArrayList<RoomExit>();
        if (exitsJson == null) {
            return exits;
        }
        for (JsonValue exitJson : exitsJson) {
            exits.add(RoomExit.create(text(exitJson, "targetRoomId"), text(exitJson, "label")));
        }
        return exits;
    }

    private static List<String> readIds(JsonValue array) {
        var ids = new ArrayList<String>();
        if (array == null || !array.isArray()) {
            return ids;
        }
        for (JsonValue value : array) {
            String id = value.asString();
            if (id != null && !id.isBlank()) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static SpellTarget spellTarget(JsonValue node) {
        String value = text(node, "target");
        if (value.isBlank()) {
            return OBJECT;
        }
        return switch (value.toLowerCase(ROOT)) {
            case "object" -> OBJECT;
            case "room" -> ROOM;
            case "creature" -> CREATURE;
            default -> throw new IllegalArgumentException("Unknown spell target: " + value);
        };
    }

    private static String text(JsonValue node, String name) {
        if (node == null) {
            return "";
        }
        String value = node.getString(name, "");
        return value == null ? "" : value;
    }
}
