import java.rmi.*;

//
// SuperNodeDef Interface
// RMI Interface
//
public interface SuperNodeDef extends Remote
{
	public String GetNode() throws RemoteException;

	public String getNodeList() throws RemoteException;

	public void PostJoin(int id) throws RemoteException;

	public String Join(String ipAddr, String port) throws RemoteException;

}
