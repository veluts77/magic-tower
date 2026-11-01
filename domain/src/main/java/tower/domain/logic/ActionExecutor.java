package tower.domain.logic;

import tower.domain.action.ActionResult;
import tower.domain.action.GameAction;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.state.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static tower.domain.action.ActionResult.Change.CURRENT_ROOM;
import static tower.domain.action.ActionResult.Change.VISITED_ROOMS;
import static tower.domain.action.ActionType.EXAMINE;
import static tower.domain.action.ActionType.MOVE;

public class ActionExecutor {

    private final ActionResolver resolver = new ActionResolver();

    public ActionResult execute(GameState state, List<RoomDefinition> rooms, GameAction action) {
        if (state == null || action == null || action.type == null) {
            return ActionResult.rejected("That action is not available.");
        }
        return switch (action.type) {
            case EXAMINE -> examine(state, rooms, action);
            case LOOK, MOVE -> roomAction(state, rooms, action);
        };
    }

    private ActionResult roomAction(GameState state, List<RoomDefinition> rooms, GameAction action) {
        GameAction available = match(resolver.availableActions(state, rooms), action);
        if (available == null) {
            var message = action.type == MOVE
                    ? "That room is not connected."
                    : "That action is not available.";
            return ActionResult.rejected(message);
        }
        if (available.type == MOVE) {
            return move(state, rooms, available);
        }
        return look(state, rooms);
    }

    private static GameAction match(List<GameAction> available, GameAction requested) {
        for (GameAction action : available) {
            if (action.type == requested.type && Objects.equals(action.target, requested.target)) {
                return action;
            }
        }
        return null;
    }

    private ActionResult examine(GameState state, List<RoomDefinition> rooms, GameAction action) {
        GameAction available = match(resolver.availableActions(state, rooms, action.target), action);
        if (available == null) {
            return ActionResult.rejected("That action is not available.");
        }
        ObjectDefinition object = Rooms.findObject(rooms, state.currentRoomId(), available.target);
        String message = object == null ? "" : object.description();
        return ActionResult.succeeded(message);
    }

    private static ActionResult look(GameState state, List<RoomDefinition> rooms) {
        RoomDefinition room = Rooms.find(rooms, state.currentRoomId());
        String message = room == null ? "" : room.description();
        return ActionResult.succeeded(message);
    }

    private static ActionResult move(GameState state, List<RoomDefinition> rooms, GameAction action) {
        String targetRoomId = action.target;
        boolean roomChanged = !targetRoomId.equals(state.currentRoomId());
        boolean newlyVisited = !state.visitedRoomIds().contains(targetRoomId);
        state.enterRoom(targetRoomId);

        RoomDefinition destination = Rooms.find(rooms, targetRoomId);
        String name = destination == null || destination.displayName().isBlank()
                ? targetRoomId
                : destination.displayName();
        var changes = new ArrayList<ActionResult.Change>();
        if (roomChanged) {
            changes.add(CURRENT_ROOM);
        }
        if (newlyVisited) {
            changes.add(VISITED_ROOMS);
        }
        var message = "You move to " + name + ".";
        return ActionResult.succeeded(message, changes.toArray(ActionResult.Change[]::new));
    }
}
