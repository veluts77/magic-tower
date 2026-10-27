package tower.domain.action;

import java.util.List;

public class ActionResult {

    public final boolean succeeded;
    public final String message;
    public final List<Change> changes;

    private ActionResult(boolean succeeded, String message, List<Change> changes) {
        this.succeeded = succeeded;
        this.message = message;
        this.changes = changes;
    }

    public static ActionResult succeeded(String message, Change... changes) {
        return new ActionResult(true, message == null ? "" : message, List.of(changes));
    }

    public static ActionResult rejected(String message) {
        return new ActionResult(false, message == null ? "" : message, List.of());
    }

    public enum Change {
        CURRENT_ROOM,
        VISITED_ROOMS
    }
}
