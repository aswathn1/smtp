import java.net.*;
import java.io.*;
import java.util.*;
import java.net.SocketTimeoutException;


public class SMTPConnection implements Runnable{


   	 private Socket socket;
	private String localHost,remoteHost;
	
	DataOutputStream toClient;
	BufferedReader fromClient;

	private static final String CRLF = "\r\n";
	private static final boolean SENDER = false;
	private static final boolean RECEIVER = true;

    public SMTPConnection(Socket socket) throws Exception 
	{
		this.socket = socket;
	}


    public void run()
	{
		try {
				processRequest();
		}
		catch (Exception e) {
				System.out.println(e);
		}
	}

    private void processRequest(){

		
		boolean quit=false;
		String requestCommand;
		
		String sender="";
		String receiver="";

		try {

			
			InputStream is = socket.getInputStream();
			toClient = new DataOutputStream(socket.getOutputStream());

			
			InputStreamReader sr = new InputStreamReader(is);
			fromClient = new BufferedReader(sr);

		}catch (IOException e) {
			System.out.println("Initialization error: "+e);
		}
		
		try {
			localHost = "127.0.0.1";
		}
		catch (Exception e) {
			System.out.println("Get hostname error: "+e);
		}
		remoteHost= "127.0.0.1";

		/* Send the SMTP Welcome command. */
		reply("220 SERVER READY");

		/* Wait the client to send the HELO*/
		while(!quit) {
			if ( (requestCommand=fetch()).length()==0 ) 
			{quit=true;}
			else if ( requestCommand.substring(0,4).equals("HELO") )
				{
				System.out.println("received HELO");
				reply("250 OK");
				break;
			}

			/* If the client want to quit this session */
			else if (requestCommand.substring(0,4).equals("QUIT")) 
			{
			quit=true;
			}	
			

			/* If unrecognized command is received, output an error */
			else	
			 { reply("502 COMMAND NOT RECOGNIZED need HELO"); }
		}

		while (!quit) {
			
			/* Wait for Mail session */
			
				if ( (requestCommand=fetch()).length()==0 ) 	
				{quit=true;}

				/* If the client send the appropriate command */
				else if ( requestCommand.substring(0,4).equals("MAIL"))
				{

						/* get the sender address */
						int nn=requestCommand.length();
						sender=requestCommand.substring(11,nn);
						if(sender.endsWith("@gmail.com"))
						{
						/* tell the client the sender is ok */
						reply("250 OK");
						System.out.println("Received MAIL FROM");
						break;
						}
						else
						{
						reply("554 MAIL ID ERROR");
						}
					
				
				}

					
				/* If unrecognized command is received, output an error */
				else { reply("502 WRONG COMMAND need MAIL FROM"); }
			}

			/* Wait for Receipant session */
			while (!quit) {
				if ( (requestCommand=fetch()).length()==0 ) quit=true;

				/* If the client send the appropriate command */
				else if ( requestCommand.substring(0,4).equals("RCPT"))
				 {
					
						/* get the receiver address */
						
						int n=requestCommand.length();
						receiver=requestCommand.substring(8,n);						
						
						System.out.println(receiver);
						boolean contains=false;
						String[] recipient=new String[]{"tejalsatish@gmail.com","faustina@gmail.com","rpmaam@gmail.com","spsir@gmail.com"};
						for(int i=0;i<4;i++)
						{
						if(recipient[i].equals(receiver))
							{
							contains=true;
							break;
							}
						}
						if(contains)
						{
						reply("250 OK");
						break;
						}
						else
						{
						reply("550 NO SUCH USER HERE");
						}



						

				}

				/* If unrecognized command is received, output an error */
				else { reply("502 INVALID COMMAND need RCPT TO"); }
			}

			/* Wait for data session */
			while (!quit) {
				if ( (requestCommand=fetch()).length()==0 ) quit=true;

				/* If the client send the appropriate command */
				else if ( requestCommand.equals("DATA"))
					 {
					String data1="";	
					try{
					reply("354 START MAIL INPUT; end with QUIT!");
					while(true)
					{
					String dat=fromClient.readLine();
					
					if(dat.equals("QUIT!"))
						{
						quit=true;
						break;
						}
					else
						{
						data1=data1+dat;
						reply("250 OK");
						}		
					}
			
					String toserv="MAIL FROM:"+sender+" RCPT TO:"+receiver+" MSG:"+data1;
					System.out.println(toserv);
					DatagramSocket client=new DatagramSocket();
					client.setSoTimeout(3000);
					InetAddress ip=InetAddress.getLocalHost();
					byte[] sendata=new byte[1024];
					sendata=toserv.getBytes();
					DatagramPacket sendpac=new DatagramPacket(sendata,sendata.length,ip,2226);
					client.send(sendpac);
					
					 byte[] receiveData = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      					client.receive(receivePacket);
      					String fromserv = new String(receivePacket.getData()); 
					client.close();
					
					reply(fromserv+" MESSAGE SAVED");

					}
					catch(SocketTimeoutException e)
					{
					reply("441 CONNECTION TO RECEIVER SMTP NOT ESTABLISHED, DATA LOST");
					continue;
					}
					catch(Exception e)
						{
						e.printStackTrace();
						}
					/* tell the client the message is saved */

					
				}

				/* If unrecognized command is received, output an error */
				else { reply("502 INCORRECT COMMAND need DATA"); }
			}
		

		/* tell the client that the server is closing the channel */
		reply("221 CLOSING SERVER");
		try {
			socket.close();
		}catch (IOException e) {
			System.out.println("Close connection error: "+e);
		}
	}

/* This method fetch every line from the client */
private String fetch()
	{
		String message="";
		try {
			
				message = fromClient.readLine();
				System.out.println(message);				
				
		}catch (Exception e) {
			System.out.println("Read socket error: "+e);
		}
		try {
			if (message == null) {
				socket.close();
				return "";
			}
		}catch (IOException e) {
			System.out.println("Close connection error: "+e);
		}
		return message;
		}



private void reply (String command)	
	{
		try
		{
			if (!socket.isClosed()) 
				{
				toClient.writeBytes(command+CRLF);
				}			
		}
		catch (IOException e)
		 {
			System.out.println("Write socket error: "+e);
		}
		System.out.println(command);
		return;
	}
protected void finalize() throws Throwable {
	    socket.close();
		super.finalize();
    }
}


