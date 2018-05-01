package base;

import java.util.List;

public class CriticalSegment extends LaneSegment {

    public List<LaneSegment> priorityApproaches;
    public double decisionZone;

    public CriticalSegment(List<LaneSegment> priorityApproaches, double decisionZone) {
        super(7.5, 5);
        this.priorityApproaches = priorityApproaches;
        this.decisionZone = decisionZone;
    }

}
