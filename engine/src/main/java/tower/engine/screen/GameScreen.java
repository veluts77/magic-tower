package tower.engine.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import tower.domain.action.ActionResult;
import tower.domain.action.GameAction;
import tower.domain.data.ArtifactDefinition;
import tower.domain.data.CreatureDefinition;
import tower.domain.data.FloorDefinition;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.data.SpellDefinition;
import tower.domain.data.SpiritDefinition;
import tower.domain.logic.ActionExecutor;
import tower.domain.logic.ActionResolver;
import tower.domain.state.GameState;
import tower.engine.render.ActionPanel;
import tower.engine.render.GameFonts;
import tower.engine.render.RoomRenderer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static tower.domain.action.ActionType.LOOK;
import static tower.domain.state.RoomState.LIT;
import static tower.engine.input.ActionKeys.NONE;
import static tower.engine.input.ActionKeys.UNAVAILABLE;
import static tower.engine.input.ActionKeys.modeSwitchPressed;
import static tower.engine.input.ActionKeys.selectedIndex;

public class GameScreen implements Screen {

    private static final GameAction UNAVAILABLE_ACTION =
            new GameAction("unavailable", LOOK, "unavailable", "Unavailable");

    private final Map<String, FloorDefinition> floors;
    private final Map<String, List<RoomDefinition>> roomsByFloor;
    private final List<FloorDefinition> floorList;
    private final List<SpellDefinition> spells;
    private final List<SpiritDefinition> spirits;
    private final List<ArtifactDefinition> artifacts;
    private final List<CreatureDefinition> creatures;
    private final GameState state;
    private final ActionResolver resolver = new ActionResolver();
    private final ActionExecutor executor = new ActionExecutor();
    private final RoomRenderer roomRenderer = new RoomRenderer();
    private final ActionPanel actionPanel = new ActionPanel();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = GameFonts.create();
    private String message = "";
    private Mode mode = Mode.ROOM;

    public GameScreen(
            GameState state,
            Map<String, FloorDefinition> floors,
            Map<String, List<RoomDefinition>> roomsByFloor,
            List<SpellDefinition> spells,
            List<SpiritDefinition> spirits,
            List<ArtifactDefinition> artifacts,
            List<CreatureDefinition> creatures) {
        if (state == null) {
            throw new IllegalArgumentException("Game state is missing");
        }
        this.state = state;
        this.floors = new LinkedHashMap<>();
        if (floors != null) {
            this.floors.putAll(floors);
        }
        this.roomsByFloor = new LinkedHashMap<>();
        if (roomsByFloor != null) {
            for (var entry : roomsByFloor.entrySet()) {
                List<RoomDefinition> roomList = entry.getValue();
                this.roomsByFloor.put(entry.getKey(), roomList == null ? List.of() : List.copyOf(roomList));
            }
        }
        this.floorList = List.copyOf(this.floors.values());
        this.spells = spells == null ? List.of() : List.copyOf(spells);
        this.spirits = spirits == null ? List.of() : List.copyOf(spirits);
        this.artifacts = artifacts == null ? List.of() : List.copyOf(artifacts);
        this.creatures = creatures == null ? List.of() : List.copyOf(creatures);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        applyInput();
        List<String> lines = lines();
        String heading = heading();
        String progress = progress();
        RoomDefinition room = currentRoom();

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.10f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);

        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        float width = camera.viewportWidth - 96f;
        float inset = actionPanel.reservedHeight(progress, heading, lines, message, font, width);
        roomRenderer.draw(
                room,
                shapes,
                batch,
                font,
                camera.viewportWidth,
                camera.viewportHeight,
                inset,
                mode == Mode.SELECT_OBJECT,
                state);
        actionPanel.draw(progress, heading, lines, message, batch, font, width, inset);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }

    private void applyInput() {
        if (modeSwitchPressed()) {
            switchMode();
            return;
        }
        switch (mode) {
            case ROOM -> chooseRoomAction();
            case SELECT_OBJECT -> chooseObject();
            case OBJECT_ACTIONS -> chooseObjectAction();
        }
    }

    private void switchMode() {
        switch (mode) {
            case ROOM -> mode = Mode.SELECT_OBJECT;
            case SELECT_OBJECT -> mode = Mode.ROOM;
            case OBJECT_ACTIONS -> leaveObject();
        }
    }

    private void chooseRoomAction() {
        List<GameAction> actions = resolver.availableActions(state, rooms(), spirits, floor());
        int index = selectedIndex(actions.size());
        if (index == NONE) {
            return;
        }
        GameAction action = index == UNAVAILABLE ? UNAVAILABLE_ACTION : actions.get(index);
        message = text(run(action));
    }

    private void chooseObject() {
        List<ObjectDefinition> objects = visibleObjects();
        int index = selectedIndex(objects.size());
        if (index == NONE) {
            return;
        }
        if (index == UNAVAILABLE) {
            message = text(run(UNAVAILABLE_ACTION));
            return;
        }
        state.selectObject(objects.get(index).id());
        mode = Mode.OBJECT_ACTIONS;
    }

    private void chooseObjectAction() {
        List<GameAction> actions = objectActions();
        int index = selectedIndex(actions.size());
        if (index == NONE) {
            return;
        }
        GameAction action = index == UNAVAILABLE ? UNAVAILABLE_ACTION : actions.get(index);
        message = text(run(action));
        if (index != UNAVAILABLE) {
            leaveObject();
        }
    }

    private void leaveObject() {
        state.clearSelection();
        mode = Mode.ROOM;
    }

    private ActionResult run(GameAction action) {
        return executor.execute(state, rooms(), action, spells, spirits, artifacts, creatures, floor(), floorList);
    }

    private static String text(ActionResult result) {
        return result.message == null ? "" : result.message;
    }

    private List<String> lines() {
        return switch (mode) {
            case ROOM -> labels(resolver.availableActions(state, rooms(), spirits, floor()));
            case SELECT_OBJECT -> objectLabels(visibleObjects());
            case OBJECT_ACTIONS -> labels(objectActions());
        };
    }

    private String heading() {
        return switch (mode) {
            case ROOM -> "Tab: choose an object";
            case SELECT_OBJECT -> "Choose an object. Tab: room";
            case OBJECT_ACTIONS -> selectedName() + ". Tab: room";
        };
    }

    private List<GameAction> objectActions() {
        return resolver.availableActions(state, rooms(), state.selectedObjectId(), spells, creatures);
    }

    private String selectedName() {
        String selectedId = state.selectedObjectId();
        for (ObjectDefinition object : visibleObjects()) {
            if (object.id().equals(selectedId)) {
                return objectName(object);
            }
        }
        if (selectedId == null || selectedId.isBlank()) {
            return "Object";
        }
        return selectedId;
    }

    private static List<String> labels(List<GameAction> actions) {
        var lines = new ArrayList<String>();
        for (GameAction action : actions) {
            lines.add(action.label);
        }
        return lines;
    }

    private List<String> objectLabels(List<ObjectDefinition> objects) {
        var lines = new ArrayList<String>();
        for (ObjectDefinition object : objects) {
            lines.add(objectName(object));
        }
        return lines;
    }

    private String objectName(ObjectDefinition object) {
        String name = object.displayName();
        if (name.isBlank()) {
            name = object.id();
        }
        if (LIT.equals(state.objectCondition(object.id()))) {
            return name + " (lit)";
        }
        return name;
    }

    private List<ObjectDefinition> visibleObjects() {
        return state.visibleObjects(currentRoom());
    }

    private String progress() {
        FloorDefinition current = floor();
        if (current == null) {
            return "";
        }
        int earned = state.floorState().earnedCount();
        String line = "Marks: " + earned + "/" + current.markCount() + ", need " + current.ascentThreshold();
        if (current.ascentUnlocked(earned)) {
            return line + ". The way up is open.";
        }
        return line;
    }

    private FloorDefinition floor() {
        return floors.get(state.currentFloorId());
    }

    private List<RoomDefinition> rooms() {
        List<RoomDefinition> found = roomsByFloor.get(state.currentFloorId());
        return found == null ? List.of() : found;
    }

    private RoomDefinition currentRoom() {
        for (RoomDefinition room : rooms()) {
            if (room.sameId(state.currentRoomId())) {
                return room;
            }
        }
        return RoomDefinition.createNonExistingRoom();
    }

    private enum Mode {
        ROOM,
        SELECT_OBJECT,
        OBJECT_ACTIONS
    }
}
