package base;

public class RoadObject implements Comparable<RoadObject>{

    public double v = 0;
    public double a = 0;
    double length = 0;
    Simulator simulator;

    double pos; //position of obj in m from start of base.LaneSegment
    public LaneSegment segment;

    public RoadObject(double pos, LaneSegment segment, Simulator s) {
        this.pos = pos;
        this.segment = segment;
        this.simulator = s;
    }

    @Override
    public int compareTo(RoadObject o) {
        if (this.pos > o.pos) return 1;
        if (this.pos < o.pos) return -1;
        return 0;
    }

}
