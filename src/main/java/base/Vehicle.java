package base;

import java.util.ArrayList;
import java.util.List;

public abstract class Vehicle extends RoadObject implements Cloneable {

    public double fuelUsed = 0; // (mL)
    public double Co2Produced = 0; // (g)

    public RoadObject objectAhead;
    public double objectAheadDist;
    ArrayList<LaneSegment> route;
    double localTime;
    double elapsedTime = 0;
    double elapsedDist = 0;
    public List<CriticalApproach> criticalApproaches;
    public FollowingModel idm;
    int source;

    List<Double[]> distTime = new ArrayList<Double[]>();
    double criticalBraking = 0;
    double limitDist = 100; //critical approach distance. (from start of critical segment)

    public void nextStep(double timeStep) {
        addCriticalAhead();
        //recordStats(timeStep);
        updateAccel(timeStep);
        if (a < -this.getB())
            criticalBraking += (-a - this.getB()) / 10;
        updateMotion(timeStep);
        updateEnergy(timeStep);
        localTime += timeStep;
        elapsedTime += timeStep;
        try {
            Object test = this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public Vehicle() {
    }

    /*
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
    */

    public Vehicle(ArrayList<LaneSegment> route, Simulator s, double v, FollowingModel idm, int source) {
        super(0, s.laneSegments[source], s);
        this.route = route;
        this.localTime = s.time;
        this.v = v;
        this.source = source;
        this.idm = idm;
    }

    /**
     * record every nth step
     */
    public void recordDistTime(double timeStep) {
        distTime.add(new Double[]{elapsedDist, localTime});
    }

    public boolean getObjectAhead() {
        objectAhead = this.segment.roadObjects.stream().filter(ro -> (ro.pos > this.pos)).sorted().findFirst().orElse(null);
        if (objectAhead == null) {
            objectAheadDist = this.segment.length - this.pos;
            getObjectAhead((ArrayList<LaneSegment>) route.clone());
        } else
            objectAheadDist = objectAhead.pos - length - this.pos;

        if (objectAheadDist < 0)
            objectAheadDist = 0;

        if (objectAhead != null)
            return true;
        else {
            objectAheadDist = 100000;
            return false;
        }
    }

    protected abstract double getB();

    //change this to record different aspects
    private void recordStats(double timeStep) {
        recordDistTime(timeStep);
    }

    public double getDelay() {
        return elapsedTime - elapsedDist / 15;
    }

    public void addCriticalAhead() {
        criticalApproaches = new ArrayList<CriticalApproach>();
        addCriticalAhead((ArrayList<LaneSegment>) (this.route.clone()), this.segment.length - this.pos);
    }

    void getObjectAhead(ArrayList<LaneSegment> rte) {
        if (rte.size() == 0)
            return;
        LaneSegment thisSegment = rte.remove(0);
        objectAhead = thisSegment.roadObjects.stream().sorted().findFirst().orElse(null);
        if (objectAhead == null) {
            objectAheadDist += thisSegment.length;
            getObjectAhead(rte);
        } else
            objectAheadDist += objectAhead.pos - length;
    }

    private void addCriticalAhead(ArrayList<LaneSegment> rte, double dist) {
        if (dist >= limitDist)
            return;

        LaneSegment thisSegment = rte.remove(0);
        if (thisSegment.getClass().isInstance(CriticalSegment.class))
            criticalApproaches.add(new CriticalApproach(thisSegment, dist));
        dist += thisSegment.length;

        addCriticalAhead(rte, dist);
    }

    public void updateAccel(double timeStep) {
        this.getObjectAhead();
        double realA = a = idm.calcAcc(objectAhead, this, this.objectAheadDist);
        if (!criticalApproaches.isEmpty()) {
            double stoppingA = 100000;
            double virtualA = 100000;
            for (CriticalApproach approach : criticalApproaches) {
                if (approach.stoppingA < stoppingA)
                    stoppingA = approach.stoppingA;
                if (approach.virtualA < virtualA)
                    virtualA = approach.virtualA;
            }
            this.a = Math.max(stoppingA, Math.min(realA, virtualA));
        }
    }

    private void updateMotion(double timeStep) {
        double newV = v + a * timeStep;
        double dist;
        // prevents negative velocity
        if (v + a * timeStep >= 0) {
            dist = v * timeStep + 0.5 * a * Math.pow(timeStep, 2);
            this.v = newV;
        } else {
            dist = -(0.5 * Math.pow(v, 2)) / a;
            this.v = 0;
        }
        elapsedDist += dist;
        pos += dist;

        //if vehicle has moved to the next base.LaneSegment or off model
        if (pos > this.segment.length) {
            this.segment.roadObjects.remove(this);
            this.pos = pos - segment.length;
            if (route.size() > 0) {
                this.segment = route.remove(0);
                this.segment.addRoadObject(this);
            } else { //driven off model
                this.simulator.finishVehicle(this);
            }

        }
    }

    public void updateEnergy(double timeStep) {
        double pi = getMass() / 1000 * a * v; // inertia component of total power (kW)
        double pc = getB1() * v + getB2() * Math.pow(v, 3); // cruise component of total power (kW)
        double ft = getFi() / 3600 + getBeta() * (pc + pi); //instantaneous fuel consumption rate (mL/s),
        fuelUsed += ft * timeStep;
        Co2Produced += ft * getfCo2() * timeStep;
    }

    public abstract double getMass();

    public abstract double getB1();

    public abstract double getB2();

    public abstract double getFi();

    public abstract double getBeta();

    public abstract double getfCo2();

    class CriticalApproach implements Comparable<CriticalApproach> {

        Vehicle owner;
        LaneSegment criticalSegment;
        double distanceToCritical;
        double stoppingA;
        double virtualA;

        public CriticalApproach(LaneSegment criticalSegment, double distanceToCritical) {
            this.criticalSegment = criticalSegment;
            this.distanceToCritical = distanceToCritical;
            owner = Vehicle.this;

            stoppingA = idm.calcAcc(new RoadObject(0, criticalSegment, simulator), Vehicle.this, distanceToCritical);

            List<CriticalApproach> allCriticalApproaches = new ArrayList<CriticalApproach>();
            simulator.vehicles.forEach(v -> allCriticalApproaches.addAll(v.criticalApproaches));

            CriticalApproach leader = allCriticalApproaches.stream().filter(ca -> ca.criticalSegment.equals(this.criticalSegment) && ca.distanceToCritical < this.distanceToCritical).sorted().findFirst().orElse(null);

            if (leader == null)
                virtualA = 1000000;
            else
                virtualA = idm.calcAcc(leader.owner, this.owner, this.distanceToCritical - leader.distanceToCritical - length);
        }

        @Override
        public int compareTo(CriticalApproach o) {
            if (this.distanceToCritical < o.distanceToCritical) return 1;
            if (this.distanceToCritical > o.distanceToCritical) return -1;
            return 0;
        }

    }

}
