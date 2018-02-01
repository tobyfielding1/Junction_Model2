package base;

import com.opencsv.bean.CsvBindByName;
import m.AVCar;
import m.AVHGV;
import m.HDCar;
import m.HDHGV;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class Simulator implements Runnable {
    final static DateFormat dateFormat = new SimpleDateFormat("mm:ss");
    //input parameters
    @CsvBindByName
    public double timeStep; // in s
    public double time = 0; // in s
    public LaneSegment[] laneSegments = {};
    public ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
    public ArrayList<Vehicle> finishedVehicles = new ArrayList<Vehicle>();
    @CsvBindByName
    String durationString; // in s
    //output parameters
    @CsvBindByName
    double maxThroughput; //vehicles leaving per minute
    @CsvBindByName
    double averageDelay;
    @CsvBindByName
    double averageCo2;
    @CsvBindByName
    double maxInput;
    @CsvBindByName
    String name;
    @CsvBindByName
    double averageCriticalEvents; //count of breaking greater than B.

    @CsvBindByName
    double AVAverageCriticalEvents;
    @CsvBindByName
    double AVAverageCo2;
    @CsvBindByName
    double AVAverageDelay;

    @CsvBindByName
    double HDAverageCriticalEvents;
    @CsvBindByName
    double HDAverageCo2;
    @CsvBindByName
    double HDAverageDelay;

    @CsvBindByName
    Integer HDCarPercent;
    @CsvBindByName
    Integer HDHGVPercent;
    @CsvBindByName
    Integer AVCarPercent;
    @CsvBindByName
    Integer AVHGVPercent;


    double duration; // in s
    int output = 0; // count of vehicles leaving
    int input = 0; // count of vehicles entering
    double lastThroughputCheckTime = 300; //when was the last time output was measured
    double lastInputCheckTime = 300;
    DistTimeGraph distTimeGraph = new DistTimeGraph("graph window");
    private long startTime = System.currentTimeMillis();
    Map<Double, Class<? extends Vehicle>> vehicleClasses;


    public Simulator() {
        Reflections reflections = new Reflections("");
        vehicleClasses = new TreeMap<Double, Class<? extends Vehicle>>();
    }

    public double getAVAverageCriticalEvents() {
        return AVAverageCriticalEvents;
    }

    public double getAVAverageCo2() {
        return AVAverageCo2;
    }

    public double getAVAverageDelay() {
        return AVAverageDelay;
    }

    public double getHDAverageCriticalEvents() {
        return HDAverageCriticalEvents;
    }

    public double getHDAverageCo2() {
        return HDAverageCo2;
    }

    public double getHDAverageDelay() {
        return HDAverageDelay;
    }

    public Integer getHDCarPercent() {
        return HDCarPercent;
    }

    public Integer getHDHGVPercent() {
        return HDHGVPercent;
    }

    public Integer getAVCarPercent() {
        return AVCarPercent;
    }

    public Integer getAVHGVPercent() {
        return AVHGVPercent;
    }

    public double getAverageCriticalEvents() {
        return averageCriticalEvents;
    }

    public double getMaxInput() {
        return maxInput;
    }

    public double getTimeStep() {
        return timeStep;
    }

    public String getDurationString() {
        return durationString;
    }

    public double getMaxThroughput() {
        return maxThroughput;
    }

    public double getAverageDelay() {
        return averageDelay;
    }

    public double getAverageCo2() {
        return averageCo2;
    }

    public String getName() {
        return this.getClass().getName();
    }

    public String getTimeString(double time) {
        return dateFormat.format(new Date(TimeUnit.SECONDS.toMillis((long) time)));
    }

    public void setDuration(String s) {
        String[] tokens = s.split(":");
        duration = 3600 * Double.parseDouble(tokens[0]) + 60 * Double.parseDouble(tokens[1]) + Double.parseDouble(tokens[2]);
    }

    public Vehicle createVehicle(int startSegment) {

        double p = Math.random() * 100;
        double cumulativeProbability = 0;
        for (Double i : vehicleClasses.keySet()) {
            try {
                cumulativeProbability += i;

                if (p <= cumulativeProbability) {
                    Constructor<?> cons = vehicleClasses.get(i).getConstructor(ArrayList.class, Simulator.class, double.class, int.class);
                    return (Vehicle) cons.newInstance(generateRoute(laneSegments[startSegment]), this, laneSegments[startSegment].targetSpeed, startSegment);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void run() {

        if (HDCarPercent > 0)
            vehicleClasses.put(this.HDCarPercent + Math.random() / 100000, HDCar.class);
        if (AVCarPercent > 0)
            vehicleClasses.put(this.AVCarPercent + Math.random() / 100000, AVCar.class);
        if (HDHGVPercent > 0)
            vehicleClasses.put(this.HDHGVPercent + Math.random() / 100000, HDHGV.class);
        if (AVHGVPercent > 0)
            vehicleClasses.put(this.AVHGVPercent + Math.random() / 100000, AVHGV.class);

        buildSegments();
        setDuration(durationString);
        while (time <= duration) {
            this.nextStep();
            time += timeStep;
        }


        averageDelay = finishedVehicles.stream().mapToDouble(veh -> veh.getDelay()).average().getAsDouble();
        averageCo2 = finishedVehicles.stream().mapToDouble(veh -> veh.Co2Produced).average().getAsDouble();
        averageCriticalEvents = finishedVehicles.stream().mapToDouble(veh -> veh.criticalEvents).average().getAsDouble();

        try {
            AVAverageDelay = finishedVehicles.stream().filter(veh -> !(veh.idm instanceof HumanModel))
                    .mapToDouble(veh -> veh.getDelay()).average().getAsDouble();
            AVAverageCo2 = finishedVehicles.stream().filter(veh -> !(veh.idm instanceof HumanModel))
                    .mapToDouble(veh -> veh.Co2Produced).average().getAsDouble();
            AVAverageCriticalEvents = finishedVehicles.stream().filter(veh -> !(veh.idm instanceof HumanModel))
                    .mapToDouble(veh -> veh.criticalEvents).average().getAsDouble();
        } catch (NoSuchElementException e) {
        }
        try {

            HDAverageDelay = finishedVehicles.stream().filter(veh -> (veh.idm instanceof HumanModel))
                    .mapToDouble(veh -> veh.getDelay()).average().getAsDouble();
            HDAverageCo2 = finishedVehicles.stream().filter(veh -> (veh.idm instanceof HumanModel))
                    .mapToDouble(veh -> veh.Co2Produced).average().getAsDouble();
            HDAverageCriticalEvents = finishedVehicles.stream().filter(veh -> (veh.idm instanceof HumanModel))
                    .mapToDouble(veh -> veh.criticalEvents).average().getAsDouble();
        } catch (NoSuchElementException e) {
        }


        System.out.println("simulation completed after running for " + getTimeString(time) + " with avg delay: " + averageDelay);

        distTimeGraph.showChart("distance time graph for " + this.getClass().getName());
        distTimeGraph.pack();
        distTimeGraph.setVisible(true);
    }

    void nextStep() {
        //logic for adding vehicles
        List<Vehicle> newVehicles = this.generateVehicles();
        for (int i = 0; i < newVehicles.size(); i++) {
            Vehicle v = newVehicles.get(i);
            double objectAheadDist;
            RoadObject objectAhead = v.segment.roadObjects.stream().sorted().findFirst().orElse(null);
            if (objectAhead != null)
                objectAheadDist = objectAhead.pos;
            else
                objectAheadDist = v.segment.getLength();
            if (objectAheadDist > RoadObject.length + RoadObject.length + 4) {
                vehicles.add(v);
                v.segment.addRoadObject(v);
            } else
                newVehicles.remove(v);
        }
        if (time - lastInputCheckTime > 60) {
            lastInputCheckTime = time;
            if (input > maxInput)
                maxInput = input;
            input = 0;
        }
        if (time > 300)
            input += newVehicles.size();
        else
            input = 0;

        //update each vehicle in the model
        for (int i = 0; i < vehicles.size(); i++)
            vehicles.get(i).nextStep(timeStep);
    }

    public abstract void buildSegments();

    public abstract List<Vehicle> generateVehicles();

    public ArrayList<LaneSegment> generateRoute(LaneSegment ls) {
        if (ls.successors.size() <= 0)
            return new ArrayList<LaneSegment>();

        LaneSegment lsNext = ls.successors.get((int) (Math.random() * ls.successors.size() - 1));
        ArrayList<LaneSegment> list = new ArrayList<LaneSegment>();
        list.add(lsNext);
        list.addAll(generateRoute(lsNext));
        return list;
    }

    public void finishVehicle(Vehicle v) {
        vehicles.remove(v);
        finishedVehicles.add(v);
        processData(v);
    }

    public void processData(Vehicle v) {
        if (time - lastThroughputCheckTime > 60) {
            lastThroughputCheckTime = time;
            if (output > maxThroughput)
                maxThroughput = output;
            output = 0;
        }
        if (time > 300)
            output++;

        distTimeGraph.addSeries(v.distTime);

        processSpecificData(v);
    }

    public abstract void processSpecificData(Vehicle v);

}
