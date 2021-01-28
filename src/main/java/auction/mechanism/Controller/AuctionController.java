package auction.mechanism.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import auction.mechanism.DAO.*;
import auction.mechanism.Model.*;
import net.tomp2p.dht.PeerDHT;

public class AuctionController implements AuctionControllerInterface{

	private static PeerDHT peerDHT;

	public AuctionController(PeerDHT peerDHT) {
		AuctionController.peerDHT = peerDHT;
	}

	public boolean registerAuction(Auction auction){
		AuctionDAO auctionDAO = AuctionDAO.getInstance(peerDHT);
		boolean response = true;
		try{
			auctionDAO.create(auction);
		}catch(Exception e){
			e.printStackTrace();
			response =  false;
		}
		return response;
	}

	/*
	public static Auction getAuctionOld(String auctionName){
		AuctionDAONew auctionDAO = AuctionDAONew.getInstance(peerDHT);
		try{
			Auction auction = auctionDAO.read(auctionName);
			return auction;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	*/

	public Auction getAuction(String auctionName){
		AuctionDAO auctionDAO = AuctionDAO.getInstance(peerDHT);
		try{
			Auction auction = auctionDAO.read(auctionName);
			Auction.Status before = this.checkAuctionStatus(auction);
			auction = auctionDAO.read(auctionName);
			this.updateAuctionWinners(auction, before);
			return auction = auctionDAO.read(auctionName);
		}catch(Exception e) {
			return null;
		}
	}

	public List <String> getAllAuctionNames(){
		AuctionDAO auctionDAO = AuctionDAO.getInstance(peerDHT);
		try{
			return auctionDAO.readAllAuctionNames();
		}catch(Exception e) {
			return null;
		}
	}

	public List <Auction> getAllAuctions(){
		List<String> allAuctionNames = null;
		List<Auction> allAuctions = null;
		try{
			allAuctionNames = getAllAuctionNames();
			
			if(allAuctionNames!=null && !allAuctionNames.isEmpty()) {
				allAuctions = new ArrayList<Auction>();
				
				for(int i=0; i<allAuctionNames.size(); i++) {
					//Invoca getAuction anche per effettuare il controllo della Data di Scadenza e dello Status
					Auction target = getAuction(allAuctionNames.get(i));
					allAuctions.add(target);
				}
			}
			return allAuctions;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<Auction> getOngoingAuctions() throws Exception{
		List<Auction> allAuctions = null;
		List<Auction> ongoingAuctions = null;
		try {
			allAuctions = getAllAuctions();
			
			if(allAuctions!=null && !allAuctions.isEmpty()) {
				ongoingAuctions = new ArrayList<Auction>();
				
				for(int i=0; i<allAuctions.size(); i++) {
					Auction target = allAuctions.get(i);
					if(target != null && target.getStatus().equals(Auction.Status.ongoing)) {
						ongoingAuctions.add(target);
					}
				}
			}
			return ongoingAuctions;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}	
	}

	public List<Auction> getExpiredAuctions() throws Exception{
		List<Auction> allAuctions = null;
		List<Auction> expiredAuctions = null;
		try {
			allAuctions = getAllAuctions();
			
			if(allAuctions!=null && !allAuctions.isEmpty()) {
				expiredAuctions = new ArrayList<Auction>();
				
				for(int i=0; i<allAuctions.size(); i++) {
					Auction target = allAuctions.get(i);
					if(target != null && target.getStatus().equals(Auction.Status.ended)) {
						expiredAuctions.add(target);
					}
				}
			}
			return expiredAuctions;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}	
	}

	public boolean updateAuction(Auction newAuction){
		AuctionDAO auctionDAO = AuctionDAO.getInstance(peerDHT);
		try{
			auctionDAO.update(newAuction);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteAuction(String auctionName){
		AuctionDAO auctionDAO = AuctionDAO.getInstance(peerDHT);
		try{
			auctionDAO.delete(auctionName);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean placeABid(AuctionBid auctionBid) throws Exception{
		UserController uc = new UserController(peerDHT);
		boolean response = false;

		// Ottiene l'auction per la quale piazzare la bid.
		Auction auctionTarget = null;
		try {
			auctionTarget = this.getAuction(auctionBid.getAuctionName());
		}catch(Exception e) {
			e.printStackTrace();
		}

		// Ottiene l'user che prova ad effettuare l'offerta
		User bidder = null;
		try {
			bidder = uc.getUser(auctionBid.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}

		if(bidder != null) {

			// Controlla che lo stato dell'auction sia "ongoing".
			if(auctionTarget.getStatus().equals(Auction.Status.ongoing)) {
				// Controlla che l'utente sia abilitato ad effettuare l'offerta.
				if(uc.checkIfAbleToPlaceABid(bidder.getUsername()) ) {
					// Controlla che l'utente che prova ad effettuare l'offerta sia DIVERSO dall'Owner dell'Auction
					if(!bidder.getUsername().equals(auctionTarget.getOwnerUsername())) {

						if(auctionTarget.getBids() == null) {
							List<AuctionBid> bids = new ArrayList<AuctionBid>();
							auctionTarget.setBids(bids);
						}

						// Controlla che auctionBid.bidAmount sia maggiore di 0 e maggiore o uguale di reservedPrice, ovvero del prezzo di riserva dell'asta.
						if((auctionBid.getBidAmount() > 0.0) && (auctionBid.getBidAmount() >= auctionTarget.getReservedPrice())) {
							if(auctionTarget.getBids().isEmpty()) {

								auctionTarget.getBids().add(auctionBid);

								//Aggiorna l'istanza auctionTarget
								this.updateAuction(auctionTarget);


								//Aggiunge l'auction per la quale e' stata effettuata l'offerta alla lista AuctionJoined dell'utente bidder
								if(!bidder.getAuctionsJoined().contains(auctionTarget.getAuctionName())) {
									bidder.getAuctionsJoined().add(auctionTarget.getAuctionName());
									//Aggiorna l'istanza bidder
									uc.updateUser(bidder);
								}
								response = true;
							}else{
								//Ordina le AuctionBids in ordine decrescente in base al parametro BidAmount. 
								Collections.sort(auctionTarget.getBids(), new SortBidsByBidAmount());


								// Controlla che l'User che sta tentando di piazzare la bid non abbia gi� piazzato una bid vincente.
								boolean winning = false;
								if(auctionTarget.getSlots() < auctionTarget.getBids().size()) {
									// Per ogni slot di indice i, controlla se le i-esime bids maggiori sono state effettuate dall'User che tenta di piazzare l'offerta.
									for(int i=0; i<auctionTarget.getSlots(); i++) {
										if(auctionTarget.getBids().get(i).getUsername().equals(auctionBid.getUsername())) {
											winning = true;
											break;
										}
									}
								}else {
									for(int i=0; i<auctionTarget.getBids().size(); i++) {
										if(auctionTarget.getBids().get(i).getUsername().equals(auctionBid.getUsername())) {
											winning = true;
											break;
										}
									}
								}

								if(winning) {
									throw new Exception("Impossible to place the bid. The user has already put a winning bid for the moment.");
								}else {
									// Se l'user non ha gi� piazzato la auctionBid vincente, ottiene la bid con bidAmount piu' alto nella lista e la confronta con la bid che si sta tentando di piazzare.
									// Se la auctionBid che si vuole piazzare ha bidAmount maggiore del bidAmount pi� alto nella lista delle bid, allora piazza auctionBid.
									if(auctionBid.getBidAmount() > auctionTarget.getBids().get(0).getBidAmount()) {
										auctionTarget.getBids().add(auctionBid);

										//Aggiorna l'istanza auctionTarget
										this.updateAuction(auctionTarget);

										//Aggiunge l'auction alla quale � stata effettuata l'offerta alla lista AuctionJoined dell'utente bidder
										if(!bidder.getAuctionsJoined().contains(auctionTarget.getAuctionName())) {
											bidder.getAuctionsJoined().add(auctionTarget.getAuctionName());
											//Aggiorna l'istanza bidder
											uc.updateUser(bidder);
										}
										response = true;
									}else {
										throw new Exception("Impossible to place the bid. \nBid amount is less than the current greatest bid for this auction. \nPlease insert a bid amount greater than the current greatest bid for this auction.");
									}
								}
							}
						}else{
							throw new Exception("Impossible to place the bid. The current bid amount is less than reserved price. \nPlease insert a bid amount greater than reserved price.");
						}
					}else {
						throw new Exception("Impossible to place the bid. User who wants to place the bid can't be the Owner of the Auction ");
					}
				}else {
					throw new Exception("Impossible to place the bid. \nThe User is not able to place a bid on any auction. \nPlease check that Address Info and PayPal Email forms are valid to be able to place any bid.");
				}
			}else {
				throw new Exception("Impossible to place the bid. The current auction is ended.");
			}
		}else {
			throw new Exception("Impossible to place the bid. User who wants to place the bid does not exist.");
		}
		this.updateAuction(auctionTarget);
		return response;
	}

	/*
	Restituisce la lista delle AuctionBid, ordinata in modo decrescente rispetto al parametro bidAmount, piazzate sull'Auction il cui auctionName � passato come parametro di input.
	 */
	public List<AuctionBid> getAuctionBidsByAuction(String auctionName) throws Exception{
		// Ottiene l'auction per la quale bisogna ottenere la lista delle bid.
		Auction auctionTarget = this.getAuction(auctionName);

		if(auctionTarget == null) { 
			throw new Exception("Impossible to retrieve the list of all the AuctionBids placed on the current Auction. Plese insert a correct auction name.");
		}else {
			if(auctionTarget.getBids() == null || auctionTarget.getBids().isEmpty()) {
				throw new Exception("Impossible to retrieve the list of all the AuctionBids placed on the current Auction. The list is null or is empty.");
			}else {
				//Ordina le AuctionBids in ordine decrescente in base al parametro BidAmount. 
				Collections.sort(auctionTarget.getBids(), new SortBidsByBidAmount());
				return auctionTarget.getBids();
			}
		}

	}

	public AuctionBid getTheHighestAuctionBidByAuction(String auctionName) throws Exception{
		List<AuctionBid> auctionBidsByAuction = this.getAuctionBidsByAuction(auctionName);
		if(auctionBidsByAuction != null && !auctionBidsByAuction.isEmpty()) {
			return auctionBidsByAuction.get(0);
		}else {
			throw new Exception("No AuctionBid placed on the current Auction was found.");
		}
	}


	public List<AuctionBid> getAuctionBidsByAuctionAndUsername(String auctionName, String username) throws Exception{
		List<AuctionBid> auctionBidsByAuction = this.getAuctionBidsByAuction(auctionName);
		List<AuctionBid> auctionBidsByAuctionAndUsername = new ArrayList<AuctionBid>();
		for(int i=0; i<auctionBidsByAuction.size(); i++) {
			if(auctionBidsByAuction.get(i).getUsername().equals(username)) {
				auctionBidsByAuctionAndUsername.add(auctionBidsByAuction.get(i));
			}
		}
		if(auctionBidsByAuctionAndUsername.isEmpty()) {
			throw new Exception("No AuctionBid placed by the specified User was found. Please verify that the User has placed an AuctionBid for the specified Auction.");
		}else {
			return auctionBidsByAuctionAndUsername;
		}
	}

	public AuctionBid getTheHighestAuctionBidPlacedByAnUser(String auctionName, String username) throws Exception{
		List<AuctionBid> auctionBidsByAuctionAndUsername = this.getAuctionBidsByAuctionAndUsername(auctionName, username);
		if(auctionBidsByAuctionAndUsername != null && !auctionBidsByAuctionAndUsername.isEmpty()) {
			return auctionBidsByAuctionAndUsername.get(0);
		}else {
			throw new Exception("No AuctionBid placed by the inserted User was found. Please verify that the User has placed an AuctionBid for the specified Auction.");
		}
	}



	public Auction.Status checkAuctionStatus(Auction auction) throws Exception {
		if(auction!=null) {
			Auction.Status before = auction.getStatus();

			// Controlla la endDate dell'asta per verificarne lo stato. Nel caso la endDate e' passata, setta lo stato dell'auction a "ended".
			try {
				auction.checkDateAndSetStatus();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Effettua l'update dell'Auction
			this.updateAuction(auction);

			return before;

		}else {
			throw new Exception("An error occured during the checking of Auction. Auction was not found.");
		}
	}

	public void updateAuctionWinners(Auction auction, Auction.Status before) throws Exception {
		UserController uc = new UserController(peerDHT);
		
		if(auction!=null) {
			Auction.Status after = auction.getStatus();

			//Se lo status dell'asta e' diventato "ended" per la PRIMA VOLTA, per ogni slot bisogna settare il vincitore ed il prezzo che il vincitore deve pagare.
			if (before.equals(Auction.Status.ongoing) && after.equals(Auction.Status.ended)){

				List<AuctionBid> bids = null;
				bids = auction.getBids();
				//bids = AuctionController.getAuctionBidsByAuction(auction.getAuctionName());

				if(bids != null && !bids.isEmpty()) {
					// Se il numero di AuctionBid piazzate � maggiore del numero di slot, il numero di vincitori e' pari al numero di slot.
					if( bids.size() > auction.getSlots() ){
						for(int i=0; i<auction.getSlots(); i++){

							User winner = uc.getUser(bids.get(i).getUsername());
							double priceToPay = bids.get(i+1).getBidAmount();

							auction.getWinners().put(winner.getUsername(), priceToPay);

							//Aggiunge la auction alla lista delle auction vinte dell'User winner 
							winner.getAuctionsWon().add(auction.getAuctionName());
							//Effettua l'update dell'User per aggiornare la lista delle auction vinte
							uc.updateUser(winner);
						}	
					}else{	
						// Se il numero di AuctionBid piazzate � minore o uguale del numero di slot, il numero di vincitori e' pari al numero di bids e l'ultimo vincitore paga il proprio prezzo offerto.
						for(int i=0; i<bids.size()-1; i++){
							User winner = uc.getUser(bids.get(i).getUsername());
							double priceToPay = bids.get(i+1).getBidAmount();
							auction.getWinners().put(winner.getUsername(), priceToPay);

							//Aggiunge la auction alla lista delle auction vinte dell'user 
							winner.getAuctionsWon().add(auction.getAuctionName());
							//Effettua l'update dell'User per aggiornare la lista delle auction vinte
							uc.updateUser(winner);
						}

						User winner = uc.getUser(bids.get(bids.size()-1).getUsername());
						double priceToPay = bids.get(bids.size()-1).getBidAmount();
						auction.getWinners().put(winner.getUsername(), priceToPay);

						//Aggiunge la auction alla lista delle auction vinte dell'user 
						winner.getAuctionsWon().add(auction.getAuctionName());
						//Effettua l'update dell'User per aggiornare la lista delle auction vinte
						uc.updateUser(winner);
					}
					//Effettua l'update dell'Auction per aggiornare la lista dei vincitori
					this.updateAuction(auction);
				}
			}	

		}else {
			throw new Exception("An error occured during the updating of Auction Winners. Auction was not found");
		}
	}

	
	public boolean leaveNetwork() {
		boolean result = false;
		try {
			peerDHT.peer().announceShutdown().start().awaitUninterruptibly();
			result = true;
		}catch(Exception e){
			result = false;
		}
		return result;
	}

}
