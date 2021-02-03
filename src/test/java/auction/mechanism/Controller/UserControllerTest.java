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

import auction.mechanism.Model.AddressInfo;
import auction.mechanism.Model.User;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
/*
 * This class tests all the User functionalities and relative generated exceptions.
*/
public class UserControllerTest {

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
    }

    @Test
    public void registerUserTest() {
    	String username = "userTest1";
    	String password = "passwordTest";
    	AddressInfo addressInfo = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
    	String emailPayPal = "angelobianchi@gmail.com";
        User x = new User(username, password, addressInfo, emailPayPal);
        
        UserController uc = new UserController(peerDHT);
        boolean response = uc.registerUser(x);
        
        assertEquals(true, response);
        assertNotNull(uc.getUser(x.getUsername()));
        assertTrue(x.equals(uc.getUser(x.getUsername())));
    }
    
    
    @Test
    public void usernameAlreadyTakenTest() {
    	String username = "userTest2";
    	String password1 = "passwordTest1";
    	String password2 = "passwordTest2";
    	AddressInfo addressInfo1 = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
    	AddressInfo addressInfo2 = new AddressInfo("Italy", "Napoli", "81010", "Via Caracciolo", "120", "+39", "32856278690");
    	String emailPayPal1 = "angelobianchi@gmail.com";
    	String emailPayPal2 = "giuseppemazzini@yahoo.com";
    	
        User x = new User(username, password1, addressInfo1, emailPayPal1);
        User y = new User(username, password2, addressInfo2, emailPayPal2);
        
        UserController uc = new UserController(peerDHT);
        boolean response1 = uc.registerUser(x);
        // Prova a registrare un'Auction con username gia' registrato. La response e' false, e lancia una UsernameAlreadyTakenException.
        boolean response2 = uc.registerUser(y);
        
        assertEquals(true, response1);
        assertEquals(false, response2);
        
        assertNotNull(uc.getUser(x.getUsername()));
        assertTrue(x.equals(uc.getUser(x.getUsername())));
        
        assertNotNull(uc.getUser(y.getUsername()));
        assertFalse(y.equals(uc.getUser(y.getUsername())));
    }
    
    
    @Test
    public void checkPasswordTest() throws Exception {
    	String username = "userTest3";
    	String password = "passwordTest";
    	
    	User x = new User(username, password, null, null);
    	        
        UserController uc = new UserController(peerDHT);
        
        uc.registerUser(x);
        
        assertNotNull(uc.getUser(x.getUsername()));
        assertTrue(x.equals(uc.getUser(x.getUsername())));
        
        boolean response1 = uc.checkPassword(username, password);
        boolean response2 = uc.checkPassword(username, "wrongPassword");
        
        assertEquals(true, response1);
        assertEquals(false, response2);
    }
    
    @Test
    public void updateUserTest() {
    	String username = "userTest4";
    	String password1 = "passwordTest";
    	String password2 = "changedPassword";
    	
    	AddressInfo addressInfo1 = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
    	AddressInfo addressInfo2 = new AddressInfo("Italy", "Napoli", "81010", "Via Caracciolo", "120", "+39", "32856278690");
    	String emailPayPal = "angelobianchi@gmail.com";
    	
        User x = new User(username, password1, addressInfo1, emailPayPal);
        
        UserController uc = new UserController(peerDHT);
        
        uc.registerUser(x);
        
        assertNotNull(uc.getUser(x.getUsername()));
        assertTrue(x.equals(uc.getUser(x.getUsername())));
        
        
        x.setPassword(password2);
        x.setAddressInfo(addressInfo2);
        boolean response = uc.updateUser(x);
        
        assertEquals(true, response);
        assertEquals(password2, uc.getUser(username).getPassword());
    }
    
    @Test
    public void checkIfAbleToPlaceABidTest() throws Exception {
    	String username1 = "userTest5";
    	String username2 = "userTest6";
    	String password = "passwordTest";
    	AddressInfo addressInfo1 = null;
    	AddressInfo addressInfo2 = new AddressInfo("Italy", "Napoli", "81010", "Via Caracciolo", "120", "+39", "32856278690");
    	String emailPayPal1 = "angelobianchi@gmail.com";
    	String emailPayPal2 = "Wrong;Format@email..com";
    	
    	User x = new User(username1, password, addressInfo1, emailPayPal1);
    	User y = new User(username2, password, addressInfo2, emailPayPal2);
        
        UserController uc = new UserController(peerDHT);
        
        uc.registerUser(x);
        
        assertNotNull(uc.getUser(x.getUsername()));
        assertTrue(x.equals(uc.getUser(x.getUsername())));
        
        uc.registerUser(y);
        

        assertNotNull(uc.getUser(y.getUsername()));
        assertTrue(y.equals(uc.getUser(y.getUsername())));
        
        boolean response1 = uc.checkIfAbleToPlaceABid(username1);
        boolean response2 = uc.checkIfAbleToPlaceABid(username2);
        
        assertEquals(false, response1);
        assertEquals(false, response2);
        
        // All'User x viene settato un indirizzo che gli permette di effettuare offerte.
        x.setAddressInfo(new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671"));
        
        // Aggiorna l'User x.
        uc.updateUser(x);
        
        assertNotNull(uc.getUser(x.getUsername()));
        assertTrue(x.equals(uc.getUser(x.getUsername())));
        
        // Verifica che l'User x sia in grado di effettuare offerte.
        boolean response3 = uc.checkIfAbleToPlaceABid(x.getUsername());
        assertEquals(true, response3);
        
    }
    
    @Test
    public void deleteUserTest() {
    	String username = "userTest7";
    	String password = "passwordTest";
    	AddressInfo addressInfo1 = new AddressInfo("Italy", "Marcianise", "81025", "Via G. Verdi", "5", "+39", "3398024671");
    	String emailPayPal1 = "angelobianchi@gmail.com";
    	
    	User x = new User(username, password, addressInfo1, emailPayPal1);
        
        UserController uc = new UserController(peerDHT);
        
        uc.registerUser(x);
       
        assertNotNull(uc.getUser(x.getUsername()));
        assertTrue(x.equals(uc.getUser(x.getUsername())));
        
        boolean response = uc.deleteUser(username);
        
        assertEquals(true, response);
        
        // Prova ad effettuare la get di un User cancellato. Lancia una UserNotFoundException.
        assertNull(uc.getUser(x.getUsername()));
        assertFalse(x.equals(uc.getUser(x.getUsername())));
    }


}