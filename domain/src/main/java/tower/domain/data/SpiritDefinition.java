package tower.domain.data;

public class SpiritDefinition {

    private String id;
    private String displayName;
    private String description;
    private SpiritFunction function;

    public static SpiritDefinition create(
            String id, String displayName, String description, SpiritFunction function) {
        return new SpiritDefinition(id, displayName, description, function);
    }

    private SpiritDefinition(String id, String displayName, String description, SpiritFunction function) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.function = function;
    }

    public String description() {
        return description == null ? "" : description;
    }

    public String displayName() {
        return displayName == null ? "" : displayName;
    }

    public SpiritFunction function() {
        return function;
    }

    public String id() {
        return id == null ? "" : id;
    }
}
