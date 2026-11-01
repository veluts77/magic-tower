package tower.engine.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import tower.domain.action.GameAction;
import tower.domain.data.RoomDefinition;
import tower.domain.logic.ActionExecutor;
import tower.domain.logic.ActionResolver;
import tower.domain.state.GameState;
import tower.engine.render.ActionPanel;
import tower.engine.render.RoomRenderer;

import java.util.List;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static tower.domain.action.ActionType.LOOK;
import static tower.engine.input.ActionKeys.NONE;
import static tower.engine.input.ActionKeys.UNAVAILABLE;
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
        applyChoice();
        List<GameAction> actions = resolver.availableActions(state, rooms);
        RoomDefinition room = currentRoom();

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.10f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);

        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        float width = camera.viewportWidth - 96f;
        float inset = actionPanel.reservedHeight(actions, message, font, width);
        roomRenderer.draw(room, shapes, batch, font, camera.viewportWidth, camera.viewportHeight, inset);
        actionPanel.draw(actions, message, batch, font, width, inset);
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

    private void applyChoice() {
        List<GameAction> actions = resolver.availableActions(state, rooms);
        int index = selectedIndex(actions.size());
        if (index == NONE) {
            return;
        }
        GameAction action = index == UNAVAILABLE ? UNAVAILABLE_ACTION : actions.get(index);
        String result = executor.execute(state, rooms, action).message;
        message = result == null ? "" : result;
    }

    private RoomDefinition currentRoom() {
        for (RoomDefinition room : rooms) {
            if (room.sameId(state.currentRoomId())) {
                return room;
            }
        }
        return RoomDefinition.createNonExistingRoom();
    }
}
