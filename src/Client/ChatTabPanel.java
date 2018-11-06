package Client;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Contains all the ChatPanels and allows the user to switch between them using tabs.
 * @author etsubu
 *
 */
public class ChatTabPanel extends JTabbedPane implements ChangeListener{
	
	private static final long serialVersionUID = 1L;
	private List<ChatPanel> chatPanels;
	private int[] keyBinds;
	private UserListPanel userListPanel;
	
	/**
	 * Initializes the ChatTabPanel
	 * @param userListPanel The panel where to update list of users on the channel
	 */
	public ChatTabPanel(UserListPanel userListPanel) {
		this.userListPanel = userListPanel;
		this.chatPanels = new ArrayList<>();
		this.addChangeListener(this);
		initKeyBinds();
	}
	
	/**
	 * Initializes the key bindings for channel switching
	 */
	private void initKeyBinds() {
		keyBinds = new int[9];
		keyBinds[0] = KeyEvent.VK_1;
		keyBinds[1] = KeyEvent.VK_2;
		keyBinds[2] = KeyEvent.VK_3;
		keyBinds[3] = KeyEvent.VK_4;
		keyBinds[4] = KeyEvent.VK_5;
		keyBinds[5] = KeyEvent.VK_6;
		keyBinds[6] = KeyEvent.VK_7;
		keyBinds[7] = KeyEvent.VK_8;
		keyBinds[8] = KeyEvent.VK_9;
	}
	
	/**
	 * Updates the key bindings
	 */
	private void updateKeyBinds() {
		for(int i = 0;i < Math.min(chatPanels.size(), 9);i++) {
			this.setMnemonicAt(i, this.keyBinds[i]);
		}
	}
	
	/**
	 * Adds new chat panel
	 * @param name Name of the panel
	 */
	public void addChatPanel(Channel channel) {
		ChatPanel chatPanel = channel.getChatPanel();
		this.addTab(channel.getChannelName(), null, chatPanel);
		this.chatPanels.add(chatPanel);
		this.setSelectedIndex(this.getTabCount() - 1);
		updateKeyBinds();
		update();
	}
	
	/**
	 * Called when connection is lost. Cleans all the panels
	 */
	public void cleanup() {
		this.removeAll();
		this.chatPanels.clear();
		this.userListPanel.cleanup();
	}
	
	/**
	 * Removes a chat panel from the screen
	 * @param channel
	 */
	public void removeChatPanel(Channel channel) {
		for(int i = 0;i < this.chatPanels.size();i++) {
			if(this.chatPanels.get(i).getChannel().equals(channel)) {
				this.chatPanels.remove(i);
				this.remove(i);
				updateKeyBinds();
			}
		}
		update();
	}
	
	/**
	 * Updates the panel and shows the right user list for the current channel
	 */
	public void update() {
		int index = this.getSelectedIndex();
		if(index == -1 || this.chatPanels.size() == 0) {
			return;
		}
		Channel channel = this.chatPanels.get(index).getChannel();
		this.userListPanel.updateUserList(channel.getUserList());
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		update();
	}
}
