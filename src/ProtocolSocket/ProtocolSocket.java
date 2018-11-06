package ProtocolSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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
	private InputStream is;
	private OutputStream os;
	
	/**
	 * Initializes the ProtocolSocket
	 * @param socket
	 * @throws IOException
	 */
	public ProtocolSocket(Socket socket) throws IOException {
		this.socket = socket;
		this.is = socket.getInputStream();
		this.os = socket.getOutputStream();
	}
	
	/**
	 * Returns the amount of available bytes to read.
	 * VOLATILE should not be used as the size of a buffer to allocate!
	 * @return The amount of bytes available
	 * @throws IOException
	 */
	public int available() throws IOException{
		return this.is.available();
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
	 * @throws IOException
	 * @throws IllegalHeaderException
	 */
	public Packet readPacket() throws IOException, IllegalHeaderException{
		byte[] headerSize=new byte[Header.HEADER_SIZE];
		int headerRead=0;
		//Read until header is received
		while(headerRead < headerSize.length){
			int read=is.read(headerSize, headerRead, headerSize.length - headerRead);
			if(read == -1){
				throw new IOException();
			}
			headerRead+=read;
		}
		Header header=new Header(headerSize);
		//Check if the header is valid
		if(header.getSize() > Packet.MAX_SIZE || header.getSize() < 0)
			throw new IllegalHeaderException();
		
		int packetRead=0;
		int packetSize=header.getSize();
		if(packetSize==0){
			return new Packet(header, null);
		}
		byte[] data=new byte[packetSize];
		//Read until whole data has arrived
		while(packetRead < packetSize){
			int read=is.read(data, packetRead, packetSize - packetRead);
			if(read == -1){
				throw new IOException();
			}
			packetRead+=read;
		}
		return new Packet(header, data);
	}
	
	/**
	 * Writes the data to the client as a structured packet
	 * @param data data to write
	 * @param type Type ID for the packet
	 * @throws IOException
	 */
	public void write(byte[] data, ProtocolID type) throws IOException{
		//If data is null only write the header
		if(data==null){
			byte[] header=Header.toBytes(0, type.ordinal());
			this.os.write(header);
			this.os.flush();
			return;
		}
		int totalSent=0;
		int offset=0;
		//Loop until the whole packet is sent
		while(totalSent < data.length){
			int toSendSize = Math.min(Packet.MAX_SIZE, data.length - totalSent);
			byte[] header=Header.toBytes(toSendSize, type.ordinal());
			byte[] fullPacket = new byte[header.length + toSendSize];
			System.arraycopy(header, 0, fullPacket, 0, header.length);
			System.arraycopy(data, offset, fullPacket, header.length, toSendSize);
			this.os.write(fullPacket);
			this.os.flush();
			totalSent += toSendSize;
		}
	}
	
	/**
	 * Closes the socket
	 */
	public void close(){
		try {
			this.socket.close();
		} catch (IOException e) {}
	}
}
