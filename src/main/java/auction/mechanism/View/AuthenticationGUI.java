package auction.mechanism.View;
import org.beryx.textio.*;

import auction.mechanism.Controller.AuctionController;
import auction.mechanism.Controller.UserController;
import auction.mechanism.Model.AddressInfo;
import auction.mechanism.Model.Auction;
import auction.mechanism.Model.User;
import net.tomp2p.dht.PeerDHT;


public class AuthenticationGUI {


	private TextTerminal < ? > terminal;
	private TextIO textIO;
	private User userSaved;
	private String keyStrokeLogin = "ctrl L";
	private String keyStrokeRegister = "ctrl R";
	static final String keyStrokeQuit = "ctrl Q";
	
	private static PeerDHT peerDHT;
	
	private boolean disableAuthenticationKeyStrokes;

	public AuthenticationGUI(TextIO textIO, TextTerminal < ? > terminal, PeerDHT peerDHT) {
		this.textIO = textIO;
		this.terminal = terminal;
		AuthenticationGUI.peerDHT = peerDHT;
		
		this.disableAuthenticationKeyStrokes = false;
	}


	public void authenticationGUIDisplay() {

			boolean loginStroke = terminal.registerHandler(keyStrokeLogin, t -> {
				if (!disableAuthenticationKeyStrokes) {
					if (!terminal.resetToBookmark("authentication")) {
						//This sequence of characters clears the screen and moves the cursor to the first row.
						System.out.print("\033[H\033[2J");
						System.out.flush();
						this.printMenu();
					}

					userSaved = this.loginGUI(peerDHT);
					if (userSaved != null) {
						this.authenticationConfirmed();
						return new ReadHandlerData(ReadInterruptionStrategy.Action.RETURN).withRedrawRequired(true);

					} else
						terminal.println("An error occurred during the login. Please try again.");
				} else
					terminal.println("\nThis command is not allowed here. You are not in the authentication session.\n");

				return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);
			});

			boolean registerStroke = terminal.registerHandler(keyStrokeRegister, t -> {
				if (!disableAuthenticationKeyStrokes) {
					if (!terminal.resetToBookmark("authentication")) {
						System.out.print("\033[H\033[2J");
						System.out.flush();
						this.printMenu();
					}
					userSaved = this.registerGUI(peerDHT);
					if (userSaved != null) {
						//this.authenticationConfirmed();
						terminal.resetToBookmark("authentication");
						
						terminal.getProperties().setPromptColor("cyan");
						terminal.println("\nUser correctly registered.");
						terminal.getProperties().setPromptColor("#00ff00");
						
						textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
						return new ReadHandlerData(ReadInterruptionStrategy.Action.RETURN).withRedrawRequired(true);
					} else {
						terminal.getProperties().setPromptColor("red");
						terminal.println("\nAn error occurred during the registration of the User. Please try again.\n");
						terminal.getProperties().setPromptColor("#00ff00");
					}
				} else {
					terminal.getProperties().setPromptColor("red");
					terminal.println("\nThis command is not allowed here. You are not in the authentication session.\n"); 
					terminal.getProperties().setPromptColor("#00ff00");
				}
				return new ReadHandlerData(ReadInterruptionStrategy.Action.RESTART).withRedrawRequired(true);

			});

			boolean quitStroke = terminal.registerHandler(keyStrokeQuit, t -> {
				this.quitGUI();
				return new ReadHandlerData(ReadInterruptionStrategy.Action.ABORT).withRedrawRequired(true);
			});

			boolean hasHandlers = loginStroke || registerStroke || quitStroke;
			if (!hasHandlers) {
				terminal.println("No handlers can be registered.");
			} else {
				System.out.print("\033[H\033[2J");
				System.out.flush();
				this.printMenu();
				terminal.setBookmark("authentication");
			}
			textIO.newStringInputReader().withPattern("(?=a)b").read("\nWaiting a command...");
		}


	private void authenticationConfirmed() {

		this.disableAuthenticationKeyStrokes = true;
		
		if (!terminal.resetToBookmark("reset")) {
			System.out.print("\033[H\033[2J");
			System.out.flush();
		}
		
		new AuctionGUI(textIO, terminal, userSaved, peerDHT).AuctionGUIDisplay();
	}


	private void printMenu() {
		TerminalProperties < ? > props = terminal.getProperties();

		props.setPromptBold(true);
		props.setPromptColor("cyan");
		terminal.println("P2P Auction System");
		props.setPromptUnderline(true);
		props.setPromptColor("#00ff00");
		props.setPromptUnderline(false);
		props.setPromptBold(false);
		terminal.println("--------------------------------------------------------------------------------");

		terminal.println("Press " + keyStrokeRegister + " to register a new User.");

		terminal.println("Press " + keyStrokeLogin + " to login.");

		terminal.println("Press " + keyStrokeQuit + " to quit.");

		terminal.println("\nUse these key combinations at any moment during authentication session.");
		terminal.println("--------------------------------------------------------------------------------");

	}



	private User registerGUI(PeerDHT peerDHT) {
		
		UserController uc = new UserController(peerDHT);
		AuctionController ac = new AuctionController(peerDHT);
		
		terminal.resetLine();
		TerminalProperties < ? > props = terminal.getProperties();
		props.setPromptColor("red");
		terminal.moveToLineStart();

		terminal.println("Register User\n");
		props.setPromptColor("#00ff00");

		boolean usernameIsRight = false;
		
		String username = "";
		
		while (!usernameIsRight) {
			username = textIO.newStringInputReader()
					.withMinLength(4).withPattern("^(?![0-9]*$)[a-zA-Z0-9]+$")
					.read("Username");
			
			User foundUser = uc.getUser(username.toLowerCase());
			Auction foundAuction = ac.getAuction(username.toLowerCase());

			if(foundUser==null && foundAuction==null) {
				usernameIsRight = true;
				username = username.toLowerCase();
			}
			
			if (!usernameIsRight) {
				props.setPromptColor("red");
				terminal.println("\nUsername chosen is already taken. Please insert a new one.");
				props.setPromptColor("#00ff00");
			}
		}
		
		String password = textIO.newStringInputReader()
				.withMinLength(6)
				.withInputMasking(true)
				.read("Password");
		terminal.println();
		
		User user = new User(username, password, new AddressInfo(), "");
		
		if (uc.registerUser(user)) {
			return user;
		} else
			return null;
	}

	private User loginGUI(PeerDHT peerDHT) {
		
		UserController uc = new UserController(peerDHT);
		
		terminal.resetLine();
		TerminalProperties < ? > props = terminal.getProperties();
		props.setPromptColor("red");
		terminal.moveToLineStart();

		terminal.println("Login\n");
		props.setPromptColor("#00ff00");

		boolean usernameIsRight = false;

		User user = new User();
		String username="";

		while (!usernameIsRight) {

			username = textIO.newStringInputReader()
					.withMinLength(4).withPattern("^(?![0-9]*$)[a-zA-Z0-9]+$")
					.read("Username");

			user = uc.getUser(username.toLowerCase());
			if (user == null) {
				props.setPromptColor("red");
				terminal.println("\nUser not found. Please check the username and try again, otherwise if you want to register a new User press CTRL + R.\n");
				props.setPromptColor("#00ff00");
			}else{
				usernameIsRight = true;
			}
		}



		boolean passwordIsRight = false;

		while (!passwordIsRight) {
			String password = textIO.newStringInputReader().withMinLength(6).withInputMasking(true).read("Password");

			try {
				if (!uc.checkPassword(username, password)) {
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
		return user;
	}

	private void quitGUI() {
		System.exit(0);
	}

}