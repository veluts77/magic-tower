package tower.domain.logic;

import tower.domain.action.ActionResult;
import tower.domain.action.GameAction;
import tower.domain.data.ArtifactDefinition;
import tower.domain.data.CreatureDefinition;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.data.RoomHazard;
import tower.domain.data.SpellDefinition;
import tower.domain.data.SpiritDefinition;
import tower.domain.state.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static tower.domain.action.ActionResult.Change.ARTIFACT_EQUIPPED;
import static tower.domain.action.ActionResult.Change.CREATURE_NEUTRALIZED;
import static tower.domain.action.ActionResult.Change.CURRENT_ROOM;
import static tower.domain.action.ActionResult.Change.OBJECT_CONDITION;
import static tower.domain.action.ActionResult.Change.REVEALED_OBJECTS;
import static tower.domain.action.ActionResult.Change.SPIRIT_SUMMONED;
import static tower.domain.action.ActionResult.Change.VISITED_ROOMS;
import static tower.domain.action.ActionType.MOVE;
import static tower.domain.action.ActionType.SUMMON;
import static tower.domain.data.SpiritFunction.SEEK;
import static tower.domain.state.RoomState.LIT;

public class ActionExecutor {

    private static final String SPARK = "spark";

    private final ActionResolver resolver = new ActionResolver();

    public ActionResult execute(GameState state, List<RoomDefinition> rooms, GameAction action) {
        return execute(state, rooms, action, List.of());
    }

    public ActionResult execute(
            GameState state, List<RoomDefinition> rooms, GameAction action, List<SpellDefinition> spells) {
        return execute(state, rooms, action, spells, List.of());
    }

    public ActionResult execute(
            GameState state,
            List<RoomDefinition> rooms,
            GameAction action,
            List<SpellDefinition> spells,
            List<SpiritDefinition> spirits) {
        return execute(state, rooms, action, spells, spirits, List.of());
    }

    public ActionResult execute(
            GameState state,
            List<RoomDefinition> rooms,
            GameAction action,
            List<SpellDefinition> spells,
            List<SpiritDefinition> spirits,
            List<ArtifactDefinition> artifacts) {
        return execute(state, rooms, action, spells, spirits, artifacts, List.of());
    }

    public ActionResult execute(
            GameState state,
            List<RoomDefinition> rooms,
            GameAction action,
            List<SpellDefinition> spells,
            List<SpiritDefinition> spirits,
            List<ArtifactDefinition> artifacts,
            List<CreatureDefinition> creatures) {
        if (state == null || action == null || action.type == null) {
            return ActionResult.rejected("That action is not available.");
        }
        List<SpellDefinition> knownSpells = spells == null ? List.of() : spells;
        List<SpiritDefinition> knownSpirits = spirits == null ? List.of() : spirits;
        List<ArtifactDefinition> knownArtifacts = artifacts == null ? List.of() : artifacts;
        List<CreatureDefinition> knownCreatures = creatures == null ? List.of() : creatures;
        return switch (action.type) {
            case EXAMINE -> examine(state, rooms, action, knownCreatures);
            case CAST -> cast(state, rooms, action, knownSpells, knownCreatures);
            case TAKE -> take(state, rooms, action, knownArtifacts, knownCreatures);
            case LOOK, MOVE, SUMMON -> roomAction(state, rooms, action, knownSpirits);
        };
    }

    private ActionResult roomAction(
            GameState state, List<RoomDefinition> rooms, GameAction action, List<SpiritDefinition> spirits) {
        GameAction available = match(resolver.availableActions(state, rooms, spirits), action);
        if (available == null) {
            var message = action.type == MOVE
                    ? "That room is not connected."
                    : "That action is not available.";
            return ActionResult.rejected(message);
        }
        if (available.type == MOVE) {
            return move(state, rooms, available);
        }
        if (available.type == SUMMON) {
            return summon(state, rooms, available, spirits);
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

    private ActionResult examine(
            GameState state, List<RoomDefinition> rooms, GameAction action, List<CreatureDefinition> creatures) {
        GameAction available = match(resolver.availableActions(state, rooms, action.target), action);
        if (available == null) {
            return ActionResult.rejected("That action is not available.");
        }
        ObjectDefinition object = Rooms.findObject(rooms, state.currentRoomId(), available.target);
        String message = descriptionOf(object, creatures);
        if (object != null && LIT.equals(state.objectCondition(object.id()))) {
            message = message + " A small flame burns there now.";
        }
        return ActionResult.succeeded(message);
    }

    private static String descriptionOf(ObjectDefinition object, List<CreatureDefinition> creatures) {
        if (object == null) {
            return "";
        }
        if (object.isCreature()) {
            CreatureDefinition creature = findCreature(creatures, object.creatureId());
            if (creature != null && !creature.description().isBlank()) {
                return creature.description();
            }
        }
        return object.description();
    }

    private ActionResult cast(
            GameState state,
            List<RoomDefinition> rooms,
            GameAction action,
            List<SpellDefinition> spells,
            List<CreatureDefinition> creatures) {
        String objectId = state.selectedObjectId();
        ActionResult blocked = blockedAccess(state, rooms, objectId, creatures);
        if (blocked != null) {
            return blocked;
        }
        GameAction available = match(
                resolver.availableActions(state, rooms, objectId, spells, creatures), action);
        if (available == null) {
            return ActionResult.rejected("That action is not available.");
        }
        ObjectDefinition object = Rooms.findObject(rooms, state.currentRoomId(), objectId);
        SpellDefinition spell = findSpell(spells, available.target);
        if (object == null || object.id() == null || object.id().isBlank() || spell == null) {
            return ActionResult.rejected("That action is not available.");
        }
        if (object.isCreature()) {
            return neutralize(state, object, spell, creatures);
        }
        if (!SPARK.equals(spell.id())) {
            return ActionResult.rejected("That spell does nothing here.");
        }
        return light(state, object);
    }

    private static ActionResult neutralize(
            GameState state, ObjectDefinition object, SpellDefinition spell, List<CreatureDefinition> creatures) {
        CreatureDefinition creature = findCreature(creatures, object.creatureId());
        if (creature == null || !creature.neutralizedBy(spell.id())) {
            return ActionResult.rejected("That spell does nothing here.");
        }
        if (state.creatureNeutralized(creature.id())) {
            return ActionResult.rejected("That action is not available.");
        }
        state.neutralizeCreature(creature.id());
        String name = creature.displayName().isBlank() ? creature.id() : creature.displayName();
        return ActionResult.succeeded("The " + name + " retreats. The way is clear.", CREATURE_NEUTRALIZED);
    }

    private static ActionResult blockedAccess(
            GameState state, List<RoomDefinition> rooms, String objectId, List<CreatureDefinition> creatures) {
        ObjectDefinition object = Rooms.findObject(rooms, state.currentRoomId(), objectId);
        if (object == null || object.blockedByCreature() == null) {
            return null;
        }
        RoomDefinition room = Rooms.find(rooms, state.currentRoomId());
        if (!state.isObjectVisible(room, object.id()) || state.creatureNeutralized(object.blockedByCreature())) {
            return null;
        }
        CreatureDefinition creature = findCreature(creatures, object.blockedByCreature());
        String name = creature == null || creature.displayName().isBlank()
                ? object.blockedByCreature()
                : creature.displayName();
        return ActionResult.rejected("The " + name + " blocks access.");
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

    private ActionResult summon(
            GameState state, List<RoomDefinition> rooms, GameAction action, List<SpiritDefinition> spirits) {
        SpiritDefinition spirit = findSpirit(spirits, action.target);
        if (spirit == null || spirit.function() != SEEK) {
            return ActionResult.rejected("That action is not available.");
        }
        state.summonSpirit(spirit.id());
        String spiritName = spirit.displayName().isBlank() ? spirit.id() : spirit.displayName();
        RoomDefinition room = Rooms.find(rooms, state.currentRoomId());
        var found = new ArrayList<String>();
        for (String hiddenId : room.hiddenObjects()) {
            if (state.isObjectVisible(room, hiddenId)) {
                continue;
            }
            ObjectDefinition object = Rooms.findObject(rooms, state.currentRoomId(), hiddenId);
            if (object == null) {
                continue;
            }
            state.revealObject(hiddenId);
            found.add(object.displayName().isBlank() ? object.id() : object.displayName());
        }
        String message = found.isEmpty()
                ? spiritName + " searches the room and finds nothing hidden."
                : spiritName + " uncovers " + String.join(", ", found) + ".";
        var changes = new ArrayList<ActionResult.Change>();
        changes.add(SPIRIT_SUMMONED);
        if (!found.isEmpty()) {
            changes.add(REVEALED_OBJECTS);
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

    private static SpiritDefinition findSpirit(List<SpiritDefinition> spirits, String spiritId) {
        for (SpiritDefinition spirit : spirits) {
            if (spirit != null && spirit.id().equals(spiritId)) {
                return spirit;
            }
        }
        return null;
    }

    private static CreatureDefinition findCreature(List<CreatureDefinition> creatures, String creatureId) {
        if (creatureId == null) {
            return null;
        }
        for (CreatureDefinition creature : creatures) {
            if (creature != null && creature.id().equals(creatureId)) {
                return creature;
            }
        }
        return null;
    }

    private ActionResult take(
            GameState state,
            List<RoomDefinition> rooms,
            GameAction action,
            List<ArtifactDefinition> artifacts,
            List<CreatureDefinition> creatures) {
        ActionResult blocked = blockedAccess(state, rooms, action.target, creatures);
        if (blocked != null) {
            return blocked;
        }
        ObjectDefinition object = Rooms.findObject(rooms, state.currentRoomId(), action.target);
        if (object == null || !object.isArtifact()) {
            return ActionResult.rejected("That action is not available.");
        }
        RoomDefinition room = Rooms.find(rooms, state.currentRoomId());
        if (!state.isObjectVisible(room, object.id()) || state.player().isEquipped(object.artifactId())) {
            return ActionResult.rejected("That action is not available.");
        }
        if (!state.player().hasFreeEquipmentSlot()) {
            return ActionResult.rejected("There are no free slots.");
        }
        ArtifactDefinition artifact = findArtifact(artifacts, object.artifactId());
        if (artifact == null || artifact.id().isBlank() || artifact.effect() == null) {
            return ActionResult.rejected("That action is not available.");
        }
        if (!state.player().equipArtifact(artifact.id(), artifact.effect())) {
            return ActionResult.rejected("There are no free slots.");
        }
        state.takeObject(object.id());
        String name = artifact.displayName().isBlank() ? artifact.id() : artifact.displayName();
        return ActionResult.succeeded("You take " + name + ".", ARTIFACT_EQUIPPED);
    }

    private static ArtifactDefinition findArtifact(List<ArtifactDefinition> artifacts, String artifactId) {
        for (ArtifactDefinition artifact : artifacts) {
            if (artifact != null && artifact.id().equals(artifactId)) {
                return artifact;
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
        RoomDefinition destination = Rooms.find(rooms, targetRoomId);
        RoomHazard hazard = destination.hazard();
        if (hazard != null && !state.player().hasEffect(hazard.requiredProtection())) {
            return ActionResult.rejected(hazard.blockedMessage());
        }
        boolean roomChanged = !targetRoomId.equals(state.currentRoomId());
        boolean newlyVisited = !state.visitedRoomIds().contains(targetRoomId);
        state.enterRoom(targetRoomId);
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
