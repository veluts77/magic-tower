package tower.domain.data;

public class ObjectDefinition {

    private String id;
    private String displayName;
    private String type;

    public static ObjectDefinition create(String id, String displayName, String type) {
        return new ObjectDefinition(id, displayName, type);
    }

    private ObjectDefinition(String id, String displayName, String type) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
    }

    public String displayName() {
        return displayName == null ? "" : displayName;
    }

    public String id() {
        return id;
    }

    public String type() {
        return type;
    }
}
