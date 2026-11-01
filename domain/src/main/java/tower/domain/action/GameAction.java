package tower.domain.action;

import static tower.domain.action.ActionType.CAST;
import static tower.domain.action.ActionType.EXAMINE;
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

    public static GameAction examine(String objectId, String label) {
        return new GameAction("examine:" + objectId, EXAMINE, objectId, label);
    }

    public static GameAction cast(String spellId, String label) {
        return new GameAction("cast:" + spellId, CAST, spellId, label);
    }
}
