package tower.domain.logic;

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
}
