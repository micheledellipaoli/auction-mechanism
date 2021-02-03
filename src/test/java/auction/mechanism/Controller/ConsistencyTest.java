package auction.mechanism.Controller;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.List;

import auction.mechanism.Model.AddressInfo;
import auction.mechanism.Model.Auction;
import auction.mechanism.Model.AuctionBid;
import auction.mechanism.Model.User;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;

/*
 * This Class tests the consistency of data in the system between multiple peers.
*/
public class ConsistencyTest {

	private static final int NUMBER_OF_PEERS = 5;

	private static PeerDHT[] peers;

	private PeerDHT peerDHT;

	@Before
	public void initialization() throws Exception {
		peers = new PeerDHT[NUMBER_OF_PEERS];

		for(int i=0; i<NUMBER_OF_PEERS; i++) {
			int peerId = i;
			String bootPeer =  "127.0.0.1";
			int masterPort = 4000;

			try{
				Peer peer = new PeerBuilder(Number160.createHash(peerId)).ports(masterPort + peerId).start();
				peerDHT = new PeerBuilderDHT(peer).start();
				peers[i] = peerDHT;

				/*
				The peer needs to know the ip address where to connect the first time it joins in the overlay newtwork.
                Bootstrapping operation finds an existing peer in the overlay, so that the first connection is addressed with a well known peer called "bootPeer".
				 */

				FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(bootPeer)).ports(masterPort+peerId).start();
				fb.awaitUninterruptibly();

				if (fb.isSuccess()) {

					FutureDiscover fd = peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
				}
				else {
					throw new Exception("An error caused during bootstrapping process.");
				}
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		// Il peer 0 elimina tutte le Auction gia' registrate da altri test per evitare conflitti.
		AuctionController ac = new AuctionController(peers[0]);
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
		Il peer 0 registra correttamente un User owner ed un'Auction.
		Tutti gli altri peer controllano se l'User e l'Auction risultano essere effettivamente registrati.
		 */

		//Creazione oggetto User owner
		String usernameOwner = "ownerRegisterAuctionConsistencyTest1";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);



		for(int i=0; i<peers.length; i++) {
			if(i==0) {
				// Il peer 0 registra l'User owner
				UserController uc = new UserController(peers[i]);
				boolean response = uc.registerUser(owner);
				assertEquals(true, response);
			}

			// Tutti gli altri peer controllano che l'User sia stato registrato
			UserController uc = new UserController(peers[i]);
			assertNotNull(uc.getUser(usernameOwner));
			assertTrue(owner.equals(uc.getUser(usernameOwner)));
		}



		//Creazione oggetto Auction
		String auctionName = "auctionRegisterAuctionConsistencyTest1";
		String description = "descriptionTest";

		// Settiamo al EndDate pari al 14/01/2022, ore: 11.20
		Calendar endDate = Calendar.getInstance();
		endDate.set(2022, 0, 14, 11, 20);

		Auction x = new Auction(auctionName, description, endDate, 0, 1, owner.getUsername());		

		for(int i=0; i<peers.length; i++) {
			if(i==0) {
				// Il peer 0 registra l'Auction x.
				AuctionController ac = new AuctionController(peers[i]);
				boolean response1 = ac.registerAuction(x);
				assertEquals(true, response1);
			}

			// Tutti gli altri peer controllano che l'Auction sia stata registrata.
			AuctionController ac = new AuctionController(peers[i]);
			assertNotNull(ac.getAuction(auctionName));
			assertTrue(x.equals(ac.getAuction(x.getAuctionName())));
		}

		// Elimina l'Auction creata per evitare problematiche con i test successivi.
		AuctionController ac = new AuctionController(peers[0]);
		boolean response1 = ac.deleteAuction(x.getAuctionName());
		assertEquals(true, response1);

	}


	@Test
	public void auctionNameAlreadyTakenTest() {
		/*
		Il peer 0 registra un User owner e tenta la registrazione di un' Auction x.
		Successivamente, il peer 1 tenta la registrazione di un'Auction y, con lo STESSO auctionName di x.
		La registrazione dell'Auction y, fallisce.
		Tutti gli altri peer controllano che l'User owner e l'Auction siano registrati, e che l'Auction y non sia registrata. 
		 */

		//Creazione oggetto User Owner
		String usernameOwner = "ownerAuctionNameAlreadyTakenConsistencyTest1";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		for(int i=0; i<peers.length; i++) {
			if(i==0) {
				// Il peer 0 registra l'User owner.
				UserController uc = new UserController(peers[i]);
				boolean response = uc.registerUser(owner);
				assertEquals(true, response);
			}

			// Tutti gli altri peer controllano che l'User sia stato registrato.
			UserController uc = new UserController(peers[i]);
			assertNotNull(uc.getUser(usernameOwner));
			assertTrue(owner.equals(uc.getUser(usernameOwner)));
		}



		//Creazione oggetto Auction
		String auctionName = "auctionAuctionNameAlreadyTakenConsistencyTest1";
		String description1 = "descriptionTest1";
		String description2 = "descriptionTest2";

		// Settiamo al EndDate pari al 14/01/2025, ore: 11.20
		Calendar endDate = Calendar.getInstance();
		endDate.set(2025, 0, 14, 11, 20);

		Auction x = new Auction(auctionName, description1, endDate, 0, 1, owner.getUsername());
		Auction y = new Auction(auctionName, description2, endDate, 0, 1, owner.getUsername());


		for(int i=0; i<peers.length; i++) {
			AuctionController ac = new AuctionController(peers[i]);
			if(i==0) {
				// Il peer 0 registra l'Auction x.
				boolean response1 = ac.registerAuction(x);
				assertEquals(true, response1);
			}
			// Tutti gli altri peer controllano che l'Auction x sia stata effettivamente registrata.
			assertNotNull(ac.getAuction(x.getAuctionName()));
			assertTrue(x.equals(ac.getAuction(x.getAuctionName())));

			if(i==1) {
				// Il peer 1 tenta la registrazione dell'Auction y.
				//La response e' false, e lancia una AuctionNameAlreadyTakenException.
				boolean response1 = ac.registerAuction(y);
				assertEquals(false, response1);
			}
			// Tutti gli altri peer controllano che l'Auction y sia NON registrata.
			// In realt�, invocando getAuction(auctionName), si ottiene l'istanza x.
			assertNotNull(ac.getAuction(y.getAuctionName()));
			// Confronta y con l'istanza ottenuta da getAuction(auctionName), e verfica che siano DIVERSE.
			assertFalse(y.equals(ac.getAuction(y.getAuctionName())));
		}

		// Elimina l'Auction creata per evitare problematiche con i test successivi.
		AuctionController ac = new AuctionController(peers[0]);
		boolean response1 = ac.deleteAuction(x.getAuctionName());
		assertEquals(true, response1);
	}


	@Test
	public void getAllAuctionsTest() {
		/*
		Il peer 0 registra un'Owner.
		Ogni peer, partendo dal peer 0, registra un'Auction.
		Ad ogni iterazione del for, il peer di indice 'i' controlla che vi siano registrate 'i+1' Auction.
		 */

		// Registrazione dell' oggetto User owner
		String usernameOwner = "ownerGetAllAuctionsConsistencyTest";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peers[0]);

		boolean response = uc.registerUser(owner);
		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));


		// Array auctions[] condiviso da tutti i peer.
		Auction[] auctions = new Auction[NUMBER_OF_PEERS];

		for(int i=0; i<peers.length; i++) {

			AuctionController ac = new AuctionController(peers[i]);

			// Ogni peer crea l' oggetto Auction
			String auctionName1 = "auctionGetAllAuctionsConsistencyTest"+String.valueOf(i);
			String description = "descriptionTest";
			Auction x = new Auction(auctionName1, description, Calendar.getInstance(), 0, 1, owner.getUsername());

			// Ogni peer memorizza la propria Auction creata nell'array auctions[] condiviso da tutti i peer.
			auctions[i] = x;

			// Registra l'Auction
			boolean response1 = ac.registerAuction(x);

			assertEquals(true, response1);
			assertTrue(x.equals(ac.getAuction(x.getAuctionName())));

			// Ogni peer ottiene tutte le auction registrate fino a questo momento.
			List<Auction> listAuctions = ac.getAllAuctions();
			assertNotNull(listAuctions);
			assertEquals(false, listAuctions.isEmpty());

			// Ogni peer di indice 'i' controlla che vi siano registrate 'i+1' Auction. 
			assertEquals(i+1, listAuctions.size());

			// Ogni peer di indice 'i' controlla che le Auction registrate dai suoi j=i-1 peer precedenti siano uguali a quelle inserite nell'array auctions[], condiviso da tutti i peer.
			for(int j=i-1; j>=0; j--) {
				assertTrue(listAuctions.get(j).equals(auctions[j]));
			}

		}

		// Ogni peer elimina l'Auction da lui creata per evitare problematiche con i test successivi.
		for(int i=0; i<peers.length; i++) {
			AuctionController ac = new AuctionController(peers[i]);
			boolean response1 = ac.deleteAuction(auctions[i].getAuctionName());
			assertEquals(true, response1);
		}

	}

	@Test
	public void updateAuctionTest() {
		/*
		Il peer 0 registra un'Owner ed un'Auction.
		Tutti gli altri peer controllano l'esistenza dell'Auction registrata.
		Successivamente, il peer 0 modifica l'Auction.
		Tutti gli altri peer controllano se la modifica dell'Auction sia effettivamente avvenuta con successo e resa visibile.
		 */

		//Creazione oggetto User Owner
		String usernameOwner = "ownerUpdateAuctionConsistencyTest";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peers[0]);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));



		//Creazione oggetto Auction
		String auctionName = "auctionUpdateAuctionConsistencyTest";

		String description1 = "descriptionTest";
		String description2 = "descriptionChanged";

		Auction x = new Auction(auctionName, description1, Calendar.getInstance(), 0, 1, owner.getUsername());

		AuctionController ac = new AuctionController(peers[0]);

		boolean response1 = ac.registerAuction(x);
		assertEquals(true, response1);

		for(int i=0; i<peers.length; i++) {

			ac = new AuctionController(peers[i]);

			assertNotNull(ac.getAuction(x.getAuctionName()));
			assertTrue(x.equals(ac.getAuction(x.getAuctionName())));

			// Ogni peer ottiene tutte le auction registrate.
			List<Auction> listAuctions = ac.getAllAuctions();
			assertNotNull(listAuctions);
			assertEquals(false, listAuctions.isEmpty()); 
			assertEquals(1, listAuctions.size());
		}

		if(response1) {
			//Modifica la descrizione dell'istanza Auction x
			x.setDescription(description2);

			ac = new AuctionController(peers[0]);

			// Il peer 0 effettua l'update dell'Auction
			boolean response2 = ac.updateAuction(x);
			assertEquals(true, response2);

			for(int i=0; i<peers.length; i++) {

				ac = new AuctionController(peers[i]);

				assertNotNull(ac.getAuction(x.getAuctionName()));
				assertTrue(x.equals(ac.getAuction(x.getAuctionName())));

				// Ogni peer controlla che la modifica alla descrizione dell'Auction visibile.
				assertTrue(description2.equals(ac.getAuction(x.getAuctionName()).getDescription()));

				// Ogni peer ottiene tutte le auction registrate.
				List<Auction> listAuctions = ac.getAllAuctions();
				assertNotNull(listAuctions);
				assertEquals(false, listAuctions.isEmpty()); 
				assertEquals(1, listAuctions.size());
			}

			// Il peer 0 elimina l'Auction creata per evitare problematiche con i test successivi.
			boolean response3 = ac.deleteAuction(x.getAuctionName());
			assertEquals(true, response3);
		}

	}

	@Test
	public void placeABidTest() throws Exception {
		/*
		Il peer 0 registra un'Owner ed un'Auction.
		Tutti gli altri peer registrano degli User bidder.

		In maniera iterativa, partendo dal peer1, i bidder effettuano un'offerta all'Auction.
		L'offerta pi� alta sar� effettuata dal i-esimo bidder, con i=peers.length-1.
		 */

		//Creazione oggetto User Owner
		String usernameOwner = "ownerPlaceABidConsistencyTest";
		String passwordOwner = "passwordTest";
		AddressInfo addressInfoOwner = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
		String emailPayPalOwner = "angelobianchi@gmail.com";
		User owner = new User(usernameOwner, passwordOwner, addressInfoOwner, emailPayPalOwner);

		UserController uc = new UserController(peers[0]);

		boolean response = uc.registerUser(owner);

		assertEquals(true, response);
		assertNotNull(uc.getUser(owner.getUsername()));
		assertTrue(owner.equals(uc.getUser(owner.getUsername())));



		//Creazione oggetto Auction
		String auctionName = "auctionPlaceABidConsistencyTest";

		String description1 = "descriptionTest";

		// Settiamo al EndDate pari al 14/01/2025, ore: 11.20
		Calendar endDate = Calendar.getInstance();
		endDate.set(2025, 0, 14, 11, 20);

		Auction x = new Auction(auctionName, description1, endDate, 0, 3, owner.getUsername());

		AuctionController ac = new AuctionController(peers[0]);

		boolean response1 = ac.registerAuction(x);
		assertEquals(true, response1);

		AuctionBid highestBid = null;

		if(response1) {
			for(int i=1; i<peers.length; i++) {

				//Ogni peer controlla che l'Auction creata dal peer 0 sia effettivamente registrata.
				ac = new AuctionController(peers[i]);
				assertNotNull(ac.getAuction(x.getAuctionName()));

				//Creazione oggetto User bidder
				String usernameBidder = "bidderPlaceABidConsistencyTest"+i;
				String passwordBidder = "passwordTest";
				AddressInfo addressInfoBidder = new AddressInfo("Italy", "Capodrise", "81130", "Via Rossi", "30", "+39", "3792345792");
				String emailPayPalBidder = "angelobianchi@gmail.com";
				User bidder = new User(usernameBidder, passwordBidder, addressInfoBidder, emailPayPalBidder);

				uc = new UserController(peers[i]);

				//Ogni peer registra un User bidder
				boolean response2 = uc.registerUser(bidder);

				assertEquals(true, response2);
				assertNotNull(uc.getUser(bidder.getUsername()));
				assertTrue(bidder.equals(uc.getUser(bidder.getUsername())));

				AuctionBid bid = new AuctionBid(auctionName, bidder.getUsername(), i);

				// Memorizza nella variabile highestBid l'offerta piazzata dal bidder con indice i=peers.length-1
				if(i==peers.length-1) {
					highestBid = bid;
				}

				// Ogni bidder piazza un'offerta sull'Auction
				boolean response3 = ac.placeABid(bid);

				assertEquals(true, response3);

			}

			ac = new AuctionController(peers[0]);

			assertNotNull(ac.getAuctionBidsByAuction(auctionName));

			// Il peer 0 controlla che il numero di offerte piazzate sia pari a peers.length-1.
			assertEquals(peers.length-1, ac.getAuctionBidsByAuction(auctionName).size());

			// Il peer 0 controlla che l'offerta pi� alta piazzata sull'Auction sia quella effettuata dall' i-esimo bidder, con i=peers.length-1
			assertTrue(highestBid.equals( ac.getTheHighestAuctionBidByAuction(auctionName) ));


			// Il peer 0 elimina l'Auction creata per evitare problematiche con i test successivi.
			boolean response3 = ac.deleteAuction(x.getAuctionName());
			assertEquals(true, response3);
		}

	}



}