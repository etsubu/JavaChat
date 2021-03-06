package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLHandshakeException;

import ProtocolSocket.ProtocolServerSocket;
import ProtocolSocket.ProtocolSocket;

/**
 * Hosts the chat server and accepts incoming connections
 * @author etsubu
 *
 */
public class Server{
	private int port;
	private ServerSocket serverSocket;
	private boolean open;
	private ServerManager manager;
	
	/**
	 * Initializes the server
	 * @param port Port to bind on
	 */
	public Server(int port){
		this.port = port;
		this.manager = new ServerManager();
	}
	
	/**
	 * Starts the server and bind on the given port during initialization
	 * @throws IOException When ServerSocket fails to be opened
	 */
	public void startServer() throws IOException {
		if(open) {
			return;
		}
		this.open = true;
		//this.serverSocket = new ServerSocket(this.port);
		ProtocolServerSocket serverSocket = new ProtocolServerSocket(this.port, true);
		while(this.open) {
		    try {
		        ProtocolSocket protoSocket = serverSocket.accept();
		        this.manager.addUser(protoSocket);
		    } catch(SSLHandshakeException e) {
		        // SSLhanshake failed. Probably the client does not trust our certificate
		    }
		}
		serverSocket.close();
	}
	
	/**
	 * Closes the server
	 */
	public void closeServer() {
		this.open = false;
		try {
			new Socket("127.0.0.1", this.port).close();
		} catch (Exception e) {
		    //
		}
		this.manager.closeConnections();
	}
}
