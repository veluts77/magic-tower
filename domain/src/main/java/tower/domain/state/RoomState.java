package tower.domain.state;

import java.util.LinkedHashMap;
import java.util.Map;

public class RoomState {

    public static final String LIT = "lit";

    private final Map<String, String> objectConditions = new LinkedHashMap<>();

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
}
