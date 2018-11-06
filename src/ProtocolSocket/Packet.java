package ProtocolSocket;

import java.util.Arrays;

/**
 * Single packet which contains the raw data and type ID of the packet
 * @author etsubu
 *
 */
public class Packet {
	public static final int MAX_SIZE=8096;
	private Header header;
	private byte[] data;
	
	/**
	 * Initializes the packet 
	 * @param header The header of the packet
	 * @param data the actual content of the packet
	 */
	public Packet(Header header, byte[] data) {
		this.header=header;
		if(data == null) {
			data = null;
		}
		else {
			this.data = Arrays.copyOf(data, data.length);
		}
	}
	
	/**
	 * Getter for the data
	 * @return the data
	 */
	public byte[] getData(){
		return this.data;
	}
	
	/**
	 * Getter for the packet header
	 * @return Packet header
	 */
	public Header getHeader(){
		return this.header;
	}
}
