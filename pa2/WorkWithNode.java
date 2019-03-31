import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
// import java.io.*;
// import java.util.*;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
// import java.io.File;
// import java.util.Map;
// import java.util.HashMap;
// import java.util.Iterator;
// import java.util.concurrent.*;

public class WorkWithNodeHandler implements WorkWithNode.Iface
{
  // each "server", node, has a handler in its object.
  // private String[] KeyRange = {"0", "0"};// init to 0
  private NavigableMap<Stirng, String> records = new TreeMap<>();

  // Chord identifier|IP address
  private String predecessorInfo;

  // NodeID|IP|Port|
  private String[] fingerTable;

  // public static void SortByKey(){
  //   // TreeMap to store values of HashMap
  //   TreeMap<String, String> sorted = new TreeMap<>();
  //
  //   // Copy all data from hashMap into TreeMap
  //   sorted.putAll(records);
  //   records = new HashMap<>();
  //
  //   // Display the TreeMap which is naturally sorted
  //   for (Map.Entry<String, Integer> entry : sorted.entrySet())
  //       System.out.println("Key = " + entry.getKey() +  ", Value = " + entry.getValue());
  //       records.put(entry.getKey(), entry.getValue());
  // }

  @Override
  public String[] UpdateDHT(){

    // find the second half
    String middleKey = (records.firstEntry().getKey()+records.lastEntry().getKey())/2;
    String pairset;

    // one way
    NavigableMap<Stirng, String> tailMap = new TreeMap<String,String>(records.tailMap(middleKey));
    //-----------------------------
    // NavigableMap<Stirng, String> tailMap = new TreeMap<String,String>();
    // tailMap.putAll(records.tailMap(middleKey));
    //-----------------------------

    
    records.navigableKeySet().removeAll(tailMap.navigableKeySet());

    // the other way
    NavigableMap<Stirng, String> tailMap = new TreeMap<String,String>();

    while(true){
      if(records.lastEntry().getKey()<=middleKey){
        break;
      }
      Map.Entry<String,String> temp = records.pollLastEntry();
      tailMap.put(temp.getKey(), temp.getValue());
    }

    // predecessor, successor, pairset
    String[] ret = {predecessorInfo, fingerTable[1], }


    return
  }

  // randomly given an ID, which is not in the interval provided by the supernode
  public String[] find_successor(String NodeID){

  }
  public String[] find_predecessor(String NodeID){

  }

}
