package tower.domain.logic;

import tower.domain.action.ActionType;
import tower.domain.data.FloorDefinition;
import tower.domain.data.MasteryMarkDefinition;
import tower.domain.state.FloorState;

import java.util.ArrayList;
import java.util.List;

public class FloorProgression {

    public List<MasteryMarkDefinition> grant(
            FloorDefinition floor, FloorState state, ActionType action, String target) {
        if (floor == null || state == null || action == null || target == null || target.isBlank()) {
            return List.of();
        }
        var granted = new ArrayList<MasteryMarkDefinition>();
        for (MasteryMarkDefinition mark : floor.marks()) {
            if (mark == null || mark.id().isBlank() || !mark.matches(action, target)) {
                continue;
            }
            if (state.addMark(mark.id())) {
                granted.add(mark);
            }
        }
        return List.copyOf(granted);
    }
}
