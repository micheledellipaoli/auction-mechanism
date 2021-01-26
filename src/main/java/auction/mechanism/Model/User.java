package auction.mechanism.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public User(){
	}
	
	public User(String username, String password, AddressInfo addressInfo, String emailPayPal){
		this.username = username;
		this.password = password;
		this.addressInfo = addressInfo;
		this.emailPayPal = emailPayPal;
		this.auctionsOwned = new ArrayList<String>();
		this.auctionsJoined = new ArrayList<String>();
		this.auctionsWon = new ArrayList<String>();
	}
	
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public AddressInfo getAddressInfo() {
		return addressInfo;
	}

	public void setAddressInfo(AddressInfo addressInfo) {
		this.addressInfo = addressInfo;
	}
	
	public void setAddressInfo(String country, String city, String postalCode, String street, String streetNumber, String prefixPhoneNumber, String phoneNumber) {
		this.addressInfo.setCountry(country);
		this.addressInfo.setCity(city);
		this.addressInfo.setPostalCode(postalCode);
		this.addressInfo.setStreet(street);
		this.addressInfo.setStreetNumber(streetNumber);
		this.addressInfo.setPrefixPhoneNumber(prefixPhoneNumber);
		this.addressInfo.setPhoneNumber(phoneNumber);
	}

	public String getEmailPayPal() {
		return emailPayPal;
	}

	public void setEmailPayPal(String emailPayPal) {
		this.emailPayPal = emailPayPal;
	}

	public List<String> getAuctionsOwned() {
		return auctionsOwned;
	}

	public void setAuctionsOwned(List<String> auctionsOwned) {
		this.auctionsOwned = auctionsOwned;
	}

	public List<String> getAuctionsJoined() {
		return auctionsJoined;
	}

	public void setAuctionsJoined(List<String> auctionsJoined) {
		this.auctionsJoined = auctionsJoined;
	}

	public List<String> getAuctionsWon() {
		return auctionsWon;
	}

	public void setAuctionsWon(List<String> auctionsWon) {
		this.auctionsWon = auctionsWon;
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", password=" + password + ", addressInfo=" + addressInfo
				+ ", emailPayPal=" + emailPayPal + ", auctionsOwned=" + auctionsOwned + ", auctionsJoined="
				+ auctionsJoined + ", auctionsWon=" + auctionsWon + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addressInfo == null) ? 0 : addressInfo.hashCode());
		result = prime * result + ((auctionsJoined == null) ? 0 : auctionsJoined.hashCode());
		result = prime * result + ((auctionsOwned == null) ? 0 : auctionsOwned.hashCode());
		result = prime * result + ((auctionsWon == null) ? 0 : auctionsWon.hashCode());
		result = prime * result + ((emailPayPal == null) ? 0 : emailPayPal.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
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
		User other = (User) obj;
		if (addressInfo == null) {
			if (other.addressInfo != null)
				return false;
		} else if (!addressInfo.equals(other.addressInfo))
			return false;
		if (auctionsJoined == null) {
			if (other.auctionsJoined != null)
				return false;
		} else if (!auctionsJoined.equals(other.auctionsJoined))
			return false;
		if (auctionsOwned == null) {
			if (other.auctionsOwned != null)
				return false;
		} else if (!auctionsOwned.equals(other.auctionsOwned))
			return false;
		if (auctionsWon == null) {
			if (other.auctionsWon != null)
				return false;
		} else if (!auctionsWon.equals(other.auctionsWon))
			return false;
		if (emailPayPal == null) {
			if (other.emailPayPal != null)
				return false;
		} else if (!emailPayPal.equals(other.emailPayPal))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}


	private String username;
	private String password;
	private AddressInfo addressInfo;
	private String emailPayPal;
	private List<String> auctionsOwned;
	private List<String> auctionsJoined;
	private List<String> auctionsWon;
}
