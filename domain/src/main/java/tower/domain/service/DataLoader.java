package tower.domain.service;

import tower.domain.data.ArtifactDefinition;
import tower.domain.data.CreatureDefinition;
import tower.domain.data.FloorDefinition;
import tower.domain.data.SpellDefinition;
import tower.domain.data.SpiritDefinition;

import java.util.List;

public interface DataLoader {

    FloorDefinition loadFloor(String floorId);

    List<SpellDefinition> loadSpells();

    List<SpiritDefinition> loadSpirits();

    List<ArtifactDefinition> loadArtifacts();

    List<CreatureDefinition> loadCreatures();
}
