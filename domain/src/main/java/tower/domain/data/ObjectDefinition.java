package tower.domain.data;

import java.util.ArrayList;
import java.util.List;

public class ObjectDefinition {

    private String id;
    private String displayName;
    private String type;
    private String description;
    private List<String> supportedSpells = new ArrayList<>();

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

    public List<String> supportedSpells() {
        if (supportedSpells == null) {
            supportedSpells = new ArrayList<>();
        }
        return supportedSpells;
    }

    public void setSupportedSpells(List<String> supportedSpells) {
        this.supportedSpells = new ArrayList<>();
        if (supportedSpells == null) {
            return;
        }
        for (String spellId : supportedSpells) {
            if (spellId != null && !spellId.isBlank()) {
                this.supportedSpells.add(spellId);
            }
        }
    }

    public boolean supportsSpell(String spellId) {
        return spellId != null && supportedSpells().contains(spellId);
    }
}
