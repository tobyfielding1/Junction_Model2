import base.LaneSegment;
import base.Simulator;
import base.Vehicle;
import com.opencsv.bean.CsvBindByName;

import java.util.LinkedList;
import java.util.List;

public class SingleLaneAVSim extends Simulator {

    @CsvBindByName
    double seg1InputRate; // cars per second

    double[] spawnTimer = {0};

    public SingleLaneAVSim() {
    }

    public double getSeg1InputRate() {
        return seg1InputRate;
    }

    @Override
    public List<Vehicle> generateVehicles() {
        List<Vehicle> newVehicles = new LinkedList<Vehicle>();

        for (int startSegment = 0; startSegment < spawnTimer.length; startSegment++) {
            if (Math.random() < seg1InputRate * timeStep && time - spawnTimer[startSegment] > AV1.T) {
                spawnTimer[startSegment] = time;
                LaneSegment ls = laneSegments[startSegment];
                newVehicles.add(new AV1(0, ls, generateRoute(ls), this, ls.targetSpeed));
            }
        }
        return newVehicles;
    }

    @Override
    public void processSpecificData(Vehicle v) {

    }

    @Override
    public void buildSegments() {
        int noSegments = 5;
        LaneSegment[] segments = new LaneSegment[noSegments];

        for (int i = 0; i < noSegments; i++)
            segments[i] = new LaneSegment(15, 300);

        for (int i = 0; i < noSegments - 1; i++)
            segments[i].addSuccessor(segments[i + 1]);

        laneSegments = segments;
    }
}
