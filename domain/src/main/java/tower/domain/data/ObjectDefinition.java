package tower.domain.data;

import java.util.ArrayList;
import java.util.List;

public class ObjectDefinition {

    public static final String ROLE_INTERACTIVE = "interactive";
    public static final String ROLE_DECORATIVE = "decorative";

    private String id;
    private String displayName;
    private String type;
    private String description;
    private String role = ROLE_INTERACTIVE;
    private String sprite;
    private float posX;
    private float posY;
    private float width = 0.1f;
    private float height = 0.1f;
    private int z;
    private String wall;
    private String transition;
    private String artifactId;
    private String creatureId;
    private String blockedByCreatureId;
    private String spellId;
    private String spiritId;
    private String itemId;
    private String grantsFlag;
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

    public String role() {
        return role == null || role.isBlank() ? ROLE_INTERACTIVE : role;
    }

    public void setRole(String role) {
        if (role == null || role.isBlank()) {
            this.role = ROLE_INTERACTIVE;
            return;
        }
        this.role = role;
    }

    public boolean isInteractive() {
        return !ROLE_DECORATIVE.equalsIgnoreCase(role());
    }

    public boolean isDecorative() {
        return ROLE_DECORATIVE.equalsIgnoreCase(role());
    }

    public String sprite() {
        if (sprite == null || sprite.isBlank()) {
            return id;
        }
        return sprite;
    }

    public void setSprite(String sprite) {
        this.sprite = blankToNull(sprite);
    }

    public float posX() {
        return posX;
    }

    public float posY() {
        return posY;
    }

    public void setPosition(float x, float y) {
        this.posX = x;
        this.posY = y;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public int z() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String wall() {
        return wall;
    }

    public void setWall(String wall) {
        this.wall = blankToNull(wall);
    }

    public String transition() {
        return transition;
    }

    public void setTransition(String transition) {
        this.transition = blankToNull(transition);
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

    public String grantsFlag() {
        return grantsFlag;
    }

    public void setGrantsFlag(String flagId) {
        this.grantsFlag = blankToNull(flagId);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
