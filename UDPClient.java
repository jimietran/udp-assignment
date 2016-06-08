import java.io.* ;
import java.net.* ;

public class UDPClient {
	
	public static void main(String[] args) throws Exception {
		
		String serverhostname = args[0]; //localhost
		
		int pktNum = 0;
		int numPkts;
		
		boolean rcvACK;
		
		byte[] rdtPacket = new byte[100];
		byte[] data = new byte[99];
		byte[] isACK;
		byte[] fileBytes;
		
		Socket socket = new Socket(serverhostname, 9876);
		DataInputStream inStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
       
        File file = new File("LowerCase.txt");
        
		try {
			
	        rcvACK = false;
	        rdtPacket = makeRDTPacket(2, 0, data);
	        while (rcvACK == false) {
	        	outStream.write(rdtPacket);
	        	isACK = new byte[100];
	        	inStream.read(isACK, 0, 100);
	        	if (extractType(isACK) == 1 && extractSeqNumber(isACK) == 0) {
	        		rcvACK = true;
	        	} 
	        }
	        
	        InputStream inWriter = new FileInputStream(file);
		    fileBytes = new byte[(int)file.length()];
		    
		    int numRead = 0;
		    int offset = 0;

		    while ((numRead = inWriter.read(fileBytes, offset, fileBytes.length-offset)) >= 0 && offset < fileBytes.length) {
		        offset += numRead;
		    }
		    
		    inWriter.close();
		    
		    if (offset < fileBytes.length) {
		        throw new IOException("Cannot read file.");
		    }

	        numPkts = (int) Math.floor(fileBytes.length/99);
	        for (int i=0; i<numPkts; i++) {
	        	data = new byte[99];
	        	for (int j=((i*99)); j<((99*i)+99); j++) {
	             	data[j-(i*99)] = fileBytes[j];
	        	}

	        	rdtPacket = makeRDTPacket(0, pktNum%8, data);
	        	pktNum++;
	        	outStream.write(rdtPacket, 0, 100);
	        	isACK = new byte[100];
	        	inStream.read(isACK, 0, 100);
	        	
	        	if (extractType(isACK) != 1) {
	        		throw new IOException("Missing ACK from server.");
	        	} 
	
	        }
	        
	        if (numPkts*99 < fileBytes.length) {
	        	data = new byte[99];
	        	for (int k=(numPkts*99) ; k<fileBytes.length ; k++) {
	             	data[k-(numPkts*99)] = fileBytes[k];
	        	}
		       	 rdtPacket = makeRDTPacket(0, pktNum%8, data);
		       	 outStream.write(rdtPacket, 0, 100);
		       	 isACK = new byte[100];
		       	 inStream.read(isACK, 0, 100);

		       	 if (extractType(isACK) != 1) {
		       		throw new IOException("Missing ACK from server.");
		       	 } 
	        }
	        
		} catch (IOException e) {
			
			throw new IOException("Cannot connect to server.");
			
        }

        rdtPacket = makeRDTPacket(3, 2, data);
        outStream.write(rdtPacket, 0, 100);
        
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
