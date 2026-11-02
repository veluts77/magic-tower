package tower.domain.logic;

import tower.domain.action.GameAction;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.data.RoomExit;
import tower.domain.data.SpellDefinition;
import tower.domain.data.SpiritDefinition;
import tower.domain.state.GameState;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static tower.domain.data.SpiritFunction.SEEK;
import static tower.domain.data.SpellTarget.OBJECT;

public class ActionResolver {

    public List<GameAction> availableActions(GameState state, List<RoomDefinition> rooms) {
        return availableActions(state, rooms, List.of());
    }

    public List<GameAction> availableActions(
            GameState state, List<RoomDefinition> rooms, List<SpiritDefinition> spirits) {
        if (state == null) {
            return List.of();
        }
        RoomDefinition room = Rooms.find(rooms, state.currentRoomId());
        var actions = new ArrayList<GameAction>();
        actions.add(GameAction.look());
        addMoveActions(actions, room, rooms);
        addSummonActions(actions, state, spirits);
        return List.copyOf(actions);
    }

    public List<GameAction> availableActions(GameState state, List<RoomDefinition> rooms, String objectId) {
        return availableActions(state, rooms, objectId, List.of());
    }

    public List<GameAction> availableActions(
            GameState state, List<RoomDefinition> rooms, String objectId, List<SpellDefinition> spells) {
        if (state == null) {
            return List.of();
        }
        ObjectDefinition object = Rooms.findObject(rooms, state.currentRoomId(), objectId);
        if (object == null) {
            return List.of();
        }
        RoomDefinition room = Rooms.find(rooms, state.currentRoomId());
        if (!state.isObjectVisible(room, object.id())) {
            return List.of();
        }
        String name = object.displayName().isBlank() ? objectId : object.displayName();
        var actions = new ArrayList<GameAction>();
        actions.add(GameAction.examine(objectId, "Examine " + name));
        addCastActions(actions, state, object, spells);
        return List.copyOf(actions);
    }

    private static void addCastActions(
            List<GameAction> actions, GameState state, ObjectDefinition object, List<SpellDefinition> spells) {
        if (spells == null) {
            return;
        }
        var seen = new LinkedHashSet<String>();
        for (SpellDefinition spell : spells) {
            if (spell == null || spell.target() != OBJECT) {
                continue;
            }
            String spellId = spell.id();
            if (spellId.isBlank() || !seen.add(spellId)) {
                continue;
            }
            if (!state.player().knowsSpell(spellId) || !object.supportsSpell(spellId)) {
                continue;
            }
            String name = spell.displayName().isBlank() ? spellId : spell.displayName();
            actions.add(GameAction.cast(spellId, "Cast: " + name));
        }
    }

    private static void addSummonActions(
            List<GameAction> actions, GameState state, List<SpiritDefinition> spirits) {
        if (spirits == null) {
            return;
        }
        var seen = new LinkedHashSet<String>();
        for (SpiritDefinition spirit : spirits) {
            if (spirit == null || spirit.function() != SEEK) {
                continue;
            }
            String spiritId = spirit.id();
            if (spiritId.isBlank() || !seen.add(spiritId)) {
                continue;
            }
            if (!state.player().knowsSpirit(spiritId) || state.spiritSummoned(spiritId)) {
                continue;
            }
            String name = spirit.displayName().isBlank() ? spiritId : spirit.displayName();
            actions.add(GameAction.summon(spiritId, "Summon: " + name));
        }
    }

    private static void addMoveActions(List<GameAction> actions, RoomDefinition room, List<RoomDefinition> rooms) {
        for (RoomExit exit : room.getExits()) {
            String targetRoomId = exit.targetRoomId();
            if (targetRoomId == null || targetRoomId.isBlank()) {
                continue;
            }
            actions.add(GameAction.move(targetRoomId, moveLabel(exit, Rooms.find(rooms, targetRoomId))));
        }
    }

    private static String moveLabel(RoomExit exit, RoomDefinition destination) {
        if (!exit.label().isBlank()) {
            return exit.label();
        }
        if (!destination.displayName().isBlank()) {
            return "Go to " + destination.displayName();
        }
        return "Go to " + exit.targetRoomId();
    }
}
