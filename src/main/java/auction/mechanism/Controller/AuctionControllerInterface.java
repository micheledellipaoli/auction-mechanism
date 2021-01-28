package auction.mechanism.Controller;

import java.util.List;

import auction.mechanism.Model.Auction;
import auction.mechanism.Model.AuctionBid;

public interface AuctionControllerInterface {
	
	public boolean registerAuction(Auction auction);
	public Auction getAuction(String auctionName);
	public List <String> getAllAuctionNames();
	public List <Auction> getAllAuctions();
	public List<Auction> getOngoingAuctions() throws Exception;
	public List<Auction> getExpiredAuctions() throws Exception;
	public boolean updateAuction(Auction newAuction);
	public boolean deleteAuction(String auctionName);
	public boolean placeABid(AuctionBid auctionBid) throws Exception;
	public List<AuctionBid> getAuctionBidsByAuction(String auctionName) throws Exception;
	public AuctionBid getTheHighestAuctionBidByAuction(String auctionName) throws Exception;
	public List<AuctionBid> getAuctionBidsByAuctionAndUsername(String auctionName, String username) throws Exception;
	public AuctionBid getTheHighestAuctionBidPlacedByAnUser(String auctionName, String username) throws Exception;
	public Auction.Status checkAuctionStatus(Auction auction) throws Exception;
	public void updateAuctionWinners(Auction auction, Auction.Status before) throws Exception;
	public boolean leaveNetwork();	

}
