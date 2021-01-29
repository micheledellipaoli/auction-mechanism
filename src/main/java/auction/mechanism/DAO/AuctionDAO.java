package auction.mechanism.DAO;

import java.util.ArrayList;
import java.util.List;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import auction.mechanism.Controller.UserController;
import auction.mechanism.Exception.AuctionNameAlreadyTakenException;
import auction.mechanism.Exception.AuctionNotFoundException;
import auction.mechanism.Model.Auction;
import auction.mechanism.Model.User;

/**
 * @author Michele Delli Paoli
 *
 */
public class AuctionDAO {

	final private PeerDHT peerDHT;

	// Istanza del singleton 
	private static AuctionDAO auctionDAOInstance = null;

	// Costruttore Privato
	private AuctionDAO(PeerDHT peerDHT) {
		this.peerDHT = peerDHT;
	}

	public static AuctionDAO getInstance(PeerDHT peerDHT) {
		if (auctionDAOInstance == null) {
			auctionDAOInstance = new AuctionDAO(peerDHT);
		}
		return auctionDAOInstance;
	}

	
	/**
	 * Get a registered Auction instance using auctionName parameter.
	 * @param auctionName Parameter used to find and get the registeredAuction instance.
	 * @return the registered Auction instance found.
	 * @throws Exception
	 */
	public Auction read(String auctionName) throws Exception {
		FutureGet fg = this.peerDHT.get(Number160.createHash(auctionName)).getLatest().start().awaitUninterruptibly();
		if (fg.isSuccess()) {
			if (fg.isEmpty()) {
				return null;
			}
			return (Auction) fg.data().object();
		}else {
			return null;
		}
	}

	/* The following methods are based on an "auctionIndex" list, which stores only the "auctionName" parameter of all Auctions registered, not the entire object*/

	
	/**
	 * Get a List of all registered Auctions names.
	 * @return a List of String which represents the names of all registered Auctions.
	 * @throws Exception
	 */
	public List<String> readAllAuctionNames() throws Exception{
		FutureGet fg = this.peerDHT.get(Number160.createHash("auctionIndex")).getLatest().start().awaitUninterruptibly();
		if (fg.isSuccess()) {
			if (fg.isEmpty()) {
				return null;
			}
			return (List<String>) fg.data().object();
		}
		return null;
	}	


	/**
	 * Register an Auction instance in the P2P system.
	 * @param auction Instance to be registered.
	 * @throws Exception
	 */
	public void create(Auction auction) throws Exception {
		UserController uc = new UserController(peerDHT);
		
		User owner = null;
		try {
			owner = uc.getUser(auction.getOwnerUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}

		if(owner != null) {
			if(auction.getReservedPrice() >= 0) {

				// Ottiene la lista di auctionNames
				List <String> auctionNames = null;
				
				try{
					auctionNames = this.readAllAuctionNames();
				}catch(Exception e) {
					e.printStackTrace();
				}

				if (auctionNames == null) {
					
					List <String> newAuctionNames = new ArrayList<String>();
					newAuctionNames.add(auction.getAuctionName());
					
					// Effettua la put della lista "auctionIndex" contenente l'auctionName dell'auction da creare.
					FuturePut fp1 = peerDHT.put(Number160.createHash("auctionIndex")).putIfAbsent().data(new Data(newAuctionNames)).start().awaitUninterruptibly();

					if(fp1.isSuccess()) {
						// Effettua la put dell'auction da creare.
						FuturePut fp2 = peerDHT.put(Number160.createHash(auction.getAuctionName())).data(new Data(auction)).start().awaitUninterruptibly();
						if(fp2.isSuccess()) {		

							//Aggiunge l'auction creata alla lista AuctionOwned dell'utente owner.
							owner.getAuctionsOwned().add(auction.getAuctionName());
							//Aggiorna l'istanza owner
							uc.updateUser(owner);

						}else {
							throw new Exception("An error occured during the creation of the Auction. Please try again.");
						}
					}else{
						throw new Exception("An error occured during the creation of the Auction. Please try again.");
					}
				}else{

					// Verifica se l'auction che si vuole aggiungere e' gia` contenuta nella lista di auctionNames
					boolean response = auctionNames.contains(auction.getAuctionName());

					if(!response) {
						// Inserimento dell'auction nella lista locale "auctionNames"
						auctionNames.add(auction.getAuctionName());

						// Put dell'istanza aggiornata della lista "auctionNames"
						FuturePut fp3 = peerDHT.put(Number160.createHash("auctionIndex")).data(new Data(auctionNames)).start().awaitUninterruptibly();

						if(fp3.isSuccess()) {

							// Put dell'auction
							FuturePut fp4 = peerDHT.put(Number160.createHash(auction.getAuctionName())).putIfAbsent().data(new Data(auction)).start().awaitUninterruptibly();

							if(fp4.isSuccess()) {

								//Aggiunge l'auction creata alla lista AuctionOwned dell'utente owner
								owner.getAuctionsOwned().add(auction.getAuctionName());
								//Aggiorna l'istanza getOwner
								uc.updateUser(owner);

							}else {
								throw new Exception("An error occured during the creation of the Auction. Please try again.");
							}
						}else {
							throw new Exception("An error occured during the creation of the Auction. Please try again.");
						}

					}else {
						throw new AuctionNameAlreadyTakenException("Auction name chosen is already taken. Please insert a new one.");
					}
				}
			}else {
				throw new Exception("An error occured during the creation of the Auction. Reserved price can not be smaller than 0.");
			}
		}else {
			throw new Exception("An error occured during the creation of the Auction. The Owner of the Auction was not found.");
		}
	}


	/**
	 * Update an existing Auction registered instance with a new one which has the same auctionName parameter.
	 * @param newAuction New instance which will replace the existing one. It must have the same auctionName parameter.
	 * @throws Exception
	 */
	public void update(Auction newAuction) throws Exception {
		// Ottenimento della lista auctionNames
		List <String> auctionNames = this.readAllAuctionNames();

		if (auctionNames == null) {
			throw new AuctionNotFoundException("No Auction was found. Impossible to update the selected auction. First create an auction and then try to update it.");
		}else{
			//Controlla se nella lista e' presente l'auction da modificare
			boolean containsAuction = auctionNames.contains(newAuction.getAuctionName());
			if(!containsAuction) {
				throw new Exception("Auction not found in the list of contents. Impossible to update the selected auction. Please check the auction name and try again.");
			}else {
				// Otteniene l'auction da aggiornare
				Auction auctionTarget = this.read(newAuction.getAuctionName());

				if(auctionTarget==null) {
					throw new Exception("Auction not found. Impossible to update the selected auction. Please check the auction name and try again.");
				}else{

					// Put dell'istanza aggiornata dell'auction
					FuturePut fp1 = peerDHT.put(Number160.createHash(newAuction.getAuctionName())).data(new Data(newAuction)).start().awaitUninterruptibly();
					if(!fp1.isSuccess()) {
						throw new Exception("An error occured during the update of the Auction. Please try again.");
					}
					
				}
			}
		}
	}


	/**
	 * De-register an Auction registered instance.
	 * @param auctionName Parameter used to find and de-register the existing Auction instance.
	 * @return a boolean: true if method has been successfull, false otherwise.
	 * @throws Exception
	 */
	public boolean delete(String auctionName) throws Exception {	
		List<String> auctionNames = new ArrayList<String>();
		Auction target = null;


		try {
			auctionNames = this.readAllAuctionNames();
			target = this.read(auctionName);
		}catch(Exception e) {
			e.printStackTrace();
			throw new Exception("An error occured during deleting the auction. Please try again.");
		}


		if(auctionNames.contains(auctionName) && target != null) {
			
			// Rimozione dell'auctionName dalla lista in locale
			auctionNames.remove(auctionName);

			// Put dell'istanza aggiornata della lista di auctionNames
			FuturePut fp1 = peerDHT.put(Number160.createHash("auctionIndex")).data(new Data(auctionNames)).start().awaitUninterruptibly();

			if(fp1.isSuccess()) {
				FutureRemove fr = peerDHT.remove(Number160.createHash(auctionName)).all().start().awaitUninterruptibly();
				if (fr.isSuccess()) {	
					return true;
				}else {
					throw new Exception("An error occured during deleting the auction. Please try again.");
				}
			}else {
				throw new Exception("An error occured during deleting the auction. Please try again.");
			}
			
		}else {
			throw new Exception("An error occured during deleting the auction. The selected auction was not found.");
		}
	}

}
