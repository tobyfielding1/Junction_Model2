package base;

import com.opencsv.bean.CsvBindByName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class Simulator implements Runnable {
    final static DateFormat dateFormat = new SimpleDateFormat("mm:ss");
    //input parameters
    @CsvBindByName
    public double timeStep; // in s
    public double time; // in s
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
    double duration; // in s
    List<Double> vehicleDelay = new ArrayList<Double>();
    List<Double> vehicleElapsedTime = new ArrayList<Double>();
    List<Double> vehicleFuel = new ArrayList<Double>();
    List<Double> vehicleCo2 = new ArrayList<Double>();
    int output = 0; // count of vehicles leaving
    int input = 0; // count of vehicles entering
    double lastThroughputCheckTime = 0; //when was the last time output was measured
    double lastInputCheckTime = 0;
    DistTimeGraph distTimeGraph = new DistTimeGraph("graph window");
    private long startTime = System.currentTimeMillis();

    public Simulator() {
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

    public String getTimeString(double time) {
        return dateFormat.format(new Date(TimeUnit.SECONDS.toMillis((long) time)));
    }

    public void setDuration(String s) {
        String[] tokens = s.split(":");
        duration = 3600 * Double.parseDouble(tokens[0]) + 60 * Double.parseDouble(tokens[1]) + Double.parseDouble(tokens[2]);
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

        //distTimeGraph.showChart("distance time graph for " + this.getClass().getName());
        //distTimeGraph.pack();
        //distTimeGraph.setVisible(true);
    }

    void nextStep() {
        //logic for adding vehicles
        List<Vehicle> newVehicles = this.generateVehicles();
        vehicles.addAll(newVehicles);
        if (time - lastInputCheckTime > 60) {
            lastInputCheckTime = time;
            if (input > maxInput) {
                maxInput = input;
                input = 0;
            }
        }
        input += newVehicles.size();

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
            if (output > maxThroughput) {
                maxThroughput = output;
                output = 0;
            }
        }
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
