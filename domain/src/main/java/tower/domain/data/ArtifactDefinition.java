package tower.domain.data;

public class ArtifactDefinition {

    private String id;
    private String displayName;
    private String description;
    private ArtifactEffect effect;

    public static ArtifactDefinition create(
            String id, String displayName, String description, ArtifactEffect effect) {
        return new ArtifactDefinition(id, displayName, description, effect);
    }

    private ArtifactDefinition(String id, String displayName, String description, ArtifactEffect effect) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.effect = effect;
    }

    public String description() {
        return description == null ? "" : description;
    }

    public String displayName() {
        return displayName == null ? "" : displayName;
    }

    public ArtifactEffect effect() {
        return effect;
    }

    public String id() {
        return id == null ? "" : id;
    }
}
