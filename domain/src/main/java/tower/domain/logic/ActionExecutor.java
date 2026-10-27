package tower.domain.logic;

import tower.domain.action.ActionResult;
import tower.domain.action.ActionType;
import tower.domain.action.GameAction;
import tower.domain.data.RoomDefinition;
import tower.domain.state.GameState;

import java.util.List;
import java.util.Objects;

public class ActionExecutor {

    private final ActionResolver resolver = new ActionResolver();

    public ActionResult execute(GameState state, List<RoomDefinition> rooms, GameAction action) {
        if (state == null || action == null || action.type == null) {
            return ActionResult.rejected("That action is not available.");
        }
        GameAction available = match(resolver.availableActions(state, rooms), action);
        if (available == null) {
            if (action.type == ActionType.MOVE) {
                return ActionResult.rejected("That room is not connected.");
            }
            return ActionResult.rejected("That action is not available.");
        }
        return switch (available.type) {
            case LOOK -> look(state, rooms);
            case MOVE -> move(state, rooms, available);
        };
    }

    private static GameAction match(List<GameAction> available, GameAction requested) {
        for (GameAction action : available) {
            if (action.type == requested.type && Objects.equals(action.target, requested.target)) {
                return action;
            }
        }
        return null;
    }

    private static ActionResult look(GameState state, List<RoomDefinition> rooms) {
        RoomDefinition room = Rooms.find(rooms, state.currentRoomId());
        String message = room == null || room.description == null ? "" : room.description;
        return ActionResult.succeeded(message);
    }

    private static ActionResult move(GameState state, List<RoomDefinition> rooms, GameAction action) {
        String targetRoomId = action.target;
        boolean roomChanged = !targetRoomId.equals(state.currentRoomId());
        boolean newlyVisited = !state.visitedRoomIds().contains(targetRoomId);
        state.enterRoom(targetRoomId);

        RoomDefinition destination = Rooms.find(rooms, targetRoomId);
        String name = destination != null && destination.displayName != null && !destination.displayName.isBlank()
                ? destination.displayName
                : targetRoomId;
        String message = "You move to " + name + ".";
        if (roomChanged && newlyVisited) {
            return ActionResult.succeeded(message, ActionResult.Change.CURRENT_ROOM, ActionResult.Change.VISITED_ROOMS);
        }
        if (roomChanged) {
            return ActionResult.succeeded(message, ActionResult.Change.CURRENT_ROOM);
        }
        if (newlyVisited) {
            return ActionResult.succeeded(message, ActionResult.Change.VISITED_ROOMS);
        }
        return ActionResult.succeeded(message);
    }
}
