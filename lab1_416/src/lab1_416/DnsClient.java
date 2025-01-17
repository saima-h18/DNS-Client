package lab1_416;


import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.lang.Math;
import java.lang.reflect.Array;


public class DnsClient {

    public static void main(String[] args) throws Exception{

        // constants
        int timeout = 5;
        int port = 53;
		int maxRetry = 3;
		String Qtype = "A"; //1 = A , 2= NS, 15= MX
		String Qname = "";
		String ip = "";
		
        // booleans checking if arguments are entered
        boolean a = true;
        boolean mx = false;
        boolean ns = false;
        boolean t = false;
        boolean r = false;
        boolean p = false;
		int at = 0 ;
		boolean syntax = false;



		try {

			// reading the arguments entered
			for (int i = 0; i < args.length; i++) {
				
				if (args.length < 2) {
		            System.out.println("ERROR\tIncorrect input syntax");
		            
		        }
				
				// iterating through characters per term entered
				char[] arrArgs = args[i].toCharArray();            
				for (int j = 0; j < arrArgs.length; j++) {  
					
					if(i==args.length-2) {
						// checks if ip starts with at sign 
						if (arrArgs[j] == '@' && arrArgs[j+1]=='@') {
	
							System.out.println("ERROR\t incorrect syntax \n Proper format: [-t] [-r] [-p] [-nx || -mx] @server name");
							System.exit(1);
						} 
						if (arrArgs[j] == '@') {
							
							// allocates server variable and name variable
							ip = args[i];
							Qname = args[i+1]; 
							at++;
							break;
						}   
	
						else {
							System.out.println("ERROR\t incorrect syntax \n Proper format: [-t] [-r] [-p] [-nx || -mx] @server name");
						}
					
				}   
			}
			}
			
			// iterating through each term up till the ip address
			for (int i = 0; i < args.length-2; i++) {
				syntax = false;

				// separate each term into character array
				char[] arrArgs = args[i].toCharArray();  
				// iterate through each character
					for (int j = 0; j < arrArgs.length; j++) {  

					if (arrArgs[j] == '-'){
						// checks if each variable is set to false because cannot enter 
						// terms twice
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
							a=false;
							syntax = true;
						}
	
						else if (arrArgs[j+1] == 'n' && arrArgs[j+2] == 's' && args[i].length()==3 && ns==false) {
							Qtype = "NS";
							ns = true;
							a=false;
							syntax = true;
						}
						
					}
					// invalid syntax every other time
					if(syntax == false) {
						System.out.println("ERROR\t incorrect syntax \n Proper format: [-t] [-r] [-p] [-nx || -mx] @server name");
						System.exit(1);
					}
					
					}
			}
			
			// cant use both terms as arguments
	        if (mx && ns) {
	            System.out.println("ERROR\tIncorrect syntax: use only mx or ns.");
	        }
			
			

			if (timeout == 0){
				System.out.println("Please enter a value greater than 0 for the timeout");
				System.exit(1);
			}
			
			if (at==0) {                              
				System.out.println("The IP address entered is not correct. Please ensure to enter'@'");
				System.exit(1);
			}


			 if (at >1) {
				 System.out.println("ERROR   Only enter one '@'");
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

		ip= ip.replaceAll("@","");
		
		String [] bytesString= ip.split("\\.");
		for(int j=0; j< bytesString.length;j++) {
			if(Integer.parseInt(bytesString[j]) <0 || Integer.parseInt(bytesString[j])>255){
				System.out.println("ERROR\t Please make sure your arguments follows the correct format");
				System.exit(1);
			}
		}

		byte[] IPBytes = new byte [] {(byte) (Integer.parseInt(bytesString[0])), (byte) (Integer.parseInt(bytesString[1])), (byte) (Integer.parseInt(bytesString[2])), (byte) (Integer.parseInt(bytesString[3]))};

		DatagramSocket clientSocket = new DatagramSocket();


		InetAddress ipAddress = InetAddress.getByAddress(IPBytes);



		// Allocate buffers for the data to be sent and received
				byte[] sendData = new byte[1024];		
				byte[] receiveData = new byte[1024];


				// two new random bytes for ID
				Random y = new Random(); 
				byte[] two = new byte[2];
				y.nextBytes(two);

				// Header
				sendData[0] = two[0];
				sendData[1] = two[1];

				sendData[2] = 00000001;  //QR =0 , OPCODE = 0000 , AA =0 , TC=0 , RD =1 Recursion

				sendData[3] = 00000000; // Ra, Z, RCODE = 0

				sendData[4] = 00000000; // QDCOUNT = 1 , 16 bits

				sendData[5] = 00000001; // 

				sendData[6] = 00000000; // 

				sendData[7] = 00000000; // 

				sendData[8] = 00000000; // 

				sendData[9] = 00000000; // 

				sendData[10] = 0; // 
				sendData[11] = 0; // 

				//DATA           

				// Splitting the domain name (removing the periods)
				String[] stringName = Qname.split("\\.");
				// thats where the data (header ends)
		        int indx = 12;
		        //splits by words (separated by periods)
		        for (int i = 0; i < stringName.length; i++) {
		            // Adds the number of characters in the name label
		            sendData[indx] = (byte) stringName[i].length();
		            indx++;
		            //splits by characters
		            for (int j = 0; j < stringName[i].length(); j++) {
		                // Converts each character in the label to the corresponding ASCII code
		                sendData[indx] = (byte) stringName[i].charAt(j);
		                indx++;
		            }
		        }
				
				sendData[indx] = 0; // End QName field 
				indx++;

				sendData[indx] = 0;		//skip a zero for type
				indx++;
				// type based on arguments
				if(a==true) {
					sendData[indx] = 1;	
				}
				else if (ns==true) {
					sendData[indx] = 2;
				}

				else if (mx==true) {
					sendData[indx] = 15;	
				}
				indx++;
				sendData[indx] = 0;	// Qclass = 1 (representing Internet address
				indx++;
				sendData[indx] = 1;	  // Qclass = 1 (representing Internet address

		// Create a UDP packet to be sent to the server
		// This involves specifying the sender's address and port number
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);


		// Summary of the query sent
		System.out.println("DnsClient sending request for " + "[" + Qname + "]" );
		System.out.println("Server:"+ "[" + ip + "]");
		System.out.println("Request type:" + "[" + Qtype + "]");
		
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

		// Send the packet
		clientSocket.send(sendPacket);
		
		//calculate time of the process
		long startTime = System.currentTimeMillis();

		
		int retry = 0;
		
		// setting timeout based on inputed values
		clientSocket.setSoTimeout(timeout*1000);

		
		while(retry<=maxRetry) {
			try {
				// keep trying to receive packets
				clientSocket.receive(receivePacket);
				break;
			}
			catch(SocketTimeoutException e) {
				//if not received then resend
				clientSocket.send(sendPacket);
				retry++;
			}
		}

		// if it increases maxretry, then socket exception
		if(retry>maxRetry) {
			try {
				maxRetriesError(maxRetry);
			}
			catch(SocketTimeoutException e){
				System.out.println("\n" + "ERROR" +"\t"+ "Maximum number of retries [" + maxRetry +"] exceeded");
			}
		}

		// Receive data from the server

		// calculate total time taken
		long endTime = System.currentTimeMillis();
		long timeTaken = (long)((endTime - startTime));
		System.out.println("Response received after " + timeTaken + " milliseconds " + "(["  + retry + "]) " + "retries");

		// call readpacket method
        readPacket(receiveData, indx);

    }

	static void readPacket(byte[] byteRec, int index) {

		// reading header bytes
		// find binary string of the 3rd byte in the header
		String bin = String.format("%8s", Integer.toBinaryString(byteRec[3]&0xff)).replace(' ', '0');
		String auth="";

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

		int nscount = (byteRec[8] << 8) | byteRec[9];
		int ancount = (byteRec[6] << 8) | byteRec[7];
		int arcount = (byteRec[10] << 8) | byteRec[11];


		System.out.println("***Answer Section (" + ancount + " records)***");

		if(ancount == 0) {
			System.out.println("NOTFOUND");
		}
		
		//length of the query because it is repeated in the response packet
		int indx = index + 1;

		indx = packetParse(byteRec, ancount, indx, auth, true);

		// not printing authority section but have to keep count
		indx = packetParse(byteRec, nscount, indx, auth, false);

		System.out.println("***Additional Section (" + arcount + " records)***");
		
		if(arcount == 0) {
			System.out.println("NOTFOUND");
		}

		indx = packetParse(byteRec, arcount, indx, auth, true);
	}

	public static int packetParse(byte[] byteRec, int increment, int indx, String auth, boolean show) {

		for (int x = 0; x < increment; x++) {

			String domainname="";
			int indexdomain=indx;
			
			//indexdomain = indx;
			domainname = findDomainName(byteRec, indx);
			indexdomain = findDomainNameIndex(byteRec, indx);

			//indx = nameIndex.indx;
			indx=indexdomain;
			
			///// type
			int responseType = (byteRec[indx] & 0xFF) << 8 | (byteRec[indx + 1] & 0xFF);
			indx = indx + 2;	//increment 2 bytes

			///// class
			int qval = (byteRec[indx] & 0xFF) << 8 | (byteRec[indx + 1] & 0xFF);
			if (qval != 1){
				System.out.println("ERROR\tA Qvalue value is not correct");
			}
			indx = indx + 2;	//increment 2 bytes

			
			///////ttl
			
			byte[] ttlArr = new byte[4];
			int mask = 0xFF;
	
			ttlArr[0] = byteRec[indx];
			indx=indx+1; // increment from index 37 to index 38..why?
	
			int unSignedval1 = ttlArr[0] & mask;
			ttlArr[1] = byteRec[indx++];
			int unSignedval2 = ttlArr[1] & mask;
			ttlArr[2] = byteRec[indx++];
	
			int unSignedval3 = ttlArr[2] & mask;
			ttlArr[3] = byteRec[indx++];
	
			int unSignedval4 = ttlArr[3] & mask;
	
	
			String ttlString = Integer.toHexString(unSignedval1)+Integer.toHexString(unSignedval2)+Integer.toHexString(unSignedval3)+Integer.toHexString(unSignedval4);
			//System.out.println("ttl:");
			int ttl = Integer.parseInt(ttlString,16);

			
			///////rdString
			
			byte[] rdArr = new byte[2];
			//dPointer++; 
			rdArr[0] = byteRec[indx++];
	
			int unSignedval5 = rdArr[0] & mask;
			rdArr[1] = byteRec[indx++];
			int unSignedval6 = rdArr[1] & mask;
	
			String rdString = Integer.toHexString(unSignedval5)+Integer.toHexString(unSignedval6);
	
			int rdString1 = Integer.parseInt(rdString,16);

			byte[] rdData = new byte[rdString1];
			//index where rData starts
			int indxrData = indx;
			//rData is an array of bytes containing rData information
			for (int j = 0; j < rdString1; j++) {
				rdData[j] = byteRec[indx];
				indx++;
			}
			
			
			//if print is set to true(only not set to true for authority section) 
			if (show==true) {
				// Type A
				if (responseType == 1) {
					byte[] IPArr = new byte[4];
					//dPointer++; 
					IPArr[0] = rdData[0];
					int unSignedval7 = IPArr[0] & mask;
					IPArr[1] = rdData[1];
					int unSignedval8 = IPArr[1] & mask;
					IPArr[2] = rdData[2];
					int unSignedval9 = IPArr[2] & mask;
					IPArr[3] = rdData[3];
					int unSignedval10 = IPArr[3] & mask;
					String IPString = Integer.toString(unSignedval7)+"."+Integer.toString(unSignedval8)+"."+Integer.toString(unSignedval9)+"."+Integer.toString(unSignedval10);
		
					System.out.println("IP \t" + IPString + "\t" + ttl + "ms"+ "\t" + auth);

				}

				// NS delegates you to an authoritative name server
				
				else if (responseType == 2) {
					domainname = findDomainName(byteRec, indxrData);
					indexdomain = findDomainNameIndex(byteRec, indxrData);					
					String alias = domainname;
					System.out.println("NS \t" + alias + "\t" + ttl + "\t" + auth);
				}
				// CNAME: an alias for another dns record, refers to another host name; access the
				// website in more than one way
				else if (responseType == 5) {
					domainname = findDomainName(byteRec, indxrData);
					indexdomain = findDomainNameIndex(byteRec, indxrData);
					String alias = domainname;
					System.out.println("CNAME \t" + alias + "\t" + ttl + "\t" + auth);
				}
				// MX host name of the computer that handles the emails
				else if (responseType == 15) {
					int pref = (byteRec[indxrData] << 8) | byteRec[indxrData + 1];
					indxrData = indxrData+ 2;
					domainname = findDomainName(byteRec, indxrData);
					indexdomain = findDomainNameIndex(byteRec, indxrData);
					String alias = domainname;
					System.out.println("MX \t" + alias + "\t" + pref + "\t" + ttl + "\t" + auth);
				}
			}
		}
		return indx;
	}

	static String findDomainName(byte[] byteRec, int indx) {

		String DomainName = "";
		int indexDomainName;
		// theres two 1s in the beginning indicating a pointer
		if ((byteRec[indx] & 0xFF) >= 0xC0) {
			DomainName = pointerName(byteRec, indx);
			indx = indx + 2;// each pointer is 16 bits so increment bytes by 2
			indexDomainName = indx;
			return DomainName;
		}

		// when no offset is recognized (no leading 1s)
		while (byteRec[indx] != 0 && (byteRec[indx] & 0xFF) < 0xc0) {
			
			int lengthWord = byteRec[indx++];
			for (int i = 0; i < lengthWord; i++) {
				DomainName = DomainName + Character.toString((char) (byteRec[indx++] & 0xFF));
			}
			DomainName = DomainName + ".";
		}

		// Still possible to run into a pointer so run pointer method
		// while running the domain name method
		if ((byteRec[indx] & 0xFF) >= 0xc0) {
			DomainName = DomainName + pointerName(byteRec, indx);
			indx= indx+2; // each pointer is 16 bits so increment bytes by 2
			indexDomainName = indx;
			return DomainName;
		}

		// extracts extra period at the end
		DomainName = DomainName.substring(0, DomainName.length() - 1);

		indexDomainName= indx;
		return DomainName;
	}

	static int findDomainNameIndex(byte[] byteRec, int indx) {

		String DomainName = "";
		int indexDomainName;
		
		// theres two 1s in the beginning indicating a pointer
		if ((byteRec[indx] & 0xFF) >= 0xC0) {
			DomainName = pointerName(byteRec, indx);
			indx = indx + 2; // each pointer is 16 bits so increment bytes by 2
			indexDomainName = indx;
			return indexDomainName;
		}

		DomainName = "";

		// when no offset is recognized (no leading 1s)

		// Still possible to run into a pointer so run pointer method
		// while running the domain name method
		if ((byteRec[indx] & 0xFF) >= 0xc0) {
			DomainName += pointerName(byteRec, indx);
			indx = indx + 2;
			indexDomainName = indx;		// each pointer is 16 bits so increment bytes by 2
			return indexDomainName;
		}

		indexDomainName= indx;
		return indexDomainName;
	}
	
	
	
	static String pointerName(byte[] byteRec, int indx) {

		String alias = "";
		//checks if there is leading 1s
		if((byteRec[indx] & 0xFF) >= 0xC0) {
			
			indx++;
			// pointer to the new address where an old domain name exists
			int pointer = byteRec[indx];
			//checks till zeros are reached (indicating the end of domain name)
			while(byteRec[pointer]!=0) {

				//the first byte indicating size of the first word before the period
				int size = byteRec[pointer];
				for(int i =0; i<size; i++) {
					pointer++;
					alias = alias + (char)byteRec[pointer];
				}

				indx++;
				//if its not zero, add a period after the first word and subsequent words till
				// zeros are reached
				if(byteRec[indx]!=0) {
					alias = alias + ".";
					pointer++;
				}
			}
			indx++;

		}

		return alias;
	}


	private static void maxRetriesError(int x) throws SocketTimeoutException {
		throw new SocketTimeoutException ();
	}


}
