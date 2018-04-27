package m;// Reference for constant-acceleration heuristic:
// Arne Kesting, Martin Treiber, Dirk Helbing
// Enhanced Intelligent Driver Model to access the impact of driving strategies on traffic capacity
// Philosophical Transactions of the Royal Society A 368, 4585-4605 (2010)

// Reference for improved intelligent driver extension: book

import base.FollowingModel;
import base.RoadObject;
import base.Vehicle;

/**
 * The Class ACC.
 */
class ACC implements FollowingModel {

    double v0;
    double a0;
    double b;
    double T;
    double s0;
    int delta = 4;
    double s1;
    double coolness;
    double alphaA;

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
    public ACC(double v0, double a0, double b, double T, double s0, double s1, double coolness) {
        this.v0 = v0;
        this.a0 = a0;
        this.b = b;
        this.T = T;
        this.s0 = s0;
        this.s1 = s1;
        this.coolness = coolness;
    }

    public double calcAcc(RoadObject lead, RoadObject follower, double separation) {
        return calcAcc(lead, follower, separation, 1, 1, alphaA);
    }

    public void setAlphaA(double alphaA) {
        this.alphaA = alphaA;
    }

    @Override
    public double getB() {
        return b;
    }

    private double calcAcc(RoadObject lead, RoadObject follower, double separation, double alphaT, double alphaV0, double alphaA) {

        double s0Here = s0;

        if (!(lead instanceof Vehicle))
            s0Here = 0;

        if (separation < 0)
            separation = 0;

        // Local dynamical variables
        final double s = separation + Math.random() * 0.2;
        final double v = follower.v;
        final double dv = follower.v - lead.v;

        final double aLead = lead.a;

        // space dependencies modeled by speedlimits, alpha's

        final double Tlocal = alphaT * T;
        // if(alphaT!=1){
        // System.out.printf("calcAcc: pos=%.2f, speed=%.2f, alphaT=%.3f, alphaV0=%.3f, T=%.3f, Tlocal=%.3f \n",
        // me.getPosition(), me.getSpeed(), alphaT, alphaV0, T, Tlocal);
        // }
        // consider external speedlimit
        final double v0Local = Math.min(alphaV0 * v0, follower.segment.targetSpeed);
        final double aLocal = alphaA * a0;

        return acc(s, v, dv, aLead, Tlocal, v0Local, aLocal, s0Here);
    }


    // Implementation of ACC model with improved IDM (IIDM)

    /**
     * Acc.
     *
     * @param s       the s
     * @param v       the v
     * @param dv      the dv
     * @param aLead   the a lead
     * @param TLocal  the t local
     * @param v0Local the v0 local
     * @param aLocal  the a local
     * @return the double
     */
    private double acc(double s, double v, double dv, double aLead, double TLocal, double v0Local, double aLocal, double s0) {
        // treat special case of v0=0 (standing obstacle)
        if (v0Local == 0) {
            return 0;
        }

        double sstar = s0
                + Math.max(
                TLocal * v + s1 * Math.sqrt((v + 0.00001) / v0Local) + 0.5 * v * dv
                        / Math.sqrt(aLocal * b), 0.);
        final double z = sstar / Math.max(s, 0.01);
        final double accEmpty = (v <= v0Local) ? aLocal * (1 - Math.pow((v / v0Local), delta)) : -b * (1 - Math.pow((v0Local / v), aLocal * delta / b));
        final double accPos = accEmpty * (1. - Math.pow(z, Math.min(2 * aLocal / accEmpty, 100.)));
        final double accInt = aLocal * (1 - z * z);

        final double accIIDM = (v < v0Local) ? (z < 1) ? accPos : accInt : (z < 1) ? accEmpty : accInt + accEmpty;

        // constant-acceleration heuristic (CAH)

        final double aLeadRestricted = Math.min(aLead, aLocal);
        final double dvp = Math.max(dv, 0.0);
        final double vLead = v - dvp;
        final double denomCAH = vLead * vLead - 2 * s * aLeadRestricted;

        final double accCAH = ((vLead * dvp < -2 * s * aLeadRestricted) && (denomCAH != 0)) ? v * v * aLeadRestricted
                / denomCAH : aLeadRestricted - 0.5 * dvp * dvp / Math.max(s, 0.0001);

        // ACC with IIDM

        final double accACC_IIDM = (accIIDM > accCAH) ? accIIDM : (1 - coolness) * accIIDM
                + coolness * (accCAH + b * Math.tanh((accIIDM - accCAH) / b));

        return accACC_IIDM;
    }
}

