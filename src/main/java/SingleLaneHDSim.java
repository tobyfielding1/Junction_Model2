import base.LaneSegment;
import base.Vehicle;

import java.util.LinkedList;
import java.util.List;

public class SingleLaneHDSim extends SingleLaneAVSim {

    @Override
    public List<Vehicle> generateVehicles() {
        List<Vehicle> newVehicles = new LinkedList<Vehicle>();

        for (int startSegment = 0; startSegment < spawnTimer.length; startSegment++) {
            if (Math.random() < seg1InputRate * timeStep && time - spawnTimer[startSegment] > HDCar.T) {
                spawnTimer[startSegment] = time;
                LaneSegment ls = laneSegments[startSegment];
                newVehicles.add(new HDCar(0, ls, generateRoute(ls), this, ls.targetSpeed));
            }
        }
        return newVehicles;
    }
}
