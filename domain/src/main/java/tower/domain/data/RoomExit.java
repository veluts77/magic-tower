package tower.domain.data;

public class RoomExit {

    private String targetRoomId;
    private String targetFloorId;
    private String label;
    private String requiresTaken;
    private String requiresFlag;

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

    public String requiresTaken() {
        return requiresTaken;
    }

    public void setRequiresTaken(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            this.requiresTaken = null;
            return;
        }
        this.requiresTaken = objectId;
    }

    public String requiresFlag() {
        return requiresFlag;
    }

    public void setRequiresFlag(String flagId) {
        if (flagId == null || flagId.isBlank()) {
            this.requiresFlag = null;
            return;
        }
        this.requiresFlag = flagId;
    }
}
