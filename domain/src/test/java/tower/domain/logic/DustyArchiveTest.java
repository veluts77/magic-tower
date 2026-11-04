package tower.domain.logic;

import org.junit.jupiter.api.Test;
import tower.domain.action.ActionResult;
import tower.domain.action.ActionType;
import tower.domain.action.GameAction;
import tower.domain.data.ArtifactDefinition;
import tower.domain.data.CreatureDefinition;
import tower.domain.data.FloorDefinition;
import tower.domain.data.MasteryMarkDefinition;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.data.RoomExit;
import tower.domain.data.SpellDefinition;
import tower.domain.data.SpiritDefinition;
import tower.domain.state.GameState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tower.domain.action.ActionResult.Change.ASCENT_UNLOCKED;
import static tower.domain.action.ActionResult.Change.CURRENT_FLOOR;
import static tower.domain.action.ActionResult.Change.MASTERY_MARK;
import static tower.domain.action.ActionType.ASCEND;
import static tower.domain.action.ActionType.CAST;
import static tower.domain.action.ActionType.EXAMINE;
import static tower.domain.action.ActionType.MOVE;
import static tower.domain.action.ActionType.SUMMON;
import static tower.domain.action.ActionType.TAKE;
import static tower.domain.data.ArtifactEffect.FIRE_PROTECTION;
import static tower.domain.data.RoomHazard.FIRE;
import static tower.domain.data.SpellTarget.OBJECT;
import static tower.domain.data.SpellTarget.ROOM;
import static tower.domain.data.SpiritFunction.SEEK;

class DustyArchiveTest {

    private final ActionResolver resolver = new ActionResolver();
    private final ActionExecutor executor = new ActionExecutor();

    @Test
    void startingMarkMustAlreadyBeDefined() {
        FloorDefinition floor = FloorDefinition.create("floor_01_dusty_archive", "Пыльный архив");
        floor.setMarks(List.of(MasteryMarkDefinition.create(
                "found_spark", "Искра", "Искра уже известна.", EXAMINE, "spark")));
        floor.setStartingMarks(List.of("found_spark", "missing", " "));
        floor.setStartingSpells(List.of("spark", "spark", " "));

        assertEquals(List.of("spark"), floor.startingSpells());
        assertEquals(List.of("found_spark"), floor.grantedAtStart());
        assertEquals(7, archive().ascentThreshold());
        assertEquals(8, archive().markCount());
    }

    @Test
    void archiveCanBeClimbed() {
        FloorDefinition floor = archive();
        FloorDefinition next = FloorDefinition.create("floor_02", "Этот этаж будет добавлен позже");
        next.setRoomIds(List.of("empty_landing"));
        List<RoomDefinition> rooms = rooms();
        List<SpellDefinition> spells = List.of(
                SpellDefinition.create("spark", "Искра", "Искра.", OBJECT),
                SpellDefinition.create("push", "Толчок", "Толчок.", OBJECT),
                SpellDefinition.create("seeker", "Искатель", "Призыв.", ROOM));
        List<SpiritDefinition> spirits = List.of(
                SpiritDefinition.create("seeker", "Дух-искатель", "Ищет.", SEEK));
        List<ArtifactDefinition> artifacts = List.of(ArtifactDefinition.create(
                "ring_of_quiet_flame", "Кольцо тихого пламени", "Кольцо.", FIRE_PROTECTION));
        List<CreatureDefinition> creatures = List.of(CreatureDefinition.create(
                "ash_slug", "Пепельный слизень", "Слизень.", List.of("spark")));
        GameState state = GameState.start(floor);
        state.player().learnSpell("spark");
        state.floorState().addMark("found_spark");

        assertEquals("entrance_hall", state.currentRoomId());
        assertTrue(state.player().knowsSpell("spark"));
        assertFalse(state.player().knowsSpell("push"));
        assertFalse(state.player().knowsSpirit("seeker"));
        assertTrue(resolver.availableActions(state, rooms, spirits, floor).stream()
                .noneMatch(action -> action.type == SUMMON || action.type == ASCEND));

        ActionResult toMaps = run(state, rooms, GameAction.move("map_hall", "Идти в зал карт"), floor, next, spells, spirits, artifacts, creatures);
        assertTrue(toMaps.succeeded);
        assertEquals(1, state.floorState().earnedCount());

        ActionResult push = run(state, rooms, GameAction.take("push_page", "Pick up"), floor, next, spells, spirits, artifacts, creatures);
        assertTrue(push.succeeded);
        assertTrue(push.message.contains("You take Толчок."));
        assertTrue(push.message.contains("Mark gained: Толчок."));
        assertTrue(state.player().knowsSpell("push"));
        assertTrue(state.floorState().hasMark("found_push"));
        assertEquals(2, state.floorState().earnedCount());

        state.selectObject("lantern");
        ActionResult lantern = run(state, rooms, GameAction.cast("spark", "Cast: Искра"), floor, next, spells, spirits, artifacts, creatures);
        assertTrue(lantern.succeeded);
        assertTrue(lantern.message.contains("Mark gained: Зажжённый фонарь."));
        assertTrue(state.floorState().hasMark("lit_the_lantern"));

        run(state, rooms, GameAction.move("flooded_gallery", "Идти в затопленную галерею"), floor, next, spells, spirits, artifacts, creatures);
        ActionResult blockedCell = run(
                state, rooms, GameAction.move("gatekeeper_cell", "Идти в келью привратника"), floor, next, spells, spirits, artifacts, creatures);
        assertFalse(blockedCell.succeeded);
        assertEquals("A veil of fire blocks the way.", blockedCell.message);
        assertEquals("flooded_gallery", state.currentRoomId());
        assertFalse(state.floorState().hasMark("entered_gatekeeper_cell"));

        ActionResult ring = run(state, rooms, GameAction.take("quiet_ring", "Pick up"), floor, next, spells, spirits, artifacts, creatures);
        assertTrue(ring.succeeded);
        assertTrue(state.player().isEquipped("ring_of_quiet_flame"));
        assertTrue(state.floorState().hasMark("picked_ring"));

        ActionResult cell = run(
                state, rooms, GameAction.move("gatekeeper_cell", "Идти в келью привратника"), floor, next, spells, spirits, artifacts, creatures);
        assertTrue(cell.succeeded);
        assertTrue(cell.message.contains("Mark gained: Келья привратника."));
        assertEquals("gatekeeper_cell", state.currentRoomId());

        ActionResult locked = run(state, rooms, GameAction.move("hidden_cell", "Идти в скрытую келью"), floor, next, spells, spirits, artifacts, creatures);
        assertFalse(locked.succeeded);
        assertEquals("The way is locked.", locked.message);
        assertEquals("gatekeeper_cell", state.currentRoomId());
        assertEquals(List.of("ash_slug", "old_chest"), visibleIds(state, rooms, "gatekeeper_cell"));
        assertTrue(resolver.availableActions(state, rooms, "old_chest", spells, creatures).stream()
                .noneMatch(action -> action.type == CAST || action.type == TAKE));

        state.selectObject("old_chest");
        ActionResult blockedLook = run(
                state, rooms, GameAction.examine("old_chest", "Examine"), floor, next, spells, spirits, artifacts, creatures);
        assertTrue(blockedLook.succeeded);
        assertFalse(blockedLook.message.contains("key"));
        assertFalse(state.hasFlag("gatekeeper_key_found"));
        ActionResult blockedChest = run(state, rooms, GameAction.cast("push", "Cast: Толчок"), floor, next, spells, spirits, artifacts, creatures);
        assertFalse(blockedChest.succeeded);
        assertTrue(blockedChest.message.contains("blocks access"));
        assertFalse(state.hasFlag("gatekeeper_key_found"));

        state.selectObject("ash_slug");
        ActionResult slug = run(state, rooms, GameAction.cast("spark", "Cast: Искра"), floor, next, spells, spirits, artifacts, creatures);
        assertTrue(slug.succeeded);
        assertTrue(slug.message.contains("Mark gained: Слизень утих."));
        assertTrue(state.creatureNeutralized("ash_slug"));
        assertEquals(6, state.floorState().earnedCount());
        assertTrue(resolver.availableActions(state, rooms, spirits, floor).stream().noneMatch(action -> action.type == ASCEND));

        ActionResult stillLocked = run(state, rooms, GameAction.move("hidden_cell", "Идти в скрытую келью"), floor, next, spells, spirits, artifacts, creatures);
        assertFalse(stillLocked.succeeded);
        assertEquals("The way is locked.", stillLocked.message);

        state.selectObject("old_chest");
        ActionResult key = run(
                state, rooms, GameAction.examine("old_chest", "Examine"), floor, next, spells, spirits, artifacts, creatures);
        assertTrue(key.succeeded);
        assertEquals("Старый сундук opens. You find the key.", key.message);
        assertTrue(state.hasFlag("gatekeeper_key_found"));
        assertEquals(6, state.floorState().earnedCount());

        ActionResult hidden = run(state, rooms, GameAction.move("hidden_cell", "Идти в скрытую келью"), floor, next, spells, spirits, artifacts, creatures);
        assertTrue(hidden.succeeded);
        assertEquals("hidden_cell", state.currentRoomId());
        RoomDefinition hiddenRoom = room(rooms, "hidden_cell");
        assertFalse(state.isObjectVisible(hiddenRoom, "ancient_rune"));
        assertTrue(resolver.availableActions(state, rooms, spirits, floor).stream().noneMatch(action -> action.type == SUMMON));

        ActionResult seeker = run(state, rooms, GameAction.take("seeker_page", "Pick up"), floor, next, spells, spirits, artifacts, creatures);
        assertTrue(seeker.succeeded);
        assertTrue(state.player().knowsSpell("seeker"));
        assertTrue(state.player().knowsSpirit("seeker"));
        assertTrue(resolver.availableActions(state, rooms, spirits, floor).stream()
                .anyMatch(action -> action.type == SUMMON && "seeker".equals(action.target)));

        ActionResult summon = run(state, rooms, GameAction.summon("seeker", "Summon: Дух-искатель"), floor, next, spells, spirits, artifacts, creatures);
        assertTrue(summon.succeeded);
        assertTrue(summon.message.contains("Mark gained: Искатель призван."));
        assertTrue(summon.message.contains("The way up is open."));
        assertTrue(summon.changes.contains(MASTERY_MARK));
        assertTrue(summon.changes.contains(ASCENT_UNLOCKED));
        assertTrue(state.isObjectVisible(hiddenRoom, "ancient_rune"));
        assertEquals(7, state.floorState().earnedCount());
        assertTrue(resolver.availableActions(state, rooms, spirits, floor).stream()
                .anyMatch(action -> action.type == ASCEND && "floor_02".equals(action.target)));

        ActionResult rune = run(state, rooms, GameAction.examine("ancient_rune", "Examine"), floor, next, spells, spirits, artifacts, creatures);
        assertTrue(rune.succeeded);
        assertTrue(rune.message.contains("Mark gained: Древняя руна."));
        assertFalse(rune.message.contains("The way up is open."));
        assertEquals(8, state.floorState().earnedCount());

        ActionResult ascent = run(state, rooms, GameAction.ascend("floor_02", "Подняться выше"), floor, next, spells, spirits, artifacts, creatures);
        assertTrue(ascent.succeeded);
        assertEquals("You ascend to Этот этаж будет добавлен позже.", ascent.message);
        assertEquals(List.of(CURRENT_FLOOR), ascent.changes);
        assertEquals("floor_02", state.currentFloorId());
        assertEquals("empty_landing", state.currentRoomId());
        assertEquals(0, state.floorState().earnedCount());
        assertTrue(state.player().knowsSpell("spark"));
        assertTrue(state.player().knowsSpell("push"));
        assertTrue(state.player().knowsSpirit("seeker"));
        assertTrue(state.player().isEquipped("ring_of_quiet_flame"));
    }

    @Test
    void examiningTheChestFindsTheKeyWithoutPush() {
        FloorDefinition floor = archive();
        List<RoomDefinition> rooms = rooms();
        List<SpellDefinition> spells = List.of(SpellDefinition.create("spark", "Искра", "Искра.", OBJECT));
        List<CreatureDefinition> creatures = List.of(CreatureDefinition.create(
                "ash_slug", "Пепельный слизень", "Слизень.", List.of("spark")));
        GameState state = GameState.start(floor);
        state.player().learnSpell("spark");
        state.enterRoom("gatekeeper_cell");

        assertTrue(resolver.availableActions(state, rooms, "old_chest", spells, creatures).stream()
                .noneMatch(action -> action.type == CAST));
        state.selectObject("ash_slug");
        ActionResult slug = run(
                state, rooms, GameAction.cast("spark", "Cast: Искра"), floor, floor, spells, List.of(), List.of(), creatures);
        assertTrue(slug.succeeded);

        List<GameAction> chest = resolver.availableActions(state, rooms, "old_chest", spells, creatures);
        assertEquals(List.of(EXAMINE), chest.stream().map(action -> action.type).toList());
        state.selectObject("old_chest");
        ActionResult key = run(
                state, rooms, GameAction.examine("old_chest", "Examine"), floor, floor, spells, List.of(), List.of(), creatures);
        assertEquals("Старый сундук opens. You find the key.", key.message);
        assertTrue(state.hasFlag("gatekeeper_key_found"));

        ActionResult hidden = run(
                state, rooms, GameAction.move("hidden_cell", "Идти в скрытую келью"), floor, floor, spells, List.of(), List.of(), creatures);
        assertTrue(hidden.succeeded);
        assertEquals("hidden_cell", state.currentRoomId());
    }

    @Test
    void archiveContentIsInTheFloorFile() throws IOException {
        String floor = asset("assets/data/floors/floor_01_dusty_archive.json");
        String upper = asset("assets/data/floors/floor_02.json");
        String spark = asset("assets/data/spells/spark.json");
        String push = asset("assets/data/spells/push.json");
        String seeker = asset("assets/data/spells/seeker.json");
        String spirit = asset("assets/data/spirits/seeker.json");
        String ring = asset("assets/data/artifacts/ring_of_quiet_flame.json");
        String slug = asset("assets/data/creatures/ash_slug.json");

        assertTrue(floor.contains("\"id\": \"floor_01_dusty_archive\""));
        assertTrue(floor.contains("\"displayName\": \"Пыльный архив\""));
        assertTrue(floor.contains("\"ascentThreshold\": 7"));
        assertTrue(floor.contains("\"startingSpells\": [\"spark\"]"));
        assertTrue(floor.contains("\"startingMarks\": [\"found_spark\"]"));
        for (String id : List.of(
                "entrance_hall",
                "map_hall",
                "flooded_gallery",
                "gatekeeper_cell",
                "hidden_cell",
                "broken_chandelier",
                "dusty_floor",
                "closed_door",
                "push_page",
                "lantern",
                "sealed_box",
                "quiet_ring",
                "ancient_folio",
                "old_chest",
                "ancient_altar",
                "seeker_page",
                "ancient_rune",
                "found_spark",
                "found_push",
                "lit_the_lantern",
                "picked_ring",
                "entered_gatekeeper_cell",
                "neutralized_slug",
                "summoned_seeker",
                "found_hidden_rune",
                "gatekeeper_key_found")) {
            assertTrue(floor.contains("\"" + id + "\""), id);
        }
        assertFalse(floor.contains("cell_key"));
        int gallery = floor.indexOf("\"id\": \"flooded_gallery\"");
        int cell = floor.indexOf("\"id\": \"gatekeeper_cell\"");
        int hidden = floor.indexOf("\"id\": \"hidden_cell\"");
        assertTrue(floor.indexOf("\"id\": \"entrance_hall\"") < gallery);
        assertFalse(floor.substring(gallery, cell).contains("\"hazard\""));
        assertTrue(floor.substring(cell, hidden).contains("\"hazard\": \"fire\""));
        assertTrue(floor.substring(cell, hidden).contains("\"blockedByCreature\": \"ash_slug\""));
        assertTrue(floor.substring(cell, hidden).contains("\"supportedSpells\": [\"push\"]"));
        assertTrue(floor.substring(cell, hidden).contains("\"grantsFlag\": \"gatekeeper_key_found\""));
        assertTrue(floor.substring(cell, hidden).contains("\"requiresFlag\": \"gatekeeper_key_found\""));
        assertTrue(floor.contains("\"targetFloorId\": \"floor_02\""));
        assertTrue(upper.contains("Этот этаж будет добавлен позже."));
        assertTrue(spark.contains("\"id\": \"spark\""));
        assertTrue(push.contains("\"id\": \"push\""));
        assertTrue(push.contains("\"target\": \"object\""));
        assertTrue(seeker.contains("\"id\": \"seeker\""));
        assertTrue(spirit.contains("\"function\": \"seek\""));
        assertTrue(ring.contains("\"effect\": \"fire_protection\""));
        assertTrue(slug.contains("\"neutralizedBy\": [\"spark\"]"));
        assertFalse(Files.exists(Path.of("..", "assets", "data", "floors", "ground.json")));
        assertFalse(Files.exists(Path.of("assets", "data", "floors", "ground.json")));
        assertFalse(Files.exists(Path.of("..", "assets", "data", "floors", "upper.json")));
        assertFalse(Files.exists(Path.of("assets", "data", "floors", "upper.json")));
    }

    private ActionResult run(
            GameState state,
            List<RoomDefinition> rooms,
            GameAction action,
            FloorDefinition floor,
            FloorDefinition next,
            List<SpellDefinition> spells,
            List<SpiritDefinition> spirits,
            List<ArtifactDefinition> artifacts,
            List<CreatureDefinition> creatures) {
        return executor.execute(
                state, rooms, action, spells, spirits, artifacts, creatures, floor, List.of(floor, next));
    }

    private static FloorDefinition archive() {
        var floor = FloorDefinition.create("floor_01_dusty_archive", "Пыльный архив");
        floor.setAscentThreshold(7);
        floor.setRoomIds(List.of(
                "entrance_hall", "map_hall", "flooded_gallery", "gatekeeper_cell", "hidden_cell"));
        floor.setStartingSpells(List.of("spark"));
        floor.setStartingMarks(List.of("found_spark"));
        floor.setMarks(List.of(
                mark("found_spark", "Искра", EXAMINE, "spark"),
                mark("found_push", "Толчок", TAKE, "push"),
                mark("lit_the_lantern", "Зажжённый фонарь", CAST, "lantern"),
                mark("picked_ring", "Кольцо тихого пламени", TAKE, "ring_of_quiet_flame"),
                mark("entered_gatekeeper_cell", "Келья привратника", MOVE, "gatekeeper_cell"),
                mark("neutralized_slug", "Слизень утих", CAST, "ash_slug"),
                mark("summoned_seeker", "Искатель призван", SUMMON, "seeker"),
                mark("found_hidden_rune", "Древняя руна", EXAMINE, "ancient_rune")));
        return floor;
    }

    private static List<RoomDefinition> rooms() {
        var push = ObjectDefinition.create("push_page", "Страница толчка", "spell", "Страница толчка.");
        push.setSpellId("push");
        var lantern = ObjectDefinition.create("lantern", "Магический фонарь", "fixture", "Фонарь.");
        lantern.setSupportedSpells(List.of("spark"));
        var ring = ObjectDefinition.create("quiet_ring", "Кольцо тихого пламени", "artifact", "Кольцо.");
        ring.setArtifactId("ring_of_quiet_flame");
        var slug = ObjectDefinition.create("ash_slug", "Пепельный слизень", "creature", "Слизень.");
        slug.setCreatureId("ash_slug");
        var chest = ObjectDefinition.create("old_chest", "Старый сундук", "container", "Сундук.");
        chest.setBlockedByCreature("ash_slug");
        chest.setSupportedSpells(List.of("push"));
        chest.setGrantsFlag("gatekeeper_key_found");
        var page = ObjectDefinition.create("seeker_page", "Страница искателя", "spell", "Страница искателя.");
        page.setSpellId("seeker");
        page.setSpiritId("seeker");
        var rune = ObjectDefinition.create("ancient_rune", "Древняя руна", "clue", "Малая руна в трещине камня.");
        RoomExit toHidden = RoomExit.create("hidden_cell", "Идти в скрытую келью");
        toHidden.setRequiresFlag("gatekeeper_key_found");

        RoomDefinition entrance = room("entrance_hall", "Прихожая архива", "Прихожая.", List.of(
                RoomExit.create("map_hall", "Идти в зал карт")));
        RoomDefinition maps = room("map_hall", "Зал карт", "Зал карт.", List.of(
                RoomExit.create("entrance_hall", "Вернуться в прихожую"),
                RoomExit.create("flooded_gallery", "Идти в затопленную галерею")));
        maps.setObjects(List.of(push, lantern));
        RoomDefinition gallery = room("flooded_gallery", "Затопленная галерея", "Галерея.", List.of(
                RoomExit.create("map_hall", "Вернуться в зал карт"),
                RoomExit.create("gatekeeper_cell", "Идти в келью привратника")));
        gallery.setObjects(List.of(ring));
        var cell = RoomDefinition.create("gatekeeper_cell", "Келья привратника", "Келья.");
        cell.setHazard(FIRE);
        cell.setObjects(List.of(slug, chest));
        cell.setExits(List.of(RoomExit.create("flooded_gallery", "Вернуться в галерею"), toHidden));
        var hidden = RoomDefinition.create("hidden_cell", "Скрытая келья", "Келья.");
        hidden.setObjects(List.of(page, rune));
        hidden.setHiddenObjects(List.of("ancient_rune"));
        hidden.setExits(List.of(
                RoomExit.create("gatekeeper_cell", "Вернуться в келью привратника"),
                RoomExit.ascend("floor_02", "Подняться выше")));
        return List.of(entrance, maps, gallery, cell, hidden);
    }

    private static RoomDefinition room(String id, String name, String description, List<RoomExit> exits) {
        var defined = RoomDefinition.create(id, name, description);
        defined.setExits(exits);
        return defined;
    }

    private static List<String> visibleIds(GameState state, List<RoomDefinition> rooms, String roomId) {
        return state.visibleObjects(room(rooms, roomId)).stream().map(ObjectDefinition::id).toList();
    }

    private static RoomDefinition room(List<RoomDefinition> rooms, String id) {
        for (RoomDefinition defined : rooms) {
            if (defined.sameId(id)) {
                return defined;
            }
        }
        return RoomDefinition.createNonExistingRoom();
    }

    private static MasteryMarkDefinition mark(String id, String name, ActionType action, String target) {
        return MasteryMarkDefinition.create(id, name, name, action, target);
    }

    private static String asset(String relative) throws IOException {
        Path fromModule = Path.of("..", relative);
        Path path = Files.exists(fromModule) ? fromModule : Path.of(relative);
        return Files.readString(path);
    }
}
