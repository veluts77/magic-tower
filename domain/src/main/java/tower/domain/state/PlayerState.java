package tower.domain.state;

import tower.domain.data.ArtifactEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PlayerState {

    public static final int EQUIPMENT_SLOTS = 3;

    private final Set<String> knownSpellIds = new LinkedHashSet<>();
    private final Set<String> knownSpiritIds = new LinkedHashSet<>();
    private final EquipmentSlot[] equipment = new EquipmentSlot[EQUIPMENT_SLOTS];

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

    public boolean equipArtifact(String artifactId, ArtifactEffect effect) {
        if (artifactId == null || artifactId.isBlank()) {
            throw new IllegalArgumentException("Artifact id is missing");
        }
        if (effect == null) {
            throw new IllegalArgumentException("Artifact effect is missing");
        }
        if (isEquipped(artifactId) || !hasFreeEquipmentSlot()) {
            return false;
        }
        for (int i = 0; i < equipment.length; i++) {
            if (equipment[i] == null) {
                equipment[i] = new EquipmentSlot(artifactId, effect);
                return true;
            }
        }
        return false;
    }

    public boolean isEquipped(String artifactId) {
        return effectOf(artifactId) != null;
    }

    public ArtifactEffect effectOf(String artifactId) {
        if (artifactId == null) {
            return null;
        }
        for (EquipmentSlot slot : equipment) {
            if (slot != null && artifactId.equals(slot.artifactId)) {
                return slot.effect;
            }
        }
        return null;
    }

    public boolean hasEffect(ArtifactEffect effect) {
        if (effect == null) {
            return false;
        }
        for (EquipmentSlot slot : equipment) {
            if (slot != null && slot.effect == effect) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFreeEquipmentSlot() {
        for (EquipmentSlot slot : equipment) {
            if (slot == null) {
                return true;
            }
        }
        return false;
    }

    public List<String> equippedArtifactIds() {
        var ids = new ArrayList<String>();
        for (EquipmentSlot slot : equipment) {
            if (slot != null) {
                ids.add(slot.artifactId);
            }
        }
        return List.copyOf(ids);
    }

    private static final class EquipmentSlot {
        private final String artifactId;
        private final ArtifactEffect effect;

        private EquipmentSlot(String artifactId, ArtifactEffect effect) {
            this.artifactId = artifactId;
            this.effect = effect;
        }
    }
}
