package handin2;

import java.io.Serializable;

public class Address implements Serializable {
    private String street;
    private String city;
    private String houseNumber;
    private String postcode;

    public Address(String city, String houseNumber, String postcode, String street) {
        this.city = city;
        this.houseNumber = houseNumber;
        this.postcode = postcode;
        this.street = street;
    }

    public String getAddressToSTR() {
        return street+ " " + houseNumber + ", " + postcode + " " + city;
    }


}
