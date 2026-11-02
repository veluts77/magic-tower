package tower.domain.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tower.domain.action.ActionResult;
import tower.domain.action.GameAction;
import tower.domain.data.FloorDefinition;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.data.RoomExit;
import tower.domain.data.SpiritDefinition;
import tower.domain.state.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tower.domain.action.ActionResult.Change.REVEALED_OBJECTS;
import static tower.domain.action.ActionResult.Change.SPIRIT_SUMMONED;
import static tower.domain.action.ActionType.LOOK;
import static tower.domain.action.ActionType.MOVE;
import static tower.domain.action.ActionType.SUMMON;
import static tower.domain.data.SpiritFunction.PROTECT;
import static tower.domain.data.SpiritFunction.SEEK;

class SpiritActionsTest {

    private static final String SEEKER_DESCRIPTION = "A patient spirit that notices what was left unseen.";
    private static final String SIGIL_DESCRIPTION =
            "A small stone sigil, easy to miss in the dust. The carving is a tight spiral.";

    private List<RoomDefinition> rooms;
    private List<SpiritDefinition> spirits;
    private GameState state;
    private ActionResolver resolver;
    private ActionExecutor executor;

    @BeforeEach
    void setUp() {
        rooms = groundRooms();
        spirits = List.of(seeker());
        state = GameState.start(groundFloor());
        resolver = new ActionResolver();
        executor = new ActionExecutor();
    }

    @Test
    void spiritDefinitionKeepsLoadedFields() {
        SpiritDefinition seeker = seeker();

        assertEquals("seeker", seeker.id());
        assertEquals("Seeker", seeker.displayName());
        assertEquals(SEEKER_DESCRIPTION, seeker.description());
        assertEquals(SEEK, seeker.function());
    }

    @Test
    void playerLearnsASpiritAndKnowsIt() {
        assertFalse(state.player().knowsSpirit("seeker"));
        assertTrue(state.player().knownSpiritIds().isEmpty());

        state.player().learnSpirit("seeker");
        state.player().learnSpirit("seeker");

        assertTrue(state.player().knowsSpirit("seeker"));
        assertFalse(state.player().knowsSpirit("other"));
        assertFalse(state.player().knowsSpirit(null));
        assertEquals(Set.of("seeker"), state.player().knownSpiritIds());
    }

    @Test
    void blankSpiritIdCannotBeLearned() {
        assertThrows(IllegalArgumentException.class, () -> state.player().learnSpirit(" "));
        assertThrows(IllegalArgumentException.class, () -> state.player().learnSpirit(null));
        assertTrue(state.player().knownSpiritIds().isEmpty());
    }

    @Test
    void knownSeekerIsOfferedInTheCurrentRoom() {
        state.player().learnSpirit("seeker");

        List<GameAction> actions = resolver.availableActions(state, rooms, spirits);

        assertEquals(3, actions.size());
        assertEquals(LOOK, actions.get(0).type);
        assertEquals(MOVE, actions.get(1).type);
        GameAction summon = actions.get(2);
        assertEquals("summon:seeker", summon.id);
        assertEquals(SUMMON, summon.type);
        assertEquals("seeker", summon.target);
        assertEquals("Summon: Seeker", summon.label);
    }

    @Test
    void summonLabelFallsBackToTheSpiritId() {
        state.player().learnSpirit("seeker");
        var unnamed = SpiritDefinition.create("seeker", " ", SEEKER_DESCRIPTION, SEEK);

        GameAction summon = resolver.availableActions(state, rooms, List.of(unnamed)).get(2);

        assertEquals("Summon: seeker", summon.label);
    }

    @Test
    void unknownSpiritIsNotOffered() {
        List<GameAction> actions = resolver.availableActions(state, rooms, spirits);

        assertEquals(2, actions.size());
        assertEquals(LOOK, actions.get(0).type);
        assertEquals(MOVE, actions.get(1).type);
        assertTrue(actions.stream().noneMatch(action -> action.type == SUMMON));
    }

    @Test
    void spiritThatDoesNotSeekIsNotOffered() {
        state.player().learnSpirit("warden");
        var warden = SpiritDefinition.create("warden", "Warden", "A guardian.", PROTECT);

        List<GameAction> actions = resolver.availableActions(state, rooms, List.of(warden));

        assertTrue(actions.stream().noneMatch(action -> action.type == SUMMON));
    }

    @Test
    void hiddenObjectStaysOutOfReachUntilSeekerIsSummoned() {
        state.player().learnSpirit("seeker");
        assertEquals(List.of("arch", "sigil"), objectIds(rooms.get(0).getObjects()));
        assertEquals(List.of("arch"), visibleIds(rooms.get(0)));

        ActionResult hidden = executor.execute(
                state, rooms, GameAction.examine("sigil", "ignored"), List.of(), spirits);

        assertFalse(hidden.succeeded);
        assertEquals("That action is not available.", hidden.message);
        assertEquals(List.of("arch"), visibleIds(rooms.get(0)));
    }

    @Test
    void summoningSeekerRevealsHiddenObjects() {
        state.player().learnSpirit("seeker");

        ActionResult result = summon();

        assertTrue(result.succeeded);
        assertEquals("Seeker uncovers Worn Sigil.", result.message);
        assertEquals(List.of(SPIRIT_SUMMONED, REVEALED_OBJECTS), result.changes);
        assertTrue(state.spiritSummoned("seeker"));
        assertEquals("entrance", state.currentRoomId());
        assertEquals(List.of("arch", "sigil"), visibleIds(rooms.get(0)));

        ActionResult examined = executor.execute(
                state, rooms, GameAction.examine("sigil", "ignored"), List.of(), spirits);
        assertEquals(SIGIL_DESCRIPTION, examined.message);
        assertTrue(resolver.availableActions(state, rooms, spirits).stream()
                .noneMatch(action -> action.type == SUMMON));
    }

    @Test
    void summoningAgainDoesNotChangeTheRoom() {
        state.player().learnSpirit("seeker");
        summon();

        ActionResult again = summon();

        assertFalse(again.succeeded);
        assertEquals("That action is not available.", again.message);
        assertTrue(again.changes.isEmpty());
        assertTrue(state.spiritSummoned("seeker"));
        assertEquals(List.of("arch", "sigil"), visibleIds(rooms.get(0)));
        assertEquals("entrance", state.currentRoomId());
    }

    @Test
    void summoningWithoutKnowledgeDoesNotReveal() {
        ActionResult result = summon();

        assertFalse(result.succeeded);
        assertEquals("That action is not available.", result.message);
        assertTrue(result.changes.isEmpty());
        assertFalse(state.spiritSummoned("seeker"));
        assertEquals(List.of("arch"), visibleIds(rooms.get(0)));
        assertEquals("entrance", state.currentRoomId());
    }

    @Test
    void summonWithNothingHiddenStillSpendsTheSpirit() {
        state.player().learnSpirit("seeker");
        executor.execute(state, rooms, GameAction.move("hall", "ignored"));

        ActionResult result = summon();

        assertTrue(result.succeeded);
        assertEquals("Seeker searches the room and finds nothing hidden.", result.message);
        assertEquals(List.of(SPIRIT_SUMMONED), result.changes);
        assertTrue(state.spiritSummoned("seeker"));
        assertEquals(List.of("arch"), visibleIds(rooms.get(0)));
        assertTrue(resolver.availableActions(state, rooms, spirits).stream()
                .noneMatch(action -> action.type == SUMMON));
    }

    @Test
    void revealedObjectsStayInTheRoomWhereTheyWereFound() {
        state.player().learnSpirit("seeker");
        summon();

        executor.execute(state, rooms, GameAction.move("hall", "ignored"));
        assertFalse(state.spiritSummoned("seeker"));
        assertEquals(List.of("arch", "sigil"), visibleIds(rooms.get(0)));
        assertTrue(resolver.availableActions(state, rooms, spirits).stream()
                .anyMatch(action -> action.type == SUMMON));

        executor.execute(state, rooms, GameAction.move("entrance", "ignored"));
        assertTrue(state.spiritSummoned("seeker"));
        assertEquals(List.of("arch", "sigil"), visibleIds(rooms.get(0)));
        assertTrue(resolver.availableActions(state, rooms, spirits).stream()
                .noneMatch(action -> action.type == SUMMON));
    }

    private ActionResult summon() {
        return executor.execute(state, rooms, GameAction.summon("seeker", "ignored"), List.of(), spirits);
    }

    private List<String> visibleIds(RoomDefinition room) {
        var ids = new ArrayList<String>();
        for (ObjectDefinition object : state.visibleObjects(room)) {
            ids.add(object.id());
        }
        return ids;
    }

    private static List<String> objectIds(List<ObjectDefinition> objects) {
        var ids = new ArrayList<String>();
        for (ObjectDefinition object : objects) {
            ids.add(object.id());
        }
        return ids;
    }

    private static SpiritDefinition seeker() {
        return SpiritDefinition.create("seeker", "Seeker", SEEKER_DESCRIPTION, SEEK);
    }

    private static FloorDefinition groundFloor() {
        var floor = FloorDefinition.create("ground", "Ground Floor");
        floor.setRoomIds(List.of("entrance", "hall", "alcove"));
        return floor;
    }

    private static List<RoomDefinition> groundRooms() {
        var entrance = room(
                "entrance",
                "Entrance",
                "A worn stone arch leads into the tower. Dust hangs in the still air.",
                List.of(
                        object("arch", "Stone Arch", "passage",
                                "The stones are worn smooth. Faint marks cover the inner curve."),
                        object("sigil", "Worn Sigil", "clue", SIGIL_DESCRIPTION)),
                exit("hall", "Go to the Quiet Hall"));
        entrance.setHiddenObjects(List.of("sigil"));
        RoomDefinition hall = room(
                "hall",
                "Quiet Hall",
                "A long hall is quiet. A cold brazier and a faded inscription wait in the dim light.",
                List.of(
                        object("brazier", "Cold Brazier", "fixture",
                                "The iron bowl is cold. Ash has settled into a hard grey crust."),
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
