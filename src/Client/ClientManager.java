package Client;

import java.awt.Toolkit;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.swing.JOptionPane;

import ProtocolSocket.Packet;
import ProtocolSocket.ProtocolID;
import ProtocolSocket.ProtocolSocket;

/**
 * Handles the connection to the server
 * @author etsubu
 *
 */
public class ClientManager implements Runnable {
    private char[] password;
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
	    this.password = null;
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
		    while(password == null) {
		        String str = JOptionPane.showInputDialog(null, "Input keystore password: ", "Keystore password", JOptionPane.QUESTION_MESSAGE);
		        if(str != null)
		            password = str.toCharArray();
		    }
			this.protoSocket = new ProtocolSocket(ip, 7777, true, password);
			this.protoSocket.write(this.nickname.getBytes(StandardCharsets.UTF_8), ProtocolID.CLIENT_NICKNAME);
		} catch(SSLHandshakeException e) {
		    JOptionPane.showMessageDialog(null, "Server's certificate is not trusted!", "SSLHandshakeException", JOptionPane.ERROR_MESSAGE);
		    return false;
		}
		catch(SSLPeerUnverifiedException e) {
		    Toolkit.getDefaultToolkit().beep();
		    JOptionPane.showMessageDialog(null, "Server certificate does not match the hostname!", "Invalid certificate", JOptionPane.ERROR_MESSAGE);
		    return false;
		}
		catch (Exception e) {
		    e.printStackTrace();
			return false;
		}
		this.actionHandler = new ClientActions(this, this.channelManager, this.protoSocket);
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
	    if(protoSocket != null)
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
