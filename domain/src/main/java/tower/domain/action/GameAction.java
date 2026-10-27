package tower.domain.action;

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
        return new GameAction("look", ActionType.LOOK, null, "Look around");
    }

    public static GameAction move(String targetRoomId, String label) {
        return new GameAction("move:" + targetRoomId, ActionType.MOVE, targetRoomId, label);
    }
}
