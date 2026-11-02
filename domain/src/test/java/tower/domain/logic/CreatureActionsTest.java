package tower.domain.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tower.domain.action.ActionResult;
import tower.domain.action.GameAction;
import tower.domain.data.CreatureDefinition;
import tower.domain.data.FloorDefinition;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.data.SpellDefinition;
import tower.domain.state.GameState;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tower.domain.action.ActionResult.Change.CREATURE_NEUTRALIZED;
import static tower.domain.action.ActionResult.Change.OBJECT_CONDITION;
import static tower.domain.action.ActionType.CAST;
import static tower.domain.action.ActionType.EXAMINE;
import static tower.domain.data.SpellTarget.OBJECT;
import static tower.domain.state.RoomState.LIT;

class CreatureActionsTest {

    private static final String SLUG_DESCRIPTION =
            "A slow slug of warm ash. It lies across the chest and will not move.";
    private static final String CHEST_DESCRIPTION =
            "A small chest crusted with ash. The lid stays shut while the slug lies across it.";

    private List<RoomDefinition> rooms;
    private List<SpellDefinition> spells;
    private List<CreatureDefinition> creatures;
    private GameState state;
    private ActionResolver resolver;
    private ActionExecutor executor;

    @BeforeEach
    void setUp() {
        rooms = List.of(alcove());
        spells = List.of(spark());
        creatures = List.of(ashSlug());
        state = GameState.start(floor());
        resolver = new ActionResolver();
        executor = new ActionExecutor();
    }

    @Test
    void creatureDefinitionKeepsLoadedFields() {
        CreatureDefinition slug = ashSlug();

        assertEquals("ash_slug", slug.id());
        assertEquals("Ash Slug", slug.displayName());
        assertEquals(SLUG_DESCRIPTION, slug.description());
        assertEquals(List.of("spark"), slug.neutralizedBy());
        assertTrue(slug.neutralizedBy("spark"));
        assertFalse(slug.neutralizedBy("other"));
        assertFalse(slug.neutralizedBy(null));
    }

    @Test
    void examiningTheCreatureReturnsItsDescription() {
        ActionResult result = execute(GameAction.examine("slug", "ignored"));

        assertTrue(result.succeeded);
        assertEquals(SLUG_DESCRIPTION, result.message);
        assertTrue(result.changes.isEmpty());
        assertFalse(state.creatureNeutralized("ash_slug"));
    }

    @Test
    void knownSparkIsOfferedOnTheCreature() {
        state.player().learnSpell("spark");

        List<GameAction> actions = actionsFor("slug");

        assertEquals(2, actions.size());
        assertEquals(EXAMINE, actions.get(0).type);
        assertEquals("Examine Ash Slug", actions.get(0).label);
        GameAction cast = actions.get(1);
        assertEquals("cast:spark", cast.id);
        assertEquals(CAST, cast.type);
        assertEquals("spark", cast.target);
        assertEquals("Cast: Spark", cast.label);
    }

    @Test
    void unknownSparkIsNotOfferedOnTheCreature() {
        List<GameAction> actions = actionsFor("slug");

        assertEquals(1, actions.size());
        assertEquals(EXAMINE, actions.get(0).type);
        assertTrue(actions.stream().noneMatch(action -> action.type == CAST));
    }

    @Test
    void blockedChestOffersOnlyExamine() {
        state.player().learnSpell("spark");

        List<GameAction> actions = actionsFor("chest");

        assertEquals(1, actions.size());
        assertEquals("examine:chest", actions.get(0).id);
        assertEquals(EXAMINE, actions.get(0).type);
        assertTrue(actions.stream().noneMatch(action -> action.type == CAST));
    }

    @Test
    void examiningABlockedChestStillReturnsItsDescription() {
        ActionResult result = execute(GameAction.examine("chest", "ignored"));

        assertTrue(result.succeeded);
        assertEquals(CHEST_DESCRIPTION, result.message);
        assertTrue(result.changes.isEmpty());
    }

    @Test
    void castingOnABlockedChestIsRejected() {
        state.player().learnSpell("spark");
        state.selectObject("chest");

        ActionResult result = execute(GameAction.cast("spark", "ignored"));

        assertFalse(result.succeeded);
        assertEquals("The Ash Slug blocks access.", result.message);
        assertTrue(result.changes.isEmpty());
        assertEquals("", state.objectCondition("chest"));
        assertFalse(state.creatureNeutralized("ash_slug"));
        assertEquals("alcove", state.currentRoomId());
    }

    @Test
    void neutralizingTheSlugOpensTheChest() {
        state.player().learnSpell("spark");
        state.selectObject("slug");

        ActionResult result = execute(GameAction.cast("spark", "ignored"));

        assertTrue(result.succeeded);
        assertEquals("The Ash Slug retreats. The way is clear.", result.message);
        assertEquals(List.of(CREATURE_NEUTRALIZED), result.changes);
        assertTrue(state.creatureNeutralized("ash_slug"));
        assertFalse(state.creatureNeutralized("other"));
        assertEquals("", state.objectCondition("slug"));
        assertEquals(List.of("slug", "chest"), visibleIds());

        List<GameAction> chestActions = actionsFor("chest");
        assertEquals(2, chestActions.size());
        assertEquals(EXAMINE, chestActions.get(0).type);
        assertEquals(CAST, chestActions.get(1).type);
        assertEquals("spark", chestActions.get(1).target);

        state.selectObject("chest");
        ActionResult opened = execute(GameAction.cast("spark", "ignored"));

        assertTrue(opened.succeeded);
        assertEquals(List.of(OBJECT_CONDITION), opened.changes);
        assertEquals(LIT, state.objectCondition("chest"));
    }

    @Test
    void neutralizingAgainIsRejected() {
        state.player().learnSpell("spark");
        state.selectObject("slug");
        execute(GameAction.cast("spark", "ignored"));
        state.selectObject("slug");

        List<GameAction> actions = actionsFor("slug");
        ActionResult again = execute(GameAction.cast("spark", "ignored"));

        assertEquals(1, actions.size());
        assertEquals(EXAMINE, actions.get(0).type);
        assertFalse(again.succeeded);
        assertEquals("That action is not available.", again.message);
        assertTrue(again.changes.isEmpty());
        assertTrue(state.creatureNeutralized("ash_slug"));
        assertEquals(List.of("slug", "chest"), visibleIds());
    }

    private ActionResult execute(GameAction action) {
        return executor.execute(state, rooms, action, spells, List.of(), List.of(), creatures);
    }

    private List<GameAction> actionsFor(String objectId) {
        return resolver.availableActions(state, rooms, objectId, spells, creatures);
    }

    private List<String> visibleIds() {
        var ids = new ArrayList<String>();
        for (ObjectDefinition object : state.visibleObjects(rooms.get(0))) {
            ids.add(object.id());
        }
        return ids;
    }

    private static FloorDefinition floor() {
        var floor = FloorDefinition.create("ground", "Ground Floor");
        floor.setRoomIds(List.of("alcove"));
        return floor;
    }

    private static RoomDefinition alcove() {
        var slug = ObjectDefinition.create("slug", "Ash Slug", "creature", "A creature lies here.");
        slug.setCreatureId("ash_slug");
        var chest = ObjectDefinition.create("chest", "Ash Chest", "container", CHEST_DESCRIPTION);
        chest.setBlockedByCreature("ash_slug");
        chest.setSupportedSpells(List.of("spark"));
        var room = RoomDefinition.create(
                "alcove",
                "Side Alcove",
                "A narrow alcove holds an empty pedestal. An ash slug lies across a small chest.");
        room.setObjects(new ArrayList<>(List.of(slug, chest)));
        return room;
    }

    private static CreatureDefinition ashSlug() {
        return CreatureDefinition.create("ash_slug", "Ash Slug", SLUG_DESCRIPTION, List.of("spark"));
    }

    private static SpellDefinition spark() {
        return SpellDefinition.create("spark", "Spark", "A brief tongue of fire.", OBJECT);
    }
}
