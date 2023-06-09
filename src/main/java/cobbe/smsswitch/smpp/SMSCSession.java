package cobbe.smsswitch.smpp;

import java.io.IOException;

import org.smpp.Connection;
import org.smpp.TCPIPConnection;
import org.smpp.Data;
import org.smpp.Receiver;
import org.smpp.ConnectionListener;
import org.smpp.SmppObject;
import org.smpp.Transmitter;
import org.smpp.pdu.PDU;
import org.smpp.pdu.PDUException;
import org.smpp.pdu.Request;
import org.smpp.pdu.Response;



public class SMSCSession extends SmppObject implements Runnable {
	private Receiver receiver;
	private Transmitter transmitter;
	private PDUProcessor pduProcessor;
	private Connection connection;
	private long receiveTimeout = Data.RECEIVER_TIMEOUT;
	private boolean keepReceiving = true;
	private boolean isReceiving = false;
	private int timeoutCntr = 0;

	/**
	 * Initialises the session with the connection the session
	 * should communicate over.
	 * @param connection the connection object for communication with client
	 */
	public SMSCSession(TCPIPConnection connection) {
		this.connection = connection;
		transmitter = new Transmitter(connection);
		receiver = new Receiver(transmitter, connection);
		connection.setListener(new ConnectionListener() {
		@Override
		public void close() {
		stop() ;
		pduProcessor.exit() ;
		}
		}) ;
	}

	/**
	 * Signals the session's thread that it should stop.
	 * Doesn't wait for the thread to be completly finished.
	 * Note that it can take some time before the thread is completly
	 * stopped.
	 * @see #run()
	 */
	public void stop() {
		debug.write("SMSCSession stopping");
		keepReceiving = false;
	}

	/**
	 * Implements the logic of receiving of the PDUs from client and passing
	 * them to PDU processor. First starts receiver, then in cycle
	 * receives PDUs and passes them to the proper PDU processor's
	 * methods. After the function <code>stop</code> is called (externally)
	 * stops the receiver, exits the PDU processor and closes the connection,
	 * so no extry tidy-up routines are necessary.
	 * @see #stop()
	 * @see PDUProcessor#clientRequest(Request)
	 * @see PDUProcessor#clientResponse(Response)
	 */
	public void run() {
		PDU pdu = null;

		debug.enter(this, "SMSCSession run()");
		debug.write("SMSCSession starting receiver");
		receiver.start();
		isReceiving = true;
		try {
			while (keepReceiving) {
				try {
					debug.write("SMSCSession going to receive a PDU");
					pdu = receiver.receive(getReceiveTimeout());
				} catch (Exception e) {
					e.printStackTrace() ;
					debug.write("SMSCSession caught exception receiving PDU " + e.getMessage());
				}

				if (pdu != null) {
					timeoutCntr = 0;
					if (pdu.isRequest()) {
						debug.write("SMSCSession got request " + pdu.debugString());
						pduProcessor.clientRequest((Request) pdu);
					} else if (pdu.isResponse()) {
						debug.write("SMSCSession got response " + pdu.debugString());
						pduProcessor.clientResponse((Response) pdu);
					} else {
						debug.write("SMSCSession not reqest nor response => not doing anything.");
					}
				} else {
					timeoutCntr++;
					if (timeoutCntr > 5) {
						debug.write("SMSCSession stoped due to inactivity");
						stop();
					}
				}
			}
		} finally {
			isReceiving = false;
		}
		debug.write("SMSCSession stopping receiver");
		receiver.stop();
		debug.write("SMSCSession exiting PDUProcessor");
		pduProcessor.exit();
		try {
			debug.write("SMSCSession closing connection");
			connection.close();
		} catch (IOException e) {
			event.write(e, "closing SMSCSession's connection.");
		}
		System.out.println("SMSCSession exiting run") ;
		debug.write("SMSCSession exiting run()");
		debug.exit(this);
	}

	/**
	 * Sends a PDU to the client.
	 * @param pdu the PDU to send
	 */
	public void send(PDU pdu) throws IOException, PDUException {
		timeoutCntr = 0;
		debug.write("SMSCSession going to send pdu over transmitter");
		transmitter.send(pdu);
		debug.write("SMSCSession pdu sent over transmitter");
	}

	/**
	 * Sets new PDU processor.
	 * @param pduProcessor the new PDU processor
	 */
	public void setPDUProcessor(PDUProcessor pduProcessor) {
		this.pduProcessor = pduProcessor;
	}


	/**
	 * Sets the timeout for receiving the complete message.
	 * @param timeout the new timeout value
	 */
	public void setReceiveTimeout(long timeout) {
		receiveTimeout = timeout;
	}

	/**
	 * Returns the current setting of receiving timeout.
	 * @return the current timeout value
	 */
	public long getReceiveTimeout() {
		return receiveTimeout;
	}

	/**
	 * Returns the details about the account that is logged in to this session
	 * @return An object representing the account. It is casted to the correct type by the implementation
	 */
	public Object getAccount() {
		return null;
	}

	/**
	 * Set details about the account that is logged in to this session 
	 * @param account An object representing the account. It is casted to the correct type by the implementation
	 */
	public void setAccount(Object account) {
	}

	/**
	 * @return Returns the isReceiving.
	 */
	public boolean isReceiving() {
		return isReceiving;
	}

	/**
	 * @param isReceiving The isReceiving to set.
	 */
	public void setReceiving(boolean isReceiving) {
		this.isReceiving = isReceiving;
	}

	public Connection getConnection() {
		return connection;
	}

	public boolean isOpen() {
		return connection != null && connection.isOpened() ;
	}


	public void close() throws IOException {
		stop() ;
		if ( connection != null ) connection.close() ;
	}
	
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2003/09/30 09:17:49  sverkera
 * Created an interface for SMSCListener and SMSCSession and implementations of them  so that it is possible to provide other implementations of these classes.
 *
 * Revision 1.1  2003/07/23 00:28:39  sverkera
 * Imported
 *
 */
