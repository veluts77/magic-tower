package tower.domain.data;

public class RoomExit {

    private String targetRoomId;
    private String targetFloorId;
    private String label;

    public static RoomExit create(String targetRoomId, String label) {
        return new RoomExit(targetRoomId, null, label);
    }

    public static RoomExit ascend(String targetFloorId, String label) {
        return new RoomExit(null, targetFloorId, label);
    }

    private RoomExit(String targetRoomId, String targetFloorId, String label) {
        this.targetRoomId = targetRoomId;
        this.targetFloorId = targetFloorId;
        this.label = label;
    }

    public boolean ascends() {
        return targetFloorId != null && !targetFloorId.isBlank();
    }

    public String label() {
        return label == null ? "" : label;
    }

    public String targetFloorId() {
        return targetFloorId;
    }

    public String targetRoomId() {
        return targetRoomId;
    }
}
