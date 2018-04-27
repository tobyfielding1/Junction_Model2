package base;

import java.util.List;

public class CriticalSegment extends LaneSegment {

    List<LaneSegment> priorityApproaches;
    double decisionZone;

    public CriticalSegment(List<LaneSegment> priorityApproaches, double decisionZone) {
        super(7.5, 5);
        this.priorityApproaches = priorityApproaches;
        this.decisionZone = decisionZone;
    }

}
