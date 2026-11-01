package tower.domain.logic;

import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;

import java.util.List;

final class Rooms {

    private Rooms() {
    }

    static RoomDefinition find(List<RoomDefinition> rooms, String roomId) {
        for (RoomDefinition room : rooms) {
            if (room.sameId(roomId)) {
                return room;
            }
        }
        return RoomDefinition.createNonExistingRoom();
    }

    static ObjectDefinition findObject(List<RoomDefinition> rooms, String roomId, String objectId) {
        if (rooms == null || objectId == null || objectId.isBlank()) {
            return null;
        }
        List<ObjectDefinition> objects = find(rooms, roomId).getObjects();
        if (objects == null) {
            return null;
        }
        for (ObjectDefinition object : objects) {
            if (object != null && objectId.equals(object.id())) {
                return object;
            }
        }
        return null;
    }
}
