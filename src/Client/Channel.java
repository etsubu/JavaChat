package Client;

/**
 * Contains all the information about a single channel
 * @author etsubu
 *
 */
public class Channel {
	private int channelID;
	private String channelName;
	private String[] userList;
	private ClientActions clientHandler;
	private ChatPanel panel;
	
	/**
	 * Initializes the Channel information
	 * @param channelID ID of the channel
	 * @param channelName Name of the channel
	 * @param clientHandler ClientActions of the connected client
	 */
	public Channel(int channelID, String channelName, ClientActions clientHandler) {
		this.channelID = channelID;
		this.channelName = channelName;
		this.userList = null;
		this.clientHandler = clientHandler;
		this.panel = new ChatPanel(this);
	}
	
	/**
	 * Getter for the Channels panel
	 * @return The panel of the Channel
	 */
	public ChatPanel getChatPanel() {
		return this.panel;
	}
	/**
	 * Getter for the channel ID
	 * @return ID of the channel
	 */
	public int getChannelID() {
		return this.channelID;
	}
	
	/**
	 * Getter for the channel name
	 * @return Name of the channel
	 */
	public String getChannelName() {
		return this.channelName;
	}
	
	/**
	 * Getter for the list of users on the channel
	 * @return Users on the channel
	 */
	public synchronized String[] getUserList() {
		return this.userList;
	}
	
	/**
	 * Sets the list of users on the channel
	 * @param userList Users on the channel
	 */
	public synchronized void setUserList(String[] userList) {
		this.userList = userList;
	}
	
	/**
	 * Send a message to this channel
	 * @param message Message to send
	 */
	public void sendMessage(String message) {
		this.clientHandler.sendMessage(message, this.channelID);
	}
	
	/**
	 * Leaves this channel
	 */
	public void leaveChannel(){
		this.clientHandler.sendLeaveChannel(this.channelID);
	}
	/**
	 * Adds the received message to the channel panel
	 * @param message
	 */
	public void messageReceived(String message) {
		if(this.panel != null) {
			this.panel.addMessage(message);
		}
	}
	
	@Override
	public String toString() {
		return this.channelName;
	}
	
	@Override
	public int hashCode() {
		return this.channelID;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || o.getClass() != getClass()) {
			return false;
		}
		Channel c = (Channel)o;
		return c.getChannelID() == this.channelID && c.getChannelName().equals(this.channelName);
	}
 }
