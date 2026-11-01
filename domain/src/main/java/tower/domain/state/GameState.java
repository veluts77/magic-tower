package tower.domain.state;

import tower.domain.data.FloorDefinition;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class GameState {

    private final String floorId;
    private String currentRoomId;
    private String selectedObjectId;
    private final PlayerState player = new PlayerState();
    private final Map<String, RoomState> roomStates = new LinkedHashMap<>();
    private final Set<String> visitedRoomIds = new LinkedHashSet<>();

    private GameState(String floorId, String startingRoomId) {
        this.floorId = floorId;
        this.currentRoomId = startingRoomId;
        this.visitedRoomIds.add(startingRoomId);
    }

    public static GameState start(FloorDefinition floor) {
        if (floor == null) {
            throw new IllegalArgumentException("Floor is missing");
        }
        if (floor.id() == null || floor.id().isBlank()) {
            throw new IllegalArgumentException("Floor id is missing");
        }
        if (floor.getRoomIds() == null || floor.getRoomIds().isEmpty()) {
            throw new IllegalArgumentException("Floor has no rooms");
        }
        String startingRoomId = floor.getRoomIds().get(0);
        if (startingRoomId == null || startingRoomId.isBlank()) {
            throw new IllegalArgumentException("Starting room is missing");
        }
        return new GameState(floor.id(), startingRoomId);
    }

    public String currentFloorId() {
        return floorId;
    }

    public String currentRoomId() {
        return currentRoomId;
    }

    public Set<String> visitedRoomIds() {
        return Collections.unmodifiableSet(visitedRoomIds);
    }

    public String selectedObjectId() {
        return selectedObjectId;
    }

    public void selectObject(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("Object id is missing");
        }
        selectedObjectId = objectId;
    }

    public void clearSelection() {
        selectedObjectId = null;
    }

    public PlayerState player() {
        return player;
    }

    public String objectCondition(String objectId) {
        return roomState(currentRoomId).objectCondition(objectId);
    }

    public void setObjectCondition(String objectId, String condition) {
        roomState(currentRoomId).setObjectCondition(objectId, condition);
    }

    private RoomState roomState(String roomId) {
        String key = roomId == null ? "" : roomId;
        return roomStates.computeIfAbsent(key, ignored -> new RoomState());
    }

    public void enterRoom(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room id is missing");
        }
        currentRoomId = roomId;
        visitedRoomIds.add(roomId);
    }
}
