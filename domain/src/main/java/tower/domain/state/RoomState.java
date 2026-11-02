package tower.domain.state;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RoomState {

    public static final String LIT = "lit";

    private final Map<String, String> objectConditions = new LinkedHashMap<>();
    private final Set<String> summonedSpiritIds = new LinkedHashSet<>();
    private final Set<String> revealedObjectIds = new LinkedHashSet<>();
    private final Set<String> takenObjectIds = new LinkedHashSet<>();
    private final Set<String> neutralizedCreatureIds = new LinkedHashSet<>();

    public String objectCondition(String objectId) {
        if (objectId == null) {
            return "";
        }
        String condition = objectConditions.get(objectId);
        return condition == null ? "" : condition;
    }

    public void setObjectCondition(String objectId, String condition) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("Object id is missing");
        }
        if (condition == null || condition.isBlank()) {
            throw new IllegalArgumentException("Object condition is missing");
        }
        objectConditions.put(objectId, condition);
    }

    public boolean spiritSummoned(String spiritId) {
        return spiritId != null && summonedSpiritIds.contains(spiritId);
    }

    public void markSpiritSummoned(String spiritId) {
        if (spiritId == null || spiritId.isBlank()) {
            throw new IllegalArgumentException("Spirit id is missing");
        }
        summonedSpiritIds.add(spiritId);
    }

    public boolean objectRevealed(String objectId) {
        return objectId != null && revealedObjectIds.contains(objectId);
    }

    public void revealObject(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("Object id is missing");
        }
        revealedObjectIds.add(objectId);
    }

    public boolean objectTaken(String objectId) {
        return objectId != null && takenObjectIds.contains(objectId);
    }

    public void markObjectTaken(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("Object id is missing");
        }
        takenObjectIds.add(objectId);
    }

    public boolean creatureNeutralized(String creatureId) {
        return creatureId != null && neutralizedCreatureIds.contains(creatureId);
    }

    public void neutralizeCreature(String creatureId) {
        if (creatureId == null || creatureId.isBlank()) {
            throw new IllegalArgumentException("Creature id is missing");
        }
        neutralizedCreatureIds.add(creatureId);
    }
}
