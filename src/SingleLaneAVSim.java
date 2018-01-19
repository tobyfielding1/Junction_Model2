import base.LaneSegment;
import base.Simulator;
import base.Vehicle;

public class SingleLaneAVSim extends Simulator {

    double v0 = this.mphToMps(110);
    double a = 1.5;
    double b = 1.5;
    double T = 1.6;
    double s0 = 2;
    int delta = 4;
    double s1 = 0;

    public SingleLaneAVSim(String name, double timeStep, String duration, double[] vehicleCreationRates) {
        super(name, timeStep, duration, vehicleCreationRates);
    }

    @Override
    public Vehicle generateVehicle(int startSegment){
        if(Math.random() < vehicleCreationRates[startSegment]*timeStep){
            LaneSegment ls = laneSegments[startSegment];
            Vehicle vehicle = new AV1(0,ls,generateRoute(ls),this, s1, new IDM(v0,a,b,T,s0,delta,0),ls.targetSpeed);
            return vehicle;
        }
        else
            return null;
    }

    @Override
    public void buildSegments() {
        int noSegments = 25;
        LaneSegment[] segments = new LaneSegment[noSegments];

        for (int i = 0;i<noSegments; i++)
            segments[i] = new LaneSegment(20,300);

        for (int i = 0;i<noSegments-1; i++)
            segments[i].addSuccessor(segments[i+1]);

        laneSegments = segments;
    }
}
