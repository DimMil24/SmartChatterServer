package com.example.smartchatters.logic;
import java.util.ArrayList;

public class Topic {

	private String name;
	private ArrayList<MultimediaFile> history;
	private ArrayList<String> historyFileNames;
	private ArrayList<ProfileName> subscribedUsers;
	private final Object monitor = new Object();
	private int fileId;
	
	public Topic(String name) {
		this.name=name;
		history=new ArrayList<MultimediaFile>();
		historyFileNames=new ArrayList<String>();
		subscribedUsers=new ArrayList<ProfileName>();
		fileId=1;
	}
	
	/* Registers user to the current topic. */
	
	public void registerUser(ProfileName user) {
		subscribedUsers.add(user);
	}
	
	/* Adds the file to the history of the current topic. */
	
	public void addMultimediaFile(MultimediaFile file) {
		history.add(file);
	}
	
	/* Adds the file name to the file name history*/
	public void addFileName(String filename) {
		historyFileNames.add(filename);
	}
	
	/* Returns the name of the current topic. */
	
	public String getName() {
		return name;
	}
	
	/* Returns the history of the current topic. */
	
	public ArrayList<MultimediaFile> getHistory() {
		return history;
	}
	
	/* Returns the subscribers of the current topic. */
	
	public ArrayList<ProfileName> getSubscribedUsers() {
		return subscribedUsers;
	}
	
	/* Shows the subscribers of the current topic. */
	
	public void showUsers() {
		System.out.println("Topic "+ name + " users:");
		for (int i=0;i<subscribedUsers.size();i++)System.out.println(subscribedUsers.get(i));
	}
	
	/* Shows the filenames of the history. */
	
	public void showFileNames() {
		System.out.println("Topic "+ name + " file names history:");
		for (int i=0;i<historyFileNames.size();i++)System.out.println(historyFileNames.get(i));
	}
	
	/* Returns the monitor of the current topic. */
	
	public Object getMonitor() {
		return monitor;
	}
	
	/* Prints the name of the current topic. */
	
	public String toString() {
		return name;
	}
	
	public int getCurrentFileID() {
		return fileId++;
	}
	
}
