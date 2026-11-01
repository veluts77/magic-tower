package tower.domain.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tower.domain.action.ActionResult;
import tower.domain.action.GameAction;
import tower.domain.data.FloorDefinition;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tower.domain.action.ActionResult.Change.CURRENT_ROOM;
import static tower.domain.action.ActionResult.Change.VISITED_ROOMS;
import static tower.domain.action.ActionType.LOOK;
import static tower.domain.action.ActionType.MOVE;

class RoomNavigationTest {

    private static final String ENTRANCE_DESCRIPTION =
            "A worn stone arch leads into the tower. Dust hangs in the still air.";

    private FloorDefinition floor;
    private List<RoomDefinition> rooms;
    private GameState state;
    private ActionResolver resolver;
    private ActionExecutor executor;

    @BeforeEach
    void setUp() {
        floor = groundFloor();
        rooms = groundRooms();
        state = GameState.start(floor);
        resolver = new ActionResolver();
        executor = new ActionExecutor();
    }

    @Test
    void startsOnTheFirstRoomAndMarksItVisited() {
        assertEquals("ground", state.currentFloorId());
        assertEquals("entrance", state.currentRoomId());
        assertEquals(Set.of("entrance"), state.visitedRoomIds());
    }

    @Test
    void startingRoomOffersLookAndConnectedMoves() {
        List<GameAction> actions = resolver.availableActions(state, rooms);

        assertEquals(2, actions.size());

        GameAction look = actions.get(0);
        assertEquals("look", look.id);
        assertEquals(LOOK, look.type);
        assertNull(look.target);
        assertEquals("Look around", look.label);

        GameAction move = actions.get(1);
        assertEquals("move:hall", move.id);
        assertEquals(MOVE, move.type);
        assertEquals("hall", move.target);
        assertEquals("Go to the Quiet Hall", move.label);

        assertTrue(actions.stream().noneMatch(action -> "alcove".equals(action.target)));
    }

    @Test
    void moveLabelFallsBackToTheDestinationName() {
        var exit = rooms.get(0).getExits().get(0);
        rooms.get(0).getExits().set(0, RoomExit.create(exit.targetRoomId(), " "));

        GameAction move = resolver.availableActions(state, rooms).get(1);

        assertEquals("Go to Quiet Hall", move.label);
    }

    @Test
    void moveToAConnectedRoomSucceeds() {
        ActionResult result = executor.execute(state, rooms, GameAction.move("hall", "Go to the Quiet Hall"));

        assertTrue(result.succeeded);
        assertEquals("You move to Quiet Hall.", result.message);
        assertEquals(List.of(CURRENT_ROOM, VISITED_ROOMS), result.changes);
    }

    @Test
    void successfulMoveChangesTheCurrentRoom() {
        executor.execute(state, rooms, GameAction.move("hall", "ignored"));

        assertEquals("hall", state.currentRoomId());
    }

    @Test
    void moveToAnUnconnectedRoomIsRejected() {
        ActionResult result = executor.execute(state, rooms, GameAction.move("alcove", "Go to the Side Alcove"));

        assertFalse(result.succeeded);
        assertEquals("That room is not connected.", result.message);
        assertTrue(result.changes.isEmpty());
        assertEquals("entrance", state.currentRoomId());
    }

    @Test
    void unavailableActionDoesNotChangeState() {
        var visitedBefore = new LinkedHashSet<String>(state.visitedRoomIds());
        String roomBefore = state.currentRoomId();

        executor.execute(state, rooms, GameAction.move("alcove", "Go to the Side Alcove"));

        assertEquals(roomBefore, state.currentRoomId());
        assertEquals(visitedBefore, state.visitedRoomIds());
    }

    @Test
    void revisitingARoomDoesNotDuplicateIt() {
        executor.execute(state, rooms, GameAction.move("hall", "Go to the Quiet Hall"));
        ActionResult back = executor.execute(state, rooms, GameAction.move("entrance", "Go back"));

        assertTrue(back.succeeded);
        assertEquals(List.of(CURRENT_ROOM), back.changes);
        assertEquals("entrance", state.currentRoomId());
        assertEquals(List.of("entrance", "hall"), new ArrayList<>(state.visitedRoomIds()));
    }

    @Test
    void lookReturnsTheRoomDescriptionWithoutChangingState() {
        var visitedBefore = new LinkedHashSet<String>(state.visitedRoomIds());

        ActionResult result = executor.execute(state, rooms, GameAction.look());

        assertTrue(result.succeeded);
        assertEquals(ENTRANCE_DESCRIPTION, result.message);
        assertTrue(result.changes.isEmpty());
        assertEquals("entrance", state.currentRoomId());
        assertEquals(visitedBefore, state.visitedRoomIds());
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
                ENTRANCE_DESCRIPTION,
                exit("hall", "Go to the Quiet Hall"));
        RoomDefinition hall = room(
                "hall",
                "Quiet Hall",
                "A long hall is quiet. A cold brazier and a faded inscription wait in the dim light.",
                exit("entrance", "Go to the Entrance"),
                exit("alcove", "Go to the Side Alcove"));
        RoomDefinition alcove = room(
                "alcove",
                "Side Alcove",
                "A narrow alcove holds an empty pedestal. The passage back is the only way out.",
                exit("hall", "Go to the Quiet Hall"));
        return List.of(entrance, hall, alcove);
    }

    private static RoomDefinition room(String id, String displayName, String description, RoomExit... exits) {
        var room = RoomDefinition.create(id, displayName, description);
        room.setExits(new ArrayList<>(List.of(exits)));
        return room;
    }

    private static RoomExit exit(String targetRoomId, String label) {
        return RoomExit.create(targetRoomId, label);
    }
}
