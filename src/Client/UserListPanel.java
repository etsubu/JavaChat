package Client;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Contains the list of users on the current channel and different options to interact with them
 * @author etsubu
 *
 */
public class UserListPanel extends JPanel{
	
	private static final long serialVersionUID = 1L;
	private DefaultListModel<String> userListModel;
	private JList<String> userList;
	private JScrollPane listScroller;
	
	/**
	 * Initializes the UserListPanel
	 */
	public UserListPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		initComponents();
		this.setPreferredSize(new Dimension(100, 100));
	}
	
	/**
	 * Initializes the components
	 */
	private void initComponents() {
		this.userListModel = new DefaultListModel<String>();
		this.userList = new JList<String>(this.userListModel);
		this.listScroller = new JScrollPane(this.userList);
		
		this.add(this.listScroller);
	}
	
	/**
	 * Called when connection is lost. Cleans the user list
	 */
	public void cleanup() {
		this.userListModel.clear();
	}
	
	/**
	 * Disables the components in the panel
	 */
	public void disable() {
		this.userListModel.clear();
	}
	
	/**
	 * Enabled the components in the panel
	 */
	public void enable(){
		
	}
	
	/**
	 * Updates the list of users on the current channel to the panel
	 * @param userList List of user names to add
	 */
	public void updateUserList(String[] userList) {
		if(userList == null) {
			return;
		}
		this.userListModel.clear();
		for(String s:userList) {
			this.userListModel.addElement(s);
		}
	}

}
