package tower.domain.logic;

import tower.domain.action.GameAction;
import tower.domain.data.RoomDefinition;
import tower.domain.data.RoomExit;
import tower.domain.state.GameState;

import java.util.ArrayList;
import java.util.List;

public class ActionResolver {

    public List<GameAction> availableActions(GameState state, List<RoomDefinition> rooms) {
        if (state == null) {
            return List.of();
        }
        RoomDefinition room = Rooms.find(rooms, state.currentRoomId());
        List<GameAction> actions = new ArrayList<>();
        actions.add(GameAction.look());
        addMoveActions(actions, room, rooms);
        return List.copyOf(actions);
    }

    private static void addMoveActions(List<GameAction> actions, RoomDefinition room, List<RoomDefinition> rooms) {
        if (room == null || room.exits == null) {
            return;
        }
        for (RoomExit exit : room.exits) {
            if (exit == null || exit.targetRoomId == null || exit.targetRoomId.isBlank()) {
                continue;
            }
            actions.add(GameAction.move(exit.targetRoomId, moveLabel(exit, Rooms.find(rooms, exit.targetRoomId))));
        }
    }

    private static String moveLabel(RoomExit exit, RoomDefinition destination) {
        if (exit.label != null && !exit.label.isBlank()) {
            return exit.label;
        }
        if (destination != null && destination.displayName != null && !destination.displayName.isBlank()) {
            return "Go to " + destination.displayName;
        }
        return "Go to " + exit.targetRoomId;
    }
}
