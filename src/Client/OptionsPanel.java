package Client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * The bottom panel of the GUI. Contains the buttons and information about the connection
 * @author etsubu
 *
 */
public class OptionsPanel extends JPanel implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	private JButton connectButton, disconnectButton, state;
	private JTextField ipField, nicknameField;
	private JLabel ipLabel, nickLabel;
	private boolean isConnected;
	private UserInterface parent;
	
	/**
	 * Initializes OptionsPanel
	 * @param parent Parent UI this panel belongs to
	 */
	public OptionsPanel(UserInterface parent){
		this.parent = parent;
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.isConnected = false;
		initComponents();
	}
	
	/**
	 * Initializes all the components
	 */
	private void initComponents() {
		this.connectButton = new JButton("Connect");
		this.connectButton.addActionListener(this);
		this.disconnectButton = new JButton("Disconnect");
		this.disconnectButton.addActionListener(this);
		this.state = new JButton(" ");
		this.state.setEnabled(false);
		this.ipLabel = new JLabel("IP: ");
		this.nickLabel = new JLabel("Nick: ");
		this.ipField = new JTextField();
		this.nicknameField = new JTextField();
		this.add(this.connectButton);
		this.add(this.state);
		this.add(this.disconnectButton);
		this.add(ipLabel);
		this.add(this.ipField);
		this.add(nickLabel);
		this.add(nicknameField);
		updateState();
	}
	
	/**
	 * Updates the visual effects and enables/disables input fields
	 */
	private void updateState() {
		if(this.isConnected) {
			this.state.setBackground(Color.GREEN);
			this.connectButton.setEnabled(false);
			this.disconnectButton.setEnabled(true);
			this.nicknameField.setEnabled(false);
			this.ipField.setEnabled(false);
		} else {
			this.state.setBackground(Color.RED);
			this.connectButton.setEnabled(true);
			this.disconnectButton.setEnabled(false);
			this.nicknameField.setEnabled(false);
			this.nicknameField.setEnabled(true);
			this.ipField.setEnabled(true);
		}
	}
	
	/**
	 * Updates the connection status
	 * @param b is the client connected
	 */
	public void setConnected(boolean b){
		this.isConnected = b;
		updateState();
	}
	
	/**
	 * Reads the user given nickname and asks for one if none is defined
	 * @return User nickname
	 */
	private String readNick() {
		String nick = this.nicknameField.getText();
		if(nick.isEmpty()) {
			nick = JOptionPane.showInputDialog(null, "Input your nickname: ", "Nickname", JOptionPane.QUESTION_MESSAGE);
			if(nick == null) {
				return null;
			}
			this.nicknameField.setText(nick);
			return nick;
		}
		return nick;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(this.connectButton)) {
			String nick = readNick();
			if(nick == null) {
				return;
			}
			parent.connectToServer(this.ipField.getText(), nick);
		} else if(e.getSource().equals(this.disconnectButton)) {
		    parent.disconnectServer();
		}
	}
}
