package tower.domain.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tower.domain.action.ActionResult;
import tower.domain.action.GameAction;
import tower.domain.data.FloorDefinition;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.data.RoomExit;
import tower.domain.data.SpellDefinition;
import tower.domain.state.GameState;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tower.domain.action.ActionResult.Change.OBJECT_CONDITION;
import static tower.domain.action.ActionType.CAST;
import static tower.domain.action.ActionType.EXAMINE;
import static tower.domain.data.SpellTarget.OBJECT;
import static tower.domain.data.SpellTarget.ROOM;
import static tower.domain.state.RoomState.LIT;

class SpellActionsTest {

    private static final String SPARK_DESCRIPTION = "A brief tongue of fire, enough to wake a cold flame.";
    private static final String BRAZIER_DESCRIPTION =
            "The iron bowl is cold. Ash has settled into a hard grey crust.";

    private List<RoomDefinition> rooms;
    private List<SpellDefinition> spells;
    private GameState state;
    private ActionResolver resolver;
    private ActionExecutor executor;

    @BeforeEach
    void setUp() {
        rooms = groundRooms();
        spells = List.of(spark());
        state = GameState.start(groundFloor());
        resolver = new ActionResolver();
        executor = new ActionExecutor();
    }

    @Test
    void spellDefinitionKeepsLoadedFields() {
        SpellDefinition spark = spark();

        assertEquals("spark", spark.id());
        assertEquals("Spark", spark.displayName());
        assertEquals(SPARK_DESCRIPTION, spark.description());
        assertEquals(OBJECT, spark.target());
    }

    @Test
    void playerLearnsASpellAndKnowsIt() {
        assertFalse(state.player().knowsSpell("spark"));
        assertTrue(state.player().knownSpellIds().isEmpty());

        state.player().learnSpell("spark");
        state.player().learnSpell("spark");

        assertTrue(state.player().knowsSpell("spark"));
        assertFalse(state.player().knowsSpell("other"));
        assertFalse(state.player().knowsSpell(null));
        assertEquals(Set.of("spark"), state.player().knownSpellIds());
    }

    @Test
    void blankSpellIdCannotBeLearned() {
        assertThrows(IllegalArgumentException.class, () -> state.player().learnSpell(" "));
        assertThrows(IllegalArgumentException.class, () -> state.player().learnSpell(null));
        assertTrue(state.player().knownSpellIds().isEmpty());
    }

    @Test
    void supportedSpellIsOfferedWhenThePlayerKnowsIt() {
        enterHall();
        state.player().learnSpell("spark");

        List<GameAction> actions = resolver.availableActions(state, rooms, "brazier", spells);

        assertEquals(2, actions.size());
        assertEquals(EXAMINE, actions.get(0).type);
        GameAction cast = actions.get(1);
        assertEquals("cast:spark", cast.id);
        assertEquals(CAST, cast.type);
        assertEquals("spark", cast.target);
        assertEquals("Cast: Spark", cast.label);
    }

    @Test
    void castLabelFallsBackToTheSpellId() {
        enterHall();
        state.player().learnSpell("spark");
        var unnamed = SpellDefinition.create("spark", " ", SPARK_DESCRIPTION, OBJECT);

        GameAction cast = resolver.availableActions(state, rooms, "brazier", List.of(unnamed)).get(1);

        assertEquals("Cast: spark", cast.label);
    }

    @Test
    void unknownSpellIsNotOffered() {
        enterHall();

        List<GameAction> actions = resolver.availableActions(state, rooms, "brazier", spells);

        assertEquals(1, actions.size());
        assertEquals(EXAMINE, actions.get(0).type);
        assertTrue(actions.stream().noneMatch(action -> action.type == CAST));
    }

    @Test
    void unsupportedSpellIsNotOffered() {
        state.player().learnSpell("spark");

        List<GameAction> actions = resolver.availableActions(state, rooms, "arch", spells);

        assertEquals(1, actions.size());
        assertEquals(EXAMINE, actions.get(0).type);
        assertTrue(actions.stream().noneMatch(action -> action.type == CAST));
    }

    @Test
    void roomTargetSpellIsNotOfferedOnAnObject() {
        enterHall();
        state.player().learnSpell("spark");
        var roomSpark = SpellDefinition.create("spark", "Spark", SPARK_DESCRIPTION, ROOM);

        List<GameAction> actions = resolver.availableActions(state, rooms, "brazier", List.of(roomSpark));

        assertEquals(1, actions.size());
        assertEquals(EXAMINE, actions.get(0).type);
    }

    @Test
    void castingSparkLightsTheSelectedObject() {
        enterHall();
        state.player().learnSpell("spark");
        state.selectObject("brazier");
        var visitedBefore = new LinkedHashSet<>(state.visitedRoomIds());

        ActionResult result = executor.execute(state, rooms, GameAction.cast("spark", "ignored"), spells);

        assertTrue(result.succeeded);
        assertEquals("A spark catches. Cold Brazier burns.", result.message);
        assertEquals(List.of(OBJECT_CONDITION), result.changes);
        assertEquals(LIT, state.objectCondition("brazier"));
        assertEquals("hall", state.currentRoomId());
        assertEquals(visitedBefore, state.visitedRoomIds());
        assertEquals("brazier", state.selectedObjectId());

        ActionResult examined = executor.execute(state, rooms, GameAction.examine("brazier", "ignored"), spells);
        assertEquals(BRAZIER_DESCRIPTION + " A small flame burns there now.", examined.message);
    }

    @Test
    void litConditionStaysWithTheRoom() {
        enterHall();
        state.player().learnSpell("spark");
        state.selectObject("brazier");
        executor.execute(state, rooms, GameAction.cast("spark", "ignored"), spells);

        executor.execute(state, rooms, GameAction.move("entrance", "ignored"));
        assertEquals("", state.objectCondition("brazier"));

        executor.execute(state, rooms, GameAction.move("hall", "ignored"));
        assertEquals(LIT, state.objectCondition("brazier"));
    }

    @Test
    void castingAgainLeavesTheObjectLit() {
        enterHall();
        state.player().learnSpell("spark");
        state.selectObject("brazier");
        executor.execute(state, rooms, GameAction.cast("spark", "ignored"), spells);

        ActionResult again = executor.execute(state, rooms, GameAction.cast("spark", "ignored"), spells);

        assertTrue(again.succeeded);
        assertEquals("Cold Brazier is already burning.", again.message);
        assertTrue(again.changes.isEmpty());
        assertEquals(LIT, state.objectCondition("brazier"));
    }

    @Test
    void castingWithoutKnowledgeDoesNotChangeTheObject() {
        enterHall();
        state.selectObject("brazier");

        ActionResult result = executor.execute(state, rooms, GameAction.cast("spark", "ignored"), spells);

        assertFalse(result.succeeded);
        assertEquals("That action is not available.", result.message);
        assertTrue(result.changes.isEmpty());
        assertEquals("", state.objectCondition("brazier"));
        assertEquals("hall", state.currentRoomId());
    }

    @Test
    void castingOnAnUnsupportedObjectDoesNotChangeIt() {
        state.player().learnSpell("spark");
        state.selectObject("arch");

        ActionResult result = executor.execute(state, rooms, GameAction.cast("spark", "ignored"), spells);

        assertFalse(result.succeeded);
        assertEquals("That action is not available.", result.message);
        assertTrue(result.changes.isEmpty());
        assertEquals("", state.objectCondition("arch"));
        assertEquals("entrance", state.currentRoomId());
    }

    private void enterHall() {
        executor.execute(state, rooms, GameAction.move("hall", "Go to the Quiet Hall"));
    }

    private static SpellDefinition spark() {
        return SpellDefinition.create("spark", "Spark", SPARK_DESCRIPTION, OBJECT);
    }

    private static FloorDefinition groundFloor() {
        var floor = FloorDefinition.create("ground", "Ground Floor");
        floor.setRoomIds(List.of("entrance", "hall", "alcove"));
        return floor;
    }

    private static List<RoomDefinition> groundRooms() {
        RoomDefinition entrance = room(
                "entrance",
                "Entrance",
                "A worn stone arch leads into the tower. Dust hangs in the still air.",
                List.of(object("arch", "Stone Arch", "passage",
                        "The stones are worn smooth. Faint marks cover the inner curve.")),
                exit("hall", "Go to the Quiet Hall"));
        var brazier = object("brazier", "Cold Brazier", "fixture", BRAZIER_DESCRIPTION);
        brazier.setSupportedSpells(List.of("spark"));
        RoomDefinition hall = room(
                "hall",
                "Quiet Hall",
                "A long hall is quiet. A cold brazier and a faded inscription wait in the dim light.",
                List.of(
                        brazier,
                        object("inscription", "Faded Inscription", "clue",
                                "The letters have faded. A few words still speak of a quiet flame.")),
                exit("entrance", "Go to the Entrance"),
                exit("alcove", "Go to the Side Alcove"));
        RoomDefinition alcove = room(
                "alcove",
                "Side Alcove",
                "A narrow alcove holds an empty pedestal. The passage back is the only way out.",
                List.of(object(
                        "pedestal",
                        "Empty Pedestal",
                        "fixture",
                        "The pedestal is empty. A round hollow marks where something once stood.")),
                exit("hall", "Go to the Quiet Hall"));
        return List.of(entrance, hall, alcove);
    }

    private static RoomDefinition room(
            String id,
            String displayName,
            String description,
            List<ObjectDefinition> objects,
            RoomExit... exits) {
        var room = RoomDefinition.create(id, displayName, description);
        room.setObjects(new ArrayList<>(objects));
        room.setExits(new ArrayList<>(List.of(exits)));
        return room;
    }

    private static ObjectDefinition object(String id, String displayName, String type, String description) {
        return ObjectDefinition.create(id, displayName, type, description);
    }

    private static RoomExit exit(String targetRoomId, String label) {
        return RoomExit.create(targetRoomId, label);
    }
}
