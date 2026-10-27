package tower.domain.state;

import tower.domain.data.FloorDefinition;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class GameState {

    private final String floorId;
    private String currentRoomId;
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
        if (floor.id == null || floor.id.isBlank()) {
            throw new IllegalArgumentException("Floor id is missing");
        }
        if (floor.roomIds == null || floor.roomIds.isEmpty()) {
            throw new IllegalArgumentException("Floor has no rooms");
        }
        String startingRoomId = floor.roomIds.get(0);
        if (startingRoomId == null || startingRoomId.isBlank()) {
            throw new IllegalArgumentException("Starting room is missing");
        }
        return new GameState(floor.id, startingRoomId);
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

    public void enterRoom(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room id is missing");
        }
        currentRoomId = roomId;
        visitedRoomIds.add(roomId);
    }
}
