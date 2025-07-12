package rs.ac.uns.ftn.informatika.rest.domain;

public class GeocodingResult {
    public double latitude;
    public double longitude;
    public boolean success;
    public String message;

    public GeocodingResult(double lat, double lon) {
        this.latitude = lat;
        this.longitude = lon;
        this.success = true;
    }

    public GeocodingResult(String errorMsg) {
        this.success = false;
        this.message = errorMsg;
    }
}