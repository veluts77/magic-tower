package tower.domain.action;

import static tower.domain.action.ActionType.LOOK;
import static tower.domain.action.ActionType.MOVE;

public class GameAction {

    public final String id;
    public final ActionType type;
    public final String target;
    public final String label;

    public GameAction(String id, ActionType type, String target, String label) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.label = label;
    }

    public static GameAction look() {
        return new GameAction("look", LOOK, null, "Look around");
    }

    public static GameAction move(String targetRoomId, String label) {
        return new GameAction("move:" + targetRoomId, MOVE, targetRoomId, label);
    }
}
