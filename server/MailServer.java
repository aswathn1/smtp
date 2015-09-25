import java.io.* ;
import java.net.* ;
import java.util.* ;

public final class MailServer {
	public static void main(String argv[]) throws Exception {

		
		int port = 2225;

		
        ServerSocket mailSocket = new ServerSocket(port);

		/* Process SMTP client requests in an infinite loop. */
		while (true) {
			
			Socket SMTPSocket = mailSocket.accept();

		
			SMTPConnection connection = new SMTPConnection(SMTPSocket);

			
			Thread thread = new Thread(connection);

			thread.start();
		}
	}
}
