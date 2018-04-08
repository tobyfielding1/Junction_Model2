package m;

import base.LaneSegment;
import base.Simulator;
import base.Vehicle;
import com.opencsv.bean.CsvBindByName;

import java.util.ArrayList;

public class HDHGV extends Vehicle {

    @CsvBindByName
    public static Integer percent;
    //input parameters for motion
    @CsvBindByName
    public static double v0;
    @CsvBindByName
    public static double a0;
    @CsvBindByName
    public static double b;
    @CsvBindByName
    public static double T;
    @CsvBindByName
    public static double s0;
    @CsvBindByName
    public static double s1;

    @CsvBindByName
    public static double coolness;

    @CsvBindByName
    static double p;
    @CsvBindByName
    static double T1;
    @CsvBindByName
    static double T2;


    //input params to do with energy usage
    @CsvBindByName
    static double fi; //constant idle fuel consumption rate in mL/h
    @CsvBindByName
    static double b1; //Drag fuel consumption parameter related mainly to the rolling resistance (kN)
    @CsvBindByName
    static double b2; //Drag fuel consumption parameter related mainly to the aerodynamic drag (kN/(m/s) 2)
    @CsvBindByName
    static double beta; //the efficiency parameter
    @CsvBindByName
    static double mass; //vehicle mass (kg) including occupants and any other load
    @CsvBindByName
    static double fCo2; //CO2 to Fuel Consumption Rate in grams per millilitre (kg per litre) of fuel (g/mL or kg/L)

    public HDHGV() {
    }

    public HDHGV(ArrayList<LaneSegment> route, Simulator s, double v, int source) {
        super(route, s, v, new TwoDimACC(v0, a0, b, T, s0, s1, T1, T2, p, s.timeStep, coolness), source);
    }

    @Override
    public double getFi() {
        return fi;
    }

    @Override
    public double getB1() {
        return b1;
    }

    @Override
    public double getB2() {
        return b2;
    }

    @Override
    public double getBeta() {
        return beta;
    }

    @Override
    public double getMass() {
        return mass;
    }

    @Override
    public double getfCo2() {
        return fCo2;
    }

    @Override
    protected double getB() {
        return b;
    }

}

