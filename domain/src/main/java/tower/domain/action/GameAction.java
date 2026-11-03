package tower.domain.action;

import static tower.domain.action.ActionType.ASCEND;
import static tower.domain.action.ActionType.CAST;
import static tower.domain.action.ActionType.EXAMINE;
import static tower.domain.action.ActionType.LOOK;
import static tower.domain.action.ActionType.MOVE;
import static tower.domain.action.ActionType.SUMMON;
import static tower.domain.action.ActionType.TAKE;

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

    public static GameAction summon(String spiritId, String label) {
        return new GameAction("summon:" + spiritId, SUMMON, spiritId, label);
    }

    public static GameAction take(String objectId, String label) {
        return new GameAction("take:" + objectId, TAKE, objectId, label);
    }

    public static GameAction ascend(String floorId, String label) {
        return new GameAction("ascend:" + floorId, ASCEND, floorId, label);
    }
}
