import base.LaneSegment;
import base.Model;
import base.Vehicle;

public class SingleLaneAVSimVariableSpeed extends SingleLaneAVSim {

    public SingleLaneAVSimVariableSpeed(String name, double timeStep, String duration, double[] vehicleCreationRates) {
        super(name, timeStep, duration, vehicleCreationRates);
    }

    @Override
    public void addVehicles(){
        for (int i = 0;i<vehicleCreationRates.length; i++){
            if(Math.random() < vehicleCreationRates[i]*timeStep){
                LaneSegment ls = model.laneSegments[i];
                double topSpeed = mphToMps(100);

                Vehicle vehicle = new AV1(0,ls,generateRoute(ls),this, s1, new IDM( topSpeed,a,b,T,s0,delta,0));
                model.addVehicle(i, vehicle);
                vehicles.add(vehicle);
            }
        }
    }

    @Override
    public void buildSegments() {
        model = new Model("single lane AV's");
        int noSegments = 5;
        LaneSegment[] segments = new LaneSegment[noSegments];

        for (int i = 0;i<noSegments; i++)
            segments[i] = new LaneSegment( 30,300);

        for (int i = 0;i<noSegments-1; i++)
            segments[i].addSuccessor(segments[i+1]);

        model.laneSegments = segments;
    }
}
