package base;

import java.util.ArrayList;
import java.util.List;

public abstract class Vehicle extends RoadObject implements Cloneable {

    public RoadObject objectAhead;
    public double objectAheadDist;
    ArrayList<LaneSegment> route;
    double localTime;
    double elapsedTime = 0;
    double elapsedDist = 0;

    List<Double[]> distTime = new ArrayList<Double[]>();

    public Vehicle(Vehicle other) {
        super(other.pos, other.segment, other.simulator);
        this.objectAhead = other.objectAhead;
        this.objectAheadDist = other.objectAheadDist;
        this.route = other.route;
        this.localTime = other.localTime;
        this.elapsedTime = other.elapsedTime;
        this.elapsedDist = other.elapsedDist;
        this.distTime = other.distTime;
    }

    public Vehicle(double pos, LaneSegment segment, ArrayList<LaneSegment> route, Simulator s, double length, double v) {
        super(pos, segment, s);
        this.route = route;
        this.localTime = s.time;
        this.length = length;
        this.v = v;
    }

    protected abstract Vehicle makeCopy();

    /**
     * record every nth step
     */
    public void recordDistTime(double timeStep){
            distTime.add(new Double[]{elapsedDist, localTime});
    }

    public void nextStep(double timeStep){
        recordStats(timeStep);
        updateAccel(timeStep);
        updateMotion(timeStep);
        localTime += timeStep;
        elapsedTime += timeStep;
    }

    //change this to record different aspects
    private void recordStats(double timeStep) {
        recordDistTime(timeStep);
    }

    public boolean getObjectAhead(){
        objectAhead = this.segment.roadObjects.stream().filter(ro->(ro.pos > this.pos)).sorted().findFirst().orElse(null);
        if (objectAhead == null) {
            objectAheadDist = this.segment.length - this.pos;
            getObjectAhead((ArrayList<LaneSegment>) route.clone());
        } else
            objectAheadDist = objectAhead.pos - objectAhead.length  - this.pos;

        if (objectAheadDist < 0)
            objectAheadDist = 0;

        if (objectAhead != null)
            return true;
        else {
            objectAheadDist = 1000;
            return false;
        }
    }

    void getObjectAhead(ArrayList<LaneSegment> rte){
        if (rte.size() == 0)
            return;
        LaneSegment thisSegment = rte.remove(0);
        objectAhead = thisSegment.roadObjects.stream().sorted().findFirst().orElse(null);
        if (objectAhead == null) {
            objectAheadDist += thisSegment.length;
            getObjectAhead(rte);
        } else
            objectAheadDist += objectAhead.pos - objectAhead.length;
    }

    private void updateMotion(double timeStep) {
        double newV = v + a * timeStep;
        double dist;
        // prevents negative velocity
        if (v + a*timeStep >= 0) {
            dist = v * timeStep + 0.5 * a * Math.pow(timeStep, 2);
            this.v = newV;
        } else {
            dist = - (0.5 * Math.pow(v, 2)) / a;
            this.v = 0;
        }
        elapsedDist += dist;
        pos += dist;

        //if vehicle has moved to the next base.LaneSegment or off model
        if (pos > this.segment.length){
            this.segment.roadObjects.remove(this);
            this.pos = pos - segment.length;
            if (route.size() > 0){
                this.segment = route.remove(0);
                this.segment.addRoadObject(this);
            } else{ //driven off model
                this.simulator.finishVehicle(this);
            }

        }

    }

    public abstract void updateAccel(double timeStep);

}
