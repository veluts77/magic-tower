package tower.domain.state;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class FloorState {

    private final Set<String> earned = new LinkedHashSet<>();

    public boolean addMark(String markId) {
        if (markId == null || markId.isBlank()) {
            throw new IllegalArgumentException("Mark id is missing");
        }
        return earned.add(markId);
    }

    public boolean hasMark(String markId) {
        return markId != null && earned.contains(markId);
    }

    public int earnedCount() {
        return earned.size();
    }

    public Set<String> earnedMarkIds() {
        return Collections.unmodifiableSet(earned);
    }
}
