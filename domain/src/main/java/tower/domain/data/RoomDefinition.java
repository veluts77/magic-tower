package tower.domain.data;

import java.util.ArrayList;
import java.util.List;

public class RoomDefinition {

    private String id;
    private String displayName;
    private String description;
    private List<ObjectDefinition> objects = new ArrayList<>();
    private List<String> hiddenObjects = new ArrayList<>();
    private List<RoomExit> exits = new ArrayList<>();
    private RoomHazard hazard;

    public static RoomDefinition create(String id, String displayName, String description) {
        return new RoomDefinition(id, displayName, description);
    }

    public static RoomDefinition createNonExistingRoom() {
        return new RoomDefinition("", "Non existing room", "Non existing room");
    }

    private RoomDefinition(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public List<ObjectDefinition> getObjects() {
        return objects;
    }

    public void setObjects(List<ObjectDefinition> objects) {
        this.objects = objects;
    }

    public List<String> hiddenObjects() {
        if (hiddenObjects == null) {
            hiddenObjects = new ArrayList<>();
        }
        return hiddenObjects;
    }

    public void setHiddenObjects(List<String> hiddenObjects) {
        this.hiddenObjects = new ArrayList<>();
        if (hiddenObjects == null) {
            return;
        }
        for (String objectId : hiddenObjects) {
            if (objectId != null && !objectId.isBlank()) {
                this.hiddenObjects.add(objectId);
            }
        }
    }

    public boolean hides(String objectId) {
        return objectId != null && hiddenObjects().contains(objectId);
    }

    public RoomHazard hazard() {
        return hazard;
    }

    public void setHazard(RoomHazard hazard) {
        this.hazard = hazard;
    }

    public List<RoomExit> getExits() {
        return exits;
    }

    public void setExits(List<RoomExit> exits) {
        this.exits = exits;
    }

    public String displayName() {
        return displayName == null ? "" : displayName;
    }

    public String description() {
        return description == null ? "" : description;
    }

    public boolean sameId(String id) {
        return this.id.equals(id);
    }

    public String id() {
        return id;
    }
}
