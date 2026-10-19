package tower.engine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import tower.domain.data.FloorDefinition;
import tower.engine.data.JsonDataLoader;
import tower.engine.screen.GameScreen;

public class TowerGame extends Game {

    @Override
    public void create() {
        JsonDataLoader loader = new JsonDataLoader();
        FloorDefinition floor = loader.loadFloor("ground");
        Gdx.app.log(
                "TowerGame",
                "Loaded floor: " + floor.displayName + ", rooms: " + floor.roomCount());
        setScreen(new GameScreen(loader.firstLoadedRoom()));
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
