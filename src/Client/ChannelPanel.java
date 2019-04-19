package Client;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Contains a list of available channel and a button join the selected channel
 * @author etsubu
 *
 */
public class ChannelPanel extends JPanel implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	private DefaultListModel<String> channelListModel;
	private JList<String> channelList;
	private JScrollPane listScroller;
	private JButton joinChannelButton, createChannelButton;
	private ClientActions clientActions;
	
	/**
	 * Initializes the ChannelPanel
	 */
	public ChannelPanel() {
		this.clientActions = null;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		initComponents();
		this.setPreferredSize(new Dimension(140, 100));
	}
	
	/**
	 * Initializes the graphical components
	 */
	private void initComponents() {
		this.channelListModel = new DefaultListModel<String>();
		this.channelList = new JList<String>(this.channelListModel);
		this.listScroller = new JScrollPane(this.channelList);
		this.joinChannelButton = new JButton("Join Channel");
		this.joinChannelButton.addActionListener(this);
		this.createChannelButton = new JButton("Create Channel");
		this.createChannelButton.addActionListener(this);
		this.add(this.listScroller);
		this.add(this.joinChannelButton);
		this.add(this.createChannelButton);
	}
	
	/**
	 * Updates the list of available channels
	 * @param channels List of channels to update
	 */
	public void update(String[] channels){
		this.channelListModel.clear();
		for(String channelName:channels) {
			this.channelListModel.addElement(channelName);
		}
	}
	
	/**
	 * Sets the status to enabled
	 * @param clientActions The ClientActions this panel communicates about channels
	 */
	public void enable(ClientActions clientActions) {
		this.clientActions = clientActions;
		this.joinChannelButton.setEnabled(true);
		this.createChannelButton.setEnabled(true);
	}
	
	/**
	 * Sets the status to disabled
	 */
	public void disable() {
		this.channelListModel.clear();
		this.joinChannelButton.setEnabled(false);
		this.createChannelButton.setEnabled(false);
	}
	
	/**
	 * Checks if a channel by the given name already exists
	 * @param name The channel name to check
	 * @return Did the name exist
	 */
	private boolean doesNameExist(String name) {
		for(int i = 0;i < this.channelListModel.size();i++) {
			if(name.equalsIgnoreCase(this.channelListModel.get(i))) {
				return true;
			}
		}
		return false;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(this.joinChannelButton)) {
			String channelName = this.channelList.getSelectedValue();
			if(channelName != null) {
				this.clientActions.joinToChannel(channelName);
			}
		} else if(e.getSource().equals(this.createChannelButton)) {
			String channelName = JOptionPane.showInputDialog(null, "Input the channel name: ", "Channel name", JOptionPane.QUESTION_MESSAGE);
			if(channelName != null) {
				if(doesNameExist(channelName)) {
					JOptionPane.showMessageDialog(null, "The channel " + channelName + " already exists!", "The channel exists", JOptionPane.ERROR_MESSAGE);
				} else {
					this.clientActions.joinToChannel(channelName);
				}
			}
		}
	}
}
