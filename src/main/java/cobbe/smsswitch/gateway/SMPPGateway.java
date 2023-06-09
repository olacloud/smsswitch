package cobbe.smsswitch.gateway ;

import java.io.IOException ;

import lombok.extern.slf4j.Slf4j;

import org.smpp.Data;
import org.smpp.ServerPDUEvent;
import org.smpp.ServerPDUEventListener;
import org.smpp.Session;
import org.smpp.SmppObject;
import org.smpp.TCPIPConnection;
import org.smpp.pdu.AddressRange;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.BindTransciever;
import org.smpp.pdu.DeliverSM;
import org.smpp.pdu.DeliverSMResp;
import org.smpp.pdu.EnquireLink;
import org.smpp.pdu.EnquireLinkResp;
import org.smpp.pdu.PDU;
import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.SubmitSMResp;
import org.smpp.util.ByteBuffer;
import org.smpp.util.NotEnoughDataInByteBufferException ;
import org.smpp.util.TerminatingZeroNotFoundException ;
import org.smpp.WrongSessionStateException ;
import org.smpp.pdu.ValueNotSetException ;
import org.smpp.pdu.WrongDateFormatException ;
import org.smpp.pdu.WrongLengthOfStringException ;
import org.smpp.TimeoutException ;
import org.smpp.pdu.PDUException ;

@Slf4j
public class SMPPGateway {

	final String host ,
		username ,
		password ;
	final int port ;
	private Session session = null ;

	public SMPPGateway(String host,int port,String username,String password) {
		this.host = host ;
		this.port = port ;
		this.username = username ;
		this.password = password ;
	}
	
	public boolean bind() {
		if ( bound() ) return true ;
		log.info("Binding to " + host + ":" +  port + " [username:" + username + ",password:" + password + "]" ) ;
		try {
		BindTransciever bindRequest = new BindTransciever();
		TCPIPConnection connection = new TCPIPConnection(host,port);
		connection.setReceiveTimeout(30000);
		session = new Session(connection);
		AddressRange addressRange = new AddressRange((byte)0,(byte)0,Data.DFLT_ADDR_RANGE);
		bindRequest.setSystemId(username);
		bindRequest.setPassword(password);
		bindRequest.setSystemType("");
		bindRequest.setInterfaceVersion((byte) 0x34);
		bindRequest.setAddressRange(addressRange);
		log.info("Bind request " + bindRequest.debugString());			
		BindResponse response = session.bind(bindRequest, new SMPPPDUEventListener(session)) ;
		if ( response == null ) {
			log.info("No response from host " + host + ":" + port ) ;
			return false ;
		}
		log.info("Bind response " + response.debugString());
		int error = response.getCommandStatus() ;
		if ( error == Data.ESME_ROK) {
			return true ;
		}
		log.info("Didn't bind because " + error(error) ) ;
		return false ;
		} catch (Exception e) {
			//e.printStackTrace();
			log.info(e.getMessage()) ;		}
		return false ;
	}

public final void submitMessage(int sequence,String sourceAddr,String destinationAddr,ByteBuffer shortMessage,byte dc_,byte mc,String serviceType) throws TerminatingZeroNotFoundException , NotEnoughDataInByteBufferException , IOException ,  WrongSessionStateException , 	PDUException , ValueNotSetException , WrongDateFormatException , WrongLengthOfStringException , TimeoutException {
if ( shortMessage.length() <= 160 ) shortMessage(sequence,sourceAddr,destinationAddr,shortMessage,dc_,mc,serviceType) ;
else longMessage(sequence,sourceAddr,destinationAddr,shortMessage,dc_,mc,serviceType) ;
}

public void longMessage(int sequence,String sourceAddr,String destinationAddr,ByteBuffer shortMessage,byte dc_,byte mc,String serviceType) throws WrongSessionStateException , IOException , TimeoutException , TerminatingZeroNotFoundException , NotEnoughDataInByteBufferException , ValueNotSetException , WrongDateFormatException , WrongLengthOfStringException , PDUException {				
log.info("Message greater than 160 characters doing long message") ;
SubmitSM request = new SubmitSM();			
request.setServiceType(serviceType);
request.setSourceAddr(sourceAddr);
request.setDestAddr(destinationAddr);
request.setReplaceIfPresentFlag((byte)0);
request.setScheduleDeliveryTime("");
request.setValidityPeriod("");
byte esm = (byte)(mc | (1 << 6));
request.setEsmClass(esm);
request.setProtocolId((byte)0);
request.setPriorityFlag((byte)0);
request.setRegisteredDelivery((byte)1);
request.setDataCoding(dc_);
request.setSmDefaultMsgId((byte)0);
request.assignSequenceNumber(true);
UDHI_Gateway udhi = new UDHI_Gateway (shortMessage,dc_) ;
while ( udhi.nextMessage(request,sequence) ) submit(request) ;			
}

public void shortMessage(int sequence,String sourceAddr,String destinationAddr,ByteBuffer shortMessage,byte dc_,byte mc,String serviceType) throws TerminatingZeroNotFoundException , NotEnoughDataInByteBufferException , IOException ,  WrongSessionStateException , 	PDUException , ValueNotSetException , WrongDateFormatException , WrongLengthOfStringException , TimeoutException {		
//print("Message less than or equals to 160 characters doing short message") ;
SubmitSM request = new SubmitSM();			
request.setServiceType(serviceType);
request.setSourceAddr(sourceAddr);
request.setDestAddr(destinationAddr);
request.setReplaceIfPresentFlag((byte)0);
request.setScheduleDeliveryTime("");
request.setValidityPeriod("");
request.setEsmClass((byte)mc);
request.setProtocolId((byte)0);
request.setPriorityFlag((byte)0);
request.setRegisteredDelivery((byte)1);
request.setDataCoding(dc_);
request.setSmDefaultMsgId((byte)0);
request.assignSequenceNumber(true);
request.setSequenceNumber(sequence) ;
request.setShortMessageData(shortMessage) ;
//print("Submit request " + request.debugString());
submit(request) ;			
}

public void submit(SubmitSM request) throws TimeoutException , IOException , WrongSessionStateException , PDUException {
if ( session != null ) session.submit(request) ;
}
	public boolean bound() {
		return session != null && session.isOpened() && session.isBound() ;
	}

	
	public void close() {
		try {
			if ( session != null ) session.close() ;
			session = null ;
		} catch(Exception e) {
			e.printStackTrace(); 
		}
	}

	
	class SMPPPDUEventListener extends SmppObject implements ServerPDUEventListener {
		Session session;

		public SMPPPDUEventListener(Session session) {
		this.session = session;
		}
		
		@Override
		public void handleEvent(ServerPDUEvent event) {
			try {
				PDU pdu = event.getPDU();
				log.info("Got PDU " + pdu.debugString()) ;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			}

			
	}


	
	public String error(int x) {
		switch ( x ) {
		case 0x00000000 : return "No Error" ;
		case 0x00000001 : return "Message too long" ;
		case 0x00000002 : return "Command length is invalid" ;
		case 0x00000003 : return "Command ID is invalid or not supported" ;
		case 0x00000004 : return "Incorrect bind status for given command" ;
		case 0x00000005 : return "Already bound" ;
		case 0x00000006 : return "Invalid Priority Flag" ;
		case 0x00000007 : return "Invalid registered delivery flag" ;
		case 0x00000008 : return "System error" ;
		case 0x00000009 : return "GENERIC FAILURE" ;
		case 0x0000000A : return "Invalid source address" ;
		case 0x0000000B : return "Invalid destination address" ;
		case 0x0000000C : return "Message ID is invalid" ;
		case 0x0000000D : return "Bind failed" ;
		case 0x0000000E : return "Invalid password" ;
		case 0x0000000F : return "Invalid System ID" ;
		case 0x00000010 : return "RADIO OFF" ;
		case 0x00000011 : return "Cancelling message failed" ;
		case 0x00000012 : return "NULL PDU" ;
		case 0x00000013 : return "Message recplacement failed" ;
		case 0x00000014 : return "Message queue full" ;
		case 0x00000015 : return "Invalid service type" ;
		case 0x00000016 : return "NO SERVICE" ;
		case 0x00000017 : return "ERROR 0" ;
		case 0x0000001A : return "ReRoute Timeout" ;
		case 0x00000033 : return "Invalid number of destinations" ;
		case 0x00000034 : return "Invalid distribution list name" ;
		case 0x00000040 : return "Invalid destination flag" ;
		case 0x00000042 : return "Invalid submit with replace request" ;
		case 0x00000043 : return "Invalid esm class set" ;
		case 0x00000044 : return "Invalid submit to ditribution list" ;
		case 0x00000045 : return "Submitting message has failed" ;
		case 0x00000048 : return "Invalid source address type of number ( TON )" ;
		case 0x00000049 : return "Invalid source address numbering plan ( NPI )" ;
		case 0x00000050 : return "Invalid destination address type of number ( TON )" ;
		case 0x00000051 : return "Invalid destination address numbering plan ( NPI )" ;
		case 0x00000053 : return "Invalid system type" ;
		case 0x00000054 : return "Invalid replace_if_present flag" ;
		case 0x00000055 : return "Invalid number of messages" ;
		case 0x00000058 : return "Throttling error" ;
		case 0x00000061 : return "Invalid scheduled delivery time" ;
		case 0x00000062 : return "Invalid Validty Period value" ;
		case 0x00000063 : return "Predefined message not found" ;
		case 0x00000064 : return "ESME Receiver temporary error" ;
		case 0x00000065 : return "ESME Receiver permanent error" ;
		case 0x00000066 : return "ESME Receiver reject message error" ;
		case 0x00000067 : return "Message query request failed" ;
		case 0x000000C0 : return "Error in the optional part of the PDU body" ;
		case 0x000000C1 : return "TLV not allowed" ;
		case 0x000000C2 : return "Invalid parameter length" ;
		case 0x000000C3 : return "Expected TLV missing" ;
		case 0x000000C4 : return "Invalid TLV value" ;
		case 0x000000FE : return "Transaction delivery failure" ;
		case 0x000000FF : return "Unknown error" ;
		case 0x00000100 : return "ESME not authorised to use specified servicetype" ;
		case 0x00000101 : return "ESME prohibited from using specified operation" ;
		case 0x00000102 : return "Specified servicetype is unavailable" ;
		case 0x00000103 : return "Specified servicetype is denied" ;
		case 0x00000104 : return "Invalid data coding scheme" ;
		case 0x00000105 : return "Invalid source address subunit" ;
		case 0x00000106 : return "Invalid destination address subunit" ;
		case 0x0000040B : return "Insufficient credits to send message" ;
		}
		return "Error : " + x ;
		}
		
	@Override
	public String toString() {
		return username ;
	}
	
}


class UDHI_Gateway {
static byte id = 0 ;
byte	length = 5 ,
	subHeaderLength = 3,
	partNumber ,
	totalParts ;
int	eachMessageLength = 153 ;
ByteBuffer message ;

public UDHI_Gateway(ByteBuffer msg,byte dc_) throws NotEnoughDataInByteBufferException {
id ++ ;
if ( dc_ == 8 ) eachMessageLength = 134 ;
this.message = msg.readBytes(msg.length()) ;
totalParts = (byte) ceil(message.length(),eachMessageLength) ;
partNumber = 0 ;
}

public boolean nextMessage(SubmitSM request,int sequence) throws NotEnoughDataInByteBufferException , PDUException , TerminatingZeroNotFoundException {
if ( partNumber >= totalParts ) return false ;
partNumber ++ ;
int l = message.length() ;
ByteBuffer m = new ByteBuffer() ;
m.appendBytes(new byte[]{length,0,subHeaderLength,id,totalParts,partNumber});
m.appendBuffer( message.removeBytes( l >= eachMessageLength ? eachMessageLength : l ) );
request.setSequenceNumber( partNumber == totalParts ? sequence : sequence + 10000 + partNumber) ;
request.setShortMessageData(m);
//System.out.println(partNumber + "Submit request " + request.debugString());
return true ;
}

public static int ceil(int a,int b) {
return a / b + ((a % b == 0) ? 0 : 1); 
}

}

