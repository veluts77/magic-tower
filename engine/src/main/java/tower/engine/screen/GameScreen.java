package tower.engine.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import tower.domain.action.ActionResult;
import tower.domain.action.GameAction;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.logic.ActionExecutor;
import tower.domain.logic.ActionResolver;
import tower.domain.state.GameState;
import tower.engine.render.ActionPanel;
import tower.engine.render.RoomRenderer;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static tower.domain.action.ActionType.LOOK;
import static tower.engine.input.ActionKeys.NONE;
import static tower.engine.input.ActionKeys.UNAVAILABLE;
import static tower.engine.input.ActionKeys.modeSwitchPressed;
import static tower.engine.input.ActionKeys.selectedIndex;

public class GameScreen implements Screen {

    private static final GameAction UNAVAILABLE_ACTION =
            new GameAction("unavailable", LOOK, "unavailable", "Unavailable");

    private final List<RoomDefinition> rooms;
    private final GameState state;
    private final ActionResolver resolver = new ActionResolver();
    private final ActionExecutor executor = new ActionExecutor();
    private final RoomRenderer roomRenderer = new RoomRenderer();
    private final ActionPanel actionPanel = new ActionPanel();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private String message = "";
    private Mode mode = Mode.ROOM;

    public GameScreen(GameState state, List<RoomDefinition> rooms) {
        if (state == null) {
            throw new IllegalArgumentException("Game state is missing");
        }
        this.state = state;
        this.rooms = rooms == null ? List.of() : List.copyOf(rooms);
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
        RoomDefinition room = currentRoom();

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.10f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);

        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        float width = camera.viewportWidth - 96f;
        float inset = actionPanel.reservedHeight(heading, lines, message, font, width);
        roomRenderer.draw(
                room,
                shapes,
                batch,
                font,
                camera.viewportWidth,
                camera.viewportHeight,
                inset,
                mode == Mode.SELECT_OBJECT);
        actionPanel.draw(heading, lines, message, batch, font, width, inset);
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
        List<GameAction> actions = resolver.availableActions(state, rooms);
        int index = selectedIndex(actions.size());
        if (index == NONE) {
            return;
        }
        GameAction action = index == UNAVAILABLE ? UNAVAILABLE_ACTION : actions.get(index);
        message = text(executor.execute(state, rooms, action));
    }

    private void chooseObject() {
        List<ObjectDefinition> objects = currentRoom().getObjects();
        int index = selectedIndex(objects.size());
        if (index == NONE) {
            return;
        }
        if (index == UNAVAILABLE) {
            message = text(executor.execute(state, rooms, UNAVAILABLE_ACTION));
            return;
        }
        state.selectObject(objects.get(index).id());
        mode = Mode.OBJECT_ACTIONS;
    }

    private void chooseObjectAction() {
        List<GameAction> actions = resolver.availableActions(state, rooms, state.selectedObjectId());
        int index = selectedIndex(actions.size());
        if (index == NONE) {
            return;
        }
        GameAction action = index == UNAVAILABLE ? UNAVAILABLE_ACTION : actions.get(index);
        message = text(executor.execute(state, rooms, action));
        if (index != UNAVAILABLE) {
            leaveObject();
        }
    }

    private void leaveObject() {
        state.clearSelection();
        mode = Mode.ROOM;
    }

    private static String text(ActionResult result) {
        return result.message == null ? "" : result.message;
    }

    private List<String> lines() {
        return switch (mode) {
            case ROOM -> labels(resolver.availableActions(state, rooms));
            case SELECT_OBJECT -> objectLabels(currentRoom().getObjects());
            case OBJECT_ACTIONS -> labels(resolver.availableActions(state, rooms, state.selectedObjectId()));
        };
    }

    private String heading() {
        return switch (mode) {
            case ROOM -> "Tab: choose an object";
            case SELECT_OBJECT -> "Choose an object. Tab: room";
            case OBJECT_ACTIONS -> selectedName() + ". Tab: room";
        };
    }

    private String selectedName() {
        String selectedId = state.selectedObjectId();
        for (ObjectDefinition object : currentRoom().getObjects()) {
            if (object.id().equals(selectedId)) {
                String name = object.displayName();
                return name.isBlank() ? selectedId : name;
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

    private static List<String> objectLabels(List<ObjectDefinition> objects) {
        var lines = new ArrayList<String>();
        for (ObjectDefinition object : objects) {
            String name = object.displayName();
            lines.add(name.isBlank() ? object.id() : name);
        }
        return lines;
    }

    private RoomDefinition currentRoom() {
        for (RoomDefinition room : rooms) {
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
