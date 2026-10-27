package tower.domain.data;

import java.util.ArrayList;
import java.util.List;

public class RoomDefinition {

    public String id;
    public String displayName;
    public String description;
    public List<ObjectDefinition> objects = new ArrayList<>();
    public List<RoomExit> exits = new ArrayList<>();
}
