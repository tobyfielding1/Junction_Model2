package m;

import base.CriticalSegment;
import base.LaneSegment;
import base.Simulator;
import base.Vehicle;
import com.opencsv.bean.CsvBindByName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TJunctionSim extends Simulator {
    @CsvBindByName
    double seg1InputRate; // cars per second

    @CsvBindByName
    double seg2InputRate;

    @CsvBindByName
    double seg0InputRate;

    double[] spawnTimer = {0, 0, 0}; // starts as all zeroes, size equals number of start segments

    double[] inputRates = {0, 0, 0};

    public double getSeg1InputRate() {
        return seg1InputRate;
    }

    public double getSeg0InputRate() {
        return seg0InputRate;
    }

    public double getSeg2InputRate() {
        return seg0InputRate;
    }

    @Override
    public List<Vehicle> generateVehicles() {
        this.inputRates[0] = seg0InputRate;
        this.inputRates[1] = seg1InputRate;
        this.inputRates[2] = seg2InputRate;

        List<Vehicle> newVehicles = new LinkedList<Vehicle>();
        for (int startSegment = 0; startSegment < spawnTimer.length; startSegment++) {
            if (Math.random() < inputRates[startSegment] * timeStep && time - spawnTimer[startSegment] > 1) {
                spawnTimer[startSegment] = time;
                newVehicles.add(this.createVehicle(startSegment));
            }
        }
        return newVehicles;
    }

    @Override
    public void processSpecificData(Vehicle v) {
        //if (v.routeArchive.contains(laneSegments[1]))
        distTimeGraph.addSeries(v.distTime);
    }

    @Override
    public void buildSegments() {
        int noSegments = 9;
        LaneSegment[] segments = new LaneSegment[noSegments];

        for (int i = 0; i < 6; i++)
            segments[i] = new LaneSegment(15, 150);

        segments[6] = new CriticalSegment(new ArrayList<LaneSegment>(Arrays.asList(segments[2])), 3);
        segments[7] = new CriticalSegment(new ArrayList<LaneSegment>(Arrays.asList(segments[8])), 3);
        segments[8] = new CriticalSegment(new ArrayList<LaneSegment>(Arrays.asList(segments[1])), 3);

        segments[0].addSuccessor(segments[7]);
        segments[1].addSuccessor(segments[8]);
        segments[2].addSuccessor(segments[6]);

        segments[6].addSuccessor(segments[5]);
        segments[6].addSuccessor(segments[8]);
        segments[7].addSuccessor(segments[3]);
        segments[7].addSuccessor(segments[6]);
        segments[8].addSuccessor(segments[4]);
        segments[8].addSuccessor(segments[7]);

        laneSegments = segments;

        maxContested = 2;
    }
}
