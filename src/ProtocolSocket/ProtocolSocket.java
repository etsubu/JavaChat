package ProtocolSocket;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import Client.CertificateStorage;

/**
 * Reads and writes the incoming packets. Uses simple packet header to keep track of the packet sizes and types
 * Structure of the packet
 * [PACKET_SIZE][PACKET_TYPE][THE_PACKET_DATA]
 * 		2 bytes		1 byte		PACKET_SIZE bytes
 * @author etsubu
 *
 */
public class ProtocolSocket {
    private Socket socket;
    private SSLContext context;
    private CertificateStorage tm;
    
    /**
     * Static SSLSocketFactory
     */
    public static final SSLSocketFactory sslSocketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
    
    /**
     * ProtocolSockets utilize hardcoded list of allowed Cipher suites for TLSv1.2
     * Reason for hardcoding the list is to ensure that only the most secure cipher suites are utilized.
     * This can't be guaranted if default cipher suites are used.
     * The selected cipher suites are from best practices:
     * https://github.com/ssllabs/research/wiki/SSL-and-TLS-Deployment-Best-Practices.
     * But those that use RSA and 128 bit AES have been removed from the list. This of course is an overkill but won't hurt anyone
     * 
     * Feel free to change these or remove them and stick to defaults if desired.
     */
    public static final String[] CIPHER_SUITES = {
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384"};
    
    /**
     * ProtocolSockets when using encryption only use TLS v1.2 this is to ensure we use the modern algorithms defined in CIPHER_SUITE
     * and to use the latest available protocol.
     */
    public static final String[] ENABLED_PROTOCOLS = {"TLSv1.2"};
    
    /**
     * Initializes the ProtocolSocket with SSL and connects to the given host
     * @param address Address to connect to
     * @param port Port to connect to
     * @param password Keystore password
     * @throws IOException If there was an error connecting to the host
     * @throws NoSuchAlgorithmException If the used TLS version was unknown
     * @throws KeyManagementException If there was an error with initializing SSLContext
     * @throws KeyStoreException If there was an error loading keystore
     * @throws SSLPeerUnverifiedException If the server certificate did not match the hostname/ip
     */
    public ProtocolSocket(String address, int port, char[] password) throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, SSLPeerUnverifiedException {
        this.tm = new CertificateStorage(password);
        configureContext();
        SSLSocket ssl = (SSLSocket) context.getSocketFactory().createSocket(address, port);
        configureSSLSocket(ssl);
        ssl.startHandshake();
        this.tm.verifyHostname(ssl.getSession());
        this.socket = ssl;
    }
    
    /**
     * Initializes the ProtocolSocket and connects to the given host
     * @param address Address to connect to
     * @param port Port to connect to
     * @param SSL True if SSL is to be enabled, false if no encryption will be used
     * @param password Keystore password
     * @throws UnknownHostException If the hostname could not be resolved
     * @throws IOException If the socket could not be connected
     * @throws NoSuchAlgorithmException If the used TLS version was unknown
     * @throws KeyManagementException If there was an error with initializing SSLContext
     * @throws KeyStoreException If there was an error loading keystore
     * @throws SSLPeerUnverifiedException If the server certificate did not match the hostname/ip
     */
    public ProtocolSocket(String address, int port, boolean SSL, char[] password) throws UnknownHostException, IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, SSLPeerUnverifiedException {
        if(SSL) {
            this.tm = new CertificateStorage(password);
            configureContext();
            SSLSocket ssl = (SSLSocket) context.getSocketFactory().createSocket(address, port);
            configureSSLSocket(ssl);
            ssl.startHandshake();
            this.tm.verifyHostname(ssl.getSession());
            this.socket = ssl;
        } else {
            this.socket = new Socket(address, port);
        }
    }
    
    /**
     * Initializes ProtocolSocket with Socket to wrap in
     * @param socket Socket to wrap in
     */
    public ProtocolSocket(Socket socket) {
        this.socket = socket;
    }
    
    /**
     * Configures the SSLSocket to utilize the predefined cipher suites and protocols
     * @param ssl SSLSocket to configure
     */
    public static void configureSSLSocket(SSLSocket ssl) {
        ssl.setEnabledCipherSuites(CIPHER_SUITES);
        ssl.setEnabledProtocols(ENABLED_PROTOCOLS);
        ssl.setNeedClientAuth(false);
    }
    
    /**
     * Configures SSLContext to use TLSv1.2 and Loading custom storage of trusted certificates
     * @throws NoSuchAlgorithmException If TLSv1.12 was unknown
     * @throws KeyManagementException If there was an error with keymanagement
     * @throws KeyStoreException If there was an error loading Keystore
     */
    public void configureContext() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        context = SSLContext.getInstance("TLSv1.2");
        context.init(null, new TrustManager[] {tm}, new SecureRandom());
    }
	
	/**
	 * Getter for the sockets IP
	 * @return IP of the client
	 */
	public String getIP(){
		return this.socket.getInetAddress().getHostAddress();
	}
	
	/**
	 * Reads a whole packet from the socket
	 * @return Packet object
	 * @throws IOException If socket throws error
	 * @throws IllegalHeaderException Received header was not valid
	 */
	public Packet readPacket() throws IOException, IllegalHeaderException{
		byte[] headerSize = new byte[Header.HEADER_SIZE];
		int headerRead = 0;
		//Read until header is received
		while(headerRead < headerSize.length){
			int read = this.socket.getInputStream().read(headerSize, headerRead, headerSize.length - headerRead);
			if(read == -1){
				throw new IOException();
			}
			headerRead += read;
		}
		Header header = new Header(headerSize);
		//Check if the header is valid
		if(header.getSize() > Packet.MAX_SIZE || header.getSize() < 0)
			throw new IllegalHeaderException();
		
		int packetRead = 0;
		int packetSize = header.getSize();
		if(packetSize == 0){
			return new Packet(header, null);
		}
		byte[] data = new byte[packetSize];
		//Read until whole data has arrived
		while(packetRead < packetSize){
			int read = this.socket.getInputStream().read(data, packetRead, packetSize - packetRead);
			if(read == -1){
				throw new IOException();
			}
			packetRead += read;
		}
		return new Packet(header, data);
	}
	
	/**
	 * Writes UTF-8 encoded string with the given protocol id to the stream
	 * @param message Message to send
	 * @param type Type of the packet
	 * @throws IOException If error occurs in writing the message
	 */
	public void write(String message, ProtocolID type) throws IOException {
	    write(message.getBytes(StandardCharsets.UTF_8), type);
	}
	
	/**
	 * Writes the data to the client as a structured packet
	 * @param data data to write
	 * @param type Type ID for the packet
	 * @throws IOException If there is an socket error
	 */
	public void write(byte[] data, ProtocolID type) throws IOException{
		//If data is null only write the header
		if(data == null){
			byte[] header = Header.toBytes(0, type.ordinal());
			this.socket.getOutputStream().write(header);
			this.socket.getOutputStream().flush();
			return;
		}
		int totalSent = 0;
		int offset = 0;
		//Loop until the whole packet is sent
		while(totalSent < data.length){
			int toSendSize = Math.min(Packet.MAX_SIZE, data.length - totalSent);
			byte[] header = Header.toBytes(toSendSize, type.ordinal());
			byte[] fullPacket = new byte[header.length + toSendSize];
			System.arraycopy(header, 0, fullPacket, 0, header.length);
			System.arraycopy(data, offset, fullPacket, header.length, toSendSize);
			this.socket.getOutputStream().write(fullPacket);
			this.socket.getOutputStream().flush();
			totalSent += toSendSize;
		}
	}
	
	/**
	 * Closes the socket
	 */
	public void close() {
	    try {
            this.socket.close();
        } catch (IOException e) {
            //
        }
	}
}
