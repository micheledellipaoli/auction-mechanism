package auction.mechanism.Controller;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import auction.mechanism.Model.AddressInfo;
import auction.mechanism.Model.Auction;
import auction.mechanism.Model.AuctionBid;
import auction.mechanism.Model.User;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;

/*
 * This class tests all the Auction functionalities and relative generated exceptions.
 * Other than the registration, get and update methods, this class tests many cases where a User tries to place a bid.
*/

public class AuctionControllerTest {

	private PeerDHT peerDHT;

	@Before
	public void initialization() throws Exception {
		int peerId = 0;
		String bootPeer =  "127.0.0.1";
		int masterPort = 4000;

		try{
			Peer peer = new PeerBuilder(Number160.createHash(peerId)).ports(masterPort + peerId).start();
			peerDHT = new PeerBuilderDHT(peer).start();

			/*
             Bootstrapping operation finds an existing peer in the overlay, so that the first connection is addressed with a well known peer called "bootPeer".
             The peer needs to know the ip address where to connect the first time.
			 */

			FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(bootPeer)).ports(masterPort+peerId).start();
			fb.awaitUninterruptibly();

			if (fb.isSuccess()) {
				peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
			}
			else {
				throw new Exception("An error occurred during bootstrapping process.");
			}
		}catch(IOException e) {
			e.printStackTrace();
		}

		// Il peer elimina tutte le Auction gi� registrate da altri test per evitare conflitti.
		AuctionController ac = new AuctionController(peerDHT);
		List<String> auctionNames = ac.getAllAuctionNames();
		if(auctionNames!=null && !auctionNames.isEmpty()) {
			for(int i=0; i<auctionNames.size(); i++) {
				ac.deleteAuction(auctionNames.get(i));
			}
		}
	}

	@Test
	public void registerAuctionTest1() {
		/*
		Registra correttamente un User owner ed un'Auction.
		 */

		//Creazione oggetto User owner
		String usernameOwner = "ownerRegisterAuctionTest1";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peerDHT);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));



		//Creazione oggetto Auction
		String auctionName = "auctionTest1";
		String description = "descriptionTest";

		// Settiamo al EndDate pari al 14/01/2025, ore: 11.20
		Calendar endDate = Calendar.getInstance();
		endDate.set(2025, 0, 14, 11, 20);

		Auction x = new Auction(auctionName, description, endDate, 0, 1, owner.getUsername());

		AuctionController ac = new AuctionController(peerDHT);
		boolean response1 = ac.registerAuction(x);

		assertEquals(true, response1);
		assertNotNull(ac.getAuction(x.getAuctionName()));
		assertTrue(x.equals(ac.getAuction(x.getAuctionName())));

	}

	@Test
	public void registerAuctionTest2() throws Exception {
		/*
        Registra un'Auction con reservedPrice MINORE di 0.
        La registrazione dell'Auction fallisce.
		 */


		//Creazione oggetto User Owner
		String usernameOwner = "ownerRegisterAuctionTest2";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peerDHT);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));



		//Creazione oggetto Auction
		String auctionName = "auctionRegisterAuctionTest2";
		String description = "descriptionTest";

		// Settiamo al EndDate pari al 14/01/2025, ore: 11.20
		Calendar endDate = Calendar.getInstance();
		endDate.set(2025, 0, 14, 11, 20);

		//ReservedPrice MINORE di 0.
		double reservedPrice = -1.0;
		Auction auction = new Auction(auctionName, description, endDate, reservedPrice, 1, owner.getUsername());

		AuctionController am = new AuctionController(peerDHT);
		//Prova a registrare l'Auction con reservedPrice MINORE di 0. La registrazione fallisce e lancia un' eccezione.
		boolean response1 = am.registerAuction(auction);

		assertEquals(false, response1);
		//Il metodo getAuction lancia un'eccezione poiche' tenta di ottenere un'Auction non registrata.
		assertNull(am.getAuction(auctionName));

		List<Auction> auctionOwned = null;
		try {
			auctionOwned = uc.getAuctionsOwned(owner.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertTrue(auctionOwned.isEmpty());


	}

	@Test
	public void auctionNameAlreadyTakenTest() {
		/*
		Registra un User owner e due Auction, x e y, con lo STESSO auctionName.
		La registrazione della seconda Auction, y, fallisce.
		 */

		//Creazione oggetto User Owner
		String usernameOwner = "ownerAuctionNameAlreadyTakenTest";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peerDHT);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));



		//Creazione oggetto Auction
		String auctionName = "auctionNameAlreadyTakenTest";
		String description1 = "descriptionTest1";
		String description2 = "descriptionTest2";

		Auction x = new Auction(auctionName, description1, Calendar.getInstance(), 0, 1, owner.getUsername());
		Auction y = new Auction(auctionName, description2, Calendar.getInstance(), 0, 1, owner.getUsername());

		AuctionController ac = new AuctionController(peerDHT);
		boolean response1 = ac.registerAuction(x);
		// Prova a registrare un'Auction con auctionName gia' registrato. La response e' false, e lancia una AuctionNameAlreadyTakenException.
		boolean response2 = ac.registerAuction(y);

		assertEquals(true, response1);
		assertEquals(false, response2);
		assertNotNull(ac.getAuction(auctionName));
		assertTrue(x.equals(ac.getAuction(x.getAuctionName())));
		assertFalse(y.equals(ac.getAuction(y.getAuctionName())));
	}

	@Test
	public void getAllAuctionsTest() {
		/*
		Registra correttamente un User owner e due Auction x ed y.
		Successivamente, effettua correttamente la get di tutte le Auctions registrate.
		 */

		//Creazione oggetto User Owner
		String usernameOwner = "ownerGetAllAuctionsTest";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peerDHT);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));



		//Creazione oggetto Auction
		String auctionName1 = "auctionTest2";
		String auctionName2 = "auctionTest3";
		String description = "descriptionTest";

		Auction x = new Auction(auctionName1, description, Calendar.getInstance(), 0, 1, owner.getUsername());
		Auction y = new Auction(auctionName2, description, Calendar.getInstance(), 0, 1, owner.getUsername());

		AuctionController ac = new AuctionController(peerDHT);

		ac.registerAuction(x);
		ac.registerAuction(y);

		assertTrue(x.equals(ac.getAuction(x.getAuctionName())));
		assertTrue(y.equals(ac.getAuction(y.getAuctionName())));

		List<String> listNames = ac.getAllAuctionNames();
		assertNotNull(listNames);
		assertEquals(false, listNames.isEmpty());
		assertTrue(listNames.contains(x.getAuctionName()));
		assertTrue(listNames.contains(y.getAuctionName()));


		List<Auction> listAuctions = ac.getAllAuctions();
		assertNotNull(listAuctions);
		assertEquals(false, listAuctions.isEmpty());

	}

	@Test
	public void updateAuctionTest() {
		/*
		Effettua la registrazione di un User owner.
		Istanzia localmente un'Auction x.
		Effettua la registrazione di x.
		Modifica locamente l'istanza di x.
		Effettua correttamente la update di x.

		Successivamente, istanzia localmente un'Auction notRegistered.
		Prova ad effettuare la update di notRegistered.
		La update fallisce in quanto non esiste alcuna Auction registrata con l'username pari a quello dell'istanza di notRegistered.
		 */

		//Creazione oggetto User Owner
		String usernameOwner = "ownerUpdateAuctionTest";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peerDHT);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));



		//Creazione oggetto Auction
		String auctionName = "auctionTest4";

		String description1 = "descriptionTest";
		String description2 = "descriptionChanged";

		Auction x = new Auction(auctionName, description1, Calendar.getInstance(), 0, 1, owner.getUsername());

		AuctionController ac = new AuctionController(peerDHT);

		boolean response1 = ac.registerAuction(x);

		assertEquals(true, response1);
		assertNotNull(ac.getAuction(auctionName));
		assertTrue(x.equals(ac.getAuction(x.getAuctionName())));

		if(response1) {
			List<Auction> list = ac.getAllAuctions();
			assertNotNull(list);
			assertEquals(false, list.isEmpty());

			//Modifica la descrizione dell'istanza Auction x
			x.setDescription(description2);
			// Effettua l'update dell'Auction
			boolean response2 = ac.updateAuction(x);

			assertEquals(true, response2);
			assertNotNull(ac.getAuction(auctionName));
			assertTrue(x.equals(ac.getAuction(x.getAuctionName())));

			// Prova ad effettuare l'update di un'Auction non registrata. La response e' pari a false e lancia un'eccezione.
			Auction notRegistered = new Auction("notRegistered", description1, Calendar.getInstance(), 0, 1, owner.getUsername());
			boolean response3 = false;
			response3 = ac.updateAuction(notRegistered);

			assertEquals(false, response3);
		}        
	}

	@Test
	public void deleteAuctionTest() {

		//Creazione oggetto User Owner
		String usernameOwner = "ownerDeleteAuctionTest";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peerDHT);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));



		//Creazione oggetto Auction
		String auctionName = "auctionDeleteTest";
		String description = "descriptionTest";

		Auction x = new Auction(auctionName, description, Calendar.getInstance(), 0, 1, owner.getUsername());

		AuctionController ac = new AuctionController(peerDHT);
		ac.registerAuction(x);

		assertNotNull(ac.getAuction(auctionName));
		assertTrue(x.equals(ac.getAuction(x.getAuctionName())));

		boolean response1 = ac.deleteAuction(auctionName);

		assertEquals(true, response1);
		//Il metodo getAuction lancia un'eccezione poich� tenta di ottenere un'Auction cancellata poco prima.
		assertNull(ac.getAuction(auctionName));
	}

	@Test
	public void placeABidTest1() throws Exception {
		/*
		Effettua correttamente la registrazione di un User owner, di un'Auction, di un User bidder.
		L'User bidder piazza un'AuctionBid valida per l'Auction creata.
		 */

		//Creazione oggetto User Owner
		String usernameOwner = "ownerPlaceABidTest1";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peerDHT);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));




		//Creazione oggetto Auction
		String auctionName = "auctionPlaceABidTest1";
		String description = "descriptionTest";

		// Settiamo al EndDate pari al 14/01/2025, ore: 11.20
		Calendar endDate = Calendar.getInstance();
		endDate.set(2025, 0, 14, 11, 20);

		Auction auction = new Auction(auctionName, description, endDate, 0, 1, owner.getUsername());

		AuctionController ac = new AuctionController(peerDHT);
		boolean response1 = ac.registerAuction(auction);

		assertEquals(true, response1);
		assertNotNull(ac.getAuction(auction.getAuctionName()));
		assertTrue(auction.equals(ac.getAuction(auction.getAuctionName())));

		List<Auction> auctionOwned = null;
		try {
			auctionOwned = uc.getAuctionsOwned(owner.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		//System.out.println(auctionOwned.toString());
		assertFalse(auctionOwned.isEmpty());



		//Creazione oggetto User che prova ad effettuare l'offerta
		String username = "bidderPlaceABidTest1";
		String password = "passwordTest";
		AddressInfo addressInfo = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
		String emailPayPal = "angelobianchi@gmail.com";
		User bidder = new User(username, password, addressInfo, emailPayPal);

		boolean response2 = uc.registerUser(bidder);

		assertEquals(true, response2);
		assertNotNull(uc.getUser(bidder.getUsername()));
		assertTrue(bidder.equals(uc.getUser(bidder.getUsername())));




		//Creazione oggetto AuctionBid
		AuctionBid bid = new AuctionBid(auction.getAuctionName(), bidder.getUsername(), 5.0);

		boolean response3;
		try {
			response3 = ac.placeABid(bid);
		}catch(Exception e) {
			e.printStackTrace();
			response3 = false;
		}

		assertEquals(true, response3);


		// Ottiene la lista delle Bid piazzate sull'Auction.
		List<AuctionBid> auctionBidsByAuction = null;
		try {
			auctionBidsByAuction = ac.getAuctionBidsByAuction(auction.getAuctionName());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuction);

		// Ottiene la lista delle Bid piazzate sull'Auction auction dall'User bidder.
		List<AuctionBid> auctionBidsByAuctionAndUsername = null;
		try {
			auctionBidsByAuctionAndUsername = ac.getAuctionBidsByAuctionAndUsername(auction.getAuctionName(), bidder.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuctionAndUsername);

		// Ottiene la pi� alta offerta piazzata sull'Auction auction dall'User bidder.
		AuctionBid greatestAuctionBidPlacedByAnUser = null;
		try {
			greatestAuctionBidPlacedByAnUser = ac.getTheHighestAuctionBidPlacedByAnUser(auction.getAuctionName(), bidder.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(greatestAuctionBidPlacedByAnUser);

		// Ottiene la lista delle Auction alle quali ha partecipato l'User bidder.
		List<Auction> auctionJoined = null;
		try {
			auctionJoined = uc.getAuctionsJoined(bidder.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertFalse(auctionJoined.isEmpty());

	}

	@Test
	public void placeABidTest2() throws Exception {
		/*
		Effettua la registrazione di un User owner e di un'Auction.
        L'User owner dell'Auction prova ad effettuare un'offerta alla sua stessa Auction.
        Il metodo placeABid fallise, in quanto l'owner non puo' piazzare un'offerta sulla sua Auction.
		 */


		//Creazione oggetto User Owner
		String usernameOwner = "ownerPlaceABidTest2";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peerDHT);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));




		//Creazione oggetto Auction
		String auctionName = "auctionPlaceABidTest2";
		String description = "descriptionTest";

		// Settiamo al EndDate pari al 14/01/2025, ore: 11.20
		Calendar endDate = Calendar.getInstance();
		endDate.set(2025, 0, 14, 11, 20);

		Auction auction = new Auction(auctionName, description, endDate, 0, 1, owner.getUsername());

		AuctionController ac = new AuctionController(peerDHT);
		boolean response1 = ac.registerAuction(auction);

		assertEquals(true, response1);
		assertNotNull(ac.getAuction(auction.getAuctionName()));
		assertTrue(auction.equals(ac.getAuction(auction.getAuctionName())));


		List<Auction> auctionOwned = null;
		try {
			auctionOwned = uc.getAuctionsOwned(owner.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertFalse(auctionOwned.isEmpty());




		//Creazione oggetto AuctionBid
		AuctionBid bid = new AuctionBid(auction.getAuctionName(), owner.getUsername(), 15.0);
		boolean response3;
		try {
			response3 = ac.placeABid(bid);
		}catch(Exception e) {
			//e.printStackTrace();
			response3 = false;
		}
		assertEquals(false, response3);



		// Ottiene la lista delle Bid piazzate sull'Auction.
		List<AuctionBid> auctionBidsByAuction = null;
		try {
			auctionBidsByAuction = ac.getAuctionBidsByAuction(auction.getAuctionName());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertNull(auctionBidsByAuction);

		// Ottiene la lista delle Bid piazzate sull'Auction dall'User owner.
		List<AuctionBid> auctionBidsByAuctionAndUsername = null;
		try {
			auctionBidsByAuctionAndUsername = ac.getAuctionBidsByAuctionAndUsername(auction.getAuctionName(), owner.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertNull(auctionBidsByAuctionAndUsername);

		// Ottiene la pi� alta offerta piazzata sull'Auction dall'User owner.
		AuctionBid greatestAuctionBidPlacedByAnUser = null;
		try {
			greatestAuctionBidPlacedByAnUser = ac.getTheHighestAuctionBidPlacedByAnUser(auction.getAuctionName(), owner.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertNull(greatestAuctionBidPlacedByAnUser);

		// Ottiene la lista delle Auction alle quali l'User owner ha partecipato.
		List<Auction> auctionJoined = null;
		try {
			auctionJoined = uc.getAuctionsJoined(owner.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertTrue(auctionJoined.isEmpty());
	}

	@Test
	public void placeABidTest3() throws Exception {
		/*
		Effettua la registrazione di un User owner, di un User bidder, e di un'Auction con endDate pari al momento ATTUALE. 
        Automaticamente, il costruttore dell'oggetto Auction setta lo status della Auction a "ended", dunque non sara' possibile effettuare un'offerta.
		Pertanto, il metodo placeABid fallisce.
		 */


		//Creazione oggetto User Owner
		String usernameOwner = "ownerPlaceABidTest3";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peerDHT);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));




		//Creazione oggetto Auction
		String auctionName = "auctionPlaceABidTest3";
		String description = "descriptionTest";

		// endDate pari al momento ATTUALE
		Calendar endDate = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));

		Auction auction = new Auction(auctionName, description, endDate, 0, 1, owner.getUsername());

		AuctionController ac = new AuctionController(peerDHT);
		boolean response1 = ac.registerAuction(auction);

		assertEquals(true, response1);
		assertNotNull(ac.getAuction(auction.getAuctionName()));
		assertTrue(auction.equals(ac.getAuction(auction.getAuctionName())));


		List<Auction> auctionOwned = null;
		try {
			auctionOwned = uc.getAuctionsOwned(owner.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertFalse(auctionOwned.isEmpty());




		//Creazione oggetto User che prova ad effettuare l'offerta
		String username = "bidderPlaceABidTest3";
		String password = "passwordTest";
		AddressInfo addressInfo = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
		String emailPayPal = "angelobianchi@gmail.com";
		User bidder = new User(username, password, addressInfo, emailPayPal);

		boolean response2 = uc.registerUser(bidder);

		assertEquals(true, response2);
		assertNotNull(uc.getUser(bidder.getUsername()));
		assertTrue(bidder.equals(uc.getUser(bidder.getUsername())));





		//Creazione oggetto AuctionBid
		AuctionBid bid = new AuctionBid(auction.getAuctionName(), bidder.getUsername(), 15.0);
		boolean response3;
		try {
			response3 = ac.placeABid(bid);
		}catch(Exception e) {
			//e.printStackTrace();
			response3 = false;
		}

		assertEquals(false, response3);



		// Ottiene la lista delle Bid piazzate sull'Auction.		
		List<AuctionBid> auctionBidsByAuction = null;
		try {
			auctionBidsByAuction = ac.getAuctionBidsByAuction(auction.getAuctionName());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertNull(auctionBidsByAuction);

		// Ottiene la lista delle Bid piazzate sull'Auction auction dall'User bidder.
		List<AuctionBid> auctionBidsByAuctionAndUsername = null;
		try {
			auctionBidsByAuctionAndUsername = ac.getAuctionBidsByAuctionAndUsername(auction.getAuctionName(), bidder.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertNull(auctionBidsByAuctionAndUsername);

		// Ottiene la pi� alta offerta piazzata sull'Auction auction dall'User bidder.
		AuctionBid greatestAuctionBidPlacedByAnUser = null;
		try {
			greatestAuctionBidPlacedByAnUser = ac.getTheHighestAuctionBidPlacedByAnUser(auction.getAuctionName(), bidder.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertNull(greatestAuctionBidPlacedByAnUser);


		List<Auction> auctionJoined = null;
		try {
			auctionJoined = uc.getAuctionsJoined(bidder.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertTrue(auctionJoined.isEmpty());    
	}

	@Test
	public void placeABidTest4() throws Exception {
		/*
    	Effettua la registrazione di un User owner, di un User bidder, e di un'Auction con reservedPrice pari a 50.
    	L'User bidder prova ad effettuare un'offerta con BidAmount pari a 20.
    	L'offerta auctionBid non potr� essere effettuata in quanto auctionBid.BidAmount � minore di reservedPrice.
		Pertanto, il metodo placeABid fallisce.		
		 */


		//Creazione oggetto User Owner
		String usernameOwner = "ownerPlaceABidTest4";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peerDHT);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));


		//Creazione oggetto Auction
		String auctionName = "auctionPlaceABidTest4";
		String description = "descriptionTest";

		// Settiamo al EndDate pari al 14/01/2025, ore: 11.20
		Calendar endDate = Calendar.getInstance();
		endDate.set(2025, 0, 14, 11, 20);

		double reservedPrice = 50.0;
		Auction auction = new Auction(auctionName, description, endDate, reservedPrice, 1, owner.getUsername());

		AuctionController ac = new AuctionController(peerDHT);
		boolean response1 = ac.registerAuction(auction);

		assertEquals(true, response1);
		assertNotNull(ac.getAuction(auction.getAuctionName()));
		assertTrue(auction.equals(ac.getAuction(auction.getAuctionName())));

		List<Auction> auctionOwned = null;
		try {
			auctionOwned = uc.getAuctionsOwned(owner.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertFalse(auctionOwned.isEmpty());





		//Creazione oggetto User
		String username = "bidderPlaceABidTest4";
		String password = "passwordTest";
		AddressInfo addressInfo = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
		String emailPayPal = "angelobianchi@gmail.com";
		User bidder = new User(username, password, addressInfo, emailPayPal);

		boolean response2 = uc.registerUser(bidder);

		assertEquals(true, response2);
		assertNotNull(uc.getUser(bidder.getUsername()));
		assertTrue(bidder.equals(uc.getUser(bidder.getUsername())));






		//Creazione oggetto AuctionBid con bidAmount pari a 20.
		AuctionBid bid = new AuctionBid(auction.getAuctionName(), bidder.getUsername(), 20.0);
		boolean response3;
		try {
			response3 = ac.placeABid(bid);
		}catch(Exception e) {
			//e.printStackTrace();
			response3 = false;
		}

		assertEquals(false, response3);


		// Ottiene la lista delle Bid piazzate sull'Auction.				
		List<AuctionBid> auctionBidsByAuction = null;
		try {
			auctionBidsByAuction = ac.getAuctionBidsByAuction(auction.getAuctionName());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertNull(auctionBidsByAuction);

		// Ottiene la lista delle Bid piazzate sull'Auction auction dall'User bidder.
		List<AuctionBid> auctionBidsByAuctionAndUsername = null;
		try {
			auctionBidsByAuctionAndUsername = ac.getAuctionBidsByAuctionAndUsername(auction.getAuctionName(), bidder.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertNull(auctionBidsByAuctionAndUsername);

		// Ottiene la piu' alta offerta piazzata sull'Auction auction dall'User bidder.
		AuctionBid greatestAuctionBidPlacedByAnUser = null;
		try {
			greatestAuctionBidPlacedByAnUser = ac.getTheHighestAuctionBidPlacedByAnUser(auction.getAuctionName(), bidder.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertNull(greatestAuctionBidPlacedByAnUser);

		// Ottiene la lista delle Auction alle quali ha partecipato l'User bidder.
		List<Auction> auctionJoined = null;
		try {
			auctionJoined = uc.getAuctionsJoined(bidder.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertTrue(auctionJoined.isEmpty());
	}

	@Test
	public void placeABidTest5() throws Exception {
		/*
    	Effettua la registrazione di un User owner, di un User bidder, e di un'Auction.
    	L'User bidder prova ad effettuare un'offerta ma NON E' ABILITATO ad effettuare l'offerta, in quanto la sua email PayPal ha un formato errato.
    	Pertanto, il metodo placeABid fallisce e l'offerta auctionBid non viene effettuata.
		 */


		//Creazione oggetto User Owner
		String usernameOwner = "ownerPlaceABidTest5";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peerDHT);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));


		//Creazione oggetto Auction
		String auctionName = "auctionPlaceABidTest5";
		String description = "descriptionTest";

		// Settiamo al EndDate pari al 14/01/2025, ore: 11.20
		Calendar endDate = Calendar.getInstance();
		endDate.set(2025, 0, 14, 11, 20);

		Auction auction = new Auction(auctionName, description, endDate, 0, 1, owner.getUsername());

		AuctionController ac = new AuctionController(peerDHT);
		boolean response1 = ac.registerAuction(auction);

		assertEquals(true, response1);
		assertNotNull(ac.getAuction(auction.getAuctionName()));
		assertTrue(auction.equals(ac.getAuction(auction.getAuctionName())));

		List<Auction> auctionOwned = null;
		try {
			auctionOwned = uc.getAuctionsOwned(owner.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertFalse(auctionOwned.isEmpty());





		//Creazione oggetto User
		String username = "bidderPlaceABidTest5";
		String password = "passwordTest";
		AddressInfo addressInfo = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
		//Email PayPal con FORMATO ERRATO
		String emailPayPal = "@@@@@@gmail.com";
		User bidder = new User(username, password, addressInfo, emailPayPal);

		boolean response2 = uc.registerUser(bidder);

		assertEquals(true, response2);
		assertNotNull(uc.getUser(bidder.getUsername()));
		assertTrue(bidder.equals(uc.getUser(bidder.getUsername())));






		//Creazione oggetto AuctionBid con bidAmount pari a 20.
		AuctionBid bid = new AuctionBid(auction.getAuctionName(), bidder.getUsername(), 20.0);
		boolean response3;
		try {
			response3 = ac.placeABid(bid);
		}catch(Exception e) {
			//e.printStackTrace();
			response3 = false;
		}

		assertEquals(false, response3);



		// Ottiene la lista delle Bid piazzate sull'Auction.
		List<AuctionBid> auctionBidsByAuction = null;
		try {
			auctionBidsByAuction = ac.getAuctionBidsByAuction(auction.getAuctionName());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertNull(auctionBidsByAuction);

		// Ottiene la lista delle Bid piazzate sull'Auction auction dall'User bidder.
		List<AuctionBid> auctionBidsByAuctionAndUsername = null;
		try {
			auctionBidsByAuctionAndUsername = ac.getAuctionBidsByAuctionAndUsername(auction.getAuctionName(), bidder.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertNull(auctionBidsByAuctionAndUsername);

		// Ottiene la piu' alta offerta piazzata sull'Auction auction dall'User bidder.
		AuctionBid greatestAuctionBidPlacedByAnUser = null;
		try {
			greatestAuctionBidPlacedByAnUser = ac.getTheHighestAuctionBidPlacedByAnUser(auction.getAuctionName(), bidder.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertNull(greatestAuctionBidPlacedByAnUser);

		// Ottiene la lista delle Auction alle quali ha partecipato l'User bidder.
		List<Auction> auctionJoined = null;
		try {
			auctionJoined = uc.getAuctionsJoined(bidder.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertTrue(auctionJoined.isEmpty());
	}

	@Test
	public void placeABidTest6() throws Exception {
		/*
    	Effettua la registrazione di un User owner, di un User bidder, e di un'Auction.
    	Lo STESSO User bidder prova a piazzare due AuctionBid.
    	La seconda volta che prova ad effettuare l'offerta, il metodo placeABid fallisce poiche' ha gia' effettuato un'offerta che risulta momentaneamente vincente.
		 */


		//Creazione oggetto User Owner
		String usernameOwner = "ownerpPlaceABidTest6";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peerDHT);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));


		//Creazione oggetto Auction
		String auctionName = "auctionPlaceABidTest6";
		String description = "descriptionTest";

		// Settiamo al EndDate pari al 14/01/2025, ore: 11.20
		Calendar endDate = Calendar.getInstance();
		endDate.set(2025, 0, 14, 11, 20);

		double reservedPrice = 0;
		Auction auction = new Auction(auctionName, description, endDate, reservedPrice, 1, owner.getUsername());

		AuctionController ac = new AuctionController(peerDHT);
		boolean response1 = ac.registerAuction(auction);

		assertEquals(true, response1);
		assertNotNull(ac.getAuction(auction.getAuctionName()));
		assertTrue(auction.equals(ac.getAuction(auction.getAuctionName())));

		List<Auction> auctionOwned = null;
		try {
			auctionOwned = uc.getAuctionsOwned(owner.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertFalse(auctionOwned.isEmpty());





		//Creazione oggetto User bidder
		String username = "bidderPlaceABidTest6";
		String password = "passwordTest";
		AddressInfo addressInfo = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
		String emailPayPal = "angelobianchi@gmail.com";
		User bidder = new User(username, password, addressInfo, emailPayPal);

		boolean response2 = uc.registerUser(bidder);

		assertEquals(true, response2);
		assertNotNull(uc.getUser(bidder.getUsername()));
		assertTrue(bidder.equals(uc.getUser(bidder.getUsername())));






		//Creazione oggetto AuctionBid bid1 con bidAmount pari a 20.
		AuctionBid bid1 = new AuctionBid(auction.getAuctionName(), bidder.getUsername(), 20.0);
		boolean response3;
		try {
			response3 = ac.placeABid(bid1);
		}catch(Exception e) {
			//e.printStackTrace();
			response3 = false;
		}

		assertEquals(true, response3);


		// Ottiene la lista delle Bid piazzate sull'Auction.
		List<AuctionBid> auctionBidsByAuction = null;
		try {
			auctionBidsByAuction = ac.getAuctionBidsByAuction(auction.getAuctionName());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuction);

		// Ottiene la lista delle Bid piazzate sull'Auction auction dall'User bidder.
		List<AuctionBid> auctionBidsByAuctionAndUsername = null;
		try {
			auctionBidsByAuctionAndUsername = ac.getAuctionBidsByAuctionAndUsername(auction.getAuctionName(), bidder.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuctionAndUsername);

		// Ottiene la piu' alta offerta piazzata sull'Auction auction dall'User bidder.
		AuctionBid greatestAuctionBidPlacedByAnUser = null;
		try {
			greatestAuctionBidPlacedByAnUser = ac.getTheHighestAuctionBidPlacedByAnUser(auction.getAuctionName(), bidder.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(greatestAuctionBidPlacedByAnUser);

		// Ottiene la lista delle Auction alle quali ha partecipato l'User bidder.
		List<Auction> auctionJoined = null;
		try {
			auctionJoined = uc.getAuctionsJoined(bidder.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertFalse(auctionJoined.isEmpty());





		//Creazione oggetto AuctionBid bid2 con bidAmount pari a 25.
		AuctionBid bid2 = new AuctionBid(auction.getAuctionName(), bidder.getUsername(), 25.0);
		boolean response4;
		try {
			response4 = ac.placeABid(bid2);
		}catch(Exception e) {
			//e.printStackTrace();
			response4 = false;
		}

		assertEquals(false, response4);


		// Ottiene la lista delle Bid piazzate sull'Auction.
		try {
			auctionBidsByAuction = ac.getAuctionBidsByAuction(auction.getAuctionName());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuction);
		assertTrue(auctionBidsByAuction.size()==1);

		// Ottiene la lista delle Bid piazzate sull'Auction auction dall'User bidder.
		try {
			auctionBidsByAuctionAndUsername = ac.getAuctionBidsByAuctionAndUsername(auction.getAuctionName(), bidder.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuctionAndUsername);
		assertTrue(auctionBidsByAuctionAndUsername.size()==1);

		// Ottiene la pi� alta offerta piazzata sull'Auction auction dall'User bidder.
		try {
			greatestAuctionBidPlacedByAnUser = ac.getTheHighestAuctionBidPlacedByAnUser(auction.getAuctionName(), bidder.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(greatestAuctionBidPlacedByAnUser);
		assertTrue(greatestAuctionBidPlacedByAnUser.getBidAmount() == bid1.getBidAmount());

		// Ottiene la lista delle Auction alle quali ha partecipato l'User bidder.
		try {
			auctionJoined = uc.getAuctionsJoined(bidder.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertFalse(auctionJoined.isEmpty());
		assertTrue(auctionJoined.size()==1);

	}

	@Test
	public void placeABidTest7() throws Exception {
		/*
    	Effettua la registrazione di un User owner e di un'Auction.
    	Successivamente, registra due User bidder1 e bidder2.
    	bidder1 effettua un'offerta sull'auction, con bidAmount pari a 10.0
    	bidder2 effettua un'offerta sull'auction, con bidAmount pari a 9.99
    	L'offerta di bidder2 non viene registrata in quanto MINORE di quella attualmente vincente, effettuata da bidder1.
		 */


		//Creazione oggetto User Owner
		String usernameOwner = "ownerPlaceABidTest7";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peerDHT);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));


		//Creazione oggetto Auction
		String auctionName = "auctionPlaceABidTest7";
		String description = "descriptionTest";

		// Settiamo al EndDate pari al 14/01/2025, ore: 11.20
		Calendar endDate = Calendar.getInstance();
		endDate.set(2025, 0, 14, 11, 20);

		double reservedPrice = 0;
		Auction auction = new Auction(auctionName, description, endDate, reservedPrice, 1, owner.getUsername());

		AuctionController ac = new AuctionController(peerDHT);
		boolean response1 = ac.registerAuction(auction);

		assertEquals(true, response1);
		assertNotNull(ac.getAuction(auction.getAuctionName()));
		assertTrue(auction.equals(ac.getAuction(auction.getAuctionName())));

		List<Auction> auctionOwned = null;
		try {
			auctionOwned = uc.getAuctionsOwned(owner.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertFalse(auctionOwned.isEmpty());





		//Creazione oggetto User bidder1
		String username1 = "bidder1PlaceABidTest7";
		String password1 = "passwordTest";
		AddressInfo addressInfo1 = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
		String emailPayPal1 = "angelobianchi@gmail.com";
		User bidder1 = new User(username1, password1, addressInfo1, emailPayPal1);

		boolean response2 = uc.registerUser(bidder1);

		assertEquals(true, response2);
		assertNotNull(uc.getUser(bidder1.getUsername()));
		assertTrue(bidder1.equals(uc.getUser(bidder1.getUsername())));



		//Creazione oggetto User bidder2
		String username2 = "bidder2PlaceABidTest7";
		String password2 = "passwordTest";
		AddressInfo addressInfo2 = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
		String emailPayPal2 = "angelobianchi@gmail.com";
		User bidder2 = new User(username2, password2, addressInfo2, emailPayPal2);

		boolean response3 = uc.registerUser(bidder2);

		assertEquals(true, response3);
		assertNotNull(uc.getUser(bidder2.getUsername()));
		assertTrue(bidder2.equals(uc.getUser(bidder2.getUsername())));






		//Creazione oggetto AuctionBid bid1 con bidAmount pari a 10.0
		AuctionBid bid1 = new AuctionBid(auction.getAuctionName(), bidder1.getUsername(), 10.0);
		boolean response4;
		try {
			response4 = ac.placeABid(bid1);
		}catch(Exception e) {
			//e.printStackTrace();
			response4 = false;
		}

		assertEquals(true, response4);



		// Ottiene la lista delle Bid piazzate sull'Auction.
		List<AuctionBid> auctionBidsByAuction = null;
		try {
			auctionBidsByAuction = ac.getAuctionBidsByAuction(auction.getAuctionName());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuction);

		// Ottiene la lista delle Bid piazzate sull'Auction auction dall'User bidder1.
		List<AuctionBid> auctionBidsByAuctionAndUsername1 = null;
		try {
			auctionBidsByAuctionAndUsername1 = ac.getAuctionBidsByAuctionAndUsername(auction.getAuctionName(), bidder1.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuctionAndUsername1);

		// Ottiene la piu' alta offerta piazzata sull'Auction auction dall'User bidder1.
		AuctionBid greatestAuctionBidPlacedByAnUser1 = null;
		try {
			greatestAuctionBidPlacedByAnUser1 = ac.getTheHighestAuctionBidPlacedByAnUser(auction.getAuctionName(), bidder1.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(greatestAuctionBidPlacedByAnUser1);

		// Ottiene la lista delle Auction alle quali ha partecipato l'User bidder1.	
		List<Auction> auctionJoined1 = null;
		try {
			auctionJoined1 = uc.getAuctionsJoined(bidder1.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertFalse(auctionJoined1.isEmpty());





		//Creazione oggetto AuctionBid bid2 con bidAmount pari a 9.99.
		AuctionBid bid2 = new AuctionBid(auction.getAuctionName(), bidder2.getUsername(), 9.99);
		boolean response5;
		try {
			//Prova ad effettuare l'offerta con bidAmount minore del reservedPrice. Il metodo placeABid() fallisce e lancia un'eccezione.
			response5 = ac.placeABid(bid2);
		}catch(Exception e) {
			e.printStackTrace();
			response5 = false;
		}

		assertEquals(false, response5);


		// Ottiene la lista delle Bid piazzate sull'Auction.
		try {
			auctionBidsByAuction = ac.getAuctionBidsByAuction(auction.getAuctionName());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuction);
		assertTrue(auctionBidsByAuction.size()==1);

		// Ottiene la lista delle Bid piazzate sull'Auction auction dall'User bidder2.
		List<AuctionBid> auctionBidsByAuctionAndUsername2 = null;
		try {
			// L'user bidder2 ha provato ad effettuare un'offerta con bidAmount minore del reservedPrice. Per tale motivo, il metodo placeABid non registra l'offerta.
			// Pertanto, non vi e' alcuna offerta piazzata sull'asta selezionata, quindi il metodo getAuctionBidsByAuctionAndUsername() lancia un'eccezione.
			auctionBidsByAuctionAndUsername2 = ac.getAuctionBidsByAuctionAndUsername(auction.getAuctionName(), bidder2.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNull(auctionBidsByAuctionAndUsername2);

		// Ottiene la lista delle Bid piazzate sull'Auction auction dall'User bidder2.
		AuctionBid greatestAuctionBidPlacedByAnUser2 = null;
		try {
			// L'user bidder2 ha provato ad effettuare un'offerta con bidAmount minore del reservedPrice. Per tale motivo, il metodo placeABid non registra l'offerta.
			// Pertanto, non vi e' alcuna offerta piazzata sull'asta selezionata, quindi il metodo getTheGreatestAuctionBidPlacedByAnUser() lancia un'eccezione.
			greatestAuctionBidPlacedByAnUser2 = ac.getTheHighestAuctionBidPlacedByAnUser(auction.getAuctionName(), bidder2.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNull(greatestAuctionBidPlacedByAnUser2);

		// Ottiene la lista delle Auction alle quali ha partecipato l'User bidder2.	
		List<Auction> auctionJoined2 = null;
		try {
			auctionJoined2 = uc.getAuctionsOwned(bidder2.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertTrue(auctionJoined2.isEmpty());
	}

	@Test
	public void placeABidTest8() throws Exception {
		/*
    	Effettua la registrazione di un User owner e di un'Auction.
    	Successivamente, registra due User bidder1 e bidder2.
    	bidder1 effettua un'offerta sull'auction, con bidAmount pari a 10.0
    	bidder2 effettua un'offerta sull'auction, con bidAmount pari a 11.0
    	Entrambe le offerte vengono registrate correttamente.
		 */


		//Creazione oggetto User Owner
		String usernameOwner = "ownerPlaceABidTest8";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peerDHT);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(usernameOwner));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));


		//Creazione oggetto Auction
		String auctionName = "auctionPlaceABidTest8";
		String description = "descriptionTest";

		// Settiamo al EndDate pari al 14/01/2025, ore: 11.20
		Calendar endDate = Calendar.getInstance();
		endDate.set(2025, 0, 14, 11, 20);

		double reservedPrice = 0;
		Auction auction = new Auction(auctionName, description, endDate, reservedPrice, 1, owner.getUsername());

		AuctionController ac = new AuctionController(peerDHT);
		boolean response1 = ac.registerAuction(auction);

		assertEquals(true, response1);
		assertNotNull(ac.getAuction(auction.getAuctionName()));
		assertTrue(auction.equals(ac.getAuction(auction.getAuctionName())));

		List<Auction> auctionOwned = null;
		try {
			auctionOwned = uc.getAuctionsOwned(owner.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertFalse(auctionOwned.isEmpty());





		//Creazione oggetto User bidder1
		String username1 = "bidder1PlaceABidTest8";
		String password1 = "passwordTest";
		AddressInfo addressInfo1 = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
		String emailPayPal1 = "angelobianchi@gmail.com";
		User bidder1 = new User(username1, password1, addressInfo1, emailPayPal1);

		boolean response2 = uc.registerUser(bidder1);

		assertEquals(true, response2);
		assertNotNull(uc.getUser(bidder1.getUsername()));
		assertTrue(bidder1.equals(uc.getUser(bidder1.getUsername())));


		//Creazione oggetto User bidder2
		String username2 = "bidder2PlaceABidTest8";
		String password2 = "passwordTest";
		AddressInfo addressInfo2 = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
		String emailPayPal2 = "angelobianchi@gmail.com";
		User bidder2 = new User(username2, password2, addressInfo2, emailPayPal2);

		boolean response3 = uc.registerUser(bidder2);

		assertEquals(true, response3);
		assertNotNull(uc.getUser(bidder2.getUsername()));
		assertTrue(bidder2.equals(uc.getUser(bidder2.getUsername())));




		//Creazione oggetto AuctionBid bid1 con bidAmount pari a 10.0
		AuctionBid bid1 = new AuctionBid(auction.getAuctionName(), bidder1.getUsername(), 10.0);
		boolean response4;
		try {
			response4 = ac.placeABid(bid1);
		}catch(Exception e) {
			//e.printStackTrace();
			response4 = false;
		}

		assertEquals(true, response4);


		// Ottiene la lista delle Bid piazzate sull'Auction.
		List<AuctionBid> auctionBidsByAuction = null;
		try {
			auctionBidsByAuction = ac.getAuctionBidsByAuction(auction.getAuctionName());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuction);

		// Ottiene la lista delle Bid piazzate sull'Auction auction dall'User bidder1.
		List<AuctionBid> auctionBidsByAuctionAndUsername1 = null;
		try {
			auctionBidsByAuctionAndUsername1 = ac.getAuctionBidsByAuctionAndUsername(auction.getAuctionName(), bidder1.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuctionAndUsername1);

		// Ottiene la pi� alta offerta piazzata sull'Auction auction dall'User bidder1.
		AuctionBid greatestAuctionBidPlacedByAnUser1 = null;
		try {
			greatestAuctionBidPlacedByAnUser1 = ac.getTheHighestAuctionBidPlacedByAnUser(auction.getAuctionName(), bidder1.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(greatestAuctionBidPlacedByAnUser1);

		// Ottiene la lista delle Auction alle quali ha partecipato l'User bidder1.	
		List<Auction> auctionJoined1 = null;
		try {
			auctionJoined1 = uc.getAuctionsJoined(bidder1.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertFalse(auctionJoined1.isEmpty());





		//Creazione oggetto AuctionBid bid2 con bidAmount pari a 9.99.
		AuctionBid bid2 = new AuctionBid(auction.getAuctionName(), bidder2.getUsername(), 11.00);
		boolean response5;
		try {
			response5 = ac.placeABid(bid2);
		}catch(Exception e) {
			e.printStackTrace();
			response5 = false;
		}

		assertEquals(true, response5);

		// Ottiene la lista delle Bid piazzate sull'Auction.
		try {
			auctionBidsByAuction = ac.getAuctionBidsByAuction(auction.getAuctionName());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuction);
		assertTrue(auctionBidsByAuction.size()==2);

		// Ottiene la lista delle Bid piazzate sull'Auction auction dall'User bidder2.
		List<AuctionBid> auctionBidsByAuctionAndUsername2 = null;
		try {
			auctionBidsByAuctionAndUsername2 = ac.getAuctionBidsByAuctionAndUsername(auction.getAuctionName(), bidder2.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuctionAndUsername2);

		// Ottiene la piu' alta offerta piazzata sull'Auction auction dall'User bidder2.
		AuctionBid greatestAuctionBidPlacedByAnUser2 = null;
		try {
			greatestAuctionBidPlacedByAnUser2 = ac.getTheHighestAuctionBidPlacedByAnUser(auction.getAuctionName(), bidder2.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(greatestAuctionBidPlacedByAnUser2);

		// Ottiene la lista delle Auction alle quali ha partecipato l'User bidder2.	
		List<Auction> auctionJoined2 = null;
		try {
			auctionJoined2 = uc.getAuctionsJoined(bidder2.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertFalse(auctionJoined2.isEmpty());
	}

	@Test
	public void placeABidTest9() throws Exception {
		/*
    	Effettua la registrazione di un User owner e di un'Auction con 2 slot disponibili e con endDate pari a 10 secondi dall'istanziazione.
    	Successivamente, registra tre User bidder1, bidder2 e bidder3.
    	bidder1 effettua un'offerta sull'auction, con bidAmount pari a 10.0
    	bidder2 effettua un'offerta sull'auction, con bidAmount pari a 15.0
    	bidder3 effettua un'offerta sull'auction, con bidAmount pari a 20.0

    	Si mette in pausa il test per 10 secondi, in modo da far scadere l'asta.
    	bidder3 e bidder2 risultano vincitori dell'asta.
    	bidder3 dovra' pagare il prezzo offerto da bidder2, ovvero 15.
    	bidder2 dovra' pagare il prezzo offerto da bidder1, ovvero 10.
		 */


		//Creazione oggetto User Owner
		String usernameOwner = "ownerPlaceABidTest9";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peerDHT);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));


		//Creazione oggetto Auction
		String auctionName = "auctionPlaceABidTest9";
		String description = "descriptionTest";

		// Settiamo al EndDate pari a 10 secondi dal momento in cui l'istanza viene creata.
		Calendar endDate = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
		long t = endDate.getTimeInMillis();
		Date endDate1 = new Date(t + (10000));
		endDate.setTime(endDate1);

		int slots = 2;
		double reservedPrice = 5;
		Auction auction = new Auction(auctionName, description, endDate, reservedPrice, slots, owner.getUsername());

		AuctionController ac = new AuctionController(peerDHT);
		boolean response1 = ac.registerAuction(auction);

		assertEquals(true, response1);
		assertNotNull(ac.getAuction(auction.getAuctionName()));
		assertTrue(auction.equals(ac.getAuction(auction.getAuctionName())));

		List<Auction> auctionOwned = null;
		try {
			auctionOwned = uc.getAuctionsOwned(owner.getUsername());
		}catch(Exception e) {
			//e.printStackTrace();
		}
		assertFalse(auctionOwned.isEmpty());





		//Creazione oggetto User bidder1
		String username1 = "bidder1PlaceABidTest9";
		String password1 = "passwordTest";
		AddressInfo addressInfo1 = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
		String emailPayPal1 = "angelobianchi@gmail.com";
		User bidder1 = new User(username1, password1, addressInfo1, emailPayPal1);

		boolean response2 = uc.registerUser(bidder1);

		assertEquals(true, response2);
		assertNotNull(uc.getUser(bidder1.getUsername()));
		assertTrue(bidder1.equals(uc.getUser(bidder1.getUsername())));


		//Creazione oggetto User bidder2
		String username2 = "bidder2PlaceABidTest9";
		String password2 = "passwordTest";
		AddressInfo addressInfo2 = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
		String emailPayPal2 = "angelobianchi@gmail.com";
		User bidder2 = new User(username2, password2, addressInfo2, emailPayPal2);

		boolean response3 = uc.registerUser(bidder2);

		assertEquals(true, response3);
		assertNotNull(uc.getUser(bidder2.getUsername()));
		assertTrue(bidder2.equals(uc.getUser(bidder2.getUsername())));

		//Creazione oggetto User bidder3
		String username3 = "bidder3PlaceABidTest9";
		String password3 = "passwordTest";
		AddressInfo addressInfo3 = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
		String emailPayPal3 = "angelobianchi@gmail.com";
		User bidder3 = new User(username3, password3, addressInfo3, emailPayPal3);

		boolean response4 = uc.registerUser(bidder3);

		assertEquals(true, response4);
		assertNotNull(uc.getUser(bidder3.getUsername()));
		assertTrue(bidder3.equals(uc.getUser(bidder3.getUsername())));





		//Creazione oggetto AuctionBid bid1 con bidAmount pari a 10.0
		AuctionBid bid1 = new AuctionBid(auction.getAuctionName(), bidder1.getUsername(), 10.0);
		boolean response5;
		try {
			response5 = ac.placeABid(bid1);
		}catch(Exception e) {
			//e.printStackTrace();
			response5 = false;
		}

		assertEquals(true, response5);


		// Ottiene la lista delle Bid piazzate sull'Auction.
		List<AuctionBid> auctionBidsByAuction = null;
		try {
			auctionBidsByAuction = ac.getAuctionBidsByAuction(auction.getAuctionName());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuction);

		// Ottiene la lista delle Bid piazzate sull'Auction auction dall'User bidder1.
		List<AuctionBid> auctionBidsByAuctionAndUsername1 = null;
		try {
			auctionBidsByAuctionAndUsername1 = ac.getAuctionBidsByAuctionAndUsername(auction.getAuctionName(), bidder1.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuctionAndUsername1);

		// Ottiene la piu' alta offerta piazzata sull'Auction auction dall'User bidder1.
		AuctionBid greatestAuctionBidPlacedByAnUser1 = null;
		try {
			greatestAuctionBidPlacedByAnUser1 = ac.getTheHighestAuctionBidPlacedByAnUser(auction.getAuctionName(), bidder1.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(greatestAuctionBidPlacedByAnUser1);

		// Ottiene la lista delle Auction alle quali ha partecipato l'User bidder1.
		List<Auction> auctionJoined1 = null;
		try {
			auctionJoined1 = uc.getAuctionsJoined(bidder1.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertFalse(auctionJoined1.isEmpty());





		//Creazione oggetto AuctionBid bid2 con bidAmount pari a 15.
		AuctionBid bid2 = new AuctionBid(auction.getAuctionName(), bidder2.getUsername(), 15.00);
		boolean response6;
		try {
			response6 = ac.placeABid(bid2);
		}catch(Exception e) {
			e.printStackTrace();
			response6 = false;
		}

		assertEquals(true, response6);


		// Ottiene la lista delle Bid piazzate sull'Auction.
		try {
			auctionBidsByAuction = ac.getAuctionBidsByAuction(auction.getAuctionName());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuction);
		assertTrue(auctionBidsByAuction.size()==2);

		// Ottiene la lista delle Bid piazzate sull'Auction auction dall'User bidder2.
		List<AuctionBid> auctionBidsByAuctionAndUsername2 = null;
		try {
			auctionBidsByAuctionAndUsername2 = ac.getAuctionBidsByAuctionAndUsername(auction.getAuctionName(), bidder2.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuctionAndUsername2);

		// Ottiene la piu' alta offerta piazzata sull'Auction auction dall'User bidder2.
		AuctionBid greatestAuctionBidPlacedByAnUser2 = null;
		try {
			greatestAuctionBidPlacedByAnUser2 = ac.getTheHighestAuctionBidPlacedByAnUser(auction.getAuctionName(), bidder2.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(greatestAuctionBidPlacedByAnUser2);

		// Ottiene la lista delle Auction alle quali ha partecipato l'User bidder2.
		List<Auction> auctionJoined2 = null;
		try {
			auctionJoined2 = uc.getAuctionsJoined(bidder2.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertFalse(auctionJoined2.isEmpty());


		//Creazione oggetto AuctionBid bid3 con bidAmount pari a 20.
		AuctionBid bid3 = new AuctionBid(auction.getAuctionName(), bidder3.getUsername(), 20.00);
		boolean response7;
		try {
			response7 = ac.placeABid(bid3);
		}catch(Exception e) {
			e.printStackTrace();
			response7 = false;
		}

		assertEquals(true, response7);


		// Ottiene la lista delle Bid piazzate sull'Auction.
		try {
			auctionBidsByAuction = ac.getAuctionBidsByAuction(auction.getAuctionName());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuction);
		assertTrue(auctionBidsByAuction.size()==3);

		// Ottiene la lista delle Bid piazzate sull'Auction auction dall'User bidder3.
		List<AuctionBid> auctionBidsByAuctionAndUsername3 = null;
		try {
			auctionBidsByAuctionAndUsername3 = ac.getAuctionBidsByAuctionAndUsername(auction.getAuctionName(), bidder3.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(auctionBidsByAuctionAndUsername3);

		// Ottiene la piu' alta offerta piazzata sull'Auction auction dall'User bidder3.
		AuctionBid greatestAuctionBidPlacedByAnUser3 = null;
		try {
			greatestAuctionBidPlacedByAnUser3 = ac.getTheHighestAuctionBidPlacedByAnUser(auction.getAuctionName(), bidder3.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertNotNull(greatestAuctionBidPlacedByAnUser3);

		// Ottiene la lista delle Auction alle quali ha partecipato l'User bidder3.	
		List<Auction> auctionJoined3 = null;
		try {
			auctionJoined3 = uc.getAuctionsJoined(bidder3.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}
		assertFalse(auctionJoined3.isEmpty());




		// Attende 10 secondi per far scadere l'asta ed ottenere i vincitori.
		try{
			Thread.sleep(10000);
		}
		catch(InterruptedException ex){
			Thread.currentThread().interrupt();
		}


		HashMap<String, Double> winners = ac.getAuction(auction.getAuctionName()).getWinners();
		assertEquals(winners.size(), 2);
		System.out.println(ac.getAuction(auction.getAuctionName()).printWinners());

	}


}