package tower.domain.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tower.domain.action.ActionResult;
import tower.domain.action.GameAction;
import tower.domain.data.FloorDefinition;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.data.RoomExit;
import tower.domain.state.GameState;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tower.domain.action.ActionType.EXAMINE;
import static tower.domain.action.ActionType.LOOK;
import static tower.domain.action.ActionType.MOVE;

class ObjectInteractionTest {

    private static final String ARCH_DESCRIPTION =
            "The stones are worn smooth. Faint marks cover the inner curve.";
    private static final String BRAZIER_DESCRIPTION =
            "The iron bowl is cold. Ash has settled into a hard grey crust.";

    private List<RoomDefinition> rooms;
    private GameState state;
    private ActionResolver resolver;
    private ActionExecutor executor;

    @BeforeEach
    void setUp() {
        rooms = groundRooms();
        state = GameState.start(groundFloor());
        resolver = new ActionResolver();
        executor = new ActionExecutor();
    }

    @Test
    void objectInTheCurrentRoomOffersExamine() {
        List<GameAction> actions = resolver.availableActions(state, rooms, "arch");

        assertEquals(1, actions.size());
        GameAction examine = actions.get(0);
        assertEquals("examine:arch", examine.id);
        assertEquals(EXAMINE, examine.type);
        assertEquals("arch", examine.target);
        assertEquals("Examine Stone Arch", examine.label);
    }

    @Test
    void examineLabelFallsBackToTheObjectId() {
        rooms.get(0).getObjects().set(0, ObjectDefinition.create("arch", " ", "passage", ARCH_DESCRIPTION));

        GameAction examine = resolver.availableActions(state, rooms, "arch").get(0);

        assertEquals("Examine arch", examine.label);
    }

    @Test
    void objectOutsideTheCurrentRoomOffersNoActions() {
        assertTrue(resolver.availableActions(state, rooms, "brazier").isEmpty());
        assertTrue(resolver.availableActions(state, rooms, "missing").isEmpty());
        assertTrue(resolver.availableActions(state, rooms, " ").isEmpty());
        assertTrue(resolver.availableActions(state, rooms, null).isEmpty());
        assertTrue(resolver.availableActions(null, rooms, "arch").isEmpty());
    }

    @Test
    void roomActionsStaySeparateFromObjectActions() {
        List<GameAction> actions = resolver.availableActions(state, rooms);

        assertEquals(2, actions.size());
        assertEquals(LOOK, actions.get(0).type);
        assertEquals(MOVE, actions.get(1).type);
    }

    @Test
    void examineReturnsTheObjectDescriptionWithoutChangingState() {
        state.selectObject("arch");
        var visitedBefore = new LinkedHashSet<String>(state.visitedRoomIds());

        ActionResult result = executor.execute(state, rooms, GameAction.examine("arch", "ignored"));

        assertTrue(result.succeeded);
        assertEquals(ARCH_DESCRIPTION, result.message);
        assertTrue(result.changes.isEmpty());
        assertEquals("entrance", state.currentRoomId());
        assertEquals(visitedBefore, state.visitedRoomIds());
        assertEquals("arch", state.selectedObjectId());
    }

    @Test
    void examineOfAnUnavailableObjectIsRejected() {
        state.selectObject("arch");
        var visitedBefore = new LinkedHashSet<String>(state.visitedRoomIds());

        ActionResult result = executor.execute(state, rooms, GameAction.examine("brazier", "Examine Cold Brazier"));

        assertFalse(result.succeeded);
        assertEquals("That action is not available.", result.message);
        assertTrue(result.changes.isEmpty());
        assertEquals("entrance", state.currentRoomId());
        assertEquals(visitedBefore, state.visitedRoomIds());
        assertEquals("arch", state.selectedObjectId());
    }

    @Test
    void selectionStartsEmptyAndCanBeSetAndCleared() {
        assertNull(state.selectedObjectId());

        state.selectObject("arch");
        assertEquals("arch", state.selectedObjectId());

        state.clearSelection();
        assertNull(state.selectedObjectId());
    }

    @Test
    void blankObjectIdCannotBeSelected() {
        assertThrows(IllegalArgumentException.class, () -> state.selectObject(" "));
        assertThrows(IllegalArgumentException.class, () -> state.selectObject(null));
        assertNull(state.selectedObjectId());
    }

    @Test
    void examineUsesTheObjectInTheCurrentRoom() {
        executor.execute(state, rooms, GameAction.move("hall", "Go to the Quiet Hall"));

        ActionResult result = executor.execute(state, rooms, GameAction.examine("brazier", "ignored"));

        assertTrue(result.succeeded);
        assertEquals(BRAZIER_DESCRIPTION, result.message);
        assertEquals("hall", state.currentRoomId());
        assertEquals(Set.of("entrance", "hall"), state.visitedRoomIds());
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
                List.of(object("arch", "Stone Arch", "passage", ARCH_DESCRIPTION)),
                exit("hall", "Go to the Quiet Hall"));
        RoomDefinition hall = room(
                "hall",
                "Quiet Hall",
                "A long hall is quiet. A cold brazier and a faded inscription wait in the dim light.",
                List.of(
                        object("brazier", "Cold Brazier", "fixture", BRAZIER_DESCRIPTION),
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
