package auction.mechanism.Controller;

import java.util.List;

import auction.mechanism.Model.Auction;
import auction.mechanism.Model.User;

public interface UserControllerInterface {
	
	public boolean registerUser(User user);
	public User getUser(String username);
	public boolean checkPassword(String username, String password) throws Exception;
	public boolean checkIfAbleToPlaceABid(String username) throws Exception;
	public boolean updateUser(User newUser);
	public boolean deleteUser(String username);
	public List<Auction> getAuctionsOwned(String username) throws Exception;
	public List<Auction> getAuctionsJoined(String username) throws Exception;
	public List<Auction> getAuctionsWon(String username) throws Exception;

}
