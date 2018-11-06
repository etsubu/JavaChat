package ProtocolSocket;

/**
 * The packet header contains the size of the packet and the type of its content
 * @author etsubu
 *
 */
public class Header {
	public static final int HEADER_SIZE=Short.BYTES + Short.BYTES;
	private int packetSize, packetType;
	
	/**
	 * Initializes the header by the raw bytes
	 * @param bytes
	 */
	public Header(byte[] bytes){
		this.packetSize=((((int)bytes[1]) & 0xFF) << 8) | (bytes[0] & 0xFF);
		this.packetType=bytes[2];
	}
	
	/**
	 * Getter for the incoming packet size
	 * @return Packet size
	 */
	public int getSize(){
		return this.packetSize;
	}
	
	/**
	 * Getter for the type ID of the packet
	 * @return
	 */
	public int getType(){
		return this.packetType;
	}
	
	/**
	 * Transforms the header into a byte array
	 * @param packetSize
	 * @param packetType
	 * @return
	 */
	public static byte[] toBytes(int packetSize, int packetType){
		byte[] bytes=new byte[HEADER_SIZE];
		bytes[0]=(byte) (packetSize & 0xFF);
		bytes[1]=(byte) (packetSize >> 8);
		bytes[2]=(byte) packetType;
		return bytes;
	}
}
