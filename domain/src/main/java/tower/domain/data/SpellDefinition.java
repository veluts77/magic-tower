package tower.domain.data;

public class SpellDefinition {

    private String id;
    private String displayName;
    private String description;
    private SpellTarget target;

    public static SpellDefinition create(String id, String displayName, String description, SpellTarget target) {
        return new SpellDefinition(id, displayName, description, target);
    }

    private SpellDefinition(String id, String displayName, String description, SpellTarget target) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.target = target;
    }

    public String description() {
        return description == null ? "" : description;
    }

    public String displayName() {
        return displayName == null ? "" : displayName;
    }

    public String id() {
        return id == null ? "" : id;
    }

    public SpellTarget target() {
        return target;
    }
}
