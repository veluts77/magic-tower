package tower.domain.logic;

import tower.domain.action.ActionResult;
import tower.domain.action.GameAction;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.data.SpellDefinition;
import tower.domain.state.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static tower.domain.action.ActionResult.Change.CURRENT_ROOM;
import static tower.domain.action.ActionResult.Change.OBJECT_CONDITION;
import static tower.domain.action.ActionResult.Change.VISITED_ROOMS;
import static tower.domain.action.ActionType.MOVE;
import static tower.domain.state.RoomState.LIT;

public class ActionExecutor {

    private static final String SPARK = "spark";

    private final ActionResolver resolver = new ActionResolver();

    public ActionResult execute(GameState state, List<RoomDefinition> rooms, GameAction action) {
        return execute(state, rooms, action, List.of());
    }

    public ActionResult execute(
            GameState state, List<RoomDefinition> rooms, GameAction action, List<SpellDefinition> spells) {
        if (state == null || action == null || action.type == null) {
            return ActionResult.rejected("That action is not available.");
        }
        List<SpellDefinition> knownSpells = spells == null ? List.of() : spells;
        return switch (action.type) {
            case EXAMINE -> examine(state, rooms, action);
            case CAST -> cast(state, rooms, action, knownSpells);
            case LOOK, MOVE -> roomAction(state, rooms, action);
        };
    }

    private ActionResult roomAction(GameState state, List<RoomDefinition> rooms, GameAction action) {
        GameAction available = match(resolver.availableActions(state, rooms), action);
        if (available == null) {
            var message = action.type == MOVE
                    ? "That room is not connected."
                    : "That action is not available.";
            return ActionResult.rejected(message);
        }
        if (available.type == MOVE) {
            return move(state, rooms, available);
        }
        return look(state, rooms);
    }

    private static GameAction match(List<GameAction> available, GameAction requested) {
        for (GameAction action : available) {
            if (action.type == requested.type && Objects.equals(action.target, requested.target)) {
                return action;
            }
        }
        return null;
    }

    private ActionResult examine(GameState state, List<RoomDefinition> rooms, GameAction action) {
        GameAction available = match(resolver.availableActions(state, rooms, action.target), action);
        if (available == null) {
            return ActionResult.rejected("That action is not available.");
        }
        ObjectDefinition object = Rooms.findObject(rooms, state.currentRoomId(), available.target);
        String message = object == null ? "" : object.description();
        if (object != null && LIT.equals(state.objectCondition(object.id()))) {
            message = message + " A small flame burns there now.";
        }
        return ActionResult.succeeded(message);
    }

    private ActionResult cast(GameState state, List<RoomDefinition> rooms, GameAction action, List<SpellDefinition> spells) {
        String objectId = state.selectedObjectId();
        GameAction available = match(resolver.availableActions(state, rooms, objectId, spells), action);
        if (available == null) {
            return ActionResult.rejected("That action is not available.");
        }
        ObjectDefinition object = Rooms.findObject(rooms, state.currentRoomId(), objectId);
        SpellDefinition spell = findSpell(spells, available.target);
        if (object == null || object.id() == null || object.id().isBlank() || spell == null) {
            return ActionResult.rejected("That action is not available.");
        }
        if (!SPARK.equals(spell.id())) {
            return ActionResult.rejected("That spell does nothing here.");
        }
        return light(state, object);
    }

    private static ActionResult light(GameState state, ObjectDefinition object) {
        boolean alreadyLit = LIT.equals(state.objectCondition(object.id()));
        state.setObjectCondition(object.id(), LIT);
        String name = object.displayName().isBlank() ? object.id() : object.displayName();
        String message = alreadyLit
                ? name + " is already burning."
                : "A spark catches. " + name + " burns.";
        var changes = new ArrayList<ActionResult.Change>();
        if (!alreadyLit) {
            changes.add(OBJECT_CONDITION);
        }
        return ActionResult.succeeded(message, changes.toArray(ActionResult.Change[]::new));
    }

    private static SpellDefinition findSpell(List<SpellDefinition> spells, String spellId) {
        for (SpellDefinition spell : spells) {
            if (spell != null && spell.id().equals(spellId)) {
                return spell;
            }
        }
        return null;
    }

    private static ActionResult look(GameState state, List<RoomDefinition> rooms) {
        RoomDefinition room = Rooms.find(rooms, state.currentRoomId());
        String message = room == null ? "" : room.description();
        return ActionResult.succeeded(message);
    }

    private static ActionResult move(GameState state, List<RoomDefinition> rooms, GameAction action) {
        String targetRoomId = action.target;
        boolean roomChanged = !targetRoomId.equals(state.currentRoomId());
        boolean newlyVisited = !state.visitedRoomIds().contains(targetRoomId);
        state.enterRoom(targetRoomId);

        RoomDefinition destination = Rooms.find(rooms, targetRoomId);
        String name = destination == null || destination.displayName().isBlank()
                ? targetRoomId
                : destination.displayName();
        var changes = new ArrayList<ActionResult.Change>();
        if (roomChanged) {
            changes.add(CURRENT_ROOM);
        }
        if (newlyVisited) {
            changes.add(VISITED_ROOMS);
        }
        var message = "You move to " + name + ".";
        return ActionResult.succeeded(message, changes.toArray(ActionResult.Change[]::new));
    }
}
