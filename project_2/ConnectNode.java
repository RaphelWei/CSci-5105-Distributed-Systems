public class ConnectNode {
	public static Node lookupBook_ByKey(int key, String title, String genre) {
		Node p = find_successor(key);
		try{
          TTransport  transport = new TSocket(p.getIP(), Integer.parseInt(p.getPort()));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          WorkWithNode.Client client = new WorkWithNode.Client(protocol);
          //Try to connect
          transport.open();
          result = client.find_successor(finger[1].getStart());
          transport.close();

        } catch(TException e) {
        }
	}

	public static String returnGenre() {

	}

	public static void insertBook_ByKey(int key, String title, String genre) {
		Node p = find_successor(key);
		try{
          TTransport  transport = new TSocket(p.getIP(), Integer.parseInt(p.getPort()));
          TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
          WorkWithNode.Client client = new WorkWithNode.Client(protocol);
          //Try to connect
          transport.open();
          result = client.find_successor(finger[1].getStart());
          transport.close();

        } catch(TException e) {
        }
	}
}