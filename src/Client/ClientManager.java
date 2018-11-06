package Client;

import java.net.Socket;
import ProtocolSocket.Packet;
import ProtocolSocket.ProtocolID;
import ProtocolSocket.ProtocolSocket;

/**
 * Handles the connection to the server
 * @author etsubu
 *
 */
public class ClientManager implements Runnable{
	private ProtocolSocket protoSocket;
	private boolean isConnected;
	private UserInterface ui;
	private String nickname;
	private ClientActions actionHandler;
	private ChannelManager channelManager;
	
	/**
	 * Initializes the ClientManager
	 * @param ui The active UserInterface
	 * @param channelManager The ChannelManager that contains all the joined channels
	 */
	public ClientManager(UserInterface ui, ChannelManager channelManager) {
		this.channelManager = channelManager;
		this.isConnected = false;
		this.protoSocket = null;
		this.ui = ui;
		this.actionHandler = null;
	}
	
	/**
	 * Sets the user nickname
	 * @param nick Nickname of the user
	 */
	public void setNickname(String nick) {
		this.nickname = nick;
	}
	
	/**
	 * Tries to connect to the given server
	 * @param ip The ip address of the server
	 * @return did the connection succeed
	 */
	public boolean connectToServer(String ip) {
		if (this.isConnected) {
			return false;
		}
		try {
			Socket socket = new Socket(ip, 7777);
			this.protoSocket = new ProtocolSocket(socket);
			this.protoSocket.write(this.nickname.getBytes("UTF-8"), ProtocolID.CLIENT_NICKNAME);
		} catch (Exception e) {
			return false;
		}
		this.actionHandler = new ClientActions(this, this.channelManager, this.protoSocket, this.ui);
		this.isConnected = true;
		return true;
	}
	
	/**
	 * Getter for the ClientActions of this Client
	 * @return ClientActions of this client
	 */
	public ClientActions getClientActions() {
		return this.actionHandler;
	}
	
	/**
	 * Disconnects the client from the server
	 */
	public void disconnect() {
		this.protoSocket.close();
		this.channelManager.cleanup();
		this.isConnected = false;
		this.ui.connectionLost("");
	}
	
	/**
	 * Getter for the connection status
	 * @return is client connected
	 */
	public boolean isConnected() {
		return this.isConnected;
	}

	@Override
	public void run() {
		while (this.isConnected) {
			try {
				Packet packet = this.protoSocket.readPacket();
				this.actionHandler.processPacket(packet);
			} catch (Exception e) {
				disconnect();
			}
		}
	}
}
