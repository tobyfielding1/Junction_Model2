import base.LaneSegment;
import base.Simulator;
import com.opencsv.bean.CsvBindByName;

import java.util.ArrayList;

public class HDCar extends AVCar {

    @CsvBindByName
    static double p;
    @CsvBindByName
    static double T1;
    @CsvBindByName
    static double T2;

    public HDCar() {
    }

    public HDCar(double pos, LaneSegment segment, ArrayList<LaneSegment> route, Simulator s, double v) {
        super(pos, segment, route, s, v);
        this.idm = new TwoDimIDM(v0, a0, b, T, s0, s1, T1, T2, p, s.timeStep);
    }
}
