package cobbe.smsswitch.smpp;

import java.io.IOException;
import java.io.InterruptedIOException;

import org.smpp.Connection;
import org.smpp.SmppObject;
import org.smpp.TCPIPConnection;

public class APPServer extends SmppObject implements Runnable {
	public static final int DSIM = 1; //16;
	public static final int DSIMD = 1;//17;
	public static final int DSIMD2 = 1; //18;


	private TCPIPConnection serverConn = null;
	private int port;
	private long acceptTimeout = org.smpp.Data.ACCEPT_TIMEOUT;
	private boolean keepReceiving = true;
	private boolean isReceiving = false;
	private boolean asynchronous = false;
	private SMPP smpp = null ;

	public APPServer(int port,SMPP smpp,boolean asynchronous) throws IOException {
		this.port = port;
		this.smpp = smpp ;
		this.asynchronous = asynchronous;
		start() ;
	}

	public synchronized void start() throws IOException {
		print("going to start SMSCListener on port " + port);
		if (!isReceiving) {
			serverConn = new org.smpp.TCPIPConnection(port);
			serverConn.setReceiveTimeout(getAcceptTimeout());
			serverConn.open();
			keepReceiving = true;
			if (asynchronous) {
				print("starting listener in separate thread.");
				Thread serverThread = new Thread(this);
				serverThread.start();
				print("listener started in separate thread.");
			} else {
				print("going to listen in the context of current thread.");
				run();
			}
		} else {
			print("already receiving, not starting the listener.");
		}
	}

	public synchronized void stop() throws IOException {
		print("going to stop SMSCListener on port " + port);
		keepReceiving = false;
		while (isReceiving) {
			Thread.yield();
		}
		serverConn.close();
		print("SMSCListener stopped on port " + port);
	}

	public void run() {
		print("run of SMSCListener on port " + port);
		isReceiving = true;
		try {
			while (keepReceiving) {
				listen();
				Thread.yield();
			}
		} finally {
			isReceiving = false;
		}
		debug.exit(this);
	}


	private void listen() {
//		print("SMSCListener listening on port " + port);
		try {
			Connection connection = null;
			serverConn.setReceiveTimeout(getAcceptTimeout());
			connection = serverConn.accept();

			if (connection != null) {
				print("SMSCListener accepted a connection from " + connection.getAddress());
				SMSCSession session = new SMSCSession((TCPIPConnection)connection);
				session.setPDUProcessor(smpp.pduProcessor(session));
				Thread thread = new Thread(session);
				thread.start();
//				print("SMSCListener launched a session on the accepted connection.");
			} else {
				print("no connection accepted this time.");
			}
		} catch (InterruptedIOException e) {
			// thrown when the timeout expires => it's ok, we just didn't
			// receive anything
			print("InterruptedIOException accepting, timeout? -> " + e);
		} catch (Exception e) {
			// accept can throw this from various reasons
			// and we don't want to continue then (?)
			print("IOException accepting connectionx" + e) ;
			event.write(e, "IOException accepting connection");
			keepReceiving = false;
		}
		debug.exit(DSIMD2, this);
	}




	public void setAcceptTimeout(int value) {
		acceptTimeout = value;
	}

	public long getAcceptTimeout() {
		return acceptTimeout;
	}

	private void print(String a) {
	System.out.println(a) ;
	}

}
