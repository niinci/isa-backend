package rs.ac.uns.ftn.informatika.rest.dto;

import java.util.UUID;

public class LocationMsg {
    private UUID id;
    private String name;
    private double lat;
    private double lng;

    public LocationMsg() {}

    public LocationMsg(UUID id, String name, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    // Getteri i setteri
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
}
