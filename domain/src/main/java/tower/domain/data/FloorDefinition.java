package tower.domain.data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class FloorDefinition {

    private String id;
    private String displayName;
    private int ascentThreshold;
    private List<String> roomIds = new ArrayList<>();
    private List<String> startingSpells = new ArrayList<>();
    private List<String> startingMarks = new ArrayList<>();
    private List<MasteryMarkDefinition> marks = new ArrayList<>();

    public static FloorDefinition create(String id, String displayName) {
        return new FloorDefinition(id, displayName);
    }

    private FloorDefinition(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public List<String> getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(List<String> roomIds) {
        this.roomIds = roomIds;
    }

    public List<MasteryMarkDefinition> marks() {
        if (marks == null) {
            marks = new ArrayList<>();
        }
        return marks;
    }

    public void setMarks(List<MasteryMarkDefinition> marks) {
        this.marks = new ArrayList<>();
        if (marks == null) {
            return;
        }
        for (MasteryMarkDefinition mark : marks) {
            if (mark != null) {
                this.marks.add(mark);
            }
        }
    }

    public void setAscentThreshold(int ascentThreshold) {
        this.ascentThreshold = Math.max(ascentThreshold, 0);
    }

    public int ascentThreshold() {
        return ascentThreshold;
    }

    public List<String> startingSpells() {
        if (startingSpells == null) {
            startingSpells = new ArrayList<>();
        }
        return startingSpells;
    }

    public void setStartingSpells(List<String> startingSpells) {
        this.startingSpells = copyIds(startingSpells);
    }

    public List<String> startingMarks() {
        if (startingMarks == null) {
            startingMarks = new ArrayList<>();
        }
        return startingMarks;
    }

    public void setStartingMarks(List<String> startingMarks) {
        this.startingMarks = copyIds(startingMarks);
    }

    public List<String> grantedAtStart() {
        var defined = new LinkedHashSet<String>();
        for (MasteryMarkDefinition mark : marks()) {
            if (mark != null && !mark.id().isBlank()) {
                defined.add(mark.id());
            }
        }
        var granted = new ArrayList<String>();
        for (String markId : startingMarks()) {
            if (defined.contains(markId)) {
                granted.add(markId);
            }
        }
        return List.copyOf(granted);
    }

    public int markCount() {
        return marks().size();
    }

    public boolean ascentUnlocked(int earnedCount) {
        return ascentThreshold > 0 && earnedCount >= ascentThreshold;
    }

    public String displayName() {
        return displayName == null ? "" : displayName;
    }

    public String id() {
        return id;
    }

    public int roomCount() {
        return roomIds == null ? 0 : roomIds.size();
    }

    private static List<String> copyIds(List<String> ids) {
        var copy = new ArrayList<String>();
        if (ids == null) {
            return copy;
        }
        for (String id : ids) {
            if (id != null && !id.isBlank() && !copy.contains(id)) {
                copy.add(id);
            }
        }
        return copy;
    }
}
