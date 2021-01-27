package auction.mechanism.App;

import java.io.IOException;
import java.net.InetAddress;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import auction.mechanism.View.AuthenticationGUI;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;

public class AuctionSystemApp {

    
    private static PeerDHT peerDHT;
    
    
    @Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
	private static String master;

	@Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
	private static int id;
	
    public static void main(String[] args) throws Exception {

        AuctionSystemApp app = new AuctionSystemApp();
        final CmdLineParser parser = new CmdLineParser(app);
        
        try{
        	
        	parser.parseArgument(args);
        	
        	int peerId = id;
        	
        	//String master = "127.0.0.1";
    		//int peerId = 0;
    		
        	
        	int port = 4000;
			Peer peer = new PeerBuilder(Number160.createHash(peerId)).ports(port).start();
			peerDHT = new PeerBuilderDHT(peer).start();

			/*
             Bootstrapping operation finds an existing peer in the overlay network, so that the first connection is addressed with a well known peer called "Master".
             The peer needs to know the IP address where to connect the first time.
			 */

			FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(master)).ports(port).start();
			fb.awaitUninterruptibly();

			if (fb.isSuccess()) {
				peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
			}
			else {
				throw new Exception("An error occurred during bootstrapping process.");
			}
			
			TextIO textIO = TextIoFactory.getTextIO();
            TextTerminal < ? > terminal = textIO.getTextTerminal();
            terminal.setBookmark("reset");
            
            new AuthenticationGUI(textIO, terminal, peerDHT).authenticationGUIDisplay(); 
            
		}catch(IOException e) {
			e.printStackTrace();
		}
    }
}