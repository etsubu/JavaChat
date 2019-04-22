package Client;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

/**
 * The GUI of the client
 * @author etsubu
 *
 */
public class UserInterface{
	private JFrame frame;
	private OptionsPanel optionsPanel;
	private ChatTabPanel chatTabPanel;
	private ChannelPanel channnelPanel;
	private UserListPanel userListPanel;
	private ClientManager manager;
	private ChannelManager channelManager;
	
	/**
	 * Initializes the GUI
	 */
	public UserInterface() {
		this.frame = new JFrame();
		this.frame.setTitle("JavaChat 1.0");
		this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.frame.setLayout(new BorderLayout());
		initComponents();
		this.channelManager = new ChannelManager(this.chatTabPanel, this.channnelPanel);
		this.manager = new ClientManager(this, this.channelManager);
		this.frame.setPreferredSize(new Dimension(500,500));
		this.frame.pack();
		this.frame.setLocationRelativeTo(null);
		this.frame.setVisible(true);
		
	}
	
	/**
	 * Initializes all the panels
	 */
	private void initComponents() {
		this.optionsPanel = new OptionsPanel(this);
		this.userListPanel = new UserListPanel();
		this.chatTabPanel = new ChatTabPanel(this.userListPanel);
		this.channnelPanel = new ChannelPanel();
		this.channnelPanel.deactive();
		this.userListPanel.deactive();
		this.frame.add(this.optionsPanel, BorderLayout.SOUTH);
		this.frame.add(this.chatTabPanel, BorderLayout.CENTER);
		this.frame.add(this.channnelPanel, BorderLayout.WEST);
		this.frame.add(this.userListPanel, BorderLayout.EAST);
	}
	
	/**
	 * Connects the client to the server
	 * @param ip IP address of the server
	 * @param nickname Nickname to connect with
	 */
	public void connectToServer(String ip, String nickname) {
		if(this.manager.isConnected()) {
			return;
		}
		this.manager.setNickname(nickname);
		if(this.manager.connectToServer(ip)) {
			this.optionsPanel.setConnected(true);
			this.channnelPanel.enable(this.manager.getClientActions());
			new Thread(this.manager).start();
		} else {
			this.optionsPanel.setConnected(false);
		}
	}
	
	/**
	 * Disconnects the client from the server and updates GUI
	 */
	public void disconnectServer() {
		this.manager.disconnect();
		this.channnelPanel.deactive();
		this.userListPanel.deactive();
		this.optionsPanel.setConnected(false);
	}
	
	/**
	 * Called when the connection was lost. Updates the GUI
	 * @param message Optional message that was received when disconnected
	 */
	public void connectionLost(String message) {
		this.optionsPanel.setConnected(false);
		JOptionPane.showMessageDialog(null, message, "Connection lost", JOptionPane.INFORMATION_MESSAGE);
	}
}
