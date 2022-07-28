package com.example.smartchatters.logic;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Broker {

	private ArrayList<String> ips;
	private ArrayList<Integer> ports;
    private ArrayList<Topic> localTopics;
    private HashMap<String,Integer> topic_broker;
    private int brokerId;
    private String ip;
    private int port;

    private ServerSocket provideSocket;
    private Socket connection = null;
    
    public Broker(int brokerId) {
    	
    	ips=new ArrayList<String>();
    	ports=new ArrayList<Integer>();
    	localTopics= new ArrayList<Topic>();
    	topic_broker=new HashMap<String,Integer>();
    	this.brokerId=brokerId;
    	
    	readConfig();
    	setLocalTopics();
    	
    	System.out.println("Broker "+ brokerId+" ip: " +ip);
    	System.out.println("Broker "+ brokerId+" port: " +port);
    	System.out.println("Broker "+ brokerId+" topics: ");
    	for (int i=0;i<localTopics.size();i++) {
    		System.out.println(localTopics.get(i));
    	}
    	System.out.println();
    	
    }

    /* Initiates the broker's server. */
    
    public void openServer() {

    	System.out.println("Broker Running.");
    	
        try {
            provideSocket = new ServerSocket(port);

            while (true) {
                connection = provideSocket.accept();
                Thread t = new BrokerHelper(connection,ips,ports,localTopics,topic_broker);
                t.start();

            }



        } catch (IOException ioException) {
        	System.err.println("Broker IO error.");
            ioException.printStackTrace();
        } finally {
            try {
                provideSocket.close();
            } catch (IOException ioException) {
            	System.err.println("Broker IO error while closing streams.");
                ioException.printStackTrace();
            }
        }

    }
    
    /* Reads the configuration file to setup brokers info. */
    
    public void readConfig() {
    	
		try {
			File fp = new File("conf.txt");
			FileReader fr;
			fr = new FileReader(fp);
			BufferedReader br = new BufferedReader(fr);
	    	String line;
	    	String[] id_ip_port;
	    	boolean passedDelimiter=false;
	    	while((line = br.readLine()) != null) { 

	    		if (line.equals("%")) {
	    			passedDelimiter=true;
	    			continue;
	    		}
	    		
	    		if (!passedDelimiter) {
	    			id_ip_port=line.split(":");
	    			if (id_ip_port[0].equals(String.valueOf(brokerId))){
	    				this.ip=id_ip_port[1];
	    				this.port=Integer.parseInt(id_ip_port[2]);
	    			}
	    			ips.add(id_ip_port[1]);
	    			ports.add(Integer.parseInt(id_ip_port[2]));
	    		}
	    		else {
	    			topic_broker.put(line,calculateKey(line));
	    		}
	    	
	    		
	    	}
			fr.close();	
		} catch (FileNotFoundException e) {
			System.err.println("cong.txt not found.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO error in conf.txt");
			e.printStackTrace();
		}
    }
    
    /* Sets the topics of the current broker. */
    
    public void setLocalTopics() {
    	for (String topic : topic_broker.keySet()){
    		if (topic_broker.get(topic)==brokerId)localTopics.add(new Topic(topic));
    	}
    }
    
    /* The hash function. */
    
    public int calculateKey(String val) {
    	return Math.abs(val.hashCode() % 3)+1;
    }
    
    /* Returns the IP address of the current broker. */
    
    public String getIP() {
    	return ip;
    }
    
    /* Returns the port address of the current broker. */
    
    public int getPort() {
    	return port;
    }

}
