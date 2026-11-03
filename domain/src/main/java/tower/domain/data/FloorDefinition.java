package tower.domain.data;

import java.util.ArrayList;
import java.util.List;

public class FloorDefinition {

    private String id;
    private String displayName;
    private int ascentThreshold;
    private List<String> roomIds = new ArrayList<>();
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
}
