package lab1_416;

/**
 * UDPClient
 * 
 * Adapted from the example given in Section 2.8 of Kurose and Ross, Computer
 * Networking: A Top-Down Approach (5th edition)
 * 
 * @author michaelrabbat
 * 
 */
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.lang.Math;
import java.lang.reflect.Array;

public class DnsClient {


	public static void main(String args[]) throws Exception
	{
		
		
		int timeout = 5;
		int maxRetry = 3;
		int port = 53;
		String Qtype = "A"; //1 = A , 2= NS, 15= MX
		String ip = "192.168.1.1";
		String Qname = "www.mcgill.ca";
		
				
		// Create a UDP socket
		// (Note, when no port number is specified, the OS will assign an arbitrary one)
		DatagramSocket clientSocket = new DatagramSocket();
		
		// Resolve a domain name to an IP address object
		// In this case, "localhost" maps to the so-called loop-back address, 127.0.0.1
		
		
		String [] bytesString= ip.split("\\.");
		
//		   System.out.print(bytesString.length + "\n");
//		   for(int i=0; i< bytesString.length ; i++) {
//		     System.out.print(bytesString[i] +" ");
//		  } 
		   
		   
		   // why is 132 being printed as -124????
		   byte[] IPBytes = new byte [] {(byte) (Integer.parseInt(bytesString[0])), (byte) (Integer.parseInt(bytesString[1])), (byte) (Integer.parseInt(bytesString[2])), (byte) (Integer.parseInt(bytesString[3]))};
		    
		   //byte[] IPAddress = new byte [] {(byte) (IPint[0]), (byte) (IPint[1]), (byte) (IPint[2]), (byte) (IPint[3])};
		  // IPAddress[0] = (byte) unByte(IPAddress[0]);
		   
		   
//		   for(int i=0; i< IPBytes.length ; i++) {
//		      System.out.print(IPBytes[i] +" ");
//		   }
		      
		      
		InetAddress ipAddress = InetAddress.getByAddress(IPBytes);
		
		
		// Allocate buffers for the data to be sent and received
		byte[] sendData = new byte[1024];		
		byte[] receiveData = new byte[1024];
		

		
		Random y = new Random(); 
		byte[] two = new byte[2];
		y.nextBytes(two);
		
		// Header
		sendData[0] = two[0];
		sendData[1] = two[1];
		
		sendData[2] = 00000001;  //QR =0 , PCODE = 0000 , AA =0 , TC=0 , RD =1 Recursion
		
		sendData[3] = 00000000; // Ra, Z, RCODE = 0
		
		sendData[4] = 00000000; // QDCOUNT = 1 , 16 bits
		
	    sendData[5] = 00000001; // QDCOUNT = 1 , 16 bits
		
        sendData[6] = 00000000; // 
		
	    sendData[7] = 00000000; // 
	    
	    sendData[8] = 00000000; // 
	    
	    sendData[9] = 00000000; // 
	    
	    sendData[10] = 0; // 
	    sendData[11] = 0; // 
	    
	    //DATA
	    
	    
	    int indx = 12;
	    
	 String [] Qstrings= Qname.split("\\.");
		 
		 int[] len = new int[Qstrings.length];
		 
		 for(int i=0; i< Qstrings.length ; i++) {
			  //System.out.print("len" +  Qstrings[i].length() + " ") ;
			  sendData[indx++] = (byte) Qstrings[i].length();
			  
			  char[] carray = Qstrings[i].toCharArray();
			  
			  for(int j=0; j<carray.length; j++) {
				 
				  sendData[indx++] = (byte) carray[j]; //did operation then increment
				  //System.out.print( "carray" + (byte) carray[j] + " ") ;
				  
			  }
			 
		   }
		 //String pointer = Qstrings[Qstrings.length-1];
		 
		 //System.out.print( "index" + indx + "\n") ;
		 //System.out.print(sendData[25] ) ;
		 
		 sendData[26] =0; // End QName field 
		 
		 
		 if(Qtype == "A") {
			 sendData[27] = 0;	
			 sendData[28] = 1;	 
		 }
		 else if (Qtype == "NS") {
				 sendData[27] = 0;	
				 sendData[28] = 2;	
		 }
		 
		 else if (Qtype == "MX") {
			 sendData[27] = 0;	
			 sendData[28] = 15;	
	 }
		 
		 
		 sendData[29] = 0;	
		 sendData[30] = 1;	  // Qclass = 1
			 		 

		
		// Create a UDP packet to be sent to the server
		// This involves specifying the sender's address and port number
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
		
		
		// Send the packet
		clientSocket.send(sendPacket);
		long startTime = System.currentTimeMillis();

		// Summary of the query sent
		System.out.println("DnsClient sending request for " + "[" + Qname + "]" );
		System.out.println("Server:"+ "[" + ip + "]");
		System.out.println("Request type:" + "[" + Qtype + "]");

		
		// Create a packet structure to store data sent back by the server
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		int retry = 0;
		clientSocket.setSoTimeout(timeout*1000);
		
		while(retry<=maxRetry) {
			try {
				clientSocket.receive(receivePacket);
				break;
			}
			catch(SocketTimeoutException e) {
				clientSocket.send(sendPacket);
				retry++;
			}
		}
		
		
		if(retry>maxRetry) {
			try {
				maxRetriesError(maxRetry);
			}
			catch(SocketTimeoutException e){
				System.out.println("\n" + "ERROR" +"\t"+ "Maximum number of retries [" + maxRetry +"] exceeded");
			}
		}
		
		
		

		// Receive data from the server
		
		//clientSocket.receive(receivePacket);
		long endTime = System.currentTimeMillis();
		System.out.println();
		long timeTaken = (long)((endTime - startTime));
		System.out.println("Response received after " + timeTaken + " milliseconds " + "(["  + retry + "]) " + "retries");
		
		
		// Extract the sentence (as a String object) from the received byte stream
		String modifiedSentence = new String(receivePacket.getData());

		
		System.out.println("From Server: " + modifiedSentence);
		
		byte[] byteRec = receivePacket.getData();

		int dPointer = 12; //pointer before domain name begins
		
		while(byteRec[dPointer] != 0) {
			dPointer++;		//domain name ends
		}
		
		dPointer = dPointer+7; // add 8 bytes to signify the beginning of the Type response

		dPointer = dPointer+1; // add 1 byte to skip over the byte of zeroes in the Type response

		String responseType= "";
		
		if(byteRec[dPointer]== 1) {
			responseType = "A";
		}
		
		if(byteRec[dPointer]== 2) {
			responseType = "NS";
		}
		
		if(byteRec[dPointer]== 15) {
			responseType = "MX";
		}
		
		if(byteRec[dPointer]== 5) {
			responseType = "CNAME";
		}
		dPointer++;
		System.out.println("reponse type:"+ responseType);
		
		if((byteRec[dPointer++]==0) && (byteRec[dPointer++]==1)){
			System.out.println("Response class IN");
			System.out.println("response type:"+ responseType);
		}
		else {
			System.out.println("ERROR : Different value encountered for Qcode");
		}
		

		
		byte[] ttlArr = new byte[4];
		int mask = 0xFF;
		
		ttlArr[0] = byteRec[dPointer];
		System.out.println(dPointer);
		dPointer++; // increment from index 37 to index 38..why?
		
		int unSignedval1 = ttlArr[0] & mask;
		ttlArr[1] = byteRec[dPointer++];
		int unSignedval2 = ttlArr[1] & mask;
		ttlArr[2] = byteRec[dPointer++];
		int unSignedval3 = ttlArr[2] & mask;
		ttlArr[3] = byteRec[dPointer++];
		int unSignedval4 = ttlArr[3] & mask;
		
		System.out.println(Arrays.toString(ttlArr));
		
		String ttlString = Integer.toHexString(unSignedval1)+Integer.toHexString(unSignedval2)+Integer.toHexString(unSignedval3)+Integer.toHexString(unSignedval4);
		System.out.println("ttl:");
		int ttl = Integer.parseInt(ttlString,16);
		System.out.println(ttlString); //hex
		System.out.println(ttl);		//decimal
		
		
		
		byte[] rdArr = new byte[2];
		//dPointer++; 
		rdArr[0] = byteRec[dPointer++];
		
		int unSignedval5 = rdArr[0] & mask;
		rdArr[1] = byteRec[dPointer++];
		int unSignedval6 = rdArr[1] & mask;

		String rdString = Integer.toHexString(unSignedval5)+Integer.toHexString(unSignedval6);

		System.out.println(rdString);
		
		
		if(responseType == "A") {
			byte[] IPArr = new byte[4];
			//dPointer++; 
			IPArr[0] = byteRec[dPointer++];
			int unSignedval7 = IPArr[0] & mask;
			IPArr[1] = byteRec[dPointer++];
			int unSignedval8 = IPArr[1] & mask;
			IPArr[2] = byteRec[dPointer++];
			int unSignedval9 = IPArr[2] & mask;
			IPArr[3] = byteRec[dPointer++];
			int unSignedval10 = IPArr[3] & mask;
			String IPString = Integer.toString(unSignedval7)+"."+Integer.toString(unSignedval8)+"."+Integer.toString(unSignedval9)+"."+Integer.toString(unSignedval10);
	
			System.out.println("IP \t" + IPString + "\t" + ttl + "\t" +"addauth");
		}
		
		else if (responseType == "NS") {
			
			System.out.println(Qname);
			//System.out.println("NS \t" + alias + "\t" + ttl + "\t" + auth);
		}
		
		else if (responseType == "CNAME") {
			dPointer++;
			String alias = "";

			while(byteRec[dPointer]!=0) {

				
				System.out.println("here"+dPointer);

				
				if(byteRec[dPointer] == 97) {
					dPointer++;
					int pointer = byteRec[dPointer];
					int size = byteRec[pointer];
					for(int i =0; i<size; i++) {
						pointer++;
						alias = alias + (char)byteRec[pointer];
					}
					dPointer++;
					if(byteRec[dPointer]!=0) {
						alias = alias + ".";
					}

					
				}
				else {
					int size = byteRec[dPointer];
					for(int i = 0; i<size; i++) {
						dPointer++;
						alias = alias + (char)byteRec[dPointer];
					}
					dPointer++;
					if(byteRec[dPointer]!=0) {
						alias = alias + ".";
					
					}
				}
			}
			System.out.println(alias);
			//System.out.println("CNAME \t" + alias + "\t" + ttl + "\t" + auth);
		}
		else if (responseType == "MX") {
		
		}
		
		
		// Close the socket
		clientSocket.close();
	}
	
	private static void maxRetriesError(int x) throws SocketTimeoutException {
		throw new SocketTimeoutException ();
	}
	
	public static int unByte(byte d) {
		return (d & 0xFF);
	}
}