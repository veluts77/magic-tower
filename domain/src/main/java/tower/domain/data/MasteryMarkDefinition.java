package tower.domain.data;

import tower.domain.action.ActionType;

public class MasteryMarkDefinition {

    private String id;
    private String displayName;
    private String description;
    private ActionType action;
    private String target;

    public static MasteryMarkDefinition create(
            String id, String displayName, String description, ActionType action, String target) {
        return new MasteryMarkDefinition(id, displayName, description, action, target);
    }

    private MasteryMarkDefinition(
            String id, String displayName, String description, ActionType action, String target) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.action = action;
        this.target = target;
    }

    public boolean matches(ActionType action, String target) {
        return this.action == action && target != null && target.equals(this.target);
    }

    public String target() {
        return target == null ? "" : target;
    }

    public ActionType action() {
        return action;
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
}
