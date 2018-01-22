import base.LaneSegment;
import base.Simulator;
import base.Vehicle;
import com.opencsv.bean.CsvBindByName;

import java.util.ArrayList;

public class AVCar extends Vehicle {

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

    IDM idm;

    public AVCar() {
    }

    public AVCar(double pos, LaneSegment segment, ArrayList<LaneSegment> route, Simulator s, double v) {
        super(pos, segment, route, s, v);
        this.idm = new IDM(v0, a0, b, T, s0, s1);
    }

    public AVCar(AVCar other) {
        super(other);
        this.idm = other.idm;
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
    protected Vehicle makeCopy() {
        return new AVCar(this);
    }

    @Override
    public void updateAccel(double timeStep) {
        if (this.getObjectAhead())
            a = idm.calcAcc(this, objectAhead.v);
        else
            a = idm.calcAcc(this, 0);
    }
}
