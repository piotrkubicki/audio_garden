package uk.ac.napier.audiogarden;

import java.util.List;

/**
 * Created by pz on 23/02/17.
 */

public class Location {
    private int id;
    private String locationName;
    private List<Double> position;
    private List<String> transmittersIds;

    public Location(int id, List<Double> position, String location_name, List<String> transmittersIds) {
        this.id = id;
        this.position = position;
        this.locationName = location_name;
        this.transmittersIds = transmittersIds;
    }

    public Location() {};

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public List<Double> getPosition() {
        return position;
    }

    public void setPosition(List<Double> position) {
        this.position = position;
    }

    public List<String> getTransmittersIds() {
        return transmittersIds;
    }

    public void setTransmittersIds(List<String> transmittersIds) {
        this.transmittersIds = transmittersIds;
    }
}
