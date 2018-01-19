package base;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class Simulator {

    private long startTime = System.currentTimeMillis();

    String name;
    public double timeStep; // in s
    double time; // in s
    public LaneSegment[] laneSegments = {};
    public ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
    double duration; // in s
    public double[] vehicleCreationRates; // vehicles per second
    final static DateFormat dateFormat = new SimpleDateFormat("mm:ss");
    Vehicle[] spawnQueue;
    List<Double> vehicleDelay = new ArrayList<Double>();
    int throughput = 0; // count of vehicles leaving
    double throughputRate; //vehicles leaving per minute
    double lastThroughputCheckTime = 0; //when was the last time throughput was measured

    DistTimeGraph distTimeGraph = new DistTimeGraph("graph window");


    public String getTimeString(double time){
        return dateFormat.format(new Date(TimeUnit.SECONDS.toMillis((long)time)));
    }

    public double mphToMps(int speed){
        return speed * 0.44704;
    }

    public void setDuration(String s){
        String[] tokens = s.split(":");
        duration = 3600 * Double.parseDouble(tokens[0]) + 60 * Double.parseDouble(tokens[1]) + Double.parseDouble(tokens[2]);
    }



    public Simulator(String name, double timeStep, String duration, double[] vehicleCreationRates) {
        this.name = name;
        this.timeStep = timeStep;
        setDuration(duration);
        this.vehicleCreationRates = vehicleCreationRates;
        spawnQueue = new Vehicle[vehicleCreationRates.length];
        buildSegments();
    }

    public void simulate(){
        while (time <= duration){
            this.nextStep();
            time += timeStep;
        }

        System.out.println("simulation completed after running for " + getTimeString(time));
        System.out.println("average delay: " + vehicleDelay.stream().mapToDouble(val -> val).average().getAsDouble());
        System.out.println("max throughput: " + throughputRate);
        //System.out.println("Runtime: " + (System.currentTimeMillis() - startTime)/new Double(1000));
        //distTimeGraph.showChart("distance time graph for " + this.name + ", vehicle-in rate: "+ this.vehicleCreationRates[0] + "/s");
        //distTimeGraph.pack();
        //distTimeGraph.setVisible(true);
    }

    void nextStep(){
        //logic for adding vehicles
        for (int startSegment = 0; startSegment<vehicleCreationRates.length; startSegment++) {
            Vehicle v;
            if (spawnQueue[startSegment] != null){
                v = spawnQueue[startSegment];
                spawnQueue[startSegment] = null;
            }else
                v = this.generateVehicle(startSegment);

            if (v != null) {
                Vehicle virtualVehicle = v.makeCopy();
                virtualVehicle.updateAccel(virtualVehicle.simulator.timeStep);
                if (virtualVehicle.a >= 0) {
                    laneSegments[startSegment].addRoadObject(v);
                    vehicles.add(v);
                } else {
                    spawnQueue[startSegment] = v;
                }
            }
        }
        //update each vehicle in the model
        for (int i = 0; i< vehicles.size(); i++)
            vehicles.get(i).nextStep(timeStep);
    }

    public abstract void buildSegments();

    public abstract Vehicle generateVehicle(int startSegment);

    public ArrayList<LaneSegment> generateRoute(LaneSegment ls){
        if (ls.successors.size() <= 0)
            return new ArrayList<LaneSegment>();

        LaneSegment lsNext = ls.successors.get((int)(Math.random() * ls.successors.size()-1));
        ArrayList<LaneSegment> list = new ArrayList<LaneSegment>();
        list.add(lsNext);
        list.addAll(generateRoute(lsNext));
        return list;
    }

    public void finishVehicle(Vehicle v){
        vehicles.remove(v);
        //System.out.println("vehicle finished at " + getTimeString(v.localTime) +". There are this many vehicles in the network: "+ vehicles.size());
        processData(v);
    }

    private void processData(Vehicle v) {
        if (time - lastThroughputCheckTime > 60 && throughput > throughputRate){
            throughputRate = throughput;
            throughput = 0;
        }
        throughput++;
        vehicleDelay.add(v.elapsedTime);


        distTimeGraph.addSeries(v.distTime);
    }


}
