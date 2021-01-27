package auction.mechanism.View;

import org.beryx.textio.*;

import auction.mechanism.Model.AddressInfo;
import auction.mechanism.Model.Auction;
import auction.mechanism.Model.AuctionBid;
import auction.mechanism.Model.User;
import net.tomp2p.dht.PeerDHT;
import auction.mechanism.App.AuctionSystemApp;
import auction.mechanism.Controller.AuctionController;
import auction.mechanism.Controller.UserController;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.*;

public class AuctionGUI {

	private TextTerminal < ? > terminal;
	private TextIO textIO;

	private static User user;

	private final String keyStrokeListAuctions = "ctrl L";

	private final String keyStrokeSearchAuction = "ctrl S";

	private final String keyStrokeCreateAuction = "ctrl R";

	private final String keyStrokeGetAuctionsOwned = "ctrl O";

	private final String keyStrokeGetAuctionsJoined = "ctrl J";

	private final String keyStrokeGetAuctionsWon = "ctrl W";

	private final String keyStrokeLogout = "ctrl Q";

	private final String keyStrokeCheckUserDetails = "ctrl U";

	private static PeerDHT peerDHT;

	private boolean disableAuctionKeyStrokes;

	private boolean disableUserEditKeyStrokes;
	private boolean disableAuctionEditKeyStrokes;
	private boolean disablePlaceABidKeyStrokes;


	AuctionGUI(TextIO textIO, TextTerminal < ? > terminal, User user, PeerDHT peerDHT) {
		this.textIO = textIO;
		this.terminal = terminal;
		AuctionGUI.user = user;
		AuctionGUI.peerDHT = peerDHT;

		this.disableAuctionKeyStrokes = false;
		this.disableUserEditKeyStrokes = true;
		this.disableAuctionEditKeyStrokes = true;
		this.disablePlaceABidKeyStrokes = true;

	}


	public void AuctionGUIDisplay() {

		TerminalProperties < ? > props = terminal.getProperties();

		UserController uc = new UserController(peerDHT);

		boolean searchAuctionStroke = terminal.registerHandler(keyStrokeSearchAuction, t -> {
			if (!disableAuctionKeyStrokes) {
				if (!terminal.resetToBookmark("auction")) {
					System.out.print("\033[H\033[2J");
					System.out.flush();
					this.printMenuGUI();
				}

				Auction auction = this.searchAuctionGUI();

				if (auction == null) {	
					props.setPromptColor("red");
					terminal.println("Auction not found. Please, check the auction name and try again.\n");
					props.setPromptColor("#00ff00");
					textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
				}else{
					System.out.print("\033[H\033[2J");
					System.out.flush();
					this.printMenuGUI();
					try {
						this.printAuctionDetailsGUI(auction, 1);
					} catch (Exception e) {
						terminal.println(e.getMessage());
					}
					props.setPromptColor("#00ff00");
				}

			} else {
				props.setPromptColor("red");
				terminal.println("\n\nThis command is not allowed here.\n");
				props.setPromptColor("#00ff00");
			}

			textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
		});


		boolean createAuctionStroke = terminal.registerHandler(keyStrokeCreateAuction, t -> {
			if (!disableAuctionKeyStrokes) {
				if (!terminal.resetToBookmark("auction")) {
					System.out.print("\033[H\033[2J");
					System.out.flush();
					this.printMenuGUI();
				}
				boolean response = this.createAuctionGUI();
				if (response) {
					// Aggiorna l'User della sessione.
					user = uc.getUser(user.getUsername());
				}else {
					terminal.resetToBookmark("auction");
					props.setPromptColor("red");
					terminal.println("\nAn error occured during the registration of the Auction. Please try again.\n");
					props.setPromptColor("#00ff00");
				}

			} else {
				props.setPromptColor("red");
				terminal.println("\n\nThis command is not allowed here.\n");
				props.setPromptColor("#00ff00");
			}
			textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART);
		});

		boolean listAllAuctionsStroke = terminal.registerHandler(keyStrokeListAuctions, t -> {
			if (!disableAuctionKeyStrokes) {
				if (!terminal.resetToBookmark("auction")) {
					System.out.print("\033[H\033[2J");
					System.out.flush();
					this.printMenuGUI();
				}
				try {
					this.listAllAuctionsGUI();
				} catch (Exception e) {
					terminal.println(e.getMessage());
				}

			} else {
				props.setPromptColor("red");
				terminal.println("\n\nThis command is not allowed here.\n");
				props.setPromptColor("#00ff00");
			}
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
		});

		boolean listAuctionsOwned = terminal.registerHandler(keyStrokeGetAuctionsOwned, t -> {
			if (!disableAuctionKeyStrokes) {
				if (!terminal.resetToBookmark("auction")) {
					System.out.print("\033[H\033[2J");
					System.out.flush();
					this.printMenuGUI();
				}
				try {
					this.listAuctionsOwnedGUI();
				} catch (Exception e) {
					terminal.println(e.getMessage());
				}

			} else {
				props.setPromptColor("red");
				terminal.println("\n\nThis command is not allowed here.\n");
				props.setPromptColor("#00ff00");
			}
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
		});


		boolean listAuctionsJoined = terminal.registerHandler(keyStrokeGetAuctionsJoined, t -> {
			if (!disableAuctionKeyStrokes) {
				if (!terminal.resetToBookmark("auction")) {
					System.out.print("\033[H\033[2J");
					System.out.flush();
					this.printMenuGUI();
				}
				try {
					this.listAuctionsJoinedGUI();
				} catch (Exception e) {
					terminal.println(e.getMessage());
				}

			} else {
				props.setPromptColor("red");
				terminal.println("\n\nThis command is not allowed here.\n");
				props.setPromptColor("#00ff00");
			}
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
		});


		boolean listAuctionsWon = terminal.registerHandler(keyStrokeGetAuctionsWon, t -> {
			if (!disableAuctionKeyStrokes) {
				if (!terminal.resetToBookmark("auction")) {
					System.out.print("\033[H\033[2J");
					System.out.flush();
					this.printMenuGUI();
				}
				try {
					this.listAuctionsWonGUI();
				} catch (Exception e) {
					terminal.println(e.getMessage());
				}

			} else {
				props.setPromptColor("red");
				terminal.println("\n\nThis command is not allowed here.\n");
				props.setPromptColor("#00ff00");
			}
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
		});


		boolean userDetailsStroke = terminal.registerHandler(keyStrokeCheckUserDetails, t -> {
			if (!disableAuctionKeyStrokes) {
				if (!terminal.resetToBookmark("auction")) {
					System.out.print("\033[H\033[2J");
					System.out.flush();
					this.printMenuGUI();
				}
				this.userDetailsGUI();

			} else {
				props.setPromptColor("red");
				terminal.println("\n\nThis command is not allowed here.\n");
				props.setPromptColor("#00ff00");
			}
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
		});

		boolean logoutStroke = terminal.registerHandler(keyStrokeLogout, t -> {
			this.logoutGUI();
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART);
		});


		boolean hasHandlers = createAuctionStroke || searchAuctionStroke || listAllAuctionsStroke || listAuctionsOwned || listAuctionsJoined || listAuctionsWon || userDetailsStroke || logoutStroke ;
		if (!hasHandlers) {
			terminal.println("No handlers can be registered.");
		} else {
			System.out.print("\033[H\033[2J");
			System.out.flush();
			this.printMenuGUI();
			terminal.setBookmark("auction");
		}
		textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
		textIO.dispose();


	}

	private void printMenuGUI() {

		TerminalProperties < ? > props = terminal.getProperties();
		props.setPromptBold(true);
		props.setPromptColor("cyan");
		terminal.println("Welcome " + user.getUsername() + " to P2P Auction System");
		props.setPromptColor("#00ff00");
		props.setPromptBold(false);

		terminal.println("--------------------------------------------------------------------------------");

		terminal.println("Press " + keyStrokeCreateAuction + " to register an auction.");
		terminal.println("Press " + keyStrokeListAuctions + " to get the list of all auctions created.");

		terminal.println("Press " + keyStrokeSearchAuction + " to search for an auction and show its details.");

		terminal.println("Press " + keyStrokeGetAuctionsOwned + " to get the list of auctions owned.");
		terminal.println("Press " + keyStrokeGetAuctionsJoined + " to get the list of auctions you joined.");
		terminal.println("Press " + keyStrokeGetAuctionsWon + " to get the list of auctions you won.");

		terminal.println("Press " + keyStrokeCheckUserDetails + " to check your user details.");

		terminal.println("Press " + keyStrokeLogout + " to logout.");

		terminal.println("\nUse these key combinations at any moment during the session.");
		terminal.println("--------------------------------------------------------------------------------");
	}


	private Auction searchAuctionGUI() {

		this.disableUserEditKeyStrokes = true;
		this.disableAuctionEditKeyStrokes = true;
		this.disablePlaceABidKeyStrokes = true;

		AuctionController ac = new AuctionController(peerDHT);

		TerminalProperties < ? > props = terminal.getProperties();
		props.setPromptColor("red");
		terminal.resetLine();

		terminal.println("Search auction\n");

		props.setPromptColor("#00ff00");
		String keyStrokeCancel = "ctrl C";
		props.setPromptColor("cyan");
		terminal.println("--------------------------------------------------------------------------------");
		terminal.println("Press " + keyStrokeCancel + " to cancel the search for an Auction.");
		terminal.println("--------------------------------------------------------------------------------\n");

		terminal.registerHandler(keyStrokeCancel, t -> {
			if (!terminal.resetToBookmark("auction")) {
				System.out.print("\033[H\033[2J");
				System.out.flush();
				this.printMenuGUI();
			}
			textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
		});

		props.setPromptColor("#00ff00");
		String auctionName = textIO.newStringInputReader().read("Auction name");
		return ac.getAuction(auctionName);
	}

	private boolean createAuctionGUI() {

		this.disableUserEditKeyStrokes = true;
		this.disableAuctionEditKeyStrokes = true;
		this.disablePlaceABidKeyStrokes = true;

		AuctionController ac = new AuctionController(peerDHT);
		UserController uc = new UserController(peerDHT);

		TerminalProperties < ? > props = terminal.getProperties();
		props.setPromptColor("red");
		terminal.moveToLineStart();

		terminal.println("Register Auction\n");


		String keyStrokeCancel = "ctrl C";
		props.setPromptColor("cyan");
		terminal.println("--------------------------------------------------------------------------------");
		terminal.println("Press " + keyStrokeCancel + " to cancel the registration of the Auction.");
		terminal.println("--------------------------------------------------------------------------------\n");

		terminal.registerHandler(keyStrokeCancel, t -> {
			if (!terminal.resetToBookmark("auction")) {
				System.out.print("\033[H\033[2J");
				System.out.flush();
				this.printMenuGUI();
			}
			textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
		});

		props.setPromptColor("#00ff00");
		boolean auctionNameIsRight = false;

		String auctionName = "";

		while(!auctionNameIsRight) {
			auctionName = textIO.newStringInputReader()
					.withMinLength(4).withPattern("(?!^\\d+$)^.+$")
					.read("Auction Name");

			Auction foundAuction = ac.getAuction(auctionName.toLowerCase());
			User foundUser = uc.getUser(auctionName.toLowerCase());

			if(foundAuction != null) {
				props.setPromptColor("red");
				terminal.println("\nAuction name chosen is already taken. Please insert a new one.\n");
				props.setPromptColor("#00ff00");
			}
			if(foundUser != null) {
				props.setPromptColor("red");
				terminal.println("\nAuction name chosen is not available. Please insert a new one.\n");
				props.setPromptColor("#00ff00");
			}
			if(foundAuction==null && foundUser==null){
				auctionNameIsRight = true;
			}
		}

		String description = textIO.newStringInputReader()
				.withMinLength(4).withPattern("(?!^\\d+$)^.+$")
				.read("Description");


		terminal.println("\nExpiration date");

		boolean dateRight = false;

		Calendar endDate = Calendar.getInstance();

		while(!dateRight) {
			int day = textIO.newIntInputReader().withMinVal(1).withMaxVal(31).read("Day");
			Month month = textIO.newEnumInputReader(Month.class).read("Month");
			int year = textIO.newIntInputReader().withMaxVal(9999).withMinVal(Calendar.getInstance().get(Calendar.YEAR)).read("Year");
			int hour = textIO.newIntInputReader().withMinVal(0).withMaxVal(23).read("Hour (from 0 to 23)");
			int minute = textIO.newIntInputReader().withMinVal(0).withMaxVal(59).read("Minute (from 0 to 59)");

			endDate.set(Calendar.DAY_OF_MONTH, day);
			endDate.set(Calendar.MONTH, month.getValue()-1);
			endDate.set(Calendar.YEAR, year);
			endDate.set(Calendar.HOUR_OF_DAY, hour);
			endDate.set(Calendar.MINUTE, minute);
			endDate.set(Calendar.SECOND, 00);

			if(Auction.getLocalTime(Calendar.getInstance()).before(endDate)) {
				dateRight = true;
			}else {
				props.setPromptColor("red");
				terminal.println("\nExpiration date is not valid. Please insert a valid Expiration date.\n");
				props.setPromptColor("#00ff00");
			}
		}


		double reservedPrice = textIO.newDoubleInputReader().withMinVal(0.0).read("\nReserved price");

		int slots = textIO.newIntInputReader().withMinVal(1).read("Slots");

		String owner = user.getUsername();

		Auction toRegister = new Auction(auctionName, description, endDate, reservedPrice, slots, owner);

		boolean responseRegistration = false;

		responseRegistration = ac.registerAuction(toRegister);

		if (responseRegistration) {
			props.setPromptColor("cyan");
			terminal.resetToBookmark("auction");
			terminal.println("Auction with name: '" + toRegister.getAuctionName() + "' correctly registered.");
			props.setPromptColor("#00ff00");
			return true;
		} else {
			return false;
		}
	}

	private void listAllAuctionsGUI() {

		this.disableUserEditKeyStrokes = true;
		this.disableAuctionEditKeyStrokes = true;
		this.disablePlaceABidKeyStrokes = true;

		AuctionController ac = new AuctionController(peerDHT);

		TerminalProperties < ? > props = terminal.getProperties();

		props.setPromptColor("red");
		terminal.resetLine();

		terminal.println("Auctions List");

		props.setPromptColor("#00ff00");

		List<String> auctionNames = null;
		List<Auction> ongoingAuctions = null;
		List<Auction> expiredAuctions = null;
		try {
			auctionNames = ac.getAllAuctionNames();
			ongoingAuctions = ac.getOngoingAuctions();
			expiredAuctions = ac.getExpiredAuctions();
		}catch(Exception e) {
			terminal.println(e.getMessage());
		}		

		if (auctionNames != null && !auctionNames.isEmpty()) {

			terminal.println("\nTotal Auctions: " + auctionNames.size());
			props.setPromptColor("cyan");
			terminal.println("--------------------------------------------------------------------------------");
			props.setPromptColor("#00ff00");
			terminal.resetLine();

			if (ongoingAuctions != null && !ongoingAuctions.isEmpty()) {
				terminal.println("\nOngoing Auctions: " + ongoingAuctions.size() +"\n");
				terminal.resetLine();

				props.setPromptColor("cyan");
				for(int i=0; i<ongoingAuctions.size(); i++) {
					props.setPromptColor("cyan");
					terminal.print("name: ");
					props.setPromptColor("#00ff00");
					terminal.print(ongoingAuctions.get(i).getAuctionName());
					terminal.print("\n");
				}

				props.setPromptColor("cyan");
				terminal.println("\n--------------------------------------------------------------------------------");
			}
			if (expiredAuctions != null && !expiredAuctions.isEmpty()) {
				props.setPromptColor("#00ff00");
				terminal.println("\nExpired Auctions: " + expiredAuctions.size() +"\n");
				terminal.resetLine();

				props.setPromptColor("cyan");
				for(int i=0; i<expiredAuctions.size(); i++) {
					props.setPromptColor("cyan");
					terminal.print("name: ");
					props.setPromptColor("#00ff00");
					terminal.print(expiredAuctions.get(i).getAuctionName());
					terminal.print("\n");
				}
				props.setPromptColor("cyan");
				terminal.println("\n--------------------------------------------------------------------------------\n");
			}
		}else {
			terminal.println("\nNo auction registered was found.");
		}
		props.setPromptColor("#00ff00");
	}

	private void listAuctionsOwnedGUI() throws Exception {

		this.disableUserEditKeyStrokes = true;
		this.disableAuctionEditKeyStrokes = true;
		this.disablePlaceABidKeyStrokes = true;

		AuctionController ac = new AuctionController(peerDHT);
		UserController uc = new UserController(peerDHT);

		TerminalProperties < ? > props = terminal.getProperties();

		props.setPromptColor("red");
		terminal.resetLine();

		terminal.println("Auctions Owned List");

		props.setPromptColor("#00ff00");

		List<Auction> auctionsOwned = null;
		try {
			auctionsOwned = uc.getAuctionsOwned(user.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}

		terminal.resetLine();

		if (auctionsOwned != null && !auctionsOwned.isEmpty()) {
			terminal.println("\nAuctions Owned: " + auctionsOwned.size());
			for(int i=0; i<auctionsOwned.size(); i++) {
				Auction auction = auctionsOwned.get(i);
				this.printAuctionDetailsGUI(auction, 0);
				terminal.println("\n");
			}
		}else {
			terminal.println("\nNo auction owned was found.");
		}
		props.setPromptColor("#00ff00"); 
	}

	private void listAuctionsJoinedGUI() throws Exception {

		this.disableUserEditKeyStrokes = true;
		this.disableAuctionEditKeyStrokes = true;
		this.disablePlaceABidKeyStrokes = true;

		AuctionController ac = new AuctionController(peerDHT);
		UserController uc = new UserController(peerDHT);

		TerminalProperties < ? > props = terminal.getProperties();

		props.setPromptColor("red");
		terminal.resetLine();

		terminal.println("Auctions Joined List");

		props.setPromptColor("#00ff00");

		List<Auction> auctionsJoined = null;
		try {
			auctionsJoined = uc.getAuctionsJoined(user.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}

		terminal.resetLine();

		if (auctionsJoined != null && !auctionsJoined.isEmpty()) {
			terminal.println("\nAuctions Joined: " + auctionsJoined.size());
			for(int i=0; i<auctionsJoined.size(); i++) {
				Auction auction = auctionsJoined.get(i);
				this.printAuctionDetailsGUI(auction, 0);
				terminal.println("\n");
			}
		}else {
			terminal.println("\nNo auction joined was found.");
		}
		props.setPromptColor("#00ff00");
	}

	private void listAuctionsWonGUI() throws Exception {

		this.disableUserEditKeyStrokes = true;
		this.disableAuctionEditKeyStrokes = true;
		this.disablePlaceABidKeyStrokes = true;

		AuctionController ac = new AuctionController(peerDHT);
		UserController uc = new UserController(peerDHT);

		TerminalProperties < ? > props = terminal.getProperties();

		props.setPromptColor("red");
		terminal.resetLine();

		terminal.println("Auctions Won List");

		props.setPromptColor("#00ff00");

		List<Auction> auctionsWon = null;
		try {
			auctionsWon = uc.getAuctionsWon(user.getUsername());
		}catch(Exception e) {
			e.printStackTrace();
		}

		terminal.resetLine();

		if (auctionsWon != null && !auctionsWon.isEmpty()) {
			terminal.println("\nAuctions Won: " + auctionsWon.size());

			for(int i=0; i<auctionsWon.size(); i++) {
				Auction auction = auctionsWon.get(i);
				this.printAuctionDetailsGUI(auction, 0);
				terminal.println("\n");
			}
		}else {
			terminal.println("\nNo auction won was found.");
		}
		props.setPromptColor("#00ff00");
	}

	// Parameter mode can be 0 or 1: 
	// 0 means that we use this method to print details of many auctions and we don't want to be able to place a bid on a specific auction;
	// 1 means that we use this method to print details of only one specific auction, and we do want to be able to place a bid on it.
	private void printAuctionDetailsGUI(Auction auction, int mode) throws Exception {

		this.disableUserEditKeyStrokes = true;
		this.disableAuctionEditKeyStrokes = true;
		this.disablePlaceABidKeyStrokes = true;

		AuctionController ac = new AuctionController(peerDHT);
		UserController uc = new UserController(peerDHT);

		TerminalProperties < ? > props = terminal.getProperties();

		if(mode == 1) {

			this.disableUserEditKeyStrokes = true;
			this.disableAuctionEditKeyStrokes = false;
			this.disablePlaceABidKeyStrokes = false;

			String keyStrokePlaceABid = "ctrl B";

			if(auction.getStatus().equals(Auction.Status.ongoing) && !auction.getOwnerUsername().equals(user.getUsername())) {
				props.setPromptColor("cyan");
				terminal.println("--------------------------------------------------------------------------------");
				terminal.println("Press " + keyStrokePlaceABid + " to place a bid on the current auction.");

				// Handler to place a bid on the Auction.
				boolean placeABidStroke = terminal.registerHandler(keyStrokePlaceABid, t -> {
					if (!disablePlaceABidKeyStrokes) {
						if (!terminal.resetToBookmark("auction")) {
							System.out.print("\033[H\033[2J");
							System.out.flush();
							this.printMenuGUI();
						}


						if(this.placeABidGUI(auction)) {
							// Aggiorna l'User in sessione
							user = uc.getUser(user.getUsername());

							props.setPromptColor("cyan");
							terminal.println("\nBid correctly placed on Auction '" + auction.getAuctionName() + "'.");
							props.setPromptColor("#00ff00");
						}else{
							props.setPromptColor("red");
							terminal.println("\nAn error occured while placing the bid on Auction '" + auction.getAuctionName() + "'. Please try again.");
							props.setPromptColor("#00ff00");
						}

					}else {
						props.setPromptColor("red");
						terminal.println("\n\nThis command is not allowed here.\n");
						props.setPromptColor("#00ff00");
					}
					textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
					return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
				});

			}



			if(auction.getOwnerUsername().equals(user.getUsername())) {
				String keyStrokeEditAuction = "ctrl E";

				props.setPromptColor("red");
				terminal.println("\nYou can't place a bid on your own Auction.\n");
				props.setPromptColor("cyan");

				if(auction.getStatus().equals(Auction.Status.ongoing)) {
					terminal.println("--------------------------------------------------------------------------------");
					terminal.println("Press " + keyStrokeEditAuction + " to edit current Auction.");

					terminal.registerHandler(keyStrokeEditAuction, t -> {
						if (!disableAuctionEditKeyStrokes) {
							if (!terminal.resetToBookmark("auction")) {
								System.out.print("\033[H\033[2J");
								System.out.flush();
								this.printMenuGUI();
							}

							boolean response = false;
							response = this.editAuctionGUI(auction);

							if(response) {
								user = uc.getUser(user.getUsername());

								props.setPromptColor("cyan");
								terminal.println("\nAuction edited successfully.");
								props.setPromptColor("#00ff00");
							}else {
								props.setPromptColor("#00ff00");
							}

						}else {
							props.setPromptColor("red");
							terminal.println("\n\nThis command is not allowed here.\n");
							props.setPromptColor("#00ff00");
						}
						textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
						return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
					});

				}else {
					props.setPromptColor("red");
					terminal.println("You can't edit an expired Auction.\n");
					disableAuctionEditKeyStrokes = true;
				}

			}else {

				if(auction.getStatus().equals(Auction.Status.ended)) {
					props.setPromptColor("red");
					terminal.println("\nYou can't place a bid on an expired Auction.");
					disablePlaceABidKeyStrokes = true;
				}
			}

		}

		props.setPromptColor("cyan");
		terminal.println("--------------------------------------------------------------------------------\n");
		props.setPromptColor("red");
		terminal.println("Auction Details\n");
		props.setPromptColor("cyan");
		terminal.println("Auction name: " + auction.getAuctionName());
		terminal.println("Description: " + auction.getDescription());
		terminal.println("Expiration Date: " + auction.getDateCastedToString() + " " + auction.getHourCastedToString());
		terminal.println("Reserved price: " + auction.getReservedPrice());
		terminal.print("Number of slots: ");
		props.setPromptColor("#00ff00");
		terminal.print(String.valueOf(auction.getSlots()) + "\n");
		props.setPromptColor("cyan");
		terminal.println("Owner: " + auction.getOwnerUsername());

		terminal.print("Stauts: ");
		if(auction.getStatus().equals(Auction.Status.ongoing)){
			props.setPromptColor("#00ff00");
		}else {
			props.setPromptColor("red");
		}
		terminal.print(auction.getStatus().toString()+"\n");
		props.setPromptColor("cyan");


		List<AuctionBid> bids = null;
		try{
			bids = ac.getAuctionBidsByAuction(auction.getAuctionName());
		}catch(Exception e) {
		}

		props.setPromptColor("cyan");
		if((bids == null) || (bids.isEmpty())) {
			props.setPromptColor("red");
			terminal.println("\nNo bids placed on the current Auction.");
		}

		if((bids != null ) && (!bids.isEmpty())) {
			props.setPromptColor("cyan");
			terminal.print("\n\nNumber of bids placed: ");
			props.setPromptColor("#00ff00");
			terminal.print(String.valueOf(ac.getAuctionBidsByAuction(auction.getAuctionName()).size()) + "\n");
			props.setPromptColor("cyan");
			terminal.println("\nWinning bids placed: ");

			if(auction.getSlots() < bids.size()) {
				for(int i=0; i<auction.getSlots(); i++) {
					props.setPromptColor("cyan");
					terminal.print("Bid amount: ");
					props.setPromptColor("#00ff00");
					terminal.print(String.valueOf(bids.get(i).getBidAmount()));
					props.setPromptColor("cyan");
					terminal.print(", from: ");
					props.setPromptColor("#00ff00");
					terminal.print(bids.get(i).getUsername() +"\n");
				}
			}else {
				for(int i=0; i<bids.size(); i++) {
					props.setPromptColor("cyan");
					terminal.print("Bid amount: ");
					props.setPromptColor("#00ff00");
					terminal.print(String.valueOf(bids.get(i).getBidAmount()));
					props.setPromptColor("cyan");
					terminal.print(", from: ");
					props.setPromptColor("#00ff00");
					terminal.print(bids.get(i).getUsername() +"\n");
				}
			}

			// Se l'User � il creatore dell'asta, pu� visualizzare tutte le offerte effettuate su tale asta.
			if(user.getUsername().equals(auction.getOwnerUsername())) {
				props.setPromptColor("cyan");
				terminal.println("\n\nList of bids placed:");
				for(int i=0; i<bids.size(); i++) {
					AuctionBid auctionBid = bids.get(i);
					props.setPromptColor("cyan");
					terminal.print("Bid amount: ");
					props.setPromptColor("#00ff00");
					terminal.print(String.valueOf(auctionBid.getBidAmount()));
					props.setPromptColor("cyan");
					terminal.print(", from: ");
					props.setPromptColor("#00ff00");
					terminal.print(auctionBid.getUsername() + "\n");
				}
			}

			if(!user.getUsername().equals(auction.getOwnerUsername())) {
				props.setPromptColor("cyan");
				terminal.println("\n\nYour highest bid placed:");
				AuctionBid myHighestBid = null;
				try{
					myHighestBid = ac.getTheHighestAuctionBidPlacedByAnUser(auction.getAuctionName(), user.getUsername());
				}catch(Exception e) {
				}

				if(myHighestBid != null) {		
					props.setPromptColor("#00ff00");
					terminal.print(String.valueOf(myHighestBid.getBidAmount()));
				}else {
					props.setPromptColor("red");
					terminal.println("\nYou've never placed a bid on the current Auction.");
					props.setPromptColor("cyan");
				}


				// Se l'User ha piazzato offerte sull'asta, pu� visualizzare tutte le sue offerte effettuate su tale asta.
				props.setPromptColor("cyan");
				terminal.println("\n\nList of your bids placed:");
				List<AuctionBid> myBids = null;
				try{
					myBids = ac.getAuctionBidsByAuctionAndUsername(auction.getAuctionName(), user.getUsername());
				}catch(Exception e) {
				}

				if((myBids == null) || (myBids.isEmpty())) {
					props.setPromptColor("red");
					terminal.println("\nYou've never placed a bid on the current Auction.");
				}

				if((myBids != null ) && (!myBids.isEmpty())) {

					for(int i=0; i<myBids.size(); i++) {
						AuctionBid auctionBid = myBids.get(i);
						props.setPromptColor("cyan");
						terminal.print("Bid amount: ");
						props.setPromptColor("#00ff00");
						terminal.print(String.valueOf(auctionBid.getBidAmount()) + "\n");
					}

					boolean winning = false;
					// Per ogni slot di indice i, controlla se le i-esime bids maggiori sono state effettuate dall'User attuale.
					for(int i=0; i<auction.getSlots(); i++) {
						List<AuctionBid> allBids = null;
						try{
							allBids = ac.getAuctionBidsByAuction(auction.getAuctionName());
						}catch(Exception e){
						}

						if(allBids.get(i).getUsername().equals(user.getUsername())) {
							winning = true;
							break;
						}
					}
					if(auction.getStatus().equals(Auction.Status.ongoing)) {
						if(winning) {
							props.setPromptColor("#00ff00");
							terminal.println("\n\nYou are winning the Auction at the moment.");
						}else {
							props.setPromptColor("red");
							terminal.println("\n\nYou are not winning the Auction.");
						}
					}else {
						if(auction.getStatus().equals(Auction.Status.ended)) {
							if(winning) {
								props.setPromptColor("#00ff00");
								terminal.println("\n\nCongratulations! You've won the Auction.");
							}else {
								props.setPromptColor("red");
								terminal.println("\n\nYou haven't won the Auction.");
							}
						}
					}			
				}

				props.setPromptColor("#00ff00");
				terminal.println("");
			}


			// Se lo status dell'Auction � "ended", vengono visualizzati a schermo i vincitori dell'asta con i relativi prezzi da pagare.
			if(auction.getStatus().equals(Auction.Status.ended)) {
				props.setPromptColor("cyan");
				terminal.println("\n\nList of winners:");
				props.setPromptColor("#00ff00");

				for (Map.Entry<String, Double> entry : auction.getWinners().entrySet()) {
					String usernameWinner = entry.getKey();
					Double priceToPay = entry.getValue();

					User winner = uc.getUser(usernameWinner);

					props.setPromptColor("cyan");
					terminal.print("Winner: ");
					props.setPromptColor("#00ff00");
					terminal.print(winner.getUsername());
					props.setPromptColor("cyan");
					terminal.print(", Price to pay: ");
					props.setPromptColor("#00ff00");
					terminal.print(String.valueOf(priceToPay) + "\n");

					// Se l'User � l'owner dell'Auction, pu� visualizzare i dettagli relativi all'indirizzo degli utenti vincitori.
					if(user.getUsername().equals(auction.getOwnerUsername())) {
						props.setPromptColor("cyan");
						terminal.println("Address info");
						props.setPromptColor("cyan");
						terminal.print("Country: ");
						props.setPromptColor("#00ff00");
						terminal.print(winner.getAddressInfo().getCountry());
						props.setPromptColor("cyan");
						terminal.print(", City: ");
						props.setPromptColor("#00ff00");
						terminal.print(winner.getAddressInfo().getCity());
						props.setPromptColor("cyan");
						terminal.print(", Postal Code: ");
						props.setPromptColor("#00ff00");
						terminal.print(winner.getAddressInfo().getPostalCode());
						props.setPromptColor("cyan");
						terminal.print(", Street: ");
						props.setPromptColor("#00ff00");
						terminal.print(winner.getAddressInfo().getStreet());
						props.setPromptColor("cyan");
						terminal.print(", number: ");
						props.setPromptColor("#00ff00");
						terminal.print(winner.getAddressInfo().getStreetNumber());
						props.setPromptColor("cyan");
						terminal.print(", Phone number: ");
						props.setPromptColor("#00ff00");
						terminal.println(winner.getAddressInfo().getPrefixPhoneNumber() + " " + winner.getAddressInfo().getPhoneNumber() + "\n");
					}
				}

			}
		}

	}

	private boolean placeABidGUI(Auction auction) {

		this.disableUserEditKeyStrokes = true;
		this.disableAuctionEditKeyStrokes = true;
		this.disablePlaceABidKeyStrokes = false;

		AuctionController ac = new AuctionController(peerDHT);

		TerminalProperties < ? > props = terminal.getProperties();
		props.setPromptColor("red");

		terminal.println("Place a bid on auction '" + auction.getAuctionName() + "'\n");
		props.setPromptColor("#00ff00");

		Double bidAmount = textIO.newDoubleInputReader().withMinVal(0.0).read("Bid amount");

		AuctionBid bid = new AuctionBid(auction.getAuctionName(), user.getUsername(), bidAmount);

		boolean response = false;

		try {
			response = ac.placeABid(bid);
		}catch(Exception e) {
			props.setPromptColor("red");
			terminal.println("\n"+e.getMessage());
			props.setPromptColor("#00ff00");
		}

		return response;
	}

	private void userDetailsGUI() {

		this.disableUserEditKeyStrokes = false;
		this.disableAuctionEditKeyStrokes = true;
		this.disablePlaceABidKeyStrokes = true;

		UserController uc = new UserController(peerDHT);

		TerminalProperties < ? > props = terminal.getProperties();

		String keyStrokeChangePassword = "ctrl P";
		String keyStrokeEditAddressInfo = "ctrl A";
		String keyStrokeEditPayPalEmail = "ctrl E";
		String keyStrokeDeleteUser = "ctrl D";

		terminal.registerHandler(keyStrokeChangePassword, t -> {
			if (!disableUserEditKeyStrokes) {
				if (!terminal.resetToBookmark("auction")) {
					System.out.print("\033[H\033[2J");
					System.out.flush();
					this.printMenuGUI();
				}

				boolean response = this.changeUserPasswordGUI();
				if(response) {
					user = uc.getUser(user.getUsername());
					props.setPromptColor("cyan");
					terminal.println("\nPassword changed successfully.");
					props.setPromptColor("#00ff00");
				}else {
					this.userDetailsGUI();
				}
			}else {
				props.setPromptColor("red");
				terminal.println("\n\nThis command is not allowed here.\n");
				props.setPromptColor("#00ff00");
			}
			textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
		});

		terminal.registerHandler(keyStrokeEditAddressInfo, t -> {
			if (!disableUserEditKeyStrokes) {
				if (!terminal.resetToBookmark("auction")) {
					System.out.print("\033[H\033[2J");
					System.out.flush();
					this.printMenuGUI();
				}

				boolean response = this.editUserAddressInfoGUI();
				if(response) {
					user = uc.getUser(user.getUsername());

					props.setPromptColor("cyan");
					terminal.println("\nAddress info updated successfully.");
					props.setPromptColor("#00ff00");
				}else {
					this.userDetailsGUI();
				}
			}else {
				props.setPromptColor("red");
				terminal.println("\n\nThis command is not allowed here.\n");
				props.setPromptColor("#00ff00");
			}
			textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
		});

		terminal.registerHandler(keyStrokeEditPayPalEmail, t -> {
			if (!disableUserEditKeyStrokes) {
				if (!terminal.resetToBookmark("auction")) {
					System.out.print("\033[H\033[2J");
					System.out.flush();
					this.printMenuGUI();
				}
				boolean response = this.editUserPayPalEmailGUI();
				if(response) {
					user = uc.getUser(user.getUsername());

					props.setPromptColor("cyan");
					terminal.println("\nPayPal email updated successfully.");
					props.setPromptColor("#00ff00");
				}else {
					this.userDetailsGUI();
				}
			}else {
				props.setPromptColor("red");
				terminal.println("\n\nThis command is not allowed here.\n");
				props.setPromptColor("#00ff00");
			}
			textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
		});

		terminal.registerHandler(keyStrokeDeleteUser, t -> {
			if (!disableUserEditKeyStrokes) {
				if (!terminal.resetToBookmark("auction")) {
					System.out.print("\033[H\033[2J");
					System.out.flush();
					this.printMenuGUI();
				}
				boolean response = this.deleteUserGUI();

				if(response) {
					props.setPromptColor("cyan");
					terminal.println("\nUser deleted successfully.");

					this.logoutGUI();
				}
			}else {
				props.setPromptColor("red");
				terminal.println("\n\nThis command is not allowed here.\n");
				props.setPromptColor("#00ff00");
			}
			textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
		});

		props.setPromptColor("red");
		terminal.resetLine();

		terminal.println("User Details\n");

		props.setPromptColor("cyan");
		terminal.println("--------------------------------------------------------------------------------");
		terminal.println("Press " + keyStrokeChangePassword + " to change password.");
		terminal.println("Press " + keyStrokeEditAddressInfo + " to edit address info.");
		terminal.println("Press " + keyStrokeEditPayPalEmail + " to edit PayPal email.");
		terminal.println("Press " + keyStrokeDeleteUser + " to delete User Profile.");
		terminal.println("--------------------------------------------------------------------------------");


		terminal.println("\nUsername: " + user.getUsername());

		String maskedPassword = "";	
		for(int i=0; i<user.getPassword().length(); i++) {
			maskedPassword += "*";
		}
		props.setPromptColor("#00ff00");
		terminal.println("Password: " + maskedPassword);

		boolean userAble = false;
		try {
			userAble = uc.checkIfAbleToPlaceABid(user.getUsername());
		} catch (Exception e) {
			e.printStackTrace();
		}


		if(user.getAddressInfo() != null) {
			props.setPromptColor("cyan");
			terminal.println("\nAddress Info");
			props.setPromptColor("#00ff00");
			terminal.println("Country: " + user.getAddressInfo().getCountry());
			terminal.println("City: " + user.getAddressInfo().getCity());
			terminal.println("Postal Code: " + user.getAddressInfo().getPostalCode());
			terminal.println("Street: " + user.getAddressInfo().getStreet() + ", number: " + user.getAddressInfo().getStreetNumber());
			terminal.println("Phone number: " + user.getAddressInfo().getPrefixPhoneNumber() + " " + user.getAddressInfo().getPhoneNumber());
		}

		if(user.getEmailPayPal() != null) {
			props.setPromptColor("cyan");
			terminal.print("\n\nPayPal Email: ");
			props.setPromptColor("#00ff00");
			terminal.print(user.getEmailPayPal() + "\n");
		}

		if(!userAble) {
			props.setPromptColor("red");
			terminal.println("\nUser is not able to place a bid on any auction. \nPlease check that Address Info and PayPal Email forms are valid to be able to place any bid.");
		}
		props.setPromptColor("#00ff00");
	}


	private boolean changeUserPasswordGUI() {

		this.disableUserEditKeyStrokes = false;
		this.disableAuctionEditKeyStrokes = true;
		this.disablePlaceABidKeyStrokes = true;

		boolean response = false;

		UserController uc = new UserController(peerDHT);
		TerminalProperties < ? > props = terminal.getProperties();

		props.setPromptColor("red");
		terminal.println("Change Password");

		String keyStrokeCancel = "ctrl C";
		props.setPromptColor("cyan");
		terminal.println("\n--------------------------------------------------------------------------------");
		terminal.println("Press " + keyStrokeCancel + " to cancel the editing of password.");
		terminal.println("--------------------------------------------------------------------------------");

		terminal.registerHandler(keyStrokeCancel, t -> {
			if (!terminal.resetToBookmark("auction")) {
				System.out.print("\033[H\033[2J");
				System.out.flush();
				this.printMenuGUI();
			}
			this.userDetailsGUI();
			textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
		});

		props.setPromptColor("cyan");
		terminal.println("\nUsername: " + user.getUsername());

		props.setPromptColor("#00ff00");
		boolean passwordIsRight = false;

		while (!passwordIsRight) {		
			String password = textIO.newStringInputReader().withMinLength(6).withInputMasking(true).read("Insert old password");
			try {
				if (!uc.checkPassword(user.getUsername(), password)) {
					props.setPromptColor("red");
					terminal.println("\nWrong password. Please try again.\n");
					props.setPromptColor("#00ff00");
				}else {
					passwordIsRight = true;
				}
			} catch (Exception e) {
				terminal.println(e.toString());
			}
		}

		String newPassword = "";
		if(passwordIsRight) {
			newPassword = textIO.newStringInputReader().withMinLength(6).withInputMasking(true).read("New Password");
			terminal.println();
		}

		User newUser = user;
		newUser.setPassword(newPassword);
		uc.updateUser(newUser);
		response = true;

		return response;
	}

	private boolean editUserAddressInfoGUI() {

		this.disableUserEditKeyStrokes = false;
		this.disableAuctionEditKeyStrokes = true;
		this.disablePlaceABidKeyStrokes = true;

		boolean response = false;

		UserController uc = new UserController(peerDHT);
		TerminalProperties < ? > props = terminal.getProperties();

		props.setPromptColor("red");
		terminal.println("Edit Address Info");

		String keyStrokeCancel = "ctrl C";
		props.setPromptColor("cyan");
		terminal.println("\n--------------------------------------------------------------------------------");
		terminal.println("Press " + keyStrokeCancel + " to cancel the editing of Address Info.");
		terminal.println("--------------------------------------------------------------------------------");

		terminal.registerHandler(keyStrokeCancel, t -> {
			if (!terminal.resetToBookmark("auction")) {
				System.out.print("\033[H\033[2J");
				System.out.flush();
				this.printMenuGUI();
			}
			this.userDetailsGUI();
			textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
		});

		props.setPromptColor("cyan");
		terminal.println("\nUsername: " + user.getUsername());


		props.setPromptColor("cyan");
		terminal.println("\nOld Country: " + user.getAddressInfo().getCountry());
		props.setPromptColor("#00ff00");
		String newCountry = textIO.newStringInputReader().withMinLength(3).read("New Country");

		props.setPromptColor("cyan");
		terminal.println("\nOld City: " + user.getAddressInfo().getCity());
		props.setPromptColor("#00ff00");
		String newCity = textIO.newStringInputReader().withMinLength(3).read("New City");

		props.setPromptColor("cyan");
		terminal.println("\nOld Postal Code: " + user.getAddressInfo().getPostalCode());
		props.setPromptColor("#00ff00");
		String newPostalCode = textIO.newStringInputReader().withMinLength(5).read("New Postal Code");

		props.setPromptColor("cyan");
		terminal.println("\nOld Street: " + user.getAddressInfo().getStreet());
		props.setPromptColor("#00ff00");
		String newStreet = textIO.newStringInputReader().withMinLength(3).read("New Street");

		props.setPromptColor("cyan");
		terminal.println("\nOld Street number: " + user.getAddressInfo().getStreetNumber());
		props.setPromptColor("#00ff00");
		String newStreetNumber = textIO.newStringInputReader().withMinLength(1).read("New Street Number");

		props.setPromptColor("cyan");
		terminal.println("\nOld phone number Prefix: " + user.getAddressInfo().getPrefixPhoneNumber());
		props.setPromptColor("#00ff00");
		String newPrefixPhoneNumber = textIO.newStringInputReader().withMinLength(2).read("New Prefix phone number");

		props.setPromptColor("cyan");
		terminal.println("\nOld phone number: " + user.getAddressInfo().getPhoneNumber());
		props.setPromptColor("#00ff00");
		String newPhoneNumber = textIO.newStringInputReader().withMinLength(7).read("New Phone number");

		User newUser = user;
		AddressInfo newAddressInfo = new AddressInfo(newCountry, newCity, newPostalCode, newStreet, newStreetNumber, newPrefixPhoneNumber, newPhoneNumber);
		newUser.setAddressInfo(newAddressInfo);
		uc.updateUser(newUser);
		response = true;

		return response;

	}

	private boolean editUserPayPalEmailGUI() {

		this.disableUserEditKeyStrokes = false;
		this.disableAuctionEditKeyStrokes = true;
		this.disablePlaceABidKeyStrokes = true;

		boolean response = false;

		UserController uc = new UserController(peerDHT);
		TerminalProperties < ? > props = terminal.getProperties();

		props.setPromptColor("red");
		terminal.println("Edit PayPal Email");

		String keyStrokeCancel = "ctrl C";
		props.setPromptColor("cyan");
		terminal.println("\n--------------------------------------------------------------------------------");
		terminal.println("Press " + keyStrokeCancel + " to cancel the editing of PayPal Email.");
		terminal.println("--------------------------------------------------------------------------------");

		terminal.registerHandler(keyStrokeCancel, t -> {
			if (!terminal.resetToBookmark("auction")) {
				System.out.print("\033[H\033[2J");
				System.out.flush();
				this.printMenuGUI();
			}
			this.userDetailsGUI();
			textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
			return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
		});

		props.setPromptColor("cyan");
		terminal.println("\nUsername: " + user.getUsername());

		terminal.println("\nOld PayPal Email: " + user.getEmailPayPal());
		props.setPromptColor("#00ff00");
		String newEmail = textIO.newStringInputReader().withMinLength(3).read("New PayPal Email");

		User newUser = user;
		newUser.setEmailPayPal(newEmail);
		uc.updateUser(newUser);
		response = true;

		return response;

	}

	private boolean deleteUserGUI() {

		this.disableUserEditKeyStrokes = false;
		this.disableAuctionEditKeyStrokes = true;
		this.disablePlaceABidKeyStrokes = true;

		UserController uc = new UserController(peerDHT);
		TerminalProperties < ? > props = terminal.getProperties();

		props.setPromptColor("cyan");
		terminal.println("Delete User Profilo");
		props.setPromptColor("#00ff00");

		terminal.println("\nUsername: " + user.getUsername());

		boolean passwordIsRight = false;
		while (!passwordIsRight) {
			String password = textIO.newStringInputReader().withMinLength(6).withInputMasking(true).read("Insert password");
			try {
				if (!uc.checkPassword(user.getUsername(), password)) {
					props.setPromptColor("red");
					terminal.println("\nWrong password. Please try again.\n");
					props.setPromptColor("#00ff00");
				}else {
					passwordIsRight = true;
				}
			} catch (Exception e) {
				terminal.println(e.toString());
			}
		}

		List<String> validValues = new ArrayList<String>();
		validValues.add("y");
		validValues.add("n");

		String areYouSure = "";

		if(passwordIsRight) {
			props.setPromptColor("red");
			terminal.println("\nAre you sure to delete the current User Profile?");
			props.setPromptColor("#00ff00");
			terminal.println("\nPress 'y' to confirm the deletion, 'n' to cancel.");
			areYouSure = textIO.newStringInputReader().withMinLength(1).withInlinePossibleValues(validValues).read();
			terminal.println();
		}

		boolean response = false;
		if(areYouSure.equals("y")) {
			uc.deleteUser(user.getUsername());
			response = true;
		}
		return response;
	}

	private boolean editAuctionGUI(Auction auction) {

		this.disableUserEditKeyStrokes = true;
		this.disableAuctionEditKeyStrokes = false;
		this.disablePlaceABidKeyStrokes = true;

		boolean response = false;

		AuctionController ac = new AuctionController(peerDHT);
		UserController uc = new UserController(peerDHT);

		TerminalProperties < ? > props = terminal.getProperties();

		props.setPromptColor("red");
		terminal.println("Edit Auction '" + auction.getAuctionName() + "'\n");

		if(auction.getStatus().equals(Auction.Status.ongoing)) {
			String keyStrokeCancel = "ctrl C";
			props.setPromptColor("cyan");
			terminal.println("\n--------------------------------------------------------------------------------");
			terminal.println("Press " + keyStrokeCancel + " to cancel the editing of Auction.");
			terminal.println("--------------------------------------------------------------------------------\n");

			terminal.registerHandler(keyStrokeCancel, t -> {
				if (!terminal.resetToBookmark("auction")) {
					System.out.print("\033[H\033[2J");
					System.out.flush();
					this.printMenuGUI();
				}
				try {
					this.printAuctionDetailsGUI(auction, 1);
					props.setPromptColor("#00ff00");
					textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
				} catch (Exception e) {
					props.setPromptColor("red");
					terminal.println("\n" + e.getMessage() + "\n");
				}
				return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
			});

			props.setPromptColor("#00ff00");
			boolean passwordIsRight = false;
			while (!passwordIsRight) {
				String password = textIO.newStringInputReader().withMinLength(6).withInputMasking(true).read("Insert User password");
				try {
					if (!uc.checkPassword(user.getUsername(), password)) {
						props.setPromptColor("red");
						terminal.println("\nWrong password. Please try again.\n");
						props.setPromptColor("#00ff00");
					}else {
						passwordIsRight = true;
					}
				} catch (Exception e) {
					terminal.println(e.toString());
				}
			}

			String newDescription = "";
			int newSlots = 0;

			Calendar newEndDate = Calendar.getInstance();

			if(passwordIsRight) {

				props.setPromptColor("cyan");
				terminal.println("\nOld description: " + auction.getDescription());

				props.setPromptColor("#00ff00");
				newDescription = textIO.newStringInputReader().withMinLength(4).read("New Description");
				terminal.println();

				props.setPromptColor("cyan");
				terminal.println("\nOld expiration date: " + auction.getDateCastedToString() + " " + auction.getHourCastedToString());

				props.setPromptColor("#00ff00");
				boolean dateRight = false;

				while(!dateRight) {
					int day = textIO.newIntInputReader().withMinVal(1).withMaxVal(31).read("Day");
					Month month = textIO.newEnumInputReader(Month.class).read("Month");
					int year = textIO.newIntInputReader().withMaxVal(9999).withMinVal(Calendar.getInstance().get(Calendar.YEAR)).read("Year");
					int hour = textIO.newIntInputReader().withMinVal(0).withMaxVal(23).read("Hour (from 0 to 23)");
					int minute = textIO.newIntInputReader().withMinVal(0).withMaxVal(59).read("Minute (from 0 to 59)");

					newEndDate.set(Calendar.DAY_OF_MONTH, day);
					newEndDate.set(Calendar.MONTH, month.getValue()-1);
					newEndDate.set(Calendar.YEAR, year);
					newEndDate.set(Calendar.HOUR_OF_DAY, hour);
					newEndDate.set(Calendar.MINUTE, minute);
					newEndDate.set(Calendar.SECOND, 00);

					
					if( newEndDate.equals(auction.getEndDate()) || newEndDate.after(auction.getEndDate()) ) {
						dateRight = true;
					}else {
						props.setPromptColor("red");
						terminal.println("\nNew expiration date is not valid. \nPlease insert a new expiration date longer or equals than the old one.\n");
						props.setPromptColor("#00ff00");
					}
				}

				props.setPromptColor("cyan");
				terminal.println("\nOld number of slots: " + auction.getSlots());

				props.setPromptColor("#00ff00");
				newSlots = textIO.newIntInputReader().withMinVal(auction.getSlots()).read("New number of slots");
				terminal.println();

			}
			
			Auction newAuction = auction;
			newAuction.setDescription(newDescription);
			newAuction.setEndDate(newEndDate);
			newAuction.setSlots(newSlots);

			response = ac.updateAuction(newAuction);
		}	

		return response;
	}

	private void logoutGUI() {

		terminal.resetToBookmark("reset");
		System.out.print("\033[H\033[2J");
		System.out.flush();

		this.disableAuctionKeyStrokes = true;
		this.disableUserEditKeyStrokes = true;
		this.disableAuctionEditKeyStrokes = true;
		this.disablePlaceABidKeyStrokes = true;

		new AuthenticationGUI(textIO, terminal, peerDHT).authenticationGUIDisplay(); 
	}

}