package Client;

import java.io.UnsupportedEncodingException;

import ProtocolSocket.Packet;
import ProtocolSocket.ProtocolID;
import ProtocolSocket.ProtocolSocket;

/**
 * Handles all the client actions and processes incoming/outgoing packets
 * @author etsubu
 *
 */
public class ClientActions {
	private ClientManager manager;
	private ProtocolSocket protoSocket;
	private ChannelManager channelManager;
	
	/**
	 * Intializes ClientActions
	 * @param manager The ClientManager this object belongs to
	 * @param channelManager The ChannelManager that contains all the connected channels
	 * @param protoSocket The ProtocolSocket of the client
	 */
	public ClientActions(ClientManager manager, ChannelManager channelManager, ProtocolSocket protoSocket, UserInterface ui) {
		this.manager = manager;
		this.channelManager = channelManager;
		this.protoSocket = protoSocket;
	}
	
	
	/**
	 * Send message to the server and handles the possible command the message represents (e.g. /join [CHANNEL] /dc)
	 * @param message Message the user sent
	 * @param channelID ChannelID the message was meant for
	 */
	public void sendMessage(String message, int channelID) {
		String formatMessage = channelID + ":" + message;
		try {
			this.protoSocket.write(formatMessage.getBytes("UTF-8"), ProtocolID.CHANNEL_BROADCAST);
		} catch(Exception e) {
			this.manager.disconnect();
		}
	}
	
	/**
	 * Sends a message that the user wants to leave the given channel
	 * @param channelID ID of the channel to leave
	 */
	public void sendLeaveChannel(int channelID) {
		try {
			this.protoSocket.write(Integer.toString(channelID).getBytes("UTF-8"), ProtocolID.LEAVE_CHANNEL);
		} catch(Exception e) {
			this.manager.disconnect();
		}
	}
	
	/**
	 * Requests a list of users on specific channel
	 * @param channelID ID of the channel to list users for
	 */
	public void listUsersOnChannel(int channelID) {
		try {
			this.protoSocket.write(Integer.toString(channelID).getBytes("UTF-8"), ProtocolID.LIST_USERS);
		} catch(Exception e) {
			this.manager.disconnect();
		}
	}
	
	/**
	 * Processes the received packet
	 * @param packet Packet to process
	 */
	public void processPacket(Packet packet) {
		try {
			int type = packet.getHeader().getType();
			String data = new String(packet.getData(), "UTF-8");
			if (type == ProtocolID.CHANNEL_BROADCAST.ordinal()) {
				processBroadcastMessage(data);
			} else if (type == ProtocolID.LIST_USERS.ordinal()) {
				processListUsers(data);
			} else if (type == ProtocolID.JOIN_CHANNEL.ordinal()) {
				processJoinChannel(data);
			} else if (type == ProtocolID.CLIENT_JOINED.ordinal()) {
				processChannelInformation(data);
			} else if (type == ProtocolID.CLIENT_LEFT.ordinal()) {
				processChannelInformation(data);
			} else if (type == ProtocolID.LIST_CHANNELS.ordinal()) {
				updateChannelList(data);
			} else if (type == ProtocolID.LEAVE_CHANNEL.ordinal()) {
				leaveChannel(data);
			}
		} catch (UnsupportedEncodingException e) {}
	}
	
	private void leaveChannel(String channelIDStr) {
		int ID = Integer.parseInt(channelIDStr);
		this.channelManager.removeChannel(this.channelManager.getChannel(ID));
	}
	
	/**
	 * Updates the name list of all available channels
	 * @param channelsStr The string containing the channel names
	 */
	private void updateChannelList(String channelsStr) {
		String[] channels = channelsStr.split("\n");
		this.channelManager.setAllChannels(channels);
	}
	
	/**
	 * Processes a information regarding a channel
	 * @param info The information message
	 */
	private void processChannelInformation(String info) {
		int index = info.indexOf(":");
		if (index == -1 || index == info.length() - 1) {
			this.manager.disconnect();
			return;
		} 
		this.channelManager.broadcastMessageReceived(Integer.parseInt(info.substring(0, index)), info.substring(index + 1));
	}
	
	/**
	 * Updates the channel to the list of joined channels
	 * @param channelStr CHANNEL_ID:CHANNEL_NAME
	 */
	private void processJoinChannel(String channelStr) {
		try {
			int index = channelStr.indexOf(":");
			if (index == -1 || index == channelStr.length() - 1) {
				this.manager.disconnect();
				return;
			} 
			int channelID = Integer.parseInt(channelStr.substring(0, index));
			String channelName = channelStr.substring(index + 1);
			this.channelManager.addChannel(new Channel(channelID, channelName, this));
		} catch(NumberFormatException e) {
			this.manager.disconnect();
		}
	}
	
	/**
	 * Tries to join to a channel by the given name. If the channel does not exist it will be created
	 * @param channelName Channel name to join
	 */
	public void joinToChannel(String channelName) {
		try {
			this.protoSocket.write(channelName.getBytes("UTF-8"), ProtocolID.JOIN_CHANNEL);
		} catch (Exception e) {
			this.manager.disconnect();
		}
	}
	
	/**
	 * Forwards the received user list to the channel
	 * @param message
	 */
	private void processListUsers(String message) {
		int index = message.indexOf(":");
		if(index == -1 || index == message.length() - 1) {
			this.manager.disconnect();
			return;
		}
		try{
			int channelID = Integer.parseInt(message.substring(0, index));
			message = message.substring(index + 1);
			String[] userList = message.split("\n");
			this.channelManager.updateChannelUsers(channelID, userList);
		} catch(NumberFormatException e) {
			this.manager.disconnect();
		}
	}
	/**
	 * Processes a received broadcastmessage
	 * @param data The data of the received packet
	 */
	public void processBroadcastMessage(String data) {
		int index = data.indexOf(":");
		if (index == -1 || index == data.length() - 1) {
			this.manager.disconnect();
			return;
		}
		try {
			int channelID = Integer.parseInt(data.substring(0, index));
			data = data.substring(index + 1);
			index = data.indexOf(":");
			if(index == -1 || index == data.length() - 1) {
				this.manager.disconnect();
				return;
			}
			String sender = data.substring(0, index);
			String message = data.substring(index + 1);
			this.channelManager.broadcastMessageReceived(channelID, sender + ": " + message);
		} catch(NumberFormatException e){
			this.manager.disconnect();
			return;
		}
	}
}
