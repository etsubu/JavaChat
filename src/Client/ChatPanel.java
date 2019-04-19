package Client;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Calendar;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 * This panel contains the chat areas
 * @author etsubu
 *
 */
public class ChatPanel extends JPanel implements KeyListener{
	
	private static final long serialVersionUID = 1L;
	private JTextPane messageArea;
	private JScrollPane messageScroller;
	private JTextField typeArea;
	private Calendar time;
	private Channel channel;
	
	/**
	 * Initialize the chat panel
	 * @param channel Channel this panel contains
	 */
	public ChatPanel(Channel channel) {
		this.channel = channel;
		this.setLayout(new BorderLayout());
		this.time = Calendar.getInstance();
		initComponents();
	}
	
	/**
	 * Initializes the text areas
	 */
	private void initComponents() {
		this.messageArea = new JTextPane();
		this.messageArea.setEditable(false);
		this.messageScroller = new JScrollPane(this.messageArea);
		this.typeArea = new JTextField();
		this.typeArea.addKeyListener(this);
		this.add(messageScroller, BorderLayout.CENTER);
		this.add(typeArea, BorderLayout.SOUTH);
	}
	/**
	 * Processes the message the user typed
	 * @param text
	 */
	private void processMessage(String text) {
		if(text.equals("/cls")) {
			this.messageArea.setText("");
		} else if(text.equals("/dc")) {
			this.channel.leaveChannel();
		} else {
			this.channel.sendMessage(text);
		}
	}
	
	/**
	 * Getter for the channel this panel contains
	 * @return The channel
	 */
	public Channel getChannel() {
		return this.channel;
	}
	
	/**
	 * Adds message to the panel with the current time
	 * @param message Message to add
	 */
	public void addMessage(String message) {
		int hours = this.time.get(Calendar.HOUR_OF_DAY);
		int minutes = this.time.get(Calendar.MINUTE);
		int seconds = this.time.get(Calendar.SECOND);
		String formatMessage = String.format("%02d:%02d:%02d| %s", hours, minutes, seconds, message);
		messageArea.setText(messageArea.getText() + formatMessage + "\n");
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			e.consume();
			String text = this.typeArea.getText();
			this.typeArea.setText("");
			if(!text.isEmpty()) {
				processMessage(text);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	    //
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	    //
	}
}
