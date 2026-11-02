package tower.domain.state;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class PlayerState {

    private final Set<String> knownSpellIds = new LinkedHashSet<>();
    private final Set<String> knownSpiritIds = new LinkedHashSet<>();

    public void learnSpell(String spellId) {
        if (spellId == null || spellId.isBlank()) {
            throw new IllegalArgumentException("Spell id is missing");
        }
        knownSpellIds.add(spellId);
    }

    public boolean knowsSpell(String spellId) {
        return spellId != null && knownSpellIds.contains(spellId);
    }

    public Set<String> knownSpellIds() {
        return Collections.unmodifiableSet(knownSpellIds);
    }

    public void learnSpirit(String spiritId) {
        if (spiritId == null || spiritId.isBlank()) {
            throw new IllegalArgumentException("Spirit id is missing");
        }
        knownSpiritIds.add(spiritId);
    }

    public boolean knowsSpirit(String spiritId) {
        return spiritId != null && knownSpiritIds.contains(spiritId);
    }

    public Set<String> knownSpiritIds() {
        return Collections.unmodifiableSet(knownSpiritIds);
    }
}
