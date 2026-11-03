package tower.domain.data;

import java.util.ArrayList;
import java.util.List;

public class ObjectDefinition {

    private String id;
    private String displayName;
    private String type;
    private String description;
    private String artifactId;
    private String creatureId;
    private String blockedByCreatureId;
    private String spellId;
    private String spiritId;
    private String itemId;
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

    public String artifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        if (artifactId == null || artifactId.isBlank()) {
            this.artifactId = null;
            return;
        }
        this.artifactId = artifactId;
    }

    public boolean isArtifact() {
        return artifactId != null;
    }

    public String creatureId() {
        return creatureId;
    }

    public void setCreatureId(String creatureId) {
        if (creatureId == null || creatureId.isBlank()) {
            this.creatureId = null;
            return;
        }
        this.creatureId = creatureId;
    }

    public boolean isCreature() {
        return creatureId != null;
    }

    public String blockedByCreature() {
        return blockedByCreatureId;
    }

    public void setBlockedByCreature(String creatureId) {
        if (creatureId == null || creatureId.isBlank()) {
            this.blockedByCreatureId = null;
            return;
        }
        this.blockedByCreatureId = creatureId;
    }

    public String spellId() {
        return spellId;
    }

    public void setSpellId(String spellId) {
        this.spellId = blankToNull(spellId);
    }

    public boolean teachesSpell() {
        return spellId != null;
    }

    public String spiritId() {
        return spiritId;
    }

    public void setSpiritId(String spiritId) {
        this.spiritId = blankToNull(spiritId);
    }

    public String itemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = blankToNull(itemId);
    }

    public boolean isItem() {
        return itemId != null;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
