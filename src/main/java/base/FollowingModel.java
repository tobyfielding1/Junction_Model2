package base;

public interface FollowingModel {
    double calcAcc(RoadObject lead, RoadObject follower, double separation);

    double getB();

    void setAlphaA(double alphaA);
}

