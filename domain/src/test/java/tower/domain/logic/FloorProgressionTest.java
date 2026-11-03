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

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import static tower.domain.data.SpiritFunction.SEEK;
import static tower.domain.state.RoomState.LIT;

class FloorProgressionTest {

    private final ActionResolver resolver = new ActionResolver();
    private final ActionExecutor executor = new ActionExecutor();

    @Test
    void floorKeepsEightMarksAndTheThreshold() {
        FloorDefinition floor = groundMarks();

        assertEquals(8, floor.markCount());
        assertEquals(7, floor.ascentThreshold());
        assertFalse(floor.ascentUnlocked(6));
        assertTrue(floor.ascentUnlocked(7));
        assertFalse(floor.ascentUnlocked(0));

        MasteryMarkDefinition first = floor.marks().get(0);
        assertEquals("faded_words", first.id());
        assertEquals("Faded Words", first.displayName());
        assertEquals("The faded inscription has been read.", first.description());
        assertEquals(EXAMINE, first.action());
        assertEquals("inscription", first.target());
        assertTrue(first.matches(EXAMINE, "inscription"));
        assertFalse(first.matches(EXAMINE, "sigil"));

        assertEquals(TAKE, floor.marks().get(1).action());
        assertEquals("ring_of_quiet_flame", floor.marks().get(1).target());
        assertEquals(CAST, floor.marks().get(2).action());
        assertEquals("ash_slug", floor.marks().get(2).target());
        assertEquals(SUMMON, floor.marks().get(3).action());
        assertEquals("seeker", floor.marks().get(3).target());
        assertEquals(MOVE, floor.marks().get(5).action());
        assertEquals("veil", floor.marks().get(5).target());
        assertEquals("chest", floor.marks().get(7).target());
    }

    @Test
    void examineGrantsAMarkOnce() {
        FloorDefinition floor = floor("ground", 2, List.of("study"), mark("faded_words", "Faded Words", EXAMINE, "inscription"));
        List<RoomDefinition> rooms = List.of(room("study", "Study", "A quiet study.", List.of(inscription()), List.of()));
        GameState state = GameState.start(floor);

        ActionResult first = run(state, rooms, GameAction.examine("inscription", "ignored"), floor);

        assertTrue(first.succeeded);
        assertTrue(first.message.contains("Old words."));
        assertTrue(first.message.contains("Mark gained: Faded Words."));
        assertFalse(first.message.contains("The way up is open."));
        assertTrue(first.changes.contains(MASTERY_MARK));
        assertEquals(1, state.floorState().earnedCount());
        assertTrue(state.floorState().hasMark("faded_words"));

        ActionResult second = run(state, rooms, GameAction.examine("inscription", "ignored"), floor);

        assertTrue(second.succeeded);
        assertFalse(second.message.contains("Mark gained"));
        assertFalse(second.changes.contains(MASTERY_MARK));
        assertEquals(1, state.floorState().earnedCount());
        assertEquals(Set.of("faded_words"), state.floorState().earnedMarkIds());
    }

    @Test
    void takeGrantsTheArtifactMark() {
        var ring = ObjectDefinition.create("ring", "Ring of Quiet Flame", "artifact", "A cool ring.");
        ring.setArtifactId("ring_of_quiet_flame");
        FloorDefinition floor = floor("ground", 7, List.of("hall"), mark("quiet_flame", "Quiet Flame", TAKE, "ring_of_quiet_flame"));
        List<RoomDefinition> rooms = List.of(room("hall", "Quiet Hall", "A hall.", List.of(ring), List.of()));
        GameState state = GameState.start(floor);
        var artifact = ArtifactDefinition.create(
                "ring_of_quiet_flame", "Ring of Quiet Flame", "A ring.", FIRE_PROTECTION);

        ActionResult result = executor.execute(
                state, rooms, GameAction.take("ring", "Pick up"), List.of(), List.of(), List.of(artifact), List.of(), floor, List.of(floor));

        assertTrue(result.succeeded);
        assertTrue(result.message.contains("Mark gained: Quiet Flame."));
        assertTrue(state.player().isEquipped("ring_of_quiet_flame"));
        assertTrue(state.floorState().hasMark("quiet_flame"));
        assertEquals(1, state.floorState().earnedCount());
    }

    @Test
    void castOnAnObjectGrantsThatObjectMark() {
        var brazier = ObjectDefinition.create("brazier", "Cold Brazier", "fixture", "Cold iron.");
        brazier.setSupportedSpells(List.of("spark"));
        FloorDefinition floor = floor("ground", 7, List.of("hall"), mark("brazier_lit", "Brazier Lit", CAST, "brazier"));
        List<RoomDefinition> rooms = List.of(room("hall", "Quiet Hall", "A hall.", List.of(brazier), List.of()));
        GameState state = GameState.start(floor);
        state.player().learnSpell("spark");
        state.selectObject("brazier");
        var spark = SpellDefinition.create("spark", "Spark", "A spark.", OBJECT);

        ActionResult result = executor.execute(
                state, rooms, GameAction.cast("spark", "Cast: Spark"), List.of(spark), List.of(), List.of(), List.of(), floor, List.of(floor));

        assertTrue(result.succeeded);
        assertTrue(result.message.contains("Mark gained: Brazier Lit."));
        assertEquals(LIT, state.objectCondition("brazier"));
        assertEquals(1, state.floorState().earnedCount());

        ActionResult again = executor.execute(
                state, rooms, GameAction.cast("spark", "Cast: Spark"), List.of(spark), List.of(), List.of(), List.of(), floor, List.of(floor));

        assertTrue(again.succeeded);
        assertFalse(again.message.contains("Mark gained"));
        assertEquals(1, state.floorState().earnedCount());
    }

    @Test
    void neutralizingACreatureGrantsTheCreatureMark() {
        var slug = ObjectDefinition.create("slug", "Ash Slug", "creature", "A slug.");
        slug.setCreatureId("ash_slug");
        FloorDefinition floor = floor("ground", 7, List.of("alcove"), mark("ash_slug_calmed", "Ash Slug Calmed", CAST, "ash_slug"));
        List<RoomDefinition> rooms = List.of(room("alcove", "Side Alcove", "An alcove.", List.of(slug), List.of()));
        GameState state = GameState.start(floor);
        state.player().learnSpell("spark");
        state.selectObject("slug");
        var spark = SpellDefinition.create("spark", "Spark", "A spark.", OBJECT);
        var creature = CreatureDefinition.create("ash_slug", "Ash Slug", "A slug.", List.of("spark"));

        ActionResult result = executor.execute(
                state,
                rooms,
                GameAction.cast("spark", "Cast: Spark"),
                List.of(spark),
                List.of(),
                List.of(),
                List.of(creature),
                floor,
                List.of(floor));

        assertTrue(result.succeeded);
        assertTrue(result.message.contains("Mark gained: Ash Slug Calmed."));
        assertTrue(state.creatureNeutralized("ash_slug"));
        assertTrue(state.floorState().hasMark("ash_slug_calmed"));
        assertFalse(state.floorState().hasMark("slug"));
    }

    @Test
    void summonGrantsTheSpiritMark() {
        FloorDefinition floor = floor("ground", 7, List.of("entrance"), mark("seeker_called", "Seeker Called", SUMMON, "seeker"));
        List<RoomDefinition> rooms = List.of(room("entrance", "Entrance", "An entrance.", List.of(), List.of()));
        GameState state = GameState.start(floor);
        state.player().learnSpirit("seeker");
        var seeker = SpiritDefinition.create("seeker", "Seeker", "Searches.", SEEK);

        ActionResult result = executor.execute(
                state, rooms, GameAction.summon("seeker", "Summon: Seeker"), List.of(), List.of(seeker), List.of(), List.of(), floor, List.of(floor));

        assertTrue(result.succeeded);
        assertTrue(result.message.contains("Mark gained: Seeker Called."));
        assertTrue(state.floorState().hasMark("seeker_called"));
        assertEquals(1, state.floorState().earnedCount());
    }

    @Test
    void moveGrantsTheRoomMark() {
        FloorDefinition floor = floor(
                "ground", 7, List.of("hall", "veil"), mark("flame_veil", "Flame Veil", MOVE, "veil"));
        List<RoomDefinition> rooms = List.of(
                room("hall", "Quiet Hall", "A hall.", List.of(), List.of(RoomExit.create("veil", "Go to the Flame Veil"))),
                room("veil", "Flame Veil", "Fire.", List.of(), List.of()));
        GameState state = GameState.start(floor);

        ActionResult result = run(state, rooms, GameAction.move("veil", "Go to the Flame Veil"), floor);

        assertTrue(result.succeeded);
        assertTrue(result.message.contains("Mark gained: Flame Veil."));
        assertEquals("veil", state.currentRoomId());
        assertTrue(state.floorState().hasMark("flame_veil"));
    }

    @Test
    void blockedMoveDoesNotGrantAMark() {
        var veil = RoomDefinition.create("veil", "Flame Veil", "Fire.");
        veil.setHazard(FIRE);
        FloorDefinition floor = floor(
                "ground", 7, List.of("hall", "veil"), mark("flame_veil", "Flame Veil", MOVE, "veil"));
        List<RoomDefinition> rooms = List.of(
                room("hall", "Quiet Hall", "A hall.", List.of(), List.of(RoomExit.create("veil", "Go to the Flame Veil"))),
                veil);
        GameState state = GameState.start(floor);

        ActionResult result = run(state, rooms, GameAction.move("veil", "Go to the Flame Veil"), floor);

        assertFalse(result.succeeded);
        assertEquals("hall", state.currentRoomId());
        assertEquals(0, state.floorState().earnedCount());
        assertFalse(state.floorState().hasMark("flame_veil"));
    }

    @Test
    void ascendStaysHiddenUntilTheThreshold() {
        FloorDefinition floor = floor("ground", 7, List.of("hall"), mark("quiet_flame", "Quiet Flame", TAKE, "ring"));
        List<RoomDefinition> rooms = List.of(ascentRoom("hall", "Quiet Hall"));
        FloorDefinition upper = floor("upper", 1, List.of("landing"));
        GameState state = GameState.start(floor);
        for (int i = 0; i < 6; i++) {
            state.floorState().addMark("mark-" + i);
        }

        assertTrue(actions(state, rooms, floor).stream().noneMatch(action -> action.type == ASCEND));
        ActionResult rejected = executor.execute(
                state, rooms, GameAction.ascend("upper", "Ascend"), List.of(), List.of(), List.of(), List.of(), floor, List.of(floor, upper));

        assertFalse(rejected.succeeded);
        assertEquals("ground", state.currentFloorId());
        assertEquals(6, state.floorState().earnedCount());

        state.floorState().addMark("mark-6");
        List<GameAction> open = actions(state, rooms, floor);

        assertTrue(open.stream().anyMatch(action -> action.type == ASCEND && "upper".equals(action.target) && "Ascend".equals(action.label)));
        assertTrue(floor.ascentUnlocked(state.floorState().earnedCount()));
    }

    @Test
    void crossingTheThresholdOpensTheWay() {
        FloorDefinition floor = floor(
                "ground",
                2,
                List.of("study"),
                mark("faded_words", "Faded Words", EXAMINE, "inscription"),
                mark("worn_sigil", "Worn Sigil", EXAMINE, "sigil"));
        List<RoomDefinition> rooms = List.of(ascentRoom(
                "study",
                "Study",
                inscription(),
                ObjectDefinition.create("sigil", "Worn Sigil", "clue", "A spiral.")));
        FloorDefinition upper = floor("upper", 1, List.of("landing"));
        GameState state = GameState.start(floor);

        ActionResult first = run(state, rooms, GameAction.examine("inscription", "ignored"), floor, List.of(floor, upper));

        assertEquals(1, state.floorState().earnedCount());
        assertFalse(first.message.contains("The way up is open."));
        assertFalse(first.changes.contains(ASCENT_UNLOCKED));
        assertTrue(actions(state, rooms, floor).stream().noneMatch(action -> action.type == ASCEND));

        ActionResult second = run(state, rooms, GameAction.examine("sigil", "ignored"), floor, List.of(floor, upper));

        assertEquals(2, state.floorState().earnedCount());
        assertTrue(second.message.contains("Mark gained: Worn Sigil."));
        assertTrue(second.message.contains("The way up is open."));
        assertTrue(second.changes.contains(MASTERY_MARK));
        assertTrue(second.changes.contains(ASCENT_UNLOCKED));
        assertTrue(actions(state, rooms, floor).stream().anyMatch(action -> action.type == ASCEND));
    }

    @Test
    void ascendResetsThePreviousFloorAndKeepsThePlayer() {
        FloorDefinition ground = floor("ground", 1, List.of("hall", "alcove"));
        var brazier = ObjectDefinition.create("brazier", "Cold Brazier", "fixture", "Cold iron.");
        List<RoomDefinition> rooms = List.of(
                room("hall", "Quiet Hall", "A hall.", List.of(brazier), List.of(
                        RoomExit.create("alcove", "Go to the Side Alcove"),
                        RoomExit.ascend("upper", "Ascend"))),
                room("alcove", "Side Alcove", "An alcove.", List.of(), List.of(RoomExit.create("hall", "Go back"))));
        FloorDefinition upper = floor("upper", 1, List.of("hall"));
        GameState state = GameState.start(ground);
        state.setObjectCondition("brazier", LIT);
        state.selectObject("brazier");
        state.player().learnSpell("spark");
        state.player().equipArtifact("ring_of_quiet_flame", FIRE_PROTECTION);
        run(state, rooms, GameAction.move("alcove", "Go to the Side Alcove"), ground);
        assertEquals(List.of("hall", "alcove"), List.copyOf(state.visitedRoomIds()));
        run(state, rooms, GameAction.move("hall", "Go back"), ground);
        assertEquals(LIT, state.objectCondition("brazier"));
        assertTrue(state.floorState().addMark("seed"));

        ActionResult result = executor.execute(
                state,
                rooms,
                GameAction.ascend("upper", "Ascend"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                ground,
                List.of(ground, upper));

        assertTrue(result.succeeded);
        assertEquals("You ascend to Upper Floor.", result.message);
        assertEquals(List.of(CURRENT_FLOOR), result.changes);
        assertEquals("upper", state.currentFloorId());
        assertEquals("hall", state.currentRoomId());
        assertEquals(Set.of("hall"), state.visitedRoomIds());
        assertEquals("", state.objectCondition("brazier"));
        assertEquals(0, state.floorState().earnedCount());
        assertFalse(state.floorState().hasMark("seed"));
        assertNull(state.selectedObjectId());
        assertTrue(state.player().knowsSpell("spark"));
        assertTrue(state.player().isEquipped("ring_of_quiet_flame"));
    }

    private ActionResult run(GameState state, List<RoomDefinition> rooms, GameAction action, FloorDefinition floor) {
        return run(state, rooms, action, floor, List.of(floor));
    }

    private ActionResult run(
            GameState state,
            List<RoomDefinition> rooms,
            GameAction action,
            FloorDefinition floor,
            List<FloorDefinition> floors) {
        return executor.execute(state, rooms, action, List.of(), List.of(), List.of(), List.of(), floor, floors);
    }

    private List<GameAction> actions(GameState state, List<RoomDefinition> rooms, FloorDefinition floor) {
        return resolver.availableActions(state, rooms, List.of(), floor);
    }

    private static FloorDefinition groundMarks() {
        return floor(
                "ground",
                7,
                List.of("entrance", "hall", "alcove", "veil"),
                mark("faded_words", "Faded Words", "The faded inscription has been read.", EXAMINE, "inscription"),
                mark("quiet_flame", "Quiet Flame", "The Ring of Quiet Flame has been taken.", TAKE, "ring_of_quiet_flame"),
                mark("ash_slug_calmed", "Ash Slug Calmed", "The ash slug no longer blocks the chest.", CAST, "ash_slug"),
                mark("seeker_called", "Seeker Called", "The seeker has been summoned on this floor.", SUMMON, "seeker"),
                mark("worn_sigil", "Worn Sigil", "The hidden sigil has been examined.", EXAMINE, "sigil"),
                mark("flame_veil", "Flame Veil", "The flame veil has been crossed.", MOVE, "veil"),
                mark("brazier_lit", "Brazier Lit", "Spark has been cast on the cold brazier.", CAST, "brazier"),
                mark("ash_chest", "Ash Chest", "Spark has been cast on the ash chest.", CAST, "chest"));
    }

    private static FloorDefinition floor(String id, int threshold, List<String> roomIds, MasteryMarkDefinition... marks) {
        String name = "upper".equals(id) ? "Upper Floor" : "Ground Floor";
        var floor = FloorDefinition.create(id, name);
        floor.setAscentThreshold(threshold);
        floor.setRoomIds(roomIds);
        floor.setMarks(List.of(marks));
        return floor;
    }

    private static MasteryMarkDefinition mark(String id, String name, ActionType action, String target) {
        return mark(id, name, name, action, target);
    }

    private static MasteryMarkDefinition mark(
            String id, String name, String description, ActionType action, String target) {
        return MasteryMarkDefinition.create(id, name, description, action, target);
    }

    private static RoomDefinition ascentRoom(String id, String name, ObjectDefinition... objects) {
        return room(id, name, "A room.", List.of(objects), List.of(RoomExit.ascend("upper", "Ascend")));
    }

    private static RoomDefinition room(
            String id, String name, String description, List<ObjectDefinition> objects, List<RoomExit> exits) {
        var room = RoomDefinition.create(id, name, description);
        room.setObjects(objects);
        room.setExits(exits);
        return room;
    }

    private static ObjectDefinition inscription() {
        return ObjectDefinition.create("inscription", "Faded Inscription", "clue", "Old words.");
    }
}
