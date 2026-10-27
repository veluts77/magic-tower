package tower.domain.logic;

import tower.domain.data.RoomDefinition;

import java.util.List;

final class Rooms {

    private Rooms() {
    }

    static RoomDefinition find(List<RoomDefinition> rooms, String roomId) {
        if (rooms == null || roomId == null) {
            return null;
        }
        for (RoomDefinition room : rooms) {
            if (room != null && roomId.equals(room.id)) {
                return room;
            }
        }
        return null;
    }
}
