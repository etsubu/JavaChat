package Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all the channels the client is connected to
 * @author etsubu
 *
 */
public class ChannelManager {
	private Map<Integer, Channel> joinedChannels;
	private String[] allChannels;
	private ChatTabPanel chatTabPanel;
	private ChannelPanel channelPanel;
	
	/**
	 * Initializes the ChannelManager
	 * @param chatTabPanel ChatTabPanel that contains all the chat panels
	 * @param channelPanel ChannelPanel that shows the list of visible panels
	 */
	public ChannelManager(ChatTabPanel chatTabPanel, ChannelPanel channelPanel) {
		this.joinedChannels = new HashMap<>();
		this.allChannels = null;
		this.chatTabPanel = chatTabPanel;
		this.channelPanel = channelPanel;
	}
	
	/**
	 * Sets the list of all visible channels
	 * @param allChannels The list of channels
	 */
	public synchronized void setAllChannels(String[] allChannels) {
		this.allChannels = allChannels;
		this.channelPanel.update(allChannels);
	}
	
	/**
	 * Returns the names of all visible channels
	 * @return All visible channels
	 */
	public synchronized String[] getAllChannels() {
		return this.allChannels;
	}
	/**
	 * Adds a new channel
	 * @param c Channel that is to be added
	 */
	public synchronized void addChannel(Channel c) {
		if(!this.joinedChannels.containsKey(c.getChannelID())) {
			this.joinedChannels.put(c.getChannelID(), c);
			this.chatTabPanel.addChatPanel(c);
		}
	}
	
	/**
	 * Removes a channel from the connected channels
	 * @param c Channel to be removed
	 */
	public synchronized void removeChannel(Channel c) {
		if(c == null) {
			return;
		}
		this.joinedChannels.remove(c.getChannelID());
		this.chatTabPanel.removeChatPanel(c);
	}
	
	/**
	 * Gets a channel by its ID
	 * @param ID ID of the channel
	 * @return Requested channel
	 */
	public synchronized Channel getChannel(int ID) {
		return this.joinedChannels.get(ID);
	}
	
	/**
	 * Called when connection is lost. Cleans all the channels
	 */
	public synchronized void cleanup() {
		this.joinedChannels.clear();
		this.channelPanel.disable();
		this.chatTabPanel.cleanup();
	}
	/**
	 * Updates the user list on the given channel
	 * @param ID ID of the channel to update
	 * @param userList The list of users
	 */
	public synchronized void updateChannelUsers(int ID, String[] userList) {
		if(this.joinedChannels.containsKey(ID)) {
			this.joinedChannels.get(ID).setUserList(userList);
			this.chatTabPanel.update();
		}
	}
	
	/**
	 * Gives the received message for the given channel
	 * @param ID ID of the channel
	 * @param message Message that was received
	 */
	public void broadcastMessageReceived(int ID, String message) {
		if(this.joinedChannels.containsKey(ID)) {
			this.joinedChannels.get(ID).messageReceived(message);
		}
	}
	
	/**
	 * Gets a list of all the channel
	 * @return List of channels the user is connected to
	 */
	public synchronized List<Channel> getListOfChannels() {
		List<Channel> channelList = new ArrayList<>();
		for(Integer i:this.joinedChannels.keySet()) {
			Channel c = this.joinedChannels.get(i);
			channelList.add(c);
		}
		return channelList;
	}
}
