package auction.mechanism.Model;

import java.io.Serializable;

public class AddressInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public AddressInfo(){	
	}
	
	public AddressInfo(String country, String city, String postalCode, String street, String streetNumber, String prefixPhoneNumber, String phoneNumber){
		this.country = country;
		this.city = city;
		this.postalCode = postalCode;
		this.street = street;
		this.streetNumber = streetNumber;
		this.prefixPhoneNumber = prefixPhoneNumber;
		this.phoneNumber = phoneNumber;
	}
	
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getStreetNumber() {
		return streetNumber;
	}

	public void setStreetNumber(String streetNumber) {
		this.streetNumber = streetNumber;
	}

	public String getPrefixPhoneNumber() {
		return prefixPhoneNumber;
	}

	public void setPrefixPhoneNumber(String prefixPhoneNumber) {
		this.prefixPhoneNumber = prefixPhoneNumber;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@Override
	public String toString() {
		return "AddressInfo [country=" + country + ", city=" + city + ", postalCode=" + postalCode + ", street="
				+ street + ", streetNumber=" + streetNumber + ", prefixPhoneNumber=" + prefixPhoneNumber
				+ ", phoneNumber=" + phoneNumber + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		result = prime * result + ((postalCode == null) ? 0 : postalCode.hashCode());
		result = prime * result + ((prefixPhoneNumber == null) ? 0 : prefixPhoneNumber.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		result = prime * result + ((streetNumber == null) ? 0 : streetNumber.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AddressInfo other = (AddressInfo) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (phoneNumber == null) {
			if (other.phoneNumber != null)
				return false;
		} else if (!phoneNumber.equals(other.phoneNumber))
			return false;
		if (postalCode == null) {
			if (other.postalCode != null)
				return false;
		} else if (!postalCode.equals(other.postalCode))
			return false;
		if (prefixPhoneNumber == null) {
			if (other.prefixPhoneNumber != null)
				return false;
		} else if (!prefixPhoneNumber.equals(other.prefixPhoneNumber))
			return false;
		if (street == null) {
			if (other.street != null)
				return false;
		} else if (!street.equals(other.street))
			return false;
		if (streetNumber == null) {
			if (other.streetNumber != null)
				return false;
		} else if (!streetNumber.equals(other.streetNumber))
			return false;
		return true;
	}

	private String country;
	private String city;
	private String postalCode;
	private String street;
	private String streetNumber;
	private String prefixPhoneNumber;
	private String phoneNumber;

}
