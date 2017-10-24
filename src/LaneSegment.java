import java.util.Collection;

public class LaneSegment {

    int targetSpeed;
    double length; // in meters
    LaneSegment to; //next LaneSegment in route
    LaneSegment from; //prev LaneSegment in route
    LaneSegment conflicts; //spatially overlapping lane, mutually exclusive to vehicles
    Collection<LaneSegment> visible; //which lane segments can be observed visually from this segment
    Collection<RoadObject> roadObjects; //any object that is in this lane segment

}
