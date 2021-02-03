# Auction-Mechanism

**Author**: Michele Delli Paoli<br>
**Homework**: Auction Mechanism<br>
**MD5**: micheledellipaoli-31 = ce3c445e5d2d168eeb501a63764cd243

<br>
<p>
<br>

### Description
Auction-Mechanism is a second-price auction system based on a P2P network, developed using TomP2P library.

Each User can sell goods by registering a new Auction, or can buy a good by placing a bid on an existing Auction.

In particular, an abled User can place multiple bids on a specific Auction as long as the amount is:
* greater than or equals to the reserved price;
* greater than the best existing bid placed by another bidder until that moment (if other bids have been placed by other bidders).

When the Auction ends, if multiple slots of the good are available, the bidder who has placed the highest bid will get the first slot by paying the amount offered by the second-highest bidder; the second-highest bidder will get the second slot by paying the amount offered by the third-highest bidder and so on...

Instead, if only one slot is available, only the highest bidder will win the auction and he will eventually pay the amount offered by himself in his winning bid, if no other bids have been placed by other bidders; otherwise he will pay the amount offered by the second-highest bidder, if other bids have been placed by other bidders. 

Also, when the Auction ends, the owner will visualize the winners with their relative "shipping Address-Info" and "PayPal e-mail" to which send a paying request eventually.<br>
Because of this feature, I've chosen to make mandatory the fulfilling of the "Address-Info" and "PayPal e-mail" forms in the User details, in order to garantee that the Users who haven't fulfilled these forms yet can't be able to place any bid.
</p>
<br>

<p>

### Functionalities
* Registration of a new User
* User Login
* Registration of a new Auction
* Get the list of all registered auctions
* Get the list of the auctions registered by ther User (Owned Auctions)
* Get the list of the auctions on which the User have placed a bid (Joined Auction)
* Get the list of the auctions won by the User
* Search for an Auction and show its details
* Edit an auction owned by the User
* Place a bid on an auction
* Show User details
* Edit User password
* Edit User Address-Info
* Edit PayPal e-mail of the User
* Delete User account

<br>
</p>
<br>

<p>

### Architecture
The system's architecture is based on two Patterns:
* **MVC**: separates the Java Object Class definition (Model) from the business logic (Controller) and from the presentation logic (View);
* **DAO** (Data Access Object): includes all the operations to store, access, and edit the data by using the *put* and *get* primitives exposed by TomP2P library.


##### Model Classes
* Auction
* User
* AuctionBid
* AddressInfo

##### View Classes
* AuthenticationGUI
* AuctionGUI

##### Controller Classes
* AuctionController
* UserController

##### DAO Classes
* AuctionDAO
* UserDAO
* AuctionBidDAO  
*This DAO Class is no more used because I've chosen to gather the AuctionBid instances inside the Auction Object.<br>
So we don't need to register, find, update any AuctionBid instance in the P2P system.<br>
Every operation on an AuctionBid instance will be done using AuctionDAO methods.*

<br>
</p>
<br>


<p>

### Insights
##### How the "Auction Changing Status" functionality has been implemented?
When the endDate of an auction is reached, the Auction Status must change from "**ongoing**" to "**ended**".<br>
Because of the de-centralized nature of the P2P network, the choose of implement an always-running thread, which monitors the endDate of all the registered Auction, was improbable.<br>
Moreover, this thread would have been subject to an increasing workload as the number of the registered auctions will grow up.

Thus, I've chosen to implement an endDate checking method which is invoked everytime that a peer access to a specic Auction instance.

When a peer gets a specific Auction, the method compare the endDate to the current date and, if the Auction is expired, the Status will be changed from "ongoing" to "ended".<br>
The transition of the Status value is done only the first time that the method recognize that the endDate has expired.


</p>
<br>


<p>

### Testing
There are three main Testing Classes:
* **UserControllerTest**<br>
This class tests all the User functionalities and relative generated exceptions such as:
    1. User Registration method;
    2. username already taken exception;
    3. checking username and password combination method;
    4. update User method;
    5. checking if an User is able to place a bid method;
    6. delete User method. <br><br>

* **AuctionControllerTest**<br>
This class tests all the Auction functionalities and relative generated exceptions.<br>
Other than the registration, get and update methods, this class tests many cases where a User tries to place a bid.<br>
In particular, it tests:   
    1. if a User can place a bid when his "Address-Info" and "PayPal e-mail" forms are not in the valid format; 
    2. if a User can place a bid on an "expired" Auction; 
    3. if a bid can be placed when the amount is smaller than the reserved price; 
    4. if a User can place a bid on his own Auction; 
    5. if a User who has already placed a winning bid on an Auction can place another bid on it; 
    6. if a User can place a bid with the amout smaller than the current winning bid, placed by another User; 
    7. the case in wich multiple Users place a bid on a specific Auction with multiple slots available, and waits until the Auction expires to test the Winning Users List.
<br><br>
* **ConsistencyTest**<br>
It tests the consistency of data in the system between multiple peers.
</p><br>

<p>

#### Executing the tests
Because the project make use of Maven, the execution of the tests can be done in the following way:

1. Make sure you are in the project folder.

2. Run the following command in the terminal:
    ```shell
        mvn test
    ```
<br>
All the tests will be executed and the console will also show some exceptions generated by test cases.

<br>

[![Console-Tests.png](https://i.postimg.cc/dV4FZ19K/Console-Tests.png)](https://postimg.cc/5XYTZfGP)

</p>

<br>
<p>

<br>

### Build your app in a Docker container

To run the application using Docker, you should first clone the project from GitHub.

1. Clone the project from Github running the following command:
    ```shell
    git clone https://github.com/micheledellipaoli/auction-mechanism.git
    ```
    <br>
2. Move to the project folder called "auction-mechanism":
    ```shell
    cd auction-mechanism
    ```
    <br>
3. Build your Docker container from the Dockerfile as following:
    ```shell
    docker build --no-cache -t auction-mechanism .
    ```
    <br>
4. Start the Master-Peer running the following command:
    ```shell
    docker run -i -t --name MASTER-PEER -e MASTERIP="127.0.0.1" -e ID=0 auction-mechanism
    ```
    
    
    **Note that**:
    
    • you have to run the Master-Peer using the **ID=0**;
    
    • after the first launch, you can launch the Master-Peer using the following command: 
        
    ```shell
    docker start -i MASTER-PEER
    ```
    <br>

    ##### Start a generic Peer  
    When the Master-Peer is started, you have to check the IP Address of your container.
    
    • First, check the Docker container ID:
    ```shell
        docker ps
    ```
    <br>
    
    • Then, check the IP Address:
    ```shell
        docker inspect <container ID>
    ```
    <br>
    
    Now you can start a generic peer varying the Peer ID, as following:
    ```shell
        docker run -i -t --name PeerNode1 -e MASTERIP=<IP Address> -e ID=1 auction-mechanism
    ```



    **Note that**: after the first launch, you can launch this Peer Node using the following command: 
    ```shell
        docker start -ai PeerNode1
    ```

</p>
