package com.example.smartchatters.logic;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class Usernode {

    Socket requestSocket = null;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;
    Scanner text = null;
    ProfileName user=null;
    Publisher publisher=null;
    Consumer consumer=null;
    private ArrayList<String> topicNames;
    private HashMap<String,Integer> topic_broker;
    private ArrayList<String> ips;
	private ArrayList<Integer> ports;

    public Usernode (ProfileName user) {

    	this.user=user;
    	connect();
    	
    }
    
    /* Current user's first connection to the Event Delivery System. */
    
    public void connect() {
    	
    	try {
            ArrayList<String> tempip = new ArrayList<String>();
            ArrayList<Integer> tempport = new ArrayList<Integer>();
            File fp = new File("confUser.txt");
            FileReader fr;
            fr = new FileReader(fp);
            BufferedReader br = new BufferedReader(fr);
            String line;
            String[] ip_port;
            while ((line = br.readLine()) != null) {
                ip_port = line.split(":");
                tempip.add(ip_port[0]);
                tempport.add(Integer.parseInt(ip_port[1]));
            }
            fr.close();

            int index = new Random().nextInt(3);
    		
            requestSocket = new Socket(tempip.get(index), tempport.get(index));
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            MultimediaFile item;
        	item = new MultimediaFile(null,user,"23/4/2022",null, 0, "NewConnection".length()+1,0);
            item.FileChunk = "NewConnection".getBytes(StandardCharsets.UTF_8);
            out.writeObject(item);
            out.flush();
            ips=(ArrayList<String>)in.readObject();
            ports=(ArrayList<Integer>)in.readObject();
            topicNames=(ArrayList<String>)in.readObject();
            topic_broker=(HashMap<String,Integer>)in.readObject();
            
            System.out.println("Connected to port: "+ tempip.get(index)+":"+tempport.get(index));
            showInfo();
            
        } catch (IOException e) {
        	System.err.println("UserNode IO error in connect.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
        	System.err.println("UserNode ClassNotFound error in connect.");
			e.printStackTrace();
		}finally {
	        try {
	            in.close();
	            out.close();
	            requestSocket.close();
	        } catch (IOException i) {
	        	System.err.println("UserNode IO error in connect while closing streams.");
	        	i.printStackTrace();
	        }
        }
    	
    }
    
    /* Shows the information about the brokers. */
    
    public void showInfo() {
    	
    	String s="Ips and ports of brokers:\n";
    	for (int i=0;i<ips.size();i++)s=s+ips.get(i)+ " ";
    	s=s+"\n";
    	for (int i=0;i<ports.size();i++)s=s+ports.get(i)+ " ";
    	s=s+"\nTopic names:\n";
    	for (int i=0;i<topicNames.size();i++) s=s+topicNames.get(i)+" ";
    	s=s+"\n";
    	for (String topic : topic_broker.keySet())s=s+"Broker " + topic_broker.get(topic) + " is responsible for topic "+topic + ".\n";
    	System.out.println(s);
    	
    }
    
    /* Registers the current user to the topic. */
    
    public void register(String topicName) {
    	
    	Integer brokerId=topic_broker.get(topicName);
    	
    	if (brokerId==null) {
    		System.out.println(topicName+"doesn't exist.");
    		return;
    	}
    	
    	try {
            requestSocket = new Socket(ips.get(brokerId-1),ports.get(brokerId-1));
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            MultimediaFile item;
        	item = new MultimediaFile(null,user,"Today",null, 0, "Register".length()+1,0);
            item.FileChunk = "Register".getBytes(StandardCharsets.UTF_8);
            out.writeObject(item);
            out.flush();
            out.writeObject(topicName);
            out.flush();
            out.writeObject(user);
            out.flush();
        } catch (IOException e) {
        	System.err.println("UserNode IO error in connect.");
            e.printStackTrace();
        }finally {
	        try {
	        	boolean succesfulRegister= in.readBoolean();
	            in.close();
	            out.close();
	            requestSocket.close();
	            if (succesfulRegister) {
	            	user.subscribeTo(topicName);
	            	consumer=new Consumer(topicName);
	            	consumer.start();
	            }
	        } catch (IOException i) {
	        	System.err.println("UserNode IO error in connect while closing streams.");
	        	i.printStackTrace();
	        }
        }
    	
    }
    
    /* User's push function. */
    
    public void publish(String topicName,String fileName,String dateCreated, String fileExtension ) {

    	Integer brokerId=topic_broker.get(topicName);
    	
    	if (brokerId==null) {
    		System.out.println(topicName+"doesn't exist.");
    		return;
    	}
    	
    	publisher=new Publisher(topicName,fileName,dateCreated,fileExtension);
    	publisher.start();
    }
    
    
    public class Publisher extends Thread {
    	
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String topicName=null;
        String fileName=null;
        String dateCreated=null;
        String fileExtension=null;

        Publisher(String topic,String fileName,String dateCreated, String fileExtension ) {
        	this.topicName=topic;
        	this.fileName=fileName;
        	this.dateCreated=dateCreated;
        	this.fileExtension=fileExtension;
        }

        /* Publisher's thread, it runs as long as a file is being uploaded. */
        
        public void run() {
        	try {
                requestSocket = new Socket(ips.get(topic_broker.get(topicName)-1),ports.get(topic_broker.get(topicName)-1));
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());
                MultimediaFile item;
            	item = new MultimediaFile(null,user,dateCreated,null, 0, "PublisherConnection".length()+1,0);
                item.FileChunk = "PublisherConnection".getBytes(StandardCharsets.UTF_8);
                out.writeObject(item);
                out.flush();
                sendFile();
            } catch (IOException e) {
            	System.err.println("UserNode IO error in run.");
                e.printStackTrace();
            } finally {
    	        try {
    	            in.close();
    	            out.close();
    	            requestSocket.close();
    	        } catch (IOException i) {
    	        	System.err.println("UserNode IO error in run while closing streams.");
    	        	i.printStackTrace();
    	        }
            }
        }

        /* Sends the file of the publisher. */
        
        public void sendFile() {
            try {
                MultimediaFile item;
                File file=null;
                if (fileExtension!=null){
                     file = new File(fileName+"."+fileExtension);
                    item = new MultimediaFile(fileName,user,dateCreated,fileExtension, (int) Math.ceil((float)file.length() / 8192), "send".length()+1,0);
                }
                else {
                    item = new MultimediaFile(fileName,user,dateCreated,fileExtension, 0, "send".length()+1,0);
                }
                item.FileChunk = "send".getBytes(StandardCharsets.UTF_8);
                out.writeObject(item);
                out.flush();
                out.writeObject(topicName);
                out.flush();
                out.writeObject(user);
                out.flush();
                boolean verification=true;
                if (!verification) {
                    System.out.println("Seems like you are trying to push in a topic that you don't belong");
                    return;
                }
                if (fileExtension!=null) {
                    InputStream FileStream = new FileInputStream(file);
                    int count=1;
                    item = new MultimediaFile(fileName, user, dateCreated,fileExtension, (int) Math.ceil((float)file.length() / 8192), 8192,count);
                    while ((FileStream.read(item.FileChunk)) > 0) {
                        out.writeObject(item);
                        out.flush();
                        System.out.println("Publisher sent "+item.getChunkId()+" chunk of file :"+ item.getName()+"."+item.getExtension());
                        item = new MultimediaFile(fileName, user, dateCreated,fileExtension, (int) Math.ceil((float)file.length() / 8192), 8192,count+1);
                        count++;
                    }
                    FileStream.close();
                }
                return;
            } catch (IOException e) {
                System.err.println("UserNode IO error in sendFile.");
                e.printStackTrace();
            }
        }
        
    }
    
    public class Consumer extends Thread {
        Socket requestSock = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String topicName=null;
        private int receivedCounter=0;
        private HashMap<Integer,OutputStream> Ids_activeFileStreams;

        Consumer(String topicName) {
        	this.topicName=topicName;
        	Ids_activeFileStreams=new HashMap<Integer,OutputStream>();
        }

        /* Consumer's thread, it doesn't stop running after the registration. */
        
        public void run() {
        	try {
                requestSocket = new Socket(ips.get(topic_broker.get(topicName)-1),ports.get(topic_broker.get(topicName)-1));
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());
                MultimediaFile item;
            	item = new MultimediaFile(null,user,"Today",null, 0, "ConsumerConnection".length()+1,0);
                item.FileChunk = "ConsumerConnection".getBytes(StandardCharsets.UTF_8);
                out.writeObject(item);
                out.flush();
                out.writeObject(topicName);
                out.flush();
                out.writeObject(user);
                out.flush();
                boolean verification=true;
                if (!verification) {
                	System.out.println("Seems like you are trying to pull from a topic that you don't belong");
                	return;
                }
                saveFiles();
                
            } catch (IOException e) {
            	System.err.println("Consumer IO error in run.");
                e.printStackTrace();
            } finally {
    	        try {
    	            in.close();
    	            out.close();
    	            requestSocket.close();
    	        } catch (IOException i) {
    	        	System.err.println("Consumer IO error in run while closing streams.");
    	        	i.printStackTrace();
    	        }
            }
        }

        /* Saves files that the consumer receives. */
        
        public void saveFiles() {
            try {
                OutputStream fetched=null;
                MultimediaFile item;
                String r = "";
                while (!r.equals("over")) {
                    item = (MultimediaFile) in.readObject();
                    r = new String(item.FileChunk, StandardCharsets.UTF_8);
                    if (r.equals("send")) {
                        if (item.getExtension()!=null){
                        	Ids_activeFileStreams.put(item.getFileId(),new FileOutputStream("C:\\Users\\jmili\\Desktop\\received\\received"+ user.getUsername()+topicName +(receivedCounter++) +"."+item.getExtension()));
                        	System.out.println("Consumer started receiving new file: "+ item.getName()+"."+item.getExtension());
                        }
                        else {
                            System.out.println("Consumer started receiving new string: "+ item.getName());
                        }
                        continue;
                    } else if (r.equals("end")) {
                        //user.addFile(topicName,item);
                        if (item.getExtension()!=null){
                            fetched=Ids_activeFileStreams.get(item.getFileId());
                            if (fetched!=null) {
                                fetched.close();
                                Ids_activeFileStreams.remove(item.getFileId());
                                System.out.println("Consumer received file.");
                            }
                        }
                        else {
                        	System.out.println("String:"+item.getName());
                            System.out.println("Consumer received string.");
                        }

                        continue;
                    }
                    Ids_activeFileStreams.get(item.getFileId()).write(item.FileChunk);
                    System.out.println("Consumer received " + item.getChunkId() + " chunk of file: " + item.getName() + "." + item.getExtension());
                }
            }catch (IOException e) {
                System.err.println("Consumer IO error in saveFile.");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.err.println("Consumer ClassNotFound error in saveFile.");
                e.printStackTrace();
            } finally {
                try {
                    Set<Integer> keys=Ids_activeFileStreams.keySet();
                    for (Integer fileId:keys) {
                        Ids_activeFileStreams.get(fileId).close();
                        Ids_activeFileStreams.remove(fileId);
                    }
                } catch (IOException e) {
                    System.err.println("Consumer IO error in receiveFile while closing streams.");
                    e.printStackTrace();
                }
            }
        }

    }

}

