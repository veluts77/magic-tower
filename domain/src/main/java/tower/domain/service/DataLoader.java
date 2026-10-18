package tower.domain.service;

import tower.domain.data.FloorDefinition;

public interface DataLoader {

    FloorDefinition loadFloor(String floorId);
}
