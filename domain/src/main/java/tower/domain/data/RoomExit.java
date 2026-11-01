package tower.domain.data;

public class RoomExit {

    private String targetRoomId;
    private String label;

    public static RoomExit create(String targetRoomId, String label) {
        return new RoomExit(targetRoomId, label);
    }

    private RoomExit(String targetRoomId, String label) {
        this.targetRoomId = targetRoomId;
        this.label = label;
    }

    public String label() {
        return label == null ? "" : label;
    }

    public String targetRoomId() {
        return targetRoomId;
    }
}
