package tower.domain.state;

import tower.domain.data.FloorDefinition;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameState {

    private String floorId = "";
    private String currentRoomId;
    private String selectedObjectId;
    private final PlayerState player = new PlayerState();
    private FloorState floorState = new FloorState();
    private final Map<String, RoomState> roomStates = new LinkedHashMap<>();
    private final Set<String> visitedRoomIds = new LinkedHashSet<>();

    private GameState() {
    }

    public static GameState start(FloorDefinition floor) {
        var state = new GameState();
        state.enterFloor(floor);
        return state;
    }

    public void enterFloor(FloorDefinition floor) {
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
        floorId = floor.id();
        currentRoomId = startingRoomId;
        selectedObjectId = null;
        floorState = new FloorState();
        roomStates.clear();
        visitedRoomIds.clear();
        visitedRoomIds.add(startingRoomId);
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

    public FloorState floorState() {
        return floorState;
    }

    public String objectCondition(String objectId) {
        return roomState(currentRoomId).objectCondition(objectId);
    }

    public void setObjectCondition(String objectId, String condition) {
        roomState(currentRoomId).setObjectCondition(objectId, condition);
    }

    public boolean spiritSummoned(String spiritId) {
        return roomState(currentRoomId).spiritSummoned(spiritId);
    }

    public void summonSpirit(String spiritId) {
        roomState(currentRoomId).markSpiritSummoned(spiritId);
    }

    public void revealObject(String objectId) {
        roomState(currentRoomId).revealObject(objectId);
    }

    public boolean isObjectVisible(RoomDefinition room, String objectId) {
        if (room == null || objectId == null || objectId.isBlank()) {
            return false;
        }
        if (roomState(room.id()).objectTaken(objectId)) {
            return false;
        }
        if (!room.hides(objectId)) {
            return true;
        }
        return roomState(room.id()).objectRevealed(objectId);
    }

    public boolean objectTaken(String objectId) {
        return roomState(currentRoomId).objectTaken(objectId);
    }

    public void takeObject(String objectId) {
        roomState(currentRoomId).markObjectTaken(objectId);
    }

    public boolean creatureNeutralized(String creatureId) {
        return roomState(currentRoomId).creatureNeutralized(creatureId);
    }

    public void neutralizeCreature(String creatureId) {
        roomState(currentRoomId).neutralizeCreature(creatureId);
    }

    public List<ObjectDefinition> visibleObjects(RoomDefinition room) {
        if (room == null || room.getObjects() == null) {
            return List.of();
        }
        var visible = new ArrayList<ObjectDefinition>();
        for (ObjectDefinition object : room.getObjects()) {
            if (object != null && isObjectVisible(room, object.id())) {
                visible.add(object);
            }
        }
        return List.copyOf(visible);
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
