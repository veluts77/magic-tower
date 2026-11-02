package tower.engine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import tower.domain.data.ArtifactDefinition;
import tower.domain.data.FloorDefinition;
import tower.domain.data.SpellDefinition;
import tower.domain.data.SpiritDefinition;
import tower.domain.state.GameState;
import tower.engine.data.JsonDataLoader;
import tower.engine.screen.GameScreen;

import java.util.List;

import static com.badlogic.gdx.Input.Keys.ESCAPE;

public class TowerGame extends Game {

    @Override
    public void create() {
        var loader = new JsonDataLoader();
        FloorDefinition floor = loader.loadFloor("ground");
        List<SpellDefinition> spells = loader.loadSpells();
        List<SpiritDefinition> spirits = loader.loadSpirits();
        List<ArtifactDefinition> artifacts = loader.loadArtifacts();
        Gdx.app.log(
                "TowerGame",
                "Loaded floor: " + floor.displayName()
                        + ", rooms: " + floor.roomCount()
                        + ", spells: " + spells.size()
                        + ", spirits: " + spirits.size()
                        + ", artifacts: " + artifacts.size());
        var state = GameState.start(floor);
        state.player().learnSpell("spark");
        state.player().learnSpirit("seeker");
        setScreen(new GameScreen(state, loader.loadedRooms(), spells, spirits, artifacts));
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(ESCAPE)) {
            Gdx.app.exit();
        }

        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
