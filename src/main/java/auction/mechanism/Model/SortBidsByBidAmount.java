package auction.mechanism.Model;

import java.util.Comparator;

public class SortBidsByBidAmount implements Comparator<AuctionBid> {

	// Used for sorting the Auction Bids in descending order of BidAmount 
	public int compare(AuctionBid a, AuctionBid b) 
	{ 
		return Double.compare(b.getBidAmount(), a.getBidAmount());
	} 
} 


