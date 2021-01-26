package auction.mechanism.DAO;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.util.List;

import auction.mechanism.Controller.AuctionController;
import auction.mechanism.Exception.UserNotFoundException;
import auction.mechanism.Exception.UsernameAlreadyTakenException;
import auction.mechanism.Model.Auction;
import auction.mechanism.Model.AuctionBid;
import auction.mechanism.Model.User;

// Classe Singleton
public class UserDAO {

	final private PeerDHT peerDHT;

	// Istanza del singleton 
	private static UserDAO userDAOInstance = null;

	// Costruttore Privato
	private UserDAO(PeerDHT peerDHT) {
		this.peerDHT = peerDHT;
	}

	public static UserDAO getInstance(PeerDHT peerDHT) {
		if (userDAOInstance == null) {
			userDAOInstance = new UserDAO(peerDHT);
		}
		return userDAOInstance;
	}


	public void create(User user) throws UsernameAlreadyTakenException, IOException {
		FuturePut fp = peerDHT.put(Number160.createHash(user.getUsername())).putIfAbsent().data(new Data(user)).start().awaitUninterruptibly();
		if (!fp.isSuccess()){
			throw new UsernameAlreadyTakenException("Username chosen is already taken. Please insert a new one.");
		}
	}


	public User read(String username) throws Exception {

		FutureGet fg = peerDHT.get(Number160.createHash(username)).getLatest().start().awaitUninterruptibly();

		if (fg.isSuccess()) {
			if (fg.isEmpty()){	// User non trovato
				throw new UserNotFoundException("User not found. Please check the username and try again.");
			}
			return (User) fg.data().object();
		}else {
			throw new Exception("An error occured during searching for the user. Please try again.");
		}
	}


	public void update(User newUser) throws Exception {
		User latestUser = this.read(newUser.getUsername());

		if(latestUser==null) {
			throw new UserNotFoundException("User not found. Impossible to update the selected user. Please check the username and try again.");
		}else{
			// Put dell'istanza aggiornata dell'user
			FuturePut fp2 = peerDHT.put(Number160.createHash(newUser.getUsername())).data(new Data(newUser)).start().awaitUninterruptibly();
			if(!fp2.isSuccess()) {
				throw new Exception("An error occured during the update of the User. Please try again.");
			}
		}
	}


	public boolean delete(String username) throws Exception {	
		FutureRemove fr = peerDHT.remove(Number160.createHash(username)).all().start().awaitUninterruptibly();
		
		if (fr.isSuccess()) {
			AuctionController ac = new AuctionController(peerDHT);
			List<Auction> allAuctions = null;
			try{
				// Ottiene la lista di tutte le Auctions
				allAuctions = ac.getAllAuctions();
			}catch(Exception e) {
				e.getMessage();
			}

			if(allAuctions != null && !allAuctions.isEmpty()) {
				for(int i=0; i<allAuctions.size(); i++) {
					Auction auction = allAuctions.get(i);
					// Per ogni Auction nella lista, ottiene le Bids piazzate su di essa
					List<AuctionBid> bids = auction.getBids();
					
					// Se l'Auction � in stato ongoing, rimuove le Bids piazzate dall'User che e' stato cancellato.
					if(auction.getStatus().equals(Auction.Status.ongoing)) {
						if(bids != null && !bids.isEmpty()) {
							for(int j=0; j<bids.size(); j++) {
								if(bids.get(j).getUsername().equals(username)) {
									bids.remove(j);
								}
							}
							// Aggiorna l'istanza locale dell'Auction
							auction.setBids(bids);
							try {
								// Propaga l'aggiornamento dell'Auction nel sistema
								ac.updateAuction(auction);
							}catch(Exception e) {
								e.getMessage();;
							}
						}
					}
				}
			}
			return true;
		}else {
			throw new Exception("An error occured during deleting the user. Please try again.");
		}
	}


}
