import java.util.Collection;

public class LaneSegment {

    int targetSpeed;
    double length; // in meters
    Collection<LaneSegment> successors; //next lane segments connected to this one
    Collection<LaneSegment> conflicts; //spatially overlapping laneSegments, one vehicle at a time
    Collection<LaneSegment> visible; //which lane segments can be observed visually from this segment
    Collection<RoadObject> roadObjects; //any object that is in this lane segment

}
