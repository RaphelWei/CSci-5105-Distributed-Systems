import java.math.BigInteger;


public class NodeDHT {
	private static Node currNode, predNode;
	private static FingerTable[] finger;
	private static int m = 128;
	private static BigInteger two = BigInteger.valueOf(2);
	private static BigInteger numKey = two.pow(m);




  	public static Node findSuccessor(int id) {
    	Node n;
    	n = findPredecessor(id);
    	return n.getSuccessor();
  	}

  	public static Node findPredecessor(int id) {
   		Node n;
    	while () {
      
    	}
    	return n;
  	}




  	// IP:Port:NodeID:KeyRange|IP:Port:NodeID:KeyRange
  	// single row of a fingertable: IP:Port:NodeID:KeyRange
  	// id is a 128-bit biginteger, stored as a string
  	public static Node cloest_prceding_finger(String id) {
    	BigInteger BItarID = new BigInteger(id, 16);
    	String[] Fingers = fingerTable.split("\\|");
    	System.out.println(Fingers.length);
    	// compared to the symbol used in the paper
    	// I will leave some comments here
    	// finger[i].node = BIfingerID
    	// n = BImyNodeID
    	// id = BItarID
    	BigInteger 
    	for (int i = Fingers.length-1; i >= 0; i--) {
      		String[] fingerPointed = Fingers[i].split(":");
      		BigInteger BIfingerID = new BigInteger(fingerPointed[2], 16);
			BigInteger BImyNodeID = new BigInteger(NodeID, 16);
      		if (BItarID.compareTo(BImyNodeID) > 0) {
      			if (BIfingerID.compareTo(BImyNodeID) > 0 && BIfingerID.compareTo(BItarID) <0)
      				return Fingers[i];
      		} else {
      			if (BIfingerID.compareTo(BImyNodeID) > 0 || BIfingerID.compareTo(BItarID) <0)
      				return Fingers[i];
      		}
      		return myInfo;

    	}
  	}



  	public static void join(Node n) {

  	}

  	public static void initFingerTable(Node n) {
  		int myID, fingerNodeID;
  		for (int i = 1; i <= m; i++) {
  			myID = currNode.getID();
  			fingerNodeID = finger[i].getSuccessor().getID();
  			if 
  		}

  	}

  	public static void updateOthers() {
  		Node p;
  		for (int i = 1; i <= m; i++) {
  			int id = me.getID() - (int)Math.pow(2,i-1) + 1;
  			if (id < 0)
  				id = id + numDHT; 

  			p = find_predecessor(id);


  			String request = "updateFing/" + me.getID() + "/" + me.getIP() + "/" + me.getPort() + "/" + i;  
  			makeConnection(p.getIP(),p.getPort(),request);

  		}
  	}



  	// SourceInfo: sourceIP:sourcePort:sourceID
  	public static void updateFingerTable(Node s, int i) {
  		Node p;
  		int normalInterval = 1;
  		int myID = me.getID();
  		int nextID = finger[i].getSuccessor().getID();
  		if (myID >= nextID) 
  			normalInterval = 0;
  		else normalInterval = 1;

               //System.out.println("here!" + s.getID() + " between " + myID + " and " + nextID);

  		if ( ((normalInterval==1 && (s.getID() >= myID && s.getID() < nextID)) ||
  			(normalInterval==0 && (s.getID() >= myID || s.getID() < nextID)))
  			&& (me.getID() != s.getID() ) ) {

  			finger[i].setSuccessor(s);
  			p = pred;

  			try{
  				TTransport  transport = new TSocket(p.getIP(), Integer.parseInt(p.getPort()));
  				TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
  				WorkWithNode.Client client = new WorkWithNode.Client(protocol);
        //Try to connect
  				transport.open();
  				client.updateFingerTable(s, i);
  				transport.close();

  				} catch(TException e) {
  			}



  	}

  		

  	}














}
