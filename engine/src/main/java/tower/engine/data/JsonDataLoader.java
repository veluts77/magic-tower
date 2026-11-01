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

    private List<RoomDefinition> loadedRooms = List.of();

    public RoomDefinition firstLoadedRoom() {
        if (loadedRooms.isEmpty()) {
            Gdx.app.error("JsonDataLoader", "Loaded floor has no rooms");
            throw new IllegalStateException("Loaded floor has no rooms");
        }
        return loadedRooms.get(0);
    }

    @Override
    public FloorDefinition loadFloor(String floorId) {
        FileHandle file = Gdx.files.internal("data/floors/" + floorId + ".json");
        if (!file.exists()) {
            Gdx.app.error("JsonDataLoader", "Floor file not found: " + file.path());
            throw new IllegalArgumentException("Floor file not found: " + floorId);
        }

        var json = new Json();
        json.setElementType(FloorFile.class, "rooms", RoomDefinition.class);
        json.setElementType(RoomDefinition.class, "objects", ObjectDefinition.class);

        var fileData = json.fromJson(FloorFile.class, file);
        if (fileData.rooms != null) {
            loadedRooms = new ArrayList<>(fileData.rooms);
        } else {
            loadedRooms = List.of();
        }
        return fileData.createFloorDefinition();
    }

    static class FloorFile {
        public String id;
        public String displayName;
        public List<RoomDefinition> rooms = new ArrayList<>();

        FloorDefinition createFloorDefinition() {
            var floor = FloorDefinition.create(id, displayName);
            var roomIds = new ArrayList<String>();
            if (rooms != null) {
                for (RoomDefinition room : rooms) {
                    roomIds.add(room.id());
                }
            }
            floor.setRoomIds(roomIds);
            return floor;
        }
    }
}
