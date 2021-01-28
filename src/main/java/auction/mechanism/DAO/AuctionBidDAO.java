package auction.mechanism.DAO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import auction.mechanism.Exception.AuctionNameAlreadyTakenException;
import auction.mechanism.Model.AuctionBid;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

public class AuctionBidDAO {
	
	final private PeerDHT peerDHT;

	// Istanza del singleton 
	private static AuctionBidDAO auctionBidDAOInstance = null;

	// Costruttore Privato
	private AuctionBidDAO(PeerDHT peerDHT) {
		this.peerDHT = peerDHT;
	}

	public static AuctionBidDAO getInstance(PeerDHT peerDHT) {
		if (auctionBidDAOInstance == null) {
			auctionBidDAOInstance = new AuctionBidDAO(peerDHT);
		}
		return auctionBidDAOInstance;
	}

	// L'id di una AuctionBid e' composto da auctionName + username
	public AuctionBid read(String auctionName, String username) throws Exception {
		FutureGet fg = this.peerDHT.get(Number160.createHash(auctionName+"|"+username)).getLatest().start().awaitUninterruptibly();
		if (fg.isSuccess()) {
			if (fg.isEmpty()) {
				return null;
			}
			return (AuctionBid) fg.data().object();
		}
		return null;
	}
	
	public HashMap <String, AuctionBid> readAll(String auctionName) throws IOException, ClassNotFoundException {
		FutureGet fg = this.peerDHT.get(Number160.createHash("getAll")).getLatest().start().awaitUninterruptibly();
		if (fg.isSuccess()) {
			if (fg.isEmpty()) {
				return null;
			}else {
				HashMap <String, AuctionBid> hashMap = (HashMap<String, AuctionBid>) fg.data().object();
				for (Map.Entry<String, AuctionBid> set : hashMap.entrySet()) {
					String auctionToSearch = set.getKey().substring(0, set.getKey().indexOf("|"));
					System.out.println("Key: " + set.getKey() + " - Value: " + set.getValue());
					System.out.println("Filtered Key: " + auctionToSearch);
					if(!auctionToSearch.equals(auctionName)) {
						hashMap.remove(set.getValue());
					}
				}
				return hashMap;
			}
		}
		return null;
	}
	
	public void create(AuctionBid auctionBid) throws Exception {
		// HashMap locale getAll
		HashMap<String, AuctionBid> getAll = this.readAll(auctionBid.getAuctionName());

		if (getAll == null) {
			HashMap <String, AuctionBid> newHashMap = new HashMap<String, AuctionBid>();
			newHashMap.put(auctionBid.getAuctionBidID(peerDHT), auctionBid);
			FuturePut fp1 = peerDHT.put(Number160.createHash("getAll")).data(new Data(newHashMap)).start().awaitUninterruptibly();
			if(fp1.isSuccess()) {
				FuturePut fp2 = peerDHT.put(Number160.createHash(auctionBid.getAuctionBidID(peerDHT))).data(new Data(auctionBid)).start().awaitUninterruptibly();
				if(!fp2.isSuccess()) {
					throw new Exception("An error occured during the creation of the AuctionBid. Please try again.");
				}
			}else{
				throw new Exception("An error occured during the creation of the AuctionBid. Please try again.");
			}
		}else{
			// Inserimento dell'auction nella hashMap locale "getAll"
			Object response = getAll.putIfAbsent(auctionBid.getAuctionBidID(peerDHT), auctionBid);
			if(response == null) {
				// Remove di getAll
				FutureRemove fr = peerDHT.remove(Number160.createHash("getAll")).start().awaitUninterruptibly();
				if(fr.isSuccess()) {
					// Put dell'istanza aggiornata di getAll
					FuturePut fp3 = peerDHT.put(Number160.createHash("getAll")).data(new Data(getAll)).start().awaitUninterruptibly();
					if(fp3.isSuccess()) {
						// Put dell'auction
						FuturePut fp4 = peerDHT.put(Number160.createHash(auctionBid.getAuctionBidID(peerDHT))).putIfAbsent().data(new Data(auctionBid)).start().awaitUninterruptibly();
						if(!fp4.isSuccess()) {
							throw new Exception("An error occured during the creation of the AuctionBid. Please try again.");
						}
					}else {
						throw new Exception("An error occured during the creation of the AuctionBid. Please try again.");
					}
				}
			}else {
				throw new AuctionNameAlreadyTakenException("Auction name chosen is already taken. Please insert a new one.");
			}
		}
	}

}
