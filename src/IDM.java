import base.Vehicle;

/**
 * The Class IDM.
 *
 * <p>
 * Implementation of the 'intelligent driver model'(IDM). <a href="http://en.wikipedia.org/wiki/Intelligent_Driver_Model">Wikipedia article
 * IDM.</a>
 * </p>
 * <p>
 * Treiber/Kesting: Traffic Flow Dynamics, 2013, chapter 11.3
 * </p>
 * <p>
 * see <a href="http://xxx.uni-augsburg.de/abs/cond-mat/0002177"> M. Treiber, A. Hennecke, and D. Helbing, Congested Traffic States in
 * Empirical Observations and Microscopic Simulations, Phys. Rev. E 62, 1805 (2000)].</a>
 * </p>
 *
 * base.Model parameters:
 * <ul>
 * <li>safe time headway T (s)</li>
 * <li>minimum gap in standstill s0 (m)</li>
 * <li>maximum desired acceleration a (m/s^2)</li>
 * <li>comfortable (desired) deceleration (m/s^2)</li>
 * <li>acceleration exponent delta (1)</li>
 * <li>gap parameter s1 (m).</li>
 * </ul>
 *
*/

public class IDM {
    /**
     * Constructor.
     *
     * @param v0
     *            desired velocity, m/s
     * @param a
     *            maximum acceleration, m/s^2
     * @param b
     *            desired deceleration (comfortable braking), m/s^2
     * @param T
     *            safe time headway, seconds
     * @param s0
     *            bumper to bumper vehicle distance in stationary traffic, meters
     * @param delta
     *            acceleration exponent
     * @param s1
     *            s1
     */
    public IDM(double v0, double a, double b, double T, double s0, int delta, double s1) {
        this.v0 = v0;
        this.a = a;
        this.b = b;
        this.T = T;
        this.s0 = s0;
        this.delta = delta;
        this.s1 = s1;
    }

    double v0;
    double a;
    double b;
    double T;
    double s0;
    int delta;
    double s1;


    public double calcAcc(Vehicle me, double frontObjectSpeed) {

        return Math.random()*calcAcc(me, frontObjectSpeed, 1, 1, 1);
    }


    private double calcAcc(Vehicle me, double frontObjectSpeed, double alphaT, double alphaV0, double alphaA) {

        // Local dynamical variables
        final double s = me.objectAheadDist;
        final double v = me.v;
        final double dv = me.v - frontObjectSpeed;

        // space dependencies modeled by speedlimits, alpha's
        final double localT = alphaT * T;
        // consider external speedlimit
        final double localV0;
        if (me.segment.targetSpeed != 0.0) {
            localV0 = Math.min(alphaV0 * v0, me.segment.targetSpeed);
        } else {
            localV0 = alphaV0 * v0;
        }
        final double localA = alphaA * a;

        return acc(s, v, dv, localT, localV0, localA);
    }


    private double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, T, v0, a);
    }

    /**
     * Acc.
     *
     * @param s
     *            the s
     * @param v
     *            the v
     * @param dv
     *            the dv
     * @param TLocal
     *            the t local
     * @param v0Local
     *            the v0 local
     * @param aLocal
     *            the a local
     * @return the double
     */
    private double acc(double s, double v, double dv, double TLocal, double v0Local, double aLocal) {
        // treat special case of v0=0 (standing obstacle)
        if (v0Local == 0.0) {
            return 0.0;
        }


        double sstar = s0 + TLocal * v + s1 * Math.sqrt((v + 0.0001) / v0Local) + (0.5 * v * dv)
                / Math.sqrt(aLocal * b);

        if (sstar < s0) {
            sstar = s0;
        }

        final double aWanted = aLocal * (1.0 - Math.pow((v / v0Local), delta) - (sstar / s) * (sstar / s));

        return aWanted; // limit to -bMax in base.Vehicle
    }


}

