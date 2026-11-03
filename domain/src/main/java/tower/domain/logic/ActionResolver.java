package tower.domain.logic;

import tower.domain.action.GameAction;
import tower.domain.data.CreatureDefinition;
import tower.domain.data.FloorDefinition;
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
        return availableActions(state, rooms, spirits, null);
    }

    public List<GameAction> availableActions(
            GameState state, List<RoomDefinition> rooms, List<SpiritDefinition> spirits, FloorDefinition floor) {
        if (state == null) {
            return List.of();
        }
        RoomDefinition room = Rooms.find(rooms, state.currentRoomId());
        var actions = new ArrayList<GameAction>();
        actions.add(GameAction.look());
        addMoveActions(actions, room, rooms, state, floor);
        addSummonActions(actions, state, spirits);
        return List.copyOf(actions);
    }

    public List<GameAction> availableActions(GameState state, List<RoomDefinition> rooms, String objectId) {
        return availableActions(state, rooms, objectId, List.of());
    }

    public List<GameAction> availableActions(
            GameState state, List<RoomDefinition> rooms, String objectId, List<SpellDefinition> spells) {
        return availableActions(state, rooms, objectId, spells, List.of());
    }

    public List<GameAction> availableActions(
            GameState state,
            List<RoomDefinition> rooms,
            String objectId,
            List<SpellDefinition> spells,
            List<CreatureDefinition> creatures) {
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
        if (blocked(state, object)) {
            return List.copyOf(actions);
        }
        addTakeAction(actions, state, object);
        if (object.isCreature()) {
            addNeutralizeActions(actions, state, object, spells, creatures);
        } else {
            addCastActions(actions, state, object, spells);
        }
        return List.copyOf(actions);
    }

    private static boolean blocked(GameState state, ObjectDefinition object) {
        String creatureId = object.blockedByCreature();
        return creatureId != null && !state.creatureNeutralized(creatureId);
    }

    private static void addNeutralizeActions(
            List<GameAction> actions,
            GameState state,
            ObjectDefinition object,
            List<SpellDefinition> spells,
            List<CreatureDefinition> creatures) {
        if (spells == null || state.creatureNeutralized(object.creatureId())) {
            return;
        }
        CreatureDefinition creature = findCreature(creatures, object.creatureId());
        if (creature == null) {
            return;
        }
        var seen = new LinkedHashSet<String>();
        for (SpellDefinition spell : spells) {
            if (spell == null) {
                continue;
            }
            String spellId = spell.id();
            if (spellId.isBlank() || !seen.add(spellId)) {
                continue;
            }
            if (!state.player().knowsSpell(spellId) || !creature.neutralizedBy(spellId)) {
                continue;
            }
            String name = spell.displayName().isBlank() ? spellId : spell.displayName();
            actions.add(GameAction.cast(spellId, "Cast: " + name));
        }
    }

    private static CreatureDefinition findCreature(List<CreatureDefinition> creatures, String creatureId) {
        if (creatures == null || creatureId == null) {
            return null;
        }
        for (CreatureDefinition creature : creatures) {
            if (creature != null && creature.id().equals(creatureId)) {
                return creature;
            }
        }
        return null;
    }

    private static void addTakeAction(List<GameAction> actions, GameState state, ObjectDefinition object) {
        String artifactId = object.artifactId();
        if (artifactId == null) {
            return;
        }
        if (state.player().isEquipped(artifactId) || !state.player().hasFreeEquipmentSlot()) {
            return;
        }
        actions.add(GameAction.take(object.id(), "Pick up"));
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

    private static void addMoveActions(
            List<GameAction> actions,
            RoomDefinition room,
            List<RoomDefinition> rooms,
            GameState state,
            FloorDefinition floor) {
        for (RoomExit exit : room.getExits()) {
            if (exit.ascends()) {
                addAscentAction(actions, exit, state, floor);
                continue;
            }
            String targetRoomId = exit.targetRoomId();
            if (targetRoomId == null || targetRoomId.isBlank()) {
                continue;
            }
            actions.add(GameAction.move(targetRoomId, moveLabel(exit, Rooms.find(rooms, targetRoomId))));
        }
    }

    private static void addAscentAction(
            List<GameAction> actions, RoomExit exit, GameState state, FloorDefinition floor) {
        if (floor == null || state == null || !floor.ascentUnlocked(state.floorState().earnedCount())) {
            return;
        }
        String floorId = exit.targetFloorId();
        if (floorId == null || floorId.isBlank()) {
            return;
        }
        String label = exit.label().isBlank() ? "Ascend" : exit.label();
        actions.add(GameAction.ascend(floorId, label));
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
