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

public class SignalizedCrossroadsSim extends Simulator {
    @CsvBindByName
    double seg1InputRate; // cars per second

    @CsvBindByName
    double seg2InputRate;

    @CsvBindByName
    double seg3InputRate;

    @CsvBindByName
    double seg0InputRate;

    double[] spawnTimer = {0, 0, 0, 0}; // starts as all zeroes, size equals number of start segments

    double[] inputRates = {0, 0, 0, 0};
    double tracker = 0;

    public double getSeg1InputRate() {
        return seg1InputRate;
    }

    public double getSeg0InputRate() {
        return seg0InputRate;
    }

    public double getSeg2InputRate() {
        return seg0InputRate;
    }

    public double getSeg3InputRate() {
        return seg0InputRate;
    }

    @Override
    public List<Vehicle> generateVehicles() {
        this.inputRates[0] = seg0InputRate;
        this.inputRates[1] = seg1InputRate;
        this.inputRates[2] = seg2InputRate;
        this.inputRates[3] = seg3InputRate;

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
    public void nextStep() {
        super.nextStep();
        tracker++;

        if (tracker == 25) {
            ((CriticalSegment) laneSegments[8]).priorityApproaches.remove(0);
        } else if (tracker == 31) {
            ((CriticalSegment) laneSegments[8]).priorityApproaches.add(laneSegments[9]);
            ((CriticalSegment) laneSegments[9]).priorityApproaches.add(laneSegments[0]);
        } else if (tracker == 50) {
            ((CriticalSegment) laneSegments[9]).priorityApproaches.remove(0);
        } else if (tracker == 56) {
            ((CriticalSegment) laneSegments[9]).priorityApproaches.add(laneSegments[10]);
            ((CriticalSegment) laneSegments[10]).priorityApproaches.add(laneSegments[1]);
        } else if (tracker == 75) {
            ((CriticalSegment) laneSegments[10]).priorityApproaches.remove(0);
        } else if (tracker == 81) {
            ((CriticalSegment) laneSegments[10]).priorityApproaches.add(laneSegments[11]);
            ((CriticalSegment) laneSegments[11]).priorityApproaches.add(laneSegments[2]);
        } else if (tracker == 100) {
            ((CriticalSegment) laneSegments[11]).priorityApproaches.remove(0);
            tracker = 0;
        } else if (tracker == 6) {
            ((CriticalSegment) laneSegments[11]).priorityApproaches.add(laneSegments[8]);
            ((CriticalSegment) laneSegments[8]).priorityApproaches.add(laneSegments[3]);

        }
    }

    @Override
    public void buildSegments() {
        int noSegments = 12;
        LaneSegment[] segments = new LaneSegment[noSegments];

        for (int i = 0; i < 8; i++)
            segments[i] = new LaneSegment(15, 150);

        segments[8] = new CriticalSegment(new ArrayList<LaneSegment>(Arrays.asList(segments[3])), 0);
        segments[9] = new CriticalSegment(new ArrayList<LaneSegment>(Arrays.asList(segments[10])), 0);
        segments[10] = new CriticalSegment(new ArrayList<LaneSegment>(Arrays.asList(segments[11])), 0);
        segments[11] = new CriticalSegment(new ArrayList<LaneSegment>(Arrays.asList(segments[8])), 0);

        segments[0].addSuccessor(segments[9]);
        segments[1].addSuccessor(segments[10]);
        segments[2].addSuccessor(segments[11]);
        segments[3].addSuccessor(segments[8]);

        segments[8].addSuccessor(segments[11]);
        segments[8].addSuccessor(segments[7]);
        segments[9].addSuccessor(segments[4]);
        segments[9].addSuccessor(segments[8]);
        segments[10].addSuccessor(segments[5]);
        segments[10].addSuccessor(segments[9]);
        segments[11].addSuccessor(segments[6]);
        segments[11].addSuccessor(segments[10]);

        laneSegments = segments;

        maxContested = 3;
    }
}
