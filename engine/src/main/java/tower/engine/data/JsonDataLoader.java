package tower.engine.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import tower.domain.data.FloorDefinition;
import tower.domain.data.ObjectDefinition;
import tower.domain.data.RoomDefinition;
import tower.domain.service.DataLoader;

import java.util.ArrayList;
import java.util.List;

public class JsonDataLoader implements DataLoader {

    @Override
    public FloorDefinition loadFloor(String floorId) {
        FileHandle file = Gdx.files.internal("data/floors/" + floorId + ".json");
        if (!file.exists()) {
            Gdx.app.error("JsonDataLoader", "Floor file not found: " + file.path());
            throw new IllegalArgumentException("Floor file not found: " + floorId);
        }

        Json json = new Json();
        json.setElementType(FloorFile.class, "rooms", RoomDefinition.class);
        json.setElementType(RoomDefinition.class, "objects", ObjectDefinition.class);

        FloorFile fileData = json.fromJson(FloorFile.class, file);
        FloorDefinition floor = new FloorDefinition();
        floor.id = fileData.id;
        floor.displayName = fileData.displayName;
        floor.roomIds = new ArrayList<>();
        if (fileData.rooms != null) {
            for (RoomDefinition room : fileData.rooms) {
                floor.roomIds.add(room.id);
            }
        }
        return floor;
    }

    static class FloorFile {
        public String id;
        public String displayName;
        public List<RoomDefinition> rooms = new ArrayList<>();
    }
}
