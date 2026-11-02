package tower.domain.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tower.domain.action.ActionResult;
import tower.domain.action.GameAction;
import tower.domain.data.ArtifactDefinition;
import tower.domain.data.FloorDefinition;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.data.RoomExit;
import tower.domain.state.GameState;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tower.domain.action.ActionResult.Change.ARTIFACT_EQUIPPED;
import static tower.domain.action.ActionResult.Change.CURRENT_ROOM;
import static tower.domain.action.ActionResult.Change.VISITED_ROOMS;
import static tower.domain.action.ActionType.EXAMINE;
import static tower.domain.action.ActionType.TAKE;
import static tower.domain.data.ArtifactEffect.FIRE_PROTECTION;
import static tower.domain.data.RoomHazard.FIRE;
import static tower.domain.state.PlayerState.EQUIPMENT_SLOTS;

class ArtifactActionsTest {

    private static final String RING_DESCRIPTION =
            "A thin metal ring, cool to the touch. A quiet flame sleeps inside the band.";
    private static final String ARTIFACT_DESCRIPTION =
            "A thin band that stays cool. Its quiet flame turns a greater fire aside.";

    private List<RoomDefinition> rooms;
    private List<ArtifactDefinition> artifacts;
    private GameState state;
    private ActionResolver resolver;
    private ActionExecutor executor;

    @BeforeEach
    void setUp() {
        rooms = groundRooms();
        artifacts = List.of(ringOfQuietFlame());
        state = GameState.start(groundFloor());
        resolver = new ActionResolver();
        executor = new ActionExecutor();
    }

    @Test
    void artifactDefinitionKeepsLoadedFields() {
        ArtifactDefinition ring = ringOfQuietFlame();

        assertEquals("ring_of_quiet_flame", ring.id());
        assertEquals("Ring of Quiet Flame", ring.displayName());
        assertEquals(ARTIFACT_DESCRIPTION, ring.description());
        assertEquals(FIRE_PROTECTION, ring.effect());
    }

    @Test
    void equipmentStartsEmpty() {
        assertTrue(state.player().equippedArtifactIds().isEmpty());
        assertTrue(state.player().hasFreeEquipmentSlot());
        assertEquals(EQUIPMENT_SLOTS, 3);
        assertFalse(state.player().isEquipped("ring_of_quiet_flame"));
        assertFalse(state.player().isEquipped(null));
        assertNull(state.player().effectOf("ring_of_quiet_flame"));
        assertFalse(state.player().hasEffect(FIRE_PROTECTION));
        assertFalse(state.player().hasEffect(null));
    }

    @Test
    void equippingStoresTheArtifactAndItsEffect() {
        assertTrue(state.player().equipArtifact("ring_of_quiet_flame", FIRE_PROTECTION));

        assertTrue(state.player().isEquipped("ring_of_quiet_flame"));
        assertEquals(FIRE_PROTECTION, state.player().effectOf("ring_of_quiet_flame"));
        assertTrue(state.player().hasEffect(FIRE_PROTECTION));
        assertEquals(List.of("ring_of_quiet_flame"), state.player().equippedArtifactIds());
        assertNull(state.player().effectOf("other"));
        assertFalse(state.player().isEquipped("other"));
    }

    @Test
    void blankArtifactCannotBeEquipped() {
        assertThrows(IllegalArgumentException.class, () -> state.player().equipArtifact(" ", FIRE_PROTECTION));
        assertThrows(IllegalArgumentException.class, () -> state.player().equipArtifact(null, FIRE_PROTECTION));
        assertThrows(IllegalArgumentException.class, () -> state.player().equipArtifact("ring", null));
        assertTrue(state.player().equippedArtifactIds().isEmpty());
    }

    @Test
    void duplicateArtifactDoesNotTakeAnotherSlot() {
        assertTrue(state.player().equipArtifact("ring_of_quiet_flame", FIRE_PROTECTION));

        assertFalse(state.player().equipArtifact("ring_of_quiet_flame", FIRE_PROTECTION));
        assertEquals(List.of("ring_of_quiet_flame"), state.player().equippedArtifactIds());
    }

    @Test
    void fullEquipmentRejectsAnotherArtifact() {
        assertTrue(state.player().equipArtifact("first", FIRE_PROTECTION));
        assertTrue(state.player().equipArtifact("second", FIRE_PROTECTION));
        assertTrue(state.player().equipArtifact("third", FIRE_PROTECTION));

        assertFalse(state.player().hasFreeEquipmentSlot());
        assertFalse(state.player().equipArtifact("fourth", FIRE_PROTECTION));
        assertEquals(List.of("first", "second", "third"), state.player().equippedArtifactIds());
    }

    @Test
    void ringOffersPickUp() {
        enterHall();

        List<GameAction> actions = resolver.availableActions(state, rooms, "ring");

        assertEquals(2, actions.size());
        assertEquals(EXAMINE, actions.get(0).type);
        GameAction pickUp = actions.get(1);
        assertEquals("take:ring", pickUp.id);
        assertEquals(TAKE, pickUp.type);
        assertEquals("ring", pickUp.target);
        assertEquals("Pick up", pickUp.label);
    }

    @Test
    void ordinaryObjectDoesNotOfferPickUp() {
        enterHall();

        List<GameAction> actions = resolver.availableActions(state, rooms, "brazier");

        assertEquals(1, actions.size());
        assertEquals(EXAMINE, actions.get(0).type);
        assertTrue(actions.stream().noneMatch(action -> action.type == TAKE));
    }

    @Test
    void examiningTheRingReturnsItsDescription() {
        enterHall();

        ActionResult result = executor.execute(state, rooms, GameAction.examine("ring", "ignored"));

        assertTrue(result.succeeded);
        assertEquals(RING_DESCRIPTION, result.message);
        assertTrue(result.changes.isEmpty());
    }

    @Test
    void takingTheRingEquipsItAndRemovesItFromTheRoom() {
        enterHall();
        assertEquals(List.of("brazier", "ring"), objectIds(hall().getObjects()));

        ActionResult result = takeRing();

        assertTrue(result.succeeded);
        assertEquals("You take Ring of Quiet Flame.", result.message);
        assertEquals(List.of(ARTIFACT_EQUIPPED), result.changes);
        assertTrue(state.player().isEquipped("ring_of_quiet_flame"));
        assertEquals(FIRE_PROTECTION, state.player().effectOf("ring_of_quiet_flame"));
        assertEquals(List.of("brazier", "ring"), objectIds(hall().getObjects()));
        assertEquals(List.of("brazier"), visibleIds(hall()));
        assertTrue(state.objectTaken("ring"));
        assertTrue(resolver.availableActions(state, rooms, "ring").isEmpty());
    }

    @Test
    void takingTheRingAgainDoesNothing() {
        enterHall();
        takeRing();

        ActionResult again = takeRing();

        assertFalse(again.succeeded);
        assertEquals("That action is not available.", again.message);
        assertTrue(again.changes.isEmpty());
        assertEquals(List.of("ring_of_quiet_flame"), state.player().equippedArtifactIds());
        assertEquals(List.of("brazier"), visibleIds(hall()));
        assertEquals("hall", state.currentRoomId());
    }

    @Test
    void takingFromAnotherRoomDoesNothing() {
        ActionResult result = takeRing();

        assertFalse(result.succeeded);
        assertEquals("That action is not available.", result.message);
        assertTrue(result.changes.isEmpty());
        assertTrue(state.player().equippedArtifactIds().isEmpty());
        assertEquals("entrance", state.currentRoomId());
        assertEquals(List.of("brazier", "ring"), visibleIds(hall()));
    }

    @Test
    void fullSlotsMakePickUpUnavailable() {
        enterHall();
        state.player().equipArtifact("first", FIRE_PROTECTION);
        state.player().equipArtifact("second", FIRE_PROTECTION);
        state.player().equipArtifact("third", FIRE_PROTECTION);

        List<GameAction> actions = resolver.availableActions(state, rooms, "ring");
        ActionResult result = takeRing();

        assertTrue(actions.stream().noneMatch(action -> action.type == TAKE));
        assertFalse(result.succeeded);
        assertEquals("There are no free slots.", result.message);
        assertTrue(result.changes.isEmpty());
        assertFalse(state.player().isEquipped("ring_of_quiet_flame"));
        assertEquals(List.of("first", "second", "third"), state.player().equippedArtifactIds());
        assertEquals(List.of("brazier", "ring"), visibleIds(hall()));
        assertEquals("hall", state.currentRoomId());
    }

    @Test
    void fireVeilBlocksEntryWithoutProtection() {
        enterHall();
        assertNull(hall().hazard());
        assertEquals(FIRE, veil().hazard());
        assertEquals(FIRE_PROTECTION, veil().hazard().requiredProtection());
        assertTrue(resolver.availableActions(state, rooms).stream()
                .anyMatch(action -> "veil".equals(action.target)));

        ActionResult blocked = moveTo("veil");

        assertFalse(blocked.succeeded);
        assertEquals("A veil of fire blocks the way.", blocked.message);
        assertTrue(blocked.changes.isEmpty());
        assertEquals("hall", state.currentRoomId());
        assertEquals(List.of("entrance", "hall"), new ArrayList<>(state.visitedRoomIds()));
        assertFalse(state.player().hasEffect(FIRE_PROTECTION));
    }

    @Test
    void equippedRingAllowsEntryThroughTheFire() {
        enterHall();
        takeRing();

        ActionResult entered = moveTo("veil");

        assertTrue(entered.succeeded);
        assertEquals("You move to Flame Veil.", entered.message);
        assertEquals(List.of(CURRENT_ROOM, VISITED_ROOMS), entered.changes);
        assertEquals("veil", state.currentRoomId());
        assertTrue(state.player().hasEffect(FIRE_PROTECTION));

        ActionResult back = moveTo("hall");

        assertTrue(back.succeeded);
        assertEquals("hall", state.currentRoomId());
        assertEquals(List.of("brazier"), visibleIds(hall()));
    }

    private void enterHall() {
        executor.execute(state, rooms, GameAction.move("hall", "ignored"));
    }

    private ActionResult takeRing() {
        return executor.execute(
                state, rooms, GameAction.take("ring", "ignored"), List.of(), List.of(), artifacts);
    }

    private ActionResult moveTo(String roomId) {
        return executor.execute(state, rooms, GameAction.move(roomId, "ignored"), List.of(), List.of(), artifacts);
    }

    private List<String> visibleIds(RoomDefinition room) {
        var ids = new ArrayList<String>();
        for (ObjectDefinition object : state.visibleObjects(room)) {
            ids.add(object.id());
        }
        return ids;
    }

    private RoomDefinition hall() {
        return rooms.get(1);
    }

    private RoomDefinition veil() {
        return rooms.get(2);
    }

    private static List<String> objectIds(List<ObjectDefinition> objects) {
        var ids = new ArrayList<String>();
        for (ObjectDefinition object : objects) {
            ids.add(object.id());
        }
        return ids;
    }

    private static ArtifactDefinition ringOfQuietFlame() {
        return ArtifactDefinition.create(
                "ring_of_quiet_flame", "Ring of Quiet Flame", ARTIFACT_DESCRIPTION, FIRE_PROTECTION);
    }

    private static FloorDefinition groundFloor() {
        var floor = FloorDefinition.create("ground", "Ground Floor");
        floor.setRoomIds(List.of("entrance", "hall", "veil"));
        return floor;
    }

    private static List<RoomDefinition> groundRooms() {
        RoomDefinition entrance = room(
                "entrance",
                "Entrance",
                "A worn stone arch leads into the tower. Dust hangs in the still air.",
                List.of(),
                exit("hall", "Go to the Quiet Hall"));
        RoomDefinition hall = room(
                "hall",
                "Quiet Hall",
                "A long hall is quiet. A cold brazier, a faded inscription, and a cool ring wait in the dim light.",
                List.of(
                        object("brazier", "Cold Brazier", "fixture",
                                "The iron bowl is cold. Ash has settled into a hard grey crust."),
                        ringObject()),
                exit("entrance", "Go to the Entrance"),
                exit("veil", "Go to the Flame Veil"));
        RoomDefinition veil = room(
                "veil",
                "Flame Veil",
                "A curtain of quiet fire fills the arch. The heat does not roar. It simply refuses passage.",
                List.of(),
                exit("hall", "Go to the Quiet Hall"));
        veil.setHazard(FIRE);
        return List.of(entrance, hall, veil);
    }

    private static RoomDefinition room(
            String id,
            String displayName,
            String description,
            List<ObjectDefinition> objects,
            RoomExit... exits) {
        var room = RoomDefinition.create(id, displayName, description);
        room.setObjects(new ArrayList<>(objects));
        room.setExits(new ArrayList<>(List.of(exits)));
        return room;
    }

    private static ObjectDefinition ringObject() {
        var ring = object("ring", "Ring of Quiet Flame", "artifact", RING_DESCRIPTION);
        ring.setArtifactId("ring_of_quiet_flame");
        return ring;
    }

    private static ObjectDefinition object(String id, String displayName, String type, String description) {
        return ObjectDefinition.create(id, displayName, type, description);
    }

    private static RoomExit exit(String targetRoomId, String label) {
        return RoomExit.create(targetRoomId, label);
    }
}
