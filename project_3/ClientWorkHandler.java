import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

// import org.json.simple.JSONObject;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import java.util.concurrent.*;

public class ClientWorkHandler implements ClientWork.Iface {
	private String ip;
	private String port;
	private int count = 0;
	private int numOfOps = 0;
	private int startTime = 0;
	public ClientWorkHandler(String ip, String port) {
		this.ip = ip;
		this.port = port;
	}


  	@Override
  	public Synchronized void printRet(String ret){
    	System.out.println(ret);
    	count++;
  	}

  	@Override
  	public void setParams(int numOfOps, int startTime) {
  		this.numOfOps = numOfOps;
  		this.startTime = startTime;
  	}



}