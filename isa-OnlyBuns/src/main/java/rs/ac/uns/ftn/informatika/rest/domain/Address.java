package rs.ac.uns.ftn.informatika.rest.domain;

public class Address {
    private String street;
    private String city;
    private String country;
    private String number;

    // Default konstruktor (potreban za Jackson)
    public Address() {}

    public Address(String street, String city, String country, String number) {
        this.street = street;
        this.city = city;
        this.country = country;
        this.number = number;
    }

    // Getteri i setteri
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
}