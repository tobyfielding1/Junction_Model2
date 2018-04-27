package base;

import java.util.ArrayList;
import java.util.Collection;

public class LaneSegment {

    public double targetSpeed;
    double length; // in meters
    ArrayList<LaneSegment> successors; //next lane segments connected to this one
    public Collection<RoadObject> roadObjects; //any object that is in this lane segment

    public LaneSegment(double targetSpeed, double length) {
        this.targetSpeed = targetSpeed;
        this.length = length;
        this.roadObjects = new ArrayList<RoadObject>();
        this.successors = new ArrayList<LaneSegment>();
    }

    public double getTargetSpeed() {
        return targetSpeed;
    }

    public void setTargetSpeed(int targetSpeed) {
        this.targetSpeed = targetSpeed;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public Collection<LaneSegment> getSuccessors() {
        return successors;
    }

    public Collection<RoadObject> getRoadObjects() {
        return roadObjects;
    }

    public boolean addSuccessor(LaneSegment x) {
        return successors.add(x);
    }

    public boolean addRoadObject(RoadObject x) {
        return roadObjects.add(x);
    }

    double getPosFromDistEnd(double distEnd) {
        return this.length - distEnd;
    }

}
