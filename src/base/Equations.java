package base;

public class Equations {
    static class S {
        static double uvt(double uvt[]) {
            return (0.5 * (uvt[0] + uvt[1]) * uvt[2]);
        }

        static double uat(double uat[]) {
            return (uat[0] * uat[2] - 0.5 * uat[1] * uat[2] * uat[2]);
        }

        static double vat(double vat[]) {
            return (vat[0] * vat[2] - 0.5 * vat[1] * vat[2] * vat[2]);
        }

        static double uva(double uva[]) {
            return ((uva[1] * uva[1] - uva[0] * uva[0]) / (2 * uva[2]));
        }
    }

    static class U {
        static double vat(double vat[]) {
            return (vat[0] - vat[1] * vat[2]);
        }

        static double svt(double svt[]) {
            return ((2 * svt[0]) / svt[2] - svt[1]);
        }

        static double sat(double sat[]) {
            return ((sat[0] - 0.5 * sat[1] * sat[2] * sat[2]) / sat[2]);
        }

        static double sva(double sva[]) {
            return (Math.sqrt(sva[1] * sva[1] - 2 * sva[2] * sva[0]));
        }
    }

    static class V {
        static double uat(double uat[]) {
            return (uat[0] + uat[1] * uat[2]);
        }

        static double sut(double sut[]) {
            return ((2 * sut[0]) / sut[2] - sut[1]);
        }

        static double sat(double sat[]) {
            return ((sat[0] + 0.5 * sat[1] * sat[2] * sat[2]) / sat[2]);
        }

        static double sua(double sua[]) {
            return (Math.sqrt(sua[1] * sua[1] + 2 * sua[2] * sua[0]));
        }
    }

    static class A {
        static double uvt(double uvt[]) {
            return ((uvt[1] - uvt[0]) / uvt[2]);
        }

        static double sut(double sut[]) {
            return ((2 * (sut[0] - sut[1] * sut[2])) / (sut[2] * sut[2]));
        }

        static double svt(double svt[]) {
            return ((-2 * (svt[1] * svt[2] + svt[0])) / (svt[2] * svt[2]));
        }

        static double suv(double suv[]) {
            return ((suv[2] * suv[2] - suv[1] * suv[1]) / (2 * suv[0]));
        }
    }

    static class T {
        static double uva(double uva[]) {
            return ((uva[1] - uva[0]) / uva[2]);
        }

        static double suv(double suv[]) {
            return ((2 * suv[0]) / (suv[1] + suv[2]));
        }

        static double sua(double sua[]) {
            // the following will return NaN if sum in sqrt() is negative!
            return ((-sua[1] + Math.sqrt(sua[1] * sua[1] + 2 * sua[2]
                    * sua[0])) / sua[2]);
        }

        static double sva(double sva[]) {
            // the following will return NaN if sum in sqrt() is negative!
            return ((-sva[1] + Math.sqrt(sva[1] * sva[1] - 2 * sva[2]
                    * sva[0])) / (-sva[2]));
        }

    }
}
