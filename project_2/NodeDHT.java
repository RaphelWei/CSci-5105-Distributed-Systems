public class NodeDHT {
	private static Node currNode, predNode;
	private static FingerTable[] finger;
	private static int numIdentifiers;

	for (int i = 1; i <=m; i++) {
		finger[i].setStart((currNode.getID() + (int)Math.pow(2, i-1)) % numIdentifiers);
	}


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

  	public static Node closestPrecedingFinger(int id) {
    	for (int i = m; i >= 1; i--) {
      	if (finger[i].)

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

  	}

  	public static void updateFingerTable() {
  		

  	}



}
