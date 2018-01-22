package base;

import com.opencsv.bean.CsvBindByName;

public class RoadObject implements Comparable<RoadObject> {

    //input parameters
    @CsvBindByName
    static double length = 0;


    public double v = 0;
    public double a = 0;
    public LaneSegment segment;
    Simulator simulator;
    double pos; //position of obj in mass from start of base.LaneSegment

    public RoadObject() {
    }

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
