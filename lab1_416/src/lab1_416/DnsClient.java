package lab1_416;


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

		boolean a=true;
		int timeout = 5;
		int maxRetry = 3;
		int port = 53;
		String Qtype = "A"; //1 = A , 2= NS, 15= MX

		String Qname = "";
		String ip = "";
		String auth = "";
		

		int indxArgs = 0;
		boolean at = false;
		boolean syntax = false;
	    boolean mx = false;
	    boolean ns = false;
	    boolean t = false;
	    boolean r = false;
	    boolean p = false;
		//reading the args


		try {

			for (int i = 0; i < args.length; i++) {
				
				if (args.length < 2) {
		            System.out.println("ERROR\tIncorrect input syntax: Server and name must be specified as inputs.");
		            
		        }
				
				char[] arrArgs = args[i].toCharArray();            
				for (int j = 0; j < arrArgs.length; j++) {  
					if(i==args.length-2) {
					if (arrArgs[j] == '@' ) {

						ip = args[i];
						Qname = args[i+1]; 
						at = true;
						break;
					}                   
////					else if (arrArgs[j] == '.') 
////						indxArgs++ ;
//
////					else if (arrArgs[j] != '@' && Character.isDigit(arrArgs[j]) && at)
////						//ipArr[indxArgs] += arrArgs[j];
////						ip = args[i];
////					//System.out.println("args" + args[i]);
////					}
					else {
						System.out.println("ERROR\t incorrect syntax");
					}
					
				}   
			}
			}

			
			for (int i = 0; i < args.length-2; i++) {
				syntax = false;

				char[] arrArgs = args[i].toCharArray();            
					for (int j = 0; j < arrArgs.length; j++) {  

					if (arrArgs[j] == '-'){
						if (arrArgs[j+1] == 't' && args[i].length()==2 && t==false) {
							i++;
							timeout = Integer.parseInt(args[i]);
							t = true;
							syntax = true;
						}
						else if (arrArgs[j+1] == 'r' && args[i].length()==2 && r==false) {
							i++;
							maxRetry = Integer.parseInt(args[i]);
							r = true;
							syntax = true;
						}
						else if (arrArgs[j+1] == 'p' && args[i].length()==2 && p==false) {
							i++;
							port = Integer.parseInt(args[i]);
							p = true;
							syntax = true;
						}
	
						else if (arrArgs[j+1] == 'm' && arrArgs[j+2] == 'x' && args[i].length()==3 && mx==false) {
							Qtype = "MX";
							mx = true;
							syntax = true;
						}
	
						else if (arrArgs[j+1] == 'n' && arrArgs[j+2] == 's' && args[i].length()==3 && ns==false) {
							Qtype = "NS";
							ns = true;
							syntax = true;
						}
						
					}
					if(syntax == false) {
						System.out.println(args[i]);
						System.out.println("ERROR\t Invalid Syntax");
						System.exit(1);
					}
					
					}
			}
			
	        if (mx && ns) {
	            System.out.println("ERROR\tIncorrect syntax: use only mx or ns.");
	        }
			
			if (!at) {                              
				System.out.println("The IP address entered is not correct. Please ensure to enter'@'");
				System.exit(1);
			}


			//            if (atSign >1) {
			//                System.out.println("ERROR   Only enter one '@'");
			//                System.exit(1);
			//            }

			if (timeout == 0){
				System.out.println("Please enter a value greater than 0 for the timeout");
				System.exit(1);
			}

		} 

		catch (IllegalArgumentException e) {
			System.out.println("Please make sure your arguments follows the correct format");
			System.exit(1);
		}
		if (ip == null || Qname == null) {
			System.out.println("Please make sure to provide the IP address and domain name");
			System.exit(1);
		}

		// Create a UDP socket
		// (Note, when no port number is specified, the OS will assign an arbitrary one)
		DatagramSocket clientSocket = new DatagramSocket();

		// Resolve a domain name to an IP address object
		// In this case, "localhost" maps to the so-called loop-back address, 127.0.0.1

		ip= ip.replaceAll("@","");

		String [] bytesString= ip.split("\\.");

		byte[] IPBytes = new byte [] {(byte) (Integer.parseInt(bytesString[0])), (byte) (Integer.parseInt(bytesString[1])), (byte) (Integer.parseInt(bytesString[2])), (byte) (Integer.parseInt(bytesString[3]))};



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

		sendData[2] = 00000001;  //QR =0 , OPCODE = 0000 , AA =0 , TC=0 , RD =1 Recursion

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
			sendData[indx++] = (byte) Qstrings[i].length();

			char[] carray = Qstrings[i].toCharArray();

			for(int j=0; j<carray.length; j++) {

				sendData[indx++] = (byte) carray[j]; //did operation then increment

			}

		}
		
		sendData[indx++] = 0; // End QName field 


		if(a==true) {
			sendData[indx++] = 0;	
			sendData[indx++] = 1;	
		}
		else if (ns==true) {
			sendData[indx++] = 0;	
			sendData[indx++] = 2;
		}

		else if (mx==true) {
			sendData[indx++] = 0;	
			sendData[indx++] = 15;	
		}

		sendData[indx++] = 0;	
		sendData[indx] = 1;	  // Qclass = 1



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
		long timeTaken = (long)((endTime - startTime));
		System.out.println("Response received after " + timeTaken + " milliseconds " + "(["  + retry + "]) " + "retries");


		/////reading response//////

		byte[] byteRec = receivePacket.getData();

		//Packet Header of the response

		//3rd byte in the header
		String bin = String.format("%8s", Integer.toBinaryString(byteRec[3]&0xff)).replace(' ', '0');


		char s = bin.charAt(5);
		if (s == '0') {
			auth = "nonauth";
		}
		else if (s == '1') {
			auth = "auth";
		}


		String bin2 = String.format("%8s", Integer.toBinaryString(byteRec[4]&0xff)).replace(' ', '0');
		// 1st character in the 4th byte
		if (bin2.charAt(1)==0) {
			System.out.println("ERROR" + "\t" + "Server does not support recursion");
		}

		String rCode = bin2.substring(4,8);

		if(rCode.equals("0001")) {
			System.out.println("ERROR" + "\t" + "Format error: the name server was unable to interpret the query");
		}
		else if(rCode.equals("0010")) {
			System.out.println("ERROR" + "\t" + "Server failure: the name server was unable to process this query due to a problem with the name server");
		}
		else if(rCode.equals("0011")) {
			System.out.println("RECORD NOT FOUND");
		}
		else if(rCode.equals("0100")) {
			System.out.println("ERROR" + "\t" + "Not implemented: the name server does not support the requested kind of query");
		}
		else if(rCode.equals("0101")) {
			System.out.println("ERROR" + "\t" + "Refused: the name server refuses to perform the requested operation for policy reason");
		}

		
		int ancount = (receiveData[6] << 8) | receiveData[7];

		System.out.println("***Answer Section (" + ancount + " records)***");

		if(ancount == 0) {
			System.out.println("NOTFOUND");
		}


		int dPointer = 12; //pointer before domain name begins

		while(byteRec[dPointer] != 0) {
			dPointer++;		//domain name ends
		}
	
		dPointer = dPointer+7; // add 8 bytes to signify the beginning of the Type response
			
		returnResult(ancount, dPointer, byteRec, auth);
		
		
		int arcount = (receiveData[10] << 8) | receiveData[11];

		System.out.println("***Additional Section (" + arcount + " records)***");

		dPointer++;
		returnResult(arcount, dPointer, byteRec, auth);

		
		if(arcount == 0) {
			System.out.println("NOTFOUND");
		}

			// Close the socket
			clientSocket.close();
	}	
			
static void returnResult(int ancount, int dPointer, byte[] byteRec, String auth) {
		for(int x=0;x<ancount;x++) {

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
			dPointer=dPointer+1; //skip zero
			dPointer++;
	
			
			if((byteRec[dPointer]&0xFF)!=1){
				System.out.println("ERROR : Different value encountered for Qcode");
			}
	
			dPointer++;
			byte[] ttlArr = new byte[4];
			int mask = 0xFF;
	
			ttlArr[0] = byteRec[dPointer];
			dPointer=dPointer+1; // increment from index 37 to index 38..why?
	
			int unSignedval1 = ttlArr[0] & mask;
			ttlArr[1] = byteRec[dPointer++];
			int unSignedval2 = ttlArr[1] & mask;
			ttlArr[2] = byteRec[dPointer++];
	
			int unSignedval3 = ttlArr[2] & mask;
			ttlArr[3] = byteRec[dPointer++];
	
			int unSignedval4 = ttlArr[3] & mask;
	
	
			String ttlString = Integer.toHexString(unSignedval1)+Integer.toHexString(unSignedval2)+Integer.toHexString(unSignedval3)+Integer.toHexString(unSignedval4);
			//System.out.println("ttl:");
			int ttl = Integer.parseInt(ttlString,16);
			//System.out.println(ttlString); //hex
			//System.out.println(ttl);		//decimal
	
	
	
			byte[] rdArr = new byte[2];
			//dPointer++; 
			rdArr[0] = byteRec[dPointer++];
	
			int unSignedval5 = rdArr[0] & mask;
			rdArr[1] = byteRec[dPointer++];
			int unSignedval6 = rdArr[1] & mask;
	
			String rdString = Integer.toHexString(unSignedval5)+Integer.toHexString(unSignedval6);
	
			//System.out.println("rdString: "+rdString);
	
	
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
	
				System.out.println("IP \t" + IPString + "\t" + ttl + "ms"+ "\t" + auth);
			}
	
	
			else if (responseType == "NS") {
	
				String alias = "";
	
				while(byteRec[dPointer]!=0) {
	
	
					if((byteRec[dPointer] & 0xFF) >= 0xC0) {
	
						dPointer++;
						int pointer = byteRec[dPointer];
						while(byteRec[pointer]!=0) {
	
							int size = byteRec[pointer];
							for(int i =0; i<size; i++) {
								pointer++;
								alias = alias + (char)byteRec[pointer];
							}
	
							dPointer++;
							if(byteRec[dPointer]!=0) {
								alias = alias + ".";
								pointer++;
							}
						}
						dPointer++;
	
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
				alias = alias.substring(0, alias.length() - 1);
	
				System.out.println("NS" + "\t" + alias + "\t" + ttl + " ms"+"\t" + auth);
	
			}
	
			else if (responseType == "CNAME") {
				//dPointer++;
				String alias = "";
	
				while(byteRec[dPointer]!=0) {
	
	
					if((byteRec[dPointer] & 0xFF) >= 0xC0) {
	
						dPointer++;
						int pointer = byteRec[dPointer];
						while(byteRec[pointer]!=0) {
	
							int size = byteRec[pointer];
							for(int i =0; i<size; i++) {
								pointer++;
								alias = alias + (char)byteRec[pointer];
							}
	
							dPointer++;
							if(byteRec[dPointer]!=0) {
								alias = alias + ".";
								pointer++;
	
							}
	
	
						}
						dPointer++;
	
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
				//System.out.println(alias);
				alias = alias.substring(0, alias.length() - 1);
	
				System.out.println("CNAME \t" + alias + "\t" + ttl + " ms"+ "\t" + auth);

			}
			else if (responseType == "MX") {
	
	
				int pref = (byteRec[dPointer] << 8) | byteRec[dPointer + 1];
				dPointer++;
	
	
				String alias = "";
	
				while(byteRec[dPointer]!=0) {
	
	
					if((byteRec[dPointer] & 0xFF) >= 0xC0) {
	
						dPointer++;
						int pointer = byteRec[dPointer];
						while(byteRec[pointer]!=0) {
	
							int size = byteRec[pointer];
							for(int i =0; i<size; i++) {
								pointer++;
								alias = alias + (char)byteRec[pointer];
							}
	
							dPointer++;
							if(byteRec[dPointer]!=0) {
								alias = alias + ".";
								pointer++;
							}
						}
						dPointer++;
	
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
				//alias = alias.substring(0, alias.length() - 1);
	
				System.out.println("MX\t" + alias + "\t" + pref + "\t" + ttl + "ms" + "\t" + auth);
	
			}
			if(responseType == "A") {
				dPointer=dPointer+2;

			}
			if(responseType=="CNAME") {
				dPointer=dPointer;

			}
		}
}



	private static void maxRetriesError(int x) throws SocketTimeoutException {
		throw new SocketTimeoutException ();
	}


}
