package ProtocolSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 * ProtocolServerSocket wraps a ServerSocket and accepts new connection. If SSL enabled all communications and ProtocolSockets
 * will be wrapped in SSL
 * @author etsubu
 * @version 19 Apr 2019
 *
 */
public class ProtocolServerSocket {
    private ServerSocket server;
    
    /**
     * Static SSLServerSocketFactory
     */
    public static final SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
    
    /**
     * Initializes ProtocolServerSocket on the given port with SSL enabled
     * @param port Port to bind on
     * @throws IOException If the port could not be opened
     */
    public ProtocolServerSocket(int port) throws IOException {
        this.server = sslServerSocketFactory.createServerSocket(port);
    }
    
    /**
     * Initializes ProtocolServerSocket on the given port
     * @param port Port to bind on
     * @param SSL True if SSL is to be enabled
     * @throws IOException If the port could not be opened
     */
    public ProtocolServerSocket(int port, boolean SSL) throws IOException {
        if(SSL) {
            server = sslServerSocketFactory.createServerSocket(port);
        } 
        else {
            server = new ServerSocket(port);
        }
    }
    
    /**
     * Accepts incoming connection and configures SSL settings if SSL is enabled
     * @return New connection wrapped in ProtocolSocket and SSL if enabled
     * @throws IOException If there was an error accepting connection
     */
    @SuppressWarnings("resource")
    public ProtocolSocket accept() throws IOException {
        Socket socket = this.server.accept();
        if(socket instanceof SSLSocket) {
            SSLSocket ssl = (SSLSocket)socket;
            ProtocolSocket.configureSSLSocket(ssl);
            ssl.startHandshake();
        }
        return new ProtocolSocket(socket);

    }
    
    /**
     * Closes the ServerSocket
     */
    public void close() {
        try {
            this.server.close();
        } catch (IOException e) {
            //
        }
    }
}
