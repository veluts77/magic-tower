package tower.domain.logic;

import tower.domain.action.ActionResult;
import tower.domain.action.GameAction;
import tower.domain.data.RoomDefinition;
import tower.domain.state.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static tower.domain.action.ActionResult.Change.CURRENT_ROOM;
import static tower.domain.action.ActionResult.Change.VISITED_ROOMS;
import static tower.domain.action.ActionType.MOVE;

public class ActionExecutor {

    private final ActionResolver resolver = new ActionResolver();

    public ActionResult execute(GameState state, List<RoomDefinition> rooms, GameAction action) {
        if (state == null || action == null || action.type == null) {
            return ActionResult.rejected("That action is not available.");
        }
        GameAction available = match(resolver.availableActions(state, rooms), action);
        if (available == null) {
            var message = action.type == MOVE
                    ? "That room is not connected."
                    : "That action is not available.";
            return ActionResult.rejected(message);
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
