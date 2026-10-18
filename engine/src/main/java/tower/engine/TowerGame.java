package tower.engine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import tower.domain.data.FloorDefinition;
import tower.domain.service.DataLoader;
import tower.engine.data.JsonDataLoader;

public class TowerGame extends Game {

    @Override
    public void create() {
        DataLoader loader = new JsonDataLoader();
        FloorDefinition floor = loader.loadFloor("ground");
        int roomCount = floor.roomIds == null ? 0 : floor.roomIds.size();
        Gdx.app.log("TowerGame", "Loaded floor: " + floor.displayName + ", rooms: " + roomCount);
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        super.render();
    }

    @Override
    public void dispose() {
        // Resource disposal will be added later.
    }
}
