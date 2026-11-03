package tower.engine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import tower.domain.data.ArtifactDefinition;
import tower.domain.data.CreatureDefinition;
import tower.domain.data.FloorDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.data.RoomExit;
import tower.domain.data.SpellDefinition;
import tower.domain.data.SpiritDefinition;
import tower.domain.state.GameState;
import tower.engine.data.JsonDataLoader;
import tower.engine.screen.GameScreen;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.badlogic.gdx.Input.Keys.ESCAPE;

public class TowerGame extends Game {

    @Override
    public void create() {
        var loader = new JsonDataLoader();
        var floors = new LinkedHashMap<String, FloorDefinition>();
        var roomsByFloor = new LinkedHashMap<String, List<RoomDefinition>>();
        loadFloors(loader, "floor_01_dusty_archive", floors, roomsByFloor);
        FloorDefinition floor = floors.get("floor_01_dusty_archive");
        List<SpellDefinition> spells = loader.loadSpells();
        List<SpiritDefinition> spirits = loader.loadSpirits();
        List<ArtifactDefinition> artifacts = loader.loadArtifacts();
        List<CreatureDefinition> creatures = loader.loadCreatures();
        Gdx.app.log(
                "TowerGame",
                "Loaded floor: " + floor.displayName()
                        + ", rooms: " + floor.roomCount()
                        + ", marks: " + floor.markCount()
                        + ", spells: " + spells.size()
                        + ", spirits: " + spirits.size()
                        + ", artifacts: " + artifacts.size()
                        + ", creatures: " + creatures.size());
        var state = GameState.start(floor);
        for (String spellId : floor.startingSpells()) {
            state.player().learnSpell(spellId);
        }
        for (String markId : floor.grantedAtStart()) {
            state.floorState().addMark(markId);
        }
        setScreen(new GameScreen(state, floors, roomsByFloor, spells, spirits, artifacts, creatures));
    }

    private static void loadFloors(
            JsonDataLoader loader,
            String floorId,
            Map<String, FloorDefinition> floors,
            Map<String, List<RoomDefinition>> roomsByFloor) {
        if (floorId == null || floorId.isBlank() || floors.containsKey(floorId)) {
            return;
        }
        FloorDefinition floor = loader.loadFloor(floorId);
        List<RoomDefinition> rooms = loader.loadedRooms();
        floors.put(floor.id(), floor);
        roomsByFloor.put(floor.id(), rooms);
        for (RoomDefinition room : rooms) {
            for (RoomExit exit : room.getExits()) {
                if (exit.ascends()) {
                    loadFloors(loader, exit.targetFloorId(), floors, roomsByFloor);
                }
            }
        }
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
