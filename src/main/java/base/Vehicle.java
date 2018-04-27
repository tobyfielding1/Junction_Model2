package base;

import m.TwoDimACC;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Vehicle extends RoadObject {

    public double fuelUsed = 0; // (mL)
    public double Co2Produced = 0; // (g)

    public RoadObject objectAhead;
    public double objectAheadDist;
    ArrayList<LaneSegment> route;
    double localTime;
    double elapsedTime = 0;
    double elapsedDist = 0;
    public SortedSet<CriticalApproach> criticalApproaches = new TreeSet<CriticalApproach>();
    public FollowingModel idm;
    int source;
    public ArrayList<LaneSegment> routeArchive = new ArrayList<LaneSegment>();
    double limitDist = 100; //critical approach distance. (from start of critical segment)
    //public List<Double[]> distTime = new ArrayList<Double[]>();
    double criticalBraking = 0;
    boolean started = false;

    public Vehicle(ArrayList<LaneSegment> route, Simulator s, double v, FollowingModel idm, int source) {
        super(-20, s.laneSegments[source], s);
        this.route = route;
        this.localTime = s.time;
        this.v = v;
        this.source = source;
        this.idm = idm;
    }

    public Vehicle() {
    }

    public void nextStep(double timeStep) {
        addCriticalAhead();
        updateAccel(timeStep);
        updateMotion(timeStep);
        if (a < -this.getB())
            criticalBraking += (-a - this.getB()) / 10;
        recordStats(timeStep);
        updateEnergy(timeStep);
        localTime += timeStep;
        elapsedTime += timeStep;

        if (elapsedDist > 20 && !started) {
            started = true;
            elapsedTime = criticalBraking = fuelUsed = Co2Produced = 0;
            //distTime = new ArrayList<Double[]>();
            elapsedDist -= 20;
        }
    }

    /**
     * record every nth step

    public void recordDistTime(double timeStep) {
        distTime.add(new Double[]{elapsedDist, localTime});
    }
     */

    public boolean getObjectAhead() {
        objectAhead = this.segment.roadObjects.stream().filter(ro -> (ro.pos > this.pos)).sorted().findFirst().orElse(null);
        if (objectAhead == null) {
            objectAheadDist = this.segment.length - this.pos;
            getObjectAhead((ArrayList<LaneSegment>) route.clone());
        } else
            objectAheadDist = objectAhead.pos - objectAhead.getLength() - this.pos;

        //if (objectAheadDist < 0)
        //objectAheadDist = 0;

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
        //recordDistTime(timeStep);
    }

    public double getDelay() {
        return elapsedTime - elapsedDist / 15;
    }

    public void addCriticalAhead() {
        criticalApproaches = new TreeSet<CriticalApproach>();
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
        } else {
            objectAheadDist += objectAhead.pos - objectAhead.getLength();
        }
    }

    private void addCriticalAhead(ArrayList<LaneSegment> rte, double dist) {
        if (dist >= limitDist || rte.size() == 0)
            return;

        LaneSegment thisSegment = rte.remove(0);
        if (thisSegment instanceof CriticalSegment) {
            CriticalApproach ca = new CriticalApproach((CriticalSegment) thisSegment, dist);
            criticalApproaches.add(ca);
            ca.generateCompetition();
        }
        dist += thisSegment.length;

        addCriticalAhead(rte, dist);
    }

    public void updateAccel(double timeStep) {
        if (!(idm instanceof TwoDimACC) && !criticalApproaches.isEmpty())
            idm.setAlphaA(simulator.alphaA);
        else
            idm.setAlphaA(1);

        this.getObjectAhead();
        if (objectAhead != null) {
            a = idm.calcAcc(objectAhead, this, this.objectAheadDist);
        } else
            a = idm.calcAcc(new RoadObject(), this, 100000);

        if (!criticalApproaches.isEmpty()) {
            double lowestA = 100000;

            double resultA = a;
            for (CriticalApproach approach : criticalApproaches) {

                //if (!criticalApproaches.isEmpty() && criticalApproaches.last().criticalSegment.roadObjects.contains(objectAhead))
                //resultA = Math.max(a, approach.getStoppingA());
                if (simulator.interleaving && !(this.idm instanceof TwoDimACC)) {
                    resultA = Math.min(a, Math.max(approach.getVirtualA(), -this.getB()) * ((limitDist - approach.distanceToCritical) / limitDist));
                    if (!this.segment.roadObjects.contains(objectAhead) && objectAhead instanceof Vehicle && !((Vehicle) objectAhead).routeArchive.contains(this.segment) || approach.cantInterleaveBehind())
                        resultA = Math.max(resultA, approach.getStoppingA());
                } else if (approach.priority)
                    resultA = a;
                else if (this.segment.roadObjects.contains(objectAhead))
                    resultA = a;
                else if (!approach.decisionZone()) {
                    if (objectAhead instanceof Vehicle && ((Vehicle) objectAhead).routeArchive.contains(this.segment))
                        resultA = Math.min(a, approach.getStoppingA());
                    else
                        resultA = approach.getStoppingA();
                } else if (approach.decisionZone()) {
                    if (approach.clearBehind()) {
                        if (objectAhead instanceof Vehicle && ((Vehicle) objectAhead).routeArchive.contains(this.segment))
                            resultA = a;
                        else
                            resultA = Math.max(approach.getStoppingA(), Math.min(a, approach.getVirtualA()));
                    } else {
                        if (objectAhead instanceof Vehicle && ((Vehicle) objectAhead).routeArchive.contains(this.segment))
                            resultA = Math.min(a, approach.getStoppingA());
                        else
                            resultA = approach.getStoppingA();
                    }
                }
                if (resultA < lowestA)
                    lowestA = resultA;
            }
            a = lowestA;
        }
    }

    private void updateMotion(double timeStep) {
        double newV = v + a * timeStep;
        double dist;
        if (v < 0.1)
            v = 0;
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
            this.pos = pos - this.segment.length;
            if (route.size() > 0) {
                routeArchive.add(this.segment);
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
        CriticalSegment criticalSegment;
        boolean priority;
        double distanceToCritical;
        List<CriticalApproach> competingCriticalApproaches;

        public CriticalApproach(CriticalSegment criticalSegment, double distanceToCritical) {
            this.criticalSegment = criticalSegment;
            this.distanceToCritical = distanceToCritical;
            owner = Vehicle.this;

            if (criticalSegment.priorityApproaches.contains(segment))
                priority = true;
        }

        public boolean cantInterleaveBehind() {
            CriticalApproach follower = competingCriticalApproaches.stream().filter(ca -> ca.distanceToCritical > this.distanceToCritical).sorted(Comparator.reverseOrder()).findFirst().orElse(null);
            if (follower == null)
                return false;
            return (follower.owner.idm instanceof TwoDimACC && follower.priority && !clearBehind());
        }

        public void generateCompetition() {
            List<CriticalApproach> allCriticalApproaches = new ArrayList<CriticalApproach>();
            simulator.vehicles.forEach(v -> allCriticalApproaches.addAll(v.criticalApproaches));
            competingCriticalApproaches = allCriticalApproaches.stream().filter(ca -> ca.criticalSegment.equals(criticalSegment)).filter(ca -> !ca.owner.segment.equals(this.owner.segment)).collect(Collectors.toList());
        }

        @Override
        public int compareTo(CriticalApproach o) {
            if (this.distanceToCritical < o.distanceToCritical) return 1;
            if (this.distanceToCritical > o.distanceToCritical) return -1;
            if (this.priority)
                return 1;
            else
                return -1;
        }

        double getVirtualA() {
            CriticalApproach leader;
            leader = competingCriticalApproaches.stream().filter(ca -> ca.distanceToCritical < this.distanceToCritical + Math.random() / 2).sorted().findFirst().orElse(null);

            if (leader == null)
                return idm.calcAcc(new RoadObject(), this.owner, 1000000);
            else
                return idm.calcAcc(leader.owner, this.owner, this.distanceToCritical - leader.distanceToCritical - leader.owner.getLength());
        }

        double getStoppingA() {
            if (distanceToCritical < 0.1)
                return 0;
            else
                return idm.calcAcc(new RoadObject(0, criticalSegment, simulator), Vehicle.this, distanceToCritical - 0.1);
        }

        boolean clearBehind() {
            CriticalApproach follower = competingCriticalApproaches.stream().filter(ca -> ca.distanceToCritical > this.distanceToCritical).sorted(Comparator.reverseOrder()).findFirst().orElse(null);
            if (follower == null)
                return true;
            return (follower.owner.idm.calcAcc(this.owner, follower.owner, follower.distanceToCritical
                    - this.distanceToCritical - owner.getLength()) >= owner.a && follower.distanceToCritical
                    - this.distanceToCritical - owner.getLength() > 2 && follower.distanceToCritical / follower.owner.v > 1);
        }

        boolean decisionZone() {
            return (distanceToCritical <= criticalSegment.decisionZone);
        }

    }

}
