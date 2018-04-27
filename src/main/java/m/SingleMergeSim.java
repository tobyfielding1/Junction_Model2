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

public class SingleMergeSim extends Simulator {

    @CsvBindByName
    double seg1InputRate; // cars per second

    @CsvBindByName
    double seg0InputRate;

    double[] spawnTimer = {0, 0}; // starts as all zeroes, size equals number of start segments

    double[] inputRates = {0, 0};

    public double getSeg1InputRate() {
        return seg1InputRate;
    }

    public double getSeg0InputRate() {
        return seg0InputRate;
    }

    @Override
    public List<Vehicle> generateVehicles() {
        this.inputRates[0] = seg0InputRate;
        this.inputRates[1] = seg1InputRate;

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
        //distTimeGraph.addSeries(v.distTime);
    }

    @Override
    public void buildSegments() {
        int noSegments = 4;
        LaneSegment[] segments = new LaneSegment[noSegments];

        for (int i = 0; i < 2; i++)
            segments[i] = new LaneSegment(15, 150);

        segments[2] = new CriticalSegment(new ArrayList<LaneSegment>(Arrays.asList(segments[1])), 3);
        segments[3] = new LaneSegment(15, 150);

        for (int i = 0; i < 2; i++)
            segments[i].addSuccessor(segments[2]);

        segments[2].addSuccessor(segments[3]);

        laneSegments = segments;
    }
}