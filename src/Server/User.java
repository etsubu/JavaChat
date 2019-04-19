package Server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ProtocolSocket.Packet;
import ProtocolSocket.ProtocolID;
import ProtocolSocket.ProtocolSocket;

public class User implements Runnable{
	private ProtocolSocket socket;
	private String nickname;
	private int ID;
	private boolean alive;
	private ServerManager manager;
	private Map<Integer, Channel> joinedChannels;
	
	/**
	 * Initialize the user
	 * @param manager The ServerManager
	 * @param socket connection socket
	 * @param ID Unique ID
	 */
	public User(ServerManager manager, ProtocolSocket socket, int ID) {
		this.manager = manager;
		this.socket = socket;
		this.ID = ID;
		this.nickname = null;
		this.alive = true;
		this.joinedChannels = new HashMap<>();
	}
	
	/**
	 * Getter for the unique user ID
	 * @return ID of the user
	 */
	public int getID(){
		return this.ID;
	}
	
	/**
	 * Closes the connection with a message
	 * @param message Message to send before closing
	 */
	public void close(String message) {
		try{
			this.socket.write(message, ProtocolID.CONNECTION_CLOSED);
		}catch(Exception e){
		    // 
		}
		cleanup();
	}
	
	/**
	 * Getter for nickname of the user
	 * @return Nickname of the user
	 */
	public String getName() {
		return this.nickname;
	}
	
	/**
	 * Closes the socket and sets the status to not connected
	 */
	public void cleanup() {
		this.socket.close();
		this.alive = false;
		this.manager.removeUser(this);
	}
	
	/**
	 * Sends a message to the client
	 * @param channel Channel ID where the message came from
	 * @param name The name of the user that sent the message
	 * @param typeID The type of the message 
	 * @param message The message content
	 */
	public void sendMessage(int channel, String name, ProtocolID typeID, String message) {
		if(!alive) {
			return;
		}
		String data = channel + ":" + name + ":" + message;
		try {
			this.socket.write(data, typeID);
		} catch (Exception e) {
			cleanup();
		}
	}
	
	/**
	 * Sends a message to the client
	 * @param channel Channel ID where the message came from
	 * @param typeID The type of the message 
	 * @param message The message content
	 */
	public void sendMessage(int channel, ProtocolID typeID, String message) {
		if (!alive) {
			return;
		}
		String data = channel + ":" + message;
		try {
			this.socket.write(data.getBytes("UTF-8"), typeID);
		} catch (Exception e) {
			cleanup();
		}
	}
	/**
	 * Reads the user nickname and joins the global channel
	 * @return was nickname the nickname valid
	 */
	private boolean readNickname() {
		try {
			Packet namePacket = this.socket.readPacket();
			String name = new String(namePacket.getData());
			if(!Channel.checkChannelNameValidity(name)) {
				close("Nickname can only contain letters and numbers!");
				return false;
			}
			if(!this.manager.isAvailableNickname(name)) {
				close("Nickname is already in use!");
				return false;
			}
			this.nickname = name;
		} catch (Exception e) {
			cleanup();
			return false;
		}
		return true;
	}
	
	/**
	 * Processes the packet that has arrived
	 * @param packet The packet that arrived
	 */
	private void processPacket(Packet packet) {
		try{
			int type = packet.getHeader().getType();
			if (type == ProtocolID.CHANNEL_BROADCAST.ordinal()) {
				processBroadcastMessage(new String(packet.getData(), StandardCharsets.UTF_8));
			} else if (type == ProtocolID.LIST_USERS.ordinal()) {
				processListUsers(new String(packet.getData(), StandardCharsets.UTF_8));
			} else if (type == ProtocolID.LIST_CHANNELS.ordinal()) {
				sendChannelList();
			} else if (type == ProtocolID.JOIN_CHANNEL.ordinal()) {
				joinUserToChannel(new String(packet.getData(), StandardCharsets.UTF_8));
			} else if (type == ProtocolID.LEAVE_CHANNEL.ordinal()) {
				leaveChannel(Integer.parseInt(new String(packet.getData(), StandardCharsets.UTF_8)));
			}
		} catch(Exception e){
			cleanup();
		}
	}
	
	/**
	 * Removes the user from the given channel
	 * @param channelID ID of the channel to leave from
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	private void leaveChannel(int channelID) throws UnsupportedEncodingException, IOException {
		Channel c = this.joinedChannels.get(channelID);
		if(c != null) {
			c.userLeave(this);
			this.joinedChannels.remove(channelID);
			this.socket.write(Integer.toString(channelID), ProtocolID.LEAVE_CHANNEL);
		}
	}
	/**
	 * Send the user list of users on the requested channel
	 * @param message The channel to request the user list from
	 */
	private void processListUsers(String message) throws Exception{
		int channelID = Integer.parseInt(message);
		Channel channel = this.joinedChannels.get(channelID);
		if (channel == null) {
			this.socket.write(Integer.toString(channelID), ProtocolID.LIST_USERS);
		} else {
			List<User> users = channel.getJoinedUsers();
			StringBuilder userList = new StringBuilder();
			userList.append(channelID+":");
			for(User u:users) {
				userList.append(u.getName() + "\n");
			}
			String listStr = userList.toString().substring(0, userList.length() - 1);
			this.socket.write(listStr, ProtocolID.LIST_USERS);
		}
	}
	/**
	 * Processes the received broadcastmessage
	 * @param data the message that was received
	 */
	private void processBroadcastMessage(String data) throws NumberFormatException {
		int index = data.indexOf(":");
		if (index == -1 || index == data.length() - 1) {
			cleanup();
		}
		int channelID = Integer.parseInt(data.substring(0, index));
		String message = data.substring(index + 1);
		Channel channel = this.joinedChannels.get(channelID);
		if (channel != null) {
			channel.broadcastMessage(this, message);
		}
	}
	
	/**
	 * Tries to join the user to the given channel if he is not already in it
	 * @param channel Channel to join
	 * @return Was the user added to channel
	 */
	private boolean joinUserToChannel(Channel channel) {
		if (this.joinedChannels.containsKey(channel.getID()) == false && channel.userJoin(this)) {
			this.joinedChannels.put(channel.getID(), channel);
			try {
				this.socket.write(new String(channel.getID() + ":" + channel.getName()), ProtocolID.JOIN_CHANNEL);
			} catch (Exception e) {
				cleanup();
				return false;
			}
			sendListUsers(channel);
			return true;
		}
		return false;
	}
	
	/**
	 * Tries to join the user to the given channel if he is not already in it
	 * @param name Name of the channel to join
	 */
	private void joinUserToChannel(String name) {
		Channel channel = this.manager.getChannel(name);
		if(channel != null) {
			joinUserToChannel(channel);
		}
	}
	
	/**
	 * Sends the user a list of users on the given channel
	 * @param channel Channel whose users list
	 */
	public void sendListUsers(Channel channel) {
		if(channel == null) {
			return;
		}
		List<User> users = channel.getJoinedUsers();
		StringBuilder userList = new StringBuilder();
		userList.append(channel.getID()+":");
		for(User u:users) {
			userList.append(u.getName() + "\n");
		}
		String listStr = userList.toString().substring(0, userList.length() - 1);
		try {
			this.socket.write(listStr.getBytes(StandardCharsets.UTF_8), ProtocolID.LIST_USERS);
		} catch (Exception e) {
			cleanup();
		}
	}
	
	/**
	 * Sends the user the list of all visible channels
	 * @throws UnsupportedEncodingException Won't be thrown
	 * @throws IOException failed to send the message
	 */
	public void sendChannelList() throws UnsupportedEncodingException, IOException {
		this.socket.write(this.manager.listChannelNames().getBytes(StandardCharsets.UTF_8), ProtocolID.LIST_CHANNELS);
	}
	@Override
	public void run() {
		try{
			if(!readNickname()) {
				return;
			}
			Channel global = this.manager.getGlobalChannel();
			joinUserToChannel(global);
			sendChannelList();
			while(this.alive) {
				Packet packet = this.socket.readPacket();
				processPacket(packet);
			}
		} catch(Exception e){
			cleanup();
		}
	}
	@Override
	public String toString(){
		if(this.nickname == null) {
			return "UNNAMED (" + this.ID + ")";
		}
		return this.nickname;
	}
	@Override
	public boolean equals(Object o) {
		if(o == null || o.getClass() != getClass()) {
			return false;
		}
		User u = (User)o;
		return u.getID() == this.ID;
	}
	@Override
	public int hashCode(){
		return this.ID;
	}
}
