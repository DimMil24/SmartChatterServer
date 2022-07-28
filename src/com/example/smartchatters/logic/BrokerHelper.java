package com.example.smartchatters.logic;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class BrokerHelper extends Thread {
	
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ArrayList<String> ips;
    private ArrayList<Integer> ports;
    private ArrayList<Topic> localTopics;
    private HashMap<String,Integer> topic_broker;
    private Topic currentTopic=null;
    private ProfileName currentUser=null;

    public BrokerHelper(Socket connection,ArrayList<String> ips, ArrayList<Integer> ports, ArrayList<Topic> localTopics, HashMap<String,Integer> topic_broker) {
        try {
        	this.ips=ips;
        	this.ports=ports;
        	this.localTopics=localTopics;
        	this.topic_broker=topic_broker;
            System.out.println("Got a connection...");
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(new BufferedInputStream(connection.getInputStream()));

        } catch (IOException e) {
        	System.err.println("BrokerHelper IO error in constructor.");
            e.printStackTrace();
        }

    }

    public void run() {
    	
        try {
            MultimediaFile item;
            String r="";
            while (!r.equals("PublisherConnection") && !r.equals("ConsumerConnection") && !r.equals("NewConnection") && !r.equals("Register")) {
            	item = (MultimediaFile) in.readObject();
                r = new String(item.FileChunk, StandardCharsets.UTF_8);
                System.out.println("Command: "+r);
                if (r.equals("PublisherConnection")) {
                	publisherBrokerHelper();
                	break;
                }
                if (r.equals("ConsumerConnection")) {
                	consumerBrokerHelper();
                	break;
                }
                if (r.equals("NewConnection")) {
                	newConnBrokerHelper();
                	break;
                }
                if (r.equals("Register")) {
                	registerBrokerHelper();
                	break;
                }
            }
           
        }catch (IOException e) {
        	System.err.println("BrokerHelper IO error in run.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
        	System.err.println("BrokerHelper ClassNotFound error in run.");
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                System.out.println("End of Connection");
            } catch (IOException ioException) {
            	System.err.println("BrokerHelper IO error in run while closing streams.");
                ioException.printStackTrace();
            }
        }

    }
    
    /* Handles first connection with the user. */
    
    public void newConnBrokerHelper() {
    	
    	try {
    		out.writeObject(ips);
			out.flush();
			out.writeObject(ports);
			out.flush();
			ArrayList<String> topicNames= new ArrayList<String>();
			for (String topic : topic_broker.keySet()){
	    		topicNames.add(topic);
	    	}
			out.writeObject(topicNames);
			out.flush();
			out.writeObject(topic_broker);
			out.flush();
		} catch (IOException e) {
			System.err.println("newConnBrokerHelper IO error.");
			e.printStackTrace();
		} 
    	
    }
    
    /* Handles registration of the user to a topic. */
    
    public void registerBrokerHelper() {
    	
    	try {
    		boolean registered=false;
    		String tempTopic=(String)in.readObject();
    		ProfileName tempProfile=(ProfileName)in.readObject();
    		for (int i=0;i<localTopics.size();i++) {
    			if (localTopics.get(i).getName().equals(tempTopic)) {
    				localTopics.get(i).registerUser(tempProfile);
    				localTopics.get(i).showUsers();
    				out.writeBoolean(true);
    				registered=true;
    				break;
    			}
    		}
    		out.writeBoolean(registered);
			out.flush();
		} catch (IOException e) {
			System.err.println("registerBrokerHelper IO error.");
			e.printStackTrace();
		}  catch (ClassNotFoundException e) {
			System.err.println("registerBrokerHelper ClassNotFound error.");
			e.printStackTrace();
		}
    	
    }
    
    /* Handles publisher's thread. */
    
    public void publisherBrokerHelper() {
    	try {
	    	MultimediaFile item;
	        String r = "";
	    	while (!r.equals("send")) {
				item = (MultimediaFile) in.readObject();
				r = new String(item.FileChunk, StandardCharsets.UTF_8);
		        System.out.println("Command: "+r);
		        if (r.equals("send")) {
		            receiveFile(item.getName(),item.getExtension(),item);
		        }
    		}
    	} catch (ClassNotFoundException e) {
			System.err.println("PublisherBrokerHelper ClassNotFound error in run.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Connection with client terminated unexpectedly");
		}
    }
    
    /* Handles consumer's thread. */
    
    public void consumerBrokerHelper() {
    	String topicName=null;
    	ProfileName userName=null;
    	boolean verification=true;
    	int updatedToHistory=0;
		try {
			topicName = (String)in.readObject();
			userName = (ProfileName)in.readObject();
			updatedToHistory = (Integer)in.readObject();
			for (int i=0;i<localTopics.size();i++) {
	    		if (localTopics.get(i).getName().equals(topicName)) {
	    			currentTopic=localTopics.get(i);
	    			break;
	    		}
	    	}
//			for (int i=0;i<currentTopic.getSubscribedUsers().size();i++) {
//	    		if (currentTopic.getSubscribedUsers().get(i).getUsername().equals(userName.getUsername())) {
//	    			verification=true;
//	    			break;
//	    		}
//	    	}
//			out.writeBoolean(verification);
//	    	out.flush();
		} catch (ClassNotFoundException e1) {
			System.err.println("ConsumerBrokerHelper ClassNotFound error in topic name fetch.");
			e1.printStackTrace();
		} catch (IOException e1) {
			System.err.println("ConsumerBrokerHelper IO error error in topic name fetch.");
			e1.printStackTrace();
		}
		if (!verification) {
			System.out.println(userName.getUsername()+" is not subscribed to "+ topicName);
			return;
		}
		else currentUser=userName;
    	MultimediaFile item;
    	while (true) {
    		try {
				Thread.sleep(250);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		synchronized (currentTopic.getMonitor()) {
	    		if (updatedToHistory<currentTopic.getHistory().size()) {
	    	        try {
	    	        	item=currentTopic.getHistory().get(updatedToHistory++);
                        System.out.println("PRIN TO SOCKET	");
	    	        	out.writeObject(item);
	    				out.flush();
	    				System.out.println("ConsumerBrokerHelper sent "+updatedToHistory + " chunk of file: "+ item.getName()+"."+item.getExtension());
					} catch (IOException e) {
						System.err.println("ConsumerBrokerHelper IO error in run.");
						e.printStackTrace();
						break;
					}
	    	        
	    		}
    		}
    	}
             
    }

    /* Handles the receiving of the file. */
    
    public void receiveFile(String fileName,String fileExtension, MultimediaFile sendMessage) {
    	String topicName=null;
    	ProfileName userName=null;
    	boolean verification=true;
		try {
			topicName = (String)in.readObject();
			userName = (ProfileName)in.readObject();
			for (int i=0;i<localTopics.size();i++) {
	    		if (localTopics.get(i).getName().equals(topicName)) {
	    			currentTopic=localTopics.get(i);
	    			break;
	    		}
	    	}
//			for (int i=0;i<currentTopic.getSubscribedUsers().size();i++) {
//	    		if (currentTopic.getSubscribedUsers().get(i).getUsername().equals(userName.getUsername())) {
//	    			verification=true;
//	    			break;
//	    		}
////	    	}
//			out.writeBoolean(verification);
//	    	out.flush();
		} catch (ClassNotFoundException e1) {
			System.err.println("PublisherBrokerHelper ClassNotFound error in topic name fetch.");
			e1.printStackTrace();
		} catch (IOException e1) {
			System.err.println("PublisherBrokerHelper IO error error in topic name fetch.");
			e1.printStackTrace();
		}
		if (!verification) {
			System.out.println(userName.getUsername()+" is not subscribed to "+ topicName);
			return;
		}
		else {
			currentUser=userName;
		}
        try {
        	int currentFileId=0;
            MultimediaFile item=null;
            int count = 1;
            long num = sendMessage.numberOfChunks;
            ArrayList<MultimediaFile> tempBuffer=new ArrayList<MultimediaFile>();
            tempBuffer.add(sendMessage);
            while (count <= num) {
            	item = (MultimediaFile) in.readObject();
				System.out.println("PublisherBrokerHelper sent "+item.getChunkId() + " chunk of file: "+ item.getName()+"."+item.getExtension());
            	tempBuffer.add(item);
            	count++;
            }
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String strDate = dateFormat.format(date);
            item = new MultimediaFile(fileName,currentUser,strDate,fileExtension, 0, "end".length()+1,0);
            item.FileChunk = "end".getBytes(StandardCharsets.UTF_8);
            tempBuffer.add(item);
            System.out.println("HRTHE MOLIS PRAMA");
            synchronized (currentTopic.getMonitor()) {
            	currentFileId=currentTopic.getCurrentFileID();
            	currentTopic.addFileName(fileName);
            }
            
            for (MultimediaFile chunk: tempBuffer)chunk.setFileId(currentFileId);
            
            for (MultimediaFile chunk:tempBuffer) {
            	synchronized (currentTopic.getMonitor()) {
            		currentTopic.addMultimediaFile(chunk);
            	}
            }
            
            tempBuffer.clear();
            
        }catch (IOException e) {
        	System.err.println("PublisherBrokerHelper IO error in receiveFile.");
        	e.printStackTrace();
        } catch (ClassNotFoundException e) {
        	System.err.println("PublisherBrokerHelper ClassNotFound error in receiveFile.");
            e.printStackTrace();
        } 
    }

}