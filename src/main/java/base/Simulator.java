package base;

import com.opencsv.bean.CsvBindByName;
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
    @CsvBindByName
    String durationString; // in s
    //output parameters
    @CsvBindByName
    double maxThroughput; //vehicles leaving per minute
    @CsvBindByName
    double averageDelay;
    @CsvBindByName
    double averageFuel;
    @CsvBindByName
    double averageCo2;
    @CsvBindByName
    double maxInput;
    @CsvBindByName
    double averageElapsedTime;
    @CsvBindByName
    String name;

    double duration; // in s
    List<Double> vehicleDelay = new ArrayList<Double>();
    List<Double> vehicleElapsedTime = new ArrayList<Double>();
    List<Double> vehicleFuel = new ArrayList<Double>();
    List<Double> vehicleCo2 = new ArrayList<Double>();
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
        for (Class vehicleClass : reflections.getSubTypesOf(Vehicle.class)) {
            try {
                vehicleClasses.put((Integer) vehicleClass.getField("percent").get(null) + Math.random(), vehicleClass);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
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

    public double getAverageFuel() {
        return averageFuel;
    }

    public double getAverageCo2() {
        return averageCo2;
    }

    public double getAverageElapsedTime() {
        return averageElapsedTime;
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

    public Vehicle createVehicle(double pos, LaneSegment segment, ArrayList<LaneSegment> route, Simulator s, double v) {

        int p = (new Random()).nextInt(100);
        int cumulativeProbability = 0;
        for (Double i : vehicleClasses.keySet()) {
            try {
                cumulativeProbability += i;

                if (p <= cumulativeProbability) {
                    Constructor<?> cons = vehicleClasses.get(i).getConstructor(double.class, LaneSegment.class, ArrayList.class, Simulator.class, double.class);
                    return (Vehicle) cons.newInstance(0, segment, generateRoute(segment), s, segment.targetSpeed);
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
        buildSegments();
        setDuration(durationString);
        while (time <= duration) {
            this.nextStep();
            time += timeStep;
        }

        averageElapsedTime = vehicleElapsedTime.stream().mapToDouble(val -> val).average().getAsDouble();
        averageDelay = vehicleDelay.stream().mapToDouble(val -> val).average().getAsDouble();
        averageFuel = vehicleFuel.stream().mapToDouble(val -> val).average().getAsDouble();
        averageCo2 = vehicleCo2.stream().mapToDouble(val -> val).average().getAsDouble();

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

        vehicleElapsedTime.add(v.elapsedTime);
        vehicleDelay.add(v.getDelay());
        vehicleFuel.add(v.fuelUsed);
        vehicleCo2.add(v.Co2Produced);
        distTimeGraph.addSeries(v.distTime);

        processSpecificData(v);
    }

    public abstract void processSpecificData(Vehicle v);

}
