package auction.mechanism.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class Auction implements Serializable{

	private static final long serialVersionUID = 1L;
	public Auction(){		
	} 

	public Auction(String auctionName, String description, Calendar endDate, double reservedPrice, int slots, String ownerUsername){
		this.auctionName = auctionName;
		this.description = description;
		this.endDate = endDate;
		this.reservedPrice = reservedPrice;
		this.slots = slots;
		this.bids = new ArrayList<AuctionBid>();
		try {
			checkDateAndSetStatus();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		// Flag che serve ad effettuare la transizione da "ongoing" ad "ended", UN'UNICA VOLTA.
		this.flagFirstTimeSetStatus = false;
		this.ownerUsername = ownerUsername;
		this.winners = new HashMap<String, Double>();		
	}
	
	// Converte una data dal TimeZone di default del sistema al TimeZone "Europe/Rome"
	public static Calendar getLocalTime(Calendar date) {
		Calendar convertedDate = date;
		convertedDate.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
	    return convertedDate;
	}

	public void checkDateAndSetStatus() throws Exception {
		if(this.getEndDate() != null) {
			if( this.getEndDate().after(Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))) ) {
				this.status = Status.ongoing;
			}else {
				// Se il flag e' false, la transizione da "ongoing" ad "ended" non e' ancora avvenuta, dunque la si esegue.
				if(!this.flagFirstTimeSetStatus) {
					this.flagFirstTimeSetStatus = true;
					this.status = Status.ended;
				}
			}
		}else{
			throw new Exception("An error occured during the checking of the Auction status. Please try again.");
		}
	}

	public String getAuctionName() {
		return auctionName;
	}

	public void setAuctionName(String auctionName) {
		this.auctionName = auctionName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Calendar getEndDate() {
		return endDate;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	public double getReservedPrice() {
		return reservedPrice;
	}

	public void setReservedPrice(double reservedPrice) {
		this.reservedPrice = reservedPrice;
	}

	public int getSlots() {
		return slots;
	}

	public void setSlots(int slots) {
		this.slots = slots;
	}

	public List<AuctionBid> getBids() {
		//Ordina le AuctionBids in ordine decrescente in base al parametro BidAmount. 
		Collections.sort(this.bids, new SortBidsByBidAmount());
		return this.bids;
	}

	public void setBids(List<AuctionBid> bids) {
		this.bids = bids;
	}

	public void addBid(AuctionBid bid) {
		this.bids.add(bid);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public boolean isFlagFirstTimeSetStatus() {
		return flagFirstTimeSetStatus;
	}

	public void setFlagFirstTimeSetStatus(boolean flagFirstTimeSetStatus) {
		this.flagFirstTimeSetStatus = flagFirstTimeSetStatus;
	}

	public String getOwnerUsername() {
		return ownerUsername;
	}

	public void setOwnerUsername(String ownerUsername) {
		this.ownerUsername = ownerUsername;
	}

	public HashMap<String, Double> getWinners() {
		return winners;
	}

	public void setWinners(HashMap<String, Double> winners) {
		this.winners = winners;
	}
	
	public String printWinners() {
		String result = "";
		for (Map.Entry<String, Double> entry : this.getWinners().entrySet()) {
			String usernameWinner = entry.getKey();
			Double priceToPay = entry.getValue();
			result += "Winner: " + usernameWinner + " - Price to pay: " + priceToPay + "\r\n";
		}
		return result;
	}

	@SuppressWarnings("static-access")
	public String getDateCastedToString() {
		
		Calendar date = this.getEndDate();
		
		if(date.get(Calendar.MONTH) == 11) {
			return date.get(Calendar.DAY_OF_MONTH)+"/12/"+date.get(Calendar.YEAR);
		}else {
			return "("+timeZone+") " + date.get(Calendar.DAY_OF_MONTH)+"/"+(date.get(Calendar.MONTH)+1)+"/"+date.get(Calendar.YEAR);
		}
	}
	
	public String getHourCastedToString() {
		String result = "";
		
		Calendar date = this.getEndDate();
		
		if(String.valueOf(date.get(Calendar.HOUR_OF_DAY)).length() < 2) {
			result += "0" + String.valueOf(date.get(Calendar.HOUR_OF_DAY)) + ":";
		}else {
			result += String.valueOf(date.get(Calendar.HOUR_OF_DAY)) + ":";
		}
		if(String.valueOf(date.get(Calendar.MINUTE)).length() < 2) {
			result += "0" + String.valueOf(date.get(Calendar.MINUTE)) + ":";
		}else {
			result += String.valueOf(date.get(Calendar.MINUTE)) + ":";
		}
		if(String.valueOf(date.get(Calendar.SECOND)).length() < 2) {
			result += "0" + String.valueOf(date.get(Calendar.SECOND));
		}else {
			result += String.valueOf(date.get(Calendar.SECOND));
		}
		
		return result;
	}

	@Override
	public String toString() {
		return "Auction [auctionName=" + auctionName + ", description=" + description + ", endDate=" + this.getDateCastedToString() + " " + this.getHourCastedToString()
				+ ", reservedPrice=" + reservedPrice + ", slots=" + slots + ", bids=" + bids + ", status=" + status
				+ ", ownerUsername=" + ownerUsername
				+ ", winners=" + winners + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((auctionName == null) ? 0 : auctionName.hashCode());
		result = prime * result + ((bids == null) ? 0 : bids.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + (flagFirstTimeSetStatus ? 1231 : 1237);
		result = prime * result + ((ownerUsername == null) ? 0 : ownerUsername.hashCode());
		long temp;
		temp = Double.doubleToLongBits(reservedPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + slots;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((winners == null) ? 0 : winners.hashCode());
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
		Auction other = (Auction) obj;
		if (auctionName == null) {
			if (other.auctionName != null)
				return false;
		} else if (!auctionName.equals(other.auctionName))
			return false;
		if (bids == null) {
			if (other.bids != null)
				return false;
		} else if (!bids.equals(other.bids))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (ownerUsername == null) {
			if (other.ownerUsername != null)
				return false;
		} else if (!ownerUsername.equals(other.ownerUsername))
			return false;
		if (Double.doubleToLongBits(reservedPrice) != Double.doubleToLongBits(other.reservedPrice))
			return false;
		if (slots != other.slots)
			return false;
		if (status != other.status)
			return false;
		if (winners == null) {
			if (other.winners != null)
				return false;
		} else if (!winners.equals(other.winners))
			return false;
		return true;
	}

	private static String timeZone = "Europe/Rome";
	private String auctionName;
	private String description;
	//private Calendar creationDate;
	private Calendar endDate;
	private double reservedPrice;
	private int slots;
	private List<AuctionBid> bids;
	public enum Status{
		ongoing, ended
	};
	private Status status;
	private boolean flagFirstTimeSetStatus;
	private String ownerUsername;
	private HashMap<String, Double> winners;
}
