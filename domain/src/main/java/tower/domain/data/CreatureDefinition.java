package tower.domain.data;

import java.util.ArrayList;
import java.util.List;

public class CreatureDefinition {

    private String id;
    private String displayName;
    private String description;
    private List<String> neutralizedBy = new ArrayList<>();

    public static CreatureDefinition create(
            String id, String displayName, String description, List<String> neutralizedBy) {
        return new CreatureDefinition(id, displayName, description, neutralizedBy);
    }

    private CreatureDefinition(String id, String displayName, String description, List<String> neutralizedBy) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.neutralizedBy = copyIds(neutralizedBy);
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

    public List<String> neutralizedBy() {
        if (neutralizedBy == null) {
            neutralizedBy = new ArrayList<>();
        }
        return neutralizedBy;
    }

    public boolean neutralizedBy(String spellId) {
        return spellId != null && neutralizedBy().contains(spellId);
    }

    private static List<String> copyIds(List<String> ids) {
        var copy = new ArrayList<String>();
        if (ids == null) {
            return copy;
        }
        for (String id : ids) {
            if (id != null && !id.isBlank() && !copy.contains(id)) {
                copy.add(id);
            }
        }
        return copy;
    }
}
