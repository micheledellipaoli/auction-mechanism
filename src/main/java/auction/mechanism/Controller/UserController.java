package auction.mechanism.Controller;

import auction.mechanism.DAO.*;
import auction.mechanism.Exception.UserNotFoundException;
import auction.mechanism.Model.*;
import net.tomp2p.dht.PeerDHT;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserController implements UserControllerInterface{

	private static PeerDHT peerDHT;

	public UserController(PeerDHT peerDHT) {
		UserController.peerDHT = peerDHT;
	}

	public boolean registerUser(User user){
		UserDAO userDAO = UserDAO.getInstance(peerDHT);
		boolean response = true;
		try{
			userDAO.create(user);
		}catch(Exception e){
			e.printStackTrace();
			response =  false;
		}
		return response;
	}

	public User getUser(String username){
		UserDAO userDAO = UserDAO.getInstance(peerDHT);
		try{
			return userDAO.read(username);
		}catch(Exception e) {
			return null;
		}
	}

	public boolean checkPassword(String username, String password) throws Exception{
		boolean response = false;
		User user = getUser(username);
		
		if(user != null) {
			if(user.getPassword().equals(password)) {
				response = true;
			}
			return response;
		}else{
			throw new UserNotFoundException("User not found.");
		}
	}

	public boolean checkIfAbleToPlaceABid(String username) throws Exception{
		boolean response = false;
		User user = getUser(username);

		if(user != null) {
			if(user.getAddressInfo() != null && user.getAddressInfo().getCountry() != null && user.getAddressInfo().getCity() != null && user.getAddressInfo().getPostalCode() != null && user.getAddressInfo().getStreet() != null && user.getAddressInfo().getStreetNumber() != null) {
				if(user.getEmailPayPal() != null) {
					String regex = "^[a-zA-Z0-9_!#$%&�*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&�*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
					Pattern pattern = Pattern.compile(regex);
					Matcher matcher = pattern.matcher(user.getEmailPayPal());
					response = matcher.matches();
					return response;
				}
			}
		}else{
			throw new UserNotFoundException("User not found.");
		}
		return response;
	}

	public boolean updateUser(User newUser){
		UserDAO userDAO = UserDAO.getInstance(peerDHT);
		try{
			userDAO.update(newUser);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteUser(String username){
		UserDAO userDAO = UserDAO.getInstance(peerDHT);
		try{
			userDAO.delete(username);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	public List<Auction> getAuctionsOwned(String username) throws Exception{
		List<String> auctionNamesOwned = null;
		List<Auction> auctionsOwned = null;
		AuctionController ac = new AuctionController(peerDHT);
		try {
			auctionNamesOwned = (List<String>) this.getUser(username).getAuctionsOwned();
			auctionsOwned = new ArrayList<Auction>();
			for(int i=0; i<auctionNamesOwned.size(); i++) {
				auctionsOwned.add(ac.getAuction(auctionNamesOwned.get(i)));
			}
		}catch(Exception e) {
			//e.printStackTrace();
		}
		return auctionsOwned;
	}
	
	public List<Auction> getAuctionsJoined(String username) throws Exception{
		List<String> auctionNamesJoined = null;
		List<Auction> auctionsJoined = null;
		AuctionController ac = new AuctionController(peerDHT);
		try {
			auctionNamesJoined = (List<String>) this.getUser(username).getAuctionsJoined();
			auctionsJoined = new ArrayList<Auction>();
			for(int i=0; i<auctionNamesJoined.size(); i++) {
				auctionsJoined.add(ac.getAuction(auctionNamesJoined.get(i)));
			}
		}catch(Exception e) {
			//e.printStackTrace();
		}
		return auctionsJoined;
	}
	
	public List<Auction> getAuctionsWon(String username) throws Exception{
		List<String> auctionNamesWon = null;
		List<Auction> auctionsWon = null;
		AuctionController ac = new AuctionController(peerDHT);
		try {
			auctionNamesWon = (List<String>) this.getUser(username).getAuctionsWon();
			auctionsWon = new ArrayList<Auction>();
			for(int i=0; i<auctionNamesWon.size(); i++) {
				auctionsWon.add(ac.getAuction(auctionNamesWon.get(i)));
			}
		}catch(Exception e) {
			//e.printStackTrace();
		}
		return auctionsWon;
	}

}
