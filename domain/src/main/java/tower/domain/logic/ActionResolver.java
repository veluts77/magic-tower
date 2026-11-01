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
        var actions = new ArrayList<GameAction>();
        actions.add(GameAction.look());
        addMoveActions(actions, room, rooms);
        return List.copyOf(actions);
    }

    private static void addMoveActions(List<GameAction> actions, RoomDefinition room, List<RoomDefinition> rooms) {
        for (RoomExit exit : room.getExits()) {
            String targetRoomId = exit.targetRoomId();
            if (targetRoomId == null || targetRoomId.isBlank()) {
                continue;
            }
            actions.add(GameAction.move(targetRoomId, moveLabel(exit, Rooms.find(rooms, targetRoomId))));
        }
    }

    private static String moveLabel(RoomExit exit, RoomDefinition destination) {
        if (!exit.label().isBlank()) {
            return exit.label();
        }
        if (!destination.displayName().isBlank()) {
            return "Go to " + destination.displayName();
        }
        return "Go to " + exit.targetRoomId();
    }
}
