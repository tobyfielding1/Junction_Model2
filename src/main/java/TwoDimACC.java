import base.Vehicle;

public class TwoDimACC extends ACC {

    double p;
    double T1;
    double T2;
    double timeStep;

    /**
     * Constructor.
     *
     * @param v0 desired velocity, m/s
     * @param a0 maximum acceleration, m/s^2
     * @param b  desired deceleration (comfortable braking), m/s^2
     * @param T  safe time headway, seconds
     * @param s0 bumper to bumper vehicle distance in stationary traffic, meters
     * @param s1 gap parameter, meters
     */
    public TwoDimACC(double v0, double a0, double b, double T, double s0, double s1, double T1, double T2, double p, double timeStep, double coolness) {
        super(v0, a0, b, T, s0, s1, coolness);
        this.p = p;
        this.T1 = T1;
        this.T2 = T2;
        this.timeStep = timeStep;
    }

    public void updateT() {
        if (Math.random() < p * timeStep)
            T = T1 + Math.random() * T2;
    }

    @Override
    public double calcAcc(Vehicle me, double frontObjectSpeed) {
        updateT();
        return super.calcAcc(me, frontObjectSpeed);
    }
}
