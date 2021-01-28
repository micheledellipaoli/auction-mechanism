package auction.mechanism.Model;

import java.io.Serializable;
import java.util.List;

import auction.mechanism.Controller.AuctionController;
import net.tomp2p.dht.PeerDHT;
 
public class AuctionBid implements Serializable{

	private static final long serialVersionUID = 1L;
	public AuctionBid(){
	}
	
	public AuctionBid(String auctionName, String username, double bidAmount){
		this.auctionName = auctionName; 
		this.username = username;
		this.bidAmount = bidAmount;
	}	
	
	public String getAuctionBidID(PeerDHT peerDHT) {
		List<AuctionBid> list = null;
		AuctionController ac = new AuctionController(peerDHT);
		try {
			list = ac.getAuctionBidsByAuctionAndUsername(this.getAuctionName(), this.getUsername());
		} catch (Exception e) {
			e.printStackTrace();
		}
		int count = 0;
		if(list != null && !list.isEmpty()) {
			for(int i=0; i<list.size(); i++) {
				if(list.get(i).getUsername().equals(this.username)) {
					count++;
				}
			}
		}		
		return this.getAuctionName()+"|"+this.getUsername()+"|"+count;
	}

	public String getAuctionName() {
		return auctionName;
	}

	public void setAuctionName(String auctionName) {
		this.auctionName = auctionName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public double getBidAmount() {
		return bidAmount;
	}

	public void setBidAmount(double bidAmount) {
		this.bidAmount = bidAmount;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuctionBid other = (AuctionBid) obj;
		if (auctionName == null) {
			if (other.auctionName != null)
				return false;
		} else if (!auctionName.equals(other.auctionName))
			return false;
		if (Double.doubleToLongBits(bidAmount) != Double.doubleToLongBits(other.bidAmount))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	
	@Override
	public String toString() {
		return "AuctionBid [auctionName=" + auctionName + ", username=" + username + ", bidAmount=" + bidAmount + "]";
	}

	private String auctionName;
	private String username;
	private double bidAmount;
}

