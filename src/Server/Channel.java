package Server;

import java.util.ArrayList;
import java.util.List;

import ProtocolSocket.ProtocolID;
import Server.ServerManager;
import Server.User;

/**
 * Contains the joined users and allows to broadcast messages to them
 * @author etsubu
 *
 */
public class Channel {
	private List<User> joinedUsers;
	private String channelName;
	private int channelID;
	private ServerManager manager;
	
	/**
	 * Initializes the channel
	 * @param channelName name of the channel
	 * @param channelID the ID of the channel
	 * @param manager ServerManager which handles clients and channels
	 * @throws InvalidChannelNameException Thrown if the channelName is invalid
	 */
	public Channel(String channelName, int channelID, ServerManager manager) throws InvalidChannelNameException {
	    if(!Channel.checkChannelNameValidity(channelName)) {
	        throw new InvalidChannelNameException(channelName);
	    }
		this.manager = manager;
		this.joinedUsers = new ArrayList<>();
		this.channelName = channelName;
		this.channelID = channelID;
	}
	
	/**
	 * Getter for the name of the channel
	 * @return The name of the channel
	 */
	public String getName() {
		return this.channelName;
	}
	
	/**
	 * Getter for the ID of the channel
	 * @return The ID of the channel
	 */
	public int getID() {
		return this.channelID;
	}
	
	/**
	 * Getter for the currently joined users
	 * @return List of users on the channel
	 */
	public List<User> getJoinedUsers() {
		return this.joinedUsers;
	}
	
	   /**
     * Checks the validity of the given name. A name can only contain letters and digits
     * @param name The name to check
     * @return True if the channel name is valid
     */
    public static boolean checkChannelNameValidity(String name) {
        for(int i = 0;i < name.length(); i++) {
            char c = name.charAt(i);
            if(Character.isAlphabetic(c) == false && Character.isDigit(c) == false) {
                return false;
            }
        }
        return true;
    }
	
	/**
	 * Adds user to the channel
	 * @param user to join
	 * @return Was the user added
	 */
	public synchronized boolean userJoin(User user) {
		for (User u:this.joinedUsers) {
			if (u.equals(user)) {
				return false;
			}
		}
		this.joinedUsers.add(user);
		for (User u : this.joinedUsers) {
			u.sendMessage(channelID, ProtocolID.CLIENT_JOINED, user.getName() + " joined the channel");
			u.sendListUsers(this);
		}
		return true;
	}
	
	/**
	 * Removes the user from the channel
	 * @param user user to remove
	 * @return Was the user removed
	 */
	public synchronized boolean userLeave(User user) {
		boolean removed = false;
		for (int i = 0; i < this.joinedUsers.size(); i++) {
			if(this.joinedUsers.get(i).equals(user)) {
				this.joinedUsers.remove(i);
				removed = true;
				break;
			}
		}
		for (User u : this.joinedUsers) {
			u.sendMessage(channelID, ProtocolID.CLIENT_LEFT, user.getName() + " left the channel");
			u.sendListUsers(this);
		}
		if (this.joinedUsers.isEmpty()) {
			this.manager.removeChannel(this);
		}
		return removed;
	}
	
	/**
	 * Broadcasts a message to the connected clients
	 * @param fromUser The user who sent the message
	 * @param message The sent message
	 */
	public synchronized void broadcastMessage(User fromUser, String message) {
		for (User u : this.joinedUsers) {
			u.sendMessage(this.channelID, fromUser.getName(), ProtocolID.CHANNEL_BROADCAST, message);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || o.getClass() != getClass()) {
			return false;
		}
		Channel channel = (Channel)o;
		return channel.getID() == this.channelID && channel.getName().equals(this.channelName);
	}

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
