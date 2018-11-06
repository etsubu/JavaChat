package Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ProtocolSocket.ProtocolSocket;

/**
 * Handles all the communication inside the server. Manages channels and connected users
 * @author etsubu
 *
 */
public class ServerManager {
	public static final int GLOBAL_CHANNEL_ID = 0;
	private List<User> users;
	private List<Channel> channels;
	private int channelIndex;
	private int userIndex;
	private final Lock userLock;
	private final Lock channelLock;
	
	/**
	 * Initializes the ServerManager
	 */
	public ServerManager() {
		this.users = new ArrayList<User>();
		this.channels = new ArrayList<Channel>();
		this.channelIndex = GLOBAL_CHANNEL_ID;
		this.userLock  = new ReentrantLock();
		this.channelLock = new ReentrantLock();
		
		//Create the default/global channel
		createChannel("Global");
	}
	
	/**
	 * Checks the validity of the given name. A name can only contain letters and digits
	 * @param name The name to check
	 * @return is the name allowed
	 */
	public boolean checkValidity(String name) {
		for(int i = 0;i < name.length(); i++) {
			char c = name.charAt(i);
			if(Character.isAlphabetic(c) == false && Character.isDigit(c) == false) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if the given nickname is not already used
	 * @param name the nickname to check
	 * @return is the nickname available
	 */
	public boolean isAvailableNickname(String name) {
		this.userLock.lock();
		String lower = name.toLowerCase();
		for(User u:this.users) {
			String userName = u.getName();
			if(userName != null && userName.toLowerCase().equals(lower)) {
				return false;
			}
		}
		this.userLock.unlock();
		return true;
	}
	
	/**
	 * Creates a new channel if the name is allowed and does not already exist
	 * @param name The name of the channel
	 * @return Was the channel created
	 */
	public boolean createChannel(String name){
		if(!checkValidity(name)) {
			return false;
		}
		String loweredName = name.toLowerCase();
		this.channelLock.lock();
		for(Channel c:this.channels) {
			if(c.getName().toLowerCase().equals(loweredName)) {
				return false;
			}
		}
		this.channels.add(new Channel(name, this.channelIndex, this));
		this.channelIndex++;
		this.channelLock.unlock();
		this.userLock.lock();
		for(User u:this.users) {
			try {
				u.sendChannelList();
			} catch (Exception e) {}
		}
		this.userLock.unlock();
		return true;
	}
	
	/**
	 * Removes the given channel
	 * @param c Channel to remove
	 * @return Was the channel removed
	 */
	public boolean removeChannel(Channel c) {
		if(c.getID() == GLOBAL_CHANNEL_ID) {
			return false;
		}
		boolean removed = false;
		this.channelLock.lock();
		for(int i = 0;i < this.channels.size();i++) {
			if(channels.get(i).equals(c)) {
				this.channels.remove(i);
				removed = true;
				break;
			}
		}
		this.channelLock.unlock();
		if(removed) {
			this.userLock.lock();
			for(User u:this.users) {
				try {
					u.sendChannelList();
				} catch (Exception e) {}
			}
			this.userLock.unlock();
		}
		return removed;
	}
	
	/**
	 * Gets a channel by the given name and creates it if it does not exist
	 * @param name Name of the channel
	 * @return Channel object or null if it does not exist and could not be created
	 */
	public synchronized Channel getChannel(String name) {
		this.channelLock.lock();
		for(Channel c:this.channels) {
			if(c.getName().equalsIgnoreCase(name)) {
				this.channelLock.unlock();
				return c;
			}
		}
		boolean created = createChannel(name);
		if(created) {
			Channel channel = this.channels.get(this.channels.size() - 1);
			this.channelLock.unlock();
			return channel;
		}
		this.channelLock.unlock();
		return null;
	}
	
	/**
	 * Lists the all visible channels and returns them as string object
	 * @return List of all visible channels
	 */
	public synchronized String listChannelNames(){
		StringBuilder nameBuilder = new StringBuilder();
		for(int i = 0;i < this.channels.size();i++) {
			nameBuilder.append(this.channels.get(i).getName() + "\n");
		}
		return nameBuilder.toString().substring(0, nameBuilder.length() - 1);
	}
	
	/**
	 * Getter for the global/default channel
	 * @return The global channel
	 */
	public Channel getGlobalChannel(){
		this.channelLock.lock();
		Channel global = this.channels.get(GLOBAL_CHANNEL_ID);
		this.channelLock.unlock();
		return global;
	}
	
	/**
	 * Adds a new user to the list of connected clients
	 * @param protoSocket The ProtocolSocket of the client
	 */
	public void addUser(ProtocolSocket protoSocket) {
		this.userLock.lock();
		User user = new User(this, protoSocket, this.userIndex);
		this.users.add(user);
		new Thread(user).start();
		this.userIndex++;
		this.userLock.unlock();
	}
	
	/**
	 * Removes a user from the list of connected clients
	 * @param user
	 */
	public void removeUser(User user) {
		this.userLock.lock();
		for(int i = 0;i < this.users.size();i++) {
			if(this.users.get(i).equals(user)) {
				System.out.println(user.toString() + " Disconnected.");
				this.users.remove(i);
				break;
			}
		}
		this.userLock.unlock();
		this.channelLock.lock();
		for(Channel c:this.channels) {
			c.userLeave(user);
		}
		this.channelLock.unlock();
	}
	
	/**
	 * Disconnects all the connected clients
	 */
	public void closeConnections() {
		this.userLock.lock();
		for(User u:this.users) {
			u.close("Server is closing!");
		}
		this.users.clear();
		this.userLock.unlock();
	}
}
