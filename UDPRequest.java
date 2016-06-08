import java.io.* ;
import java.net.* ;
import java.util.* ;

public final class UDPRequest implements Runnable {
	
    byte[] rdtPacket = new byte[100];
	byte[] isACK = new byte[100];
	byte[] data = new byte[99];
	byte[] packet;
	
	ArrayList<byte[]> array = new ArrayList<byte[]>();
	
	Socket socket;
	
	public UDPRequest(Socket socket) {
		
		this.socket = socket;
		
	}
	
	public void run() {
		try {
			
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			DataInputStream inStream = new DataInputStream(socket.getInputStream());
		    
		    FileOutputStream outWriter = new FileOutputStream("UpperCase.txt");
		    
		    boolean rcvSYN = false;
		    
		    while (rcvSYN == false) {
		    	inStream.read(rdtPacket, 0, 100);
		    	if (extractType(rdtPacket) == 2) {
		    		rcvSYN = true;
			    	isACK = makeRDTPacket(1, 0, data);
			    	outStream.write(isACK, 0, 100);
		    	}
		    }  

		    inStream.read(rdtPacket, 0, 100);
		    array.add(0, rdtPacket);

	    	isACK = makeRDTPacket(1, extractSeqNumber(rdtPacket), data);
	    	outStream.write(isACK, 0, 100);

		    int itn  = 1;

		    while (extractType(rdtPacket) == 0) {
		    	rdtPacket = new byte[100];
		    	inStream.read(rdtPacket, 0, 100);
		    	array.add(itn, rdtPacket);
		    	itn++;
		    	isACK = makeRDTPacket(1, extractSeqNumber(rdtPacket), data);
		    	outStream.write(isACK, 0, 100);
		    }
		    
		    for (int i=0; i<array.size()-1; i++) {
		    	packet = array.get(i);
		    	for (int j=1; j<packet.length ; j++) {
					if (packet[j] != 0) {
						System.out.print(Character.toUpperCase((char)packet[j]));
					} 
				}
		    }
			
			byte[] fullFile = new byte[array.size()*99];
			
			for (int i=0; i<array.size()-1; i++) {
		    	for (int j=1; j<100 ; j++) {
		    		fullFile[(i*99)+j-1] = (byte)(Character.toUpperCase((char)array.get(i)[j]));
		    	}
		    }
			
			outWriter.write(fullFile);
			outWriter.close();
		    outStream.close();
		    inStream.close();
		    socket.close();
			
		} catch (IOException e) {
			
		    e.printStackTrace();
		    
		}
	}

	// the following function creates rdtPacket. It computes the value of the header base on 
	// the value of type and seq Number and version. It attaches the data at the end of the packet.
	public static byte[] makeRDTPacket(int type, int seqNumber, byte[] data){
		
		byte[] rdtPacket = new byte[100];
		
		byte header = (byte)0;
		// create Version
		header = 1 << 5;
		
		// shift left type such that it is moved to its correct position in the header and convert the result to byte, 
		type = (byte) type << 3;
		byte typeField = (byte)type;
		
		//convert seqNumber to byte
		byte seqField = (byte) seqNumber;
		
		// combine version, type and seqNumber and create the header 
		header = (byte)(header | typeField | seqField );
		rdtPacket[0] = header;
		
		// add data at the end of rdt packet.
		for (int i=0; i<data.length; i++)
			rdtPacket[i+1] = data[i];
		
		return rdtPacket;
		}
	
	//The following fuction extracts the type from 
	// rdtPacket.
	public static int extractType(byte[] rdtPacket){
		
		Byte header = rdtPacket[0];

		Byte typeField = (byte)(header.byteValue() &((byte) 24));  // AND header with 00011000 to get the value of type in byte
		int type = (int)(typeField.byteValue() >>3);    

		return type;
	}
	
	//The following fuction extracts the sequence number from
	// rdtPacket.
	public static int extractSeqNumber(byte[] rdtPacket){
		
		Byte header = rdtPacket[0];

		Byte seqNumberField = (byte) (header.byteValue() & (byte) 7);  //AND with 00000111
		int seqNumber = seqNumberField.intValue(); //convert the result to int

		return seqNumber;
	}
	
}

