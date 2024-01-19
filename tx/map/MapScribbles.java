package tx.map;

import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.Team;

import java.util.Objects;

public class MapScribbles {
    boolean isOccupied = false;
    /**Every Pathing operation marks up the map. If we keep an ID, we don't have to erase the whole map every iteration*/
    int requestID = -1;
    MapInfo info;

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public MapScribbles getNext() {
        return next;
    }

    public void setNext(MapScribbles next) {
        this.next = next;
    }

    public MapScribbles getPrev() {
        return prev;
    }

    public void setPrev(MapScribbles prev) {
        this.prev = prev;
    }

    public void setOccupantTeam(Team occupantTeam) {
        this.occupantTeam = occupantTeam;
    }

    public int getPathLength() {
        return pathLength;
    }

    public void setPathLength(int pathLength) {
        this.pathLength = pathLength;
    }

    MapScribbles next;

    MapScribbles prev;

    Team occupantTeam = Team.NEUTRAL;

    int pathLength;

    public MapScribbles(MapInfo info) {
        this.info = info;
    }

    public MapLocation loc(){
        return info.getMapLocation();
    }

    public MapInfo getInfo(){
        return info;
    }

    public void setInfo(MapInfo mapInfo){
        info = mapInfo;
    }

    public boolean isPassable(){
        return info.isPassable();
    }

    public int reqID(){
        return requestID;
    }

    public void setID(int ID){
        this.requestID = ID;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapScribbles that = (MapScribbles) o;
        return info.getMapLocation().x == that.info.getMapLocation().x && info.getMapLocation().y == that.info.getMapLocation().y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(info.getMapLocation().x, info.getMapLocation().y);
    }

    public Team getOccupantTeam(){
        return occupantTeam;
    }
}
