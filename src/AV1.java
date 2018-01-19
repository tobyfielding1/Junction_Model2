import base.LaneSegment;
import base.Simulator;
import base.Vehicle;

import java.util.ArrayList;

public class AV1 extends Vehicle {

    IDM idm;

    public AV1(double pos, LaneSegment segment, ArrayList<LaneSegment> route, Simulator s, double length, IDM idm, double v) {
        super(pos, segment, route, s, length, v);
        this.idm = idm;
    }

    public AV1(AV1 other) {
        super(other);
        this.idm = other.idm;
    }

    @Override
    protected Vehicle makeCopy() {
        return new AV1(this);
    }

    @Override
    public void updateAccel(double timeStep) {
        if (this.getObjectAhead())
            a = idm.calcAcc(this,objectAhead.v);
        else
            a = idm.calcAcc(this,0);
    }
}
