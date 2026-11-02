package tower.domain.data;

import static tower.domain.data.ArtifactEffect.FIRE_PROTECTION;

public enum RoomHazard {
    FIRE;

    public ArtifactEffect requiredProtection() {
        return switch (this) {
            case FIRE -> FIRE_PROTECTION;
        };
    }

    public String blockedMessage() {
        return switch (this) {
            case FIRE -> "A veil of fire blocks the way.";
        };
    }
}
