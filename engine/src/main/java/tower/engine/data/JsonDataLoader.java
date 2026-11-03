package tower.engine.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import tower.domain.action.ActionType;
import tower.domain.data.ArtifactDefinition;
import tower.domain.data.ArtifactEffect;
import tower.domain.data.CreatureDefinition;
import tower.domain.data.FloorDefinition;
import tower.domain.data.MasteryMarkDefinition;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.data.RoomExit;
import tower.domain.data.RoomHazard;
import tower.domain.data.SpellDefinition;
import tower.domain.data.SpellTarget;
import tower.domain.data.SpiritDefinition;
import tower.domain.data.SpiritFunction;
import tower.domain.service.DataLoader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Locale.ROOT;
import static tower.domain.action.ActionType.CAST;
import static tower.domain.action.ActionType.EXAMINE;
import static tower.domain.action.ActionType.LOOK;
import static tower.domain.action.ActionType.MOVE;
import static tower.domain.action.ActionType.SUMMON;
import static tower.domain.action.ActionType.TAKE;
import static tower.domain.data.ArtifactEffect.FIRE_PROTECTION;
import static tower.domain.data.RoomHazard.FIRE;
import static tower.domain.data.SpellTarget.CREATURE;
import static tower.domain.data.SpellTarget.OBJECT;
import static tower.domain.data.SpellTarget.ROOM;
import static tower.domain.data.SpiritFunction.OPEN;
import static tower.domain.data.SpiritFunction.PROTECT;
import static tower.domain.data.SpiritFunction.SEEK;

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
        floor.setMarks(readMarks(root.get("marks")));
        floor.setAscentThreshold(root.getInt("ascentThreshold", 0));
        floor.setStartingSpells(readIds(root.get("startingSpells")));
        floor.setStartingMarks(readIds(root.get("startingMarks")));
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

    @Override
    public List<SpiritDefinition> loadSpirits() {
        FileHandle directory = Gdx.files.internal("data/spirits");
        if (!directory.exists() || !directory.isDirectory()) {
            Gdx.app.error("JsonDataLoader", "Spirits directory not found: " + directory.path());
            throw new IllegalArgumentException("Spirits directory not found");
        }
        FileHandle[] files = directory.list(".json");
        var spirits = new ArrayList<SpiritDefinition>();
        if (files != null) {
            for (FileHandle file : files) {
                spirits.add(readSpirit(file));
            }
        }
        spirits.sort(Comparator.comparing(SpiritDefinition::id));
        return List.copyOf(spirits);
    }

    @Override
    public List<ArtifactDefinition> loadArtifacts() {
        FileHandle directory = Gdx.files.internal("data/artifacts");
        if (!directory.exists() || !directory.isDirectory()) {
            Gdx.app.error("JsonDataLoader", "Artifacts directory not found: " + directory.path());
            throw new IllegalArgumentException("Artifacts directory not found");
        }
        FileHandle[] files = directory.list(".json");
        var artifacts = new ArrayList<ArtifactDefinition>();
        if (files != null) {
            for (FileHandle file : files) {
                artifacts.add(readArtifact(file));
            }
        }
        artifacts.sort(Comparator.comparing(ArtifactDefinition::id));
        return List.copyOf(artifacts);
    }

    @Override
    public List<CreatureDefinition> loadCreatures() {
        FileHandle directory = Gdx.files.internal("data/creatures");
        if (!directory.exists() || !directory.isDirectory()) {
            Gdx.app.error("JsonDataLoader", "Creatures directory not found: " + directory.path());
            throw new IllegalArgumentException("Creatures directory not found");
        }
        FileHandle[] files = directory.list(".json");
        var creatures = new ArrayList<CreatureDefinition>();
        if (files != null) {
            for (FileHandle file : files) {
                creatures.add(readCreature(file));
            }
        }
        creatures.sort(Comparator.comparing(CreatureDefinition::id));
        return List.copyOf(creatures);
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

    private static SpiritDefinition readSpirit(FileHandle file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("Spirit file not found");
        }
        JsonValue root = new JsonReader().parse(file);
        var spirit = SpiritDefinition.create(
                text(root, "id"),
                text(root, "displayName"),
                text(root, "description"),
                spiritFunction(root));
        if (spirit.id().isBlank()) {
            throw new IllegalArgumentException("Spirit id is missing: " + file.path());
        }
        return spirit;
    }

    private static ArtifactDefinition readArtifact(FileHandle file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("Artifact file not found");
        }
        JsonValue root = new JsonReader().parse(file);
        var artifact = ArtifactDefinition.create(
                text(root, "id"),
                text(root, "displayName"),
                text(root, "description"),
                artifactEffect(root));
        if (artifact.id().isBlank()) {
            throw new IllegalArgumentException("Artifact id is missing: " + file.path());
        }
        return artifact;
    }

    private static CreatureDefinition readCreature(FileHandle file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("Creature file not found");
        }
        JsonValue root = new JsonReader().parse(file);
        var creature = CreatureDefinition.create(
                text(root, "id"),
                text(root, "displayName"),
                text(root, "description"),
                readIds(root.get("neutralizedBy")));
        if (creature.id().isBlank()) {
            throw new IllegalArgumentException("Creature id is missing: " + file.path());
        }
        return creature;
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
            room.setHiddenObjects(readIds(roomJson.get("hiddenObjects")));
            room.setExits(readExits(roomJson.get("exits")));
            room.setHazard(roomHazard(roomJson));
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
            object.setArtifactId(text(objectJson, "artifact"));
            object.setCreatureId(text(objectJson, "creature"));
            object.setBlockedByCreature(text(objectJson, "blockedByCreature"));
            object.setSpellId(text(objectJson, "spell"));
            object.setSpiritId(text(objectJson, "spirit"));
            object.setItemId(text(objectJson, "item"));
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
            String floorId = text(exitJson, "targetFloorId");
            if (!floorId.isBlank()) {
                exits.add(RoomExit.ascend(floorId, text(exitJson, "label")));
                continue;
            }
            RoomExit exit = RoomExit.create(text(exitJson, "targetRoomId"), text(exitJson, "label"));
            exit.setRequiresTaken(text(exitJson, "requiresTaken"));
            exits.add(exit);
        }
        return exits;
    }

    private static List<MasteryMarkDefinition> readMarks(JsonValue marksJson) {
        var marks = new ArrayList<MasteryMarkDefinition>();
        if (marksJson == null || !marksJson.isArray()) {
            return marks;
        }
        for (JsonValue markJson : marksJson) {
            var mark = MasteryMarkDefinition.create(
                    text(markJson, "id"),
                    text(markJson, "displayName"),
                    text(markJson, "description"),
                    markAction(markJson),
                    text(markJson, "target"));
            if (mark.id().isBlank()) {
                throw new IllegalArgumentException("Mark id is missing");
            }
            marks.add(mark);
        }
        return marks;
    }

    private static ActionType markAction(JsonValue node) {
        String value = text(node, "action");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Mark action is missing");
        }
        return switch (value.toLowerCase(ROOT)) {
            case "look" -> LOOK;
            case "move" -> MOVE;
            case "examine" -> EXAMINE;
            case "cast" -> CAST;
            case "summon" -> SUMMON;
            case "take" -> TAKE;
            default -> throw new IllegalArgumentException("Unknown mark action: " + value);
        };
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

    private static ArtifactEffect artifactEffect(JsonValue node) {
        String value = text(node, "effect");
        if (value.isBlank()) {
            return FIRE_PROTECTION;
        }
        return switch (value.toLowerCase(ROOT)) {
            case "fire_protection" -> FIRE_PROTECTION;
            default -> throw new IllegalArgumentException("Unknown artifact effect: " + value);
        };
    }

    private static RoomHazard roomHazard(JsonValue roomJson) {
        String value = text(roomJson, "hazard");
        if (value.isBlank()) {
            return null;
        }
        return switch (value.toLowerCase(ROOT)) {
            case "fire" -> FIRE;
            default -> throw new IllegalArgumentException("Unknown room hazard: " + value);
        };
    }

    private static SpiritFunction spiritFunction(JsonValue node) {
        String value = text(node, "function");
        if (value.isBlank()) {
            return SEEK;
        }
        return switch (value.toLowerCase(ROOT)) {
            case "seek" -> SEEK;
            case "open" -> OPEN;
            case "protect" -> PROTECT;
            default -> throw new IllegalArgumentException("Unknown spirit function: " + value);
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
