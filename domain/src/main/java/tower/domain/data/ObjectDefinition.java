package tower.domain.data;

public class ObjectDefinition {

    private String id;
    private String displayName;
    private String type;
    private String description;

    public static ObjectDefinition create(String id, String displayName, String type, String description) {
        return new ObjectDefinition(id, displayName, type, description);
    }

    private ObjectDefinition(String id, String displayName, String type, String description) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.description = description;
    }

    public String description() {
        return description == null ? "" : description;
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
