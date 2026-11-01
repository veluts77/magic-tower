package tower.domain.data;

import java.util.ArrayList;
import java.util.List;

public class FloorDefinition {

    private String id;
    private String displayName;
    private List<String> roomIds = new ArrayList<>();

    public static FloorDefinition create(String id, String displayName) {
        return new FloorDefinition(id, displayName);
    }

    private FloorDefinition(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public List<String> getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(List<String> roomIds) {
        this.roomIds = roomIds;
    }

    public String displayName() {
        return displayName == null ? "" : displayName;
    }

    public String id() {
        return id;
    }

    public int roomCount() {
        return roomIds == null ? 0 : roomIds.size();
    }
}
