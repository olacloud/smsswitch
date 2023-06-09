package cobbe.smsswitch.smpp;


import java.io.IOException;

import org.smpp.Data;
import org.smpp.pdu.Address;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.DeliverSM;
import org.smpp.pdu.DeliverSMResp;
import org.smpp.pdu.EnquireLink;
import org.smpp.pdu.EnquireLinkResp;
import org.smpp.pdu.PDUException;
import org.smpp.pdu.ReplaceSM;
import org.smpp.pdu.Request;
import org.smpp.pdu.Response;
import org.smpp.pdu.SubmitMultiSM;
import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.SubmitSMResp;
import org.smpp.pdu.WrongLengthOfStringException;
import org.smpp.SmppException ;

public class PDUProcessor  {
protected boolean running = false ;
protected SMSCSession session = null;
protected String username = null , password = null , ip = null ;

public PDUProcessor(SMSCSession session) {
this.session = session;
running = true ;
}

protected boolean isOpen() {
return session != null && session.isOpen() ;
}

protected  void clientRequest(Request request) {
//print("got request" + request.debugString()) ;
try {
switch (request.getCommandId()) {
case Data.BIND_TRANSMITTER : 
case Data.BIND_RECEIVER : 
case Data.BIND_TRANSCEIVER : bind((BindRequest)request,request.getCommandId()); break ;
case Data.SUBMIT_SM : checkUsername() ; submitSM((SubmitSM)request); break ;
case Data.SUBMIT_MULTI : checkUsername() ; submitMultiSM((SubmitMultiSM)request); break ;
case Data.DELIVER_SM : checkUsername() ; deliverSM((DeliverSM)request); break ;
case Data.REPLACE_SM : checkUsername() ; replaceSM((ReplaceSM)request); break ;
case Data.ENQUIRE_LINK : enquireLink((EnquireLink)request) ; break ;
default : ;
}
} catch (Exception e ) {e.printStackTrace() ; }
}

protected  void clientResponse(Response response) {
//print("got response" + response.debugString()) ;
try {
switch (response.getCommandId()) {
case Data.SUBMIT_SM_RESP : checkUsername() ; submitSMResp((SubmitSMResp)response); break ;
default : ;
}
} catch (Exception e ) {e.printStackTrace() ; }
}

private void checkUsername() throws IOException {
if ( username == null ) throw new IOException("No username") ;
}

protected  void exit() {
print("exit not done") ;
}

public void close() throws IOException {
if ( session != null ) session.close() ; 
}

protected void submitMultiSM(SubmitMultiSM submitSm) throws IOException, PDUException {
print("submit multi sm not done") ;
}

protected void submitSM(SubmitSM submitSm) throws IOException, SmppException {
print("submit sm not done") ;
}

protected void submitSMResp(SubmitSMResp submitSmResp) throws IOException, PDUException {
print("submit smresp not done") ;
}

protected void replaceSM(ReplaceSM replaceSm) throws IOException, PDUException {
print("replacesm not done") ;
}

protected void deliverSM(DeliverSM deliverSm) throws IOException, PDUException {
DeliverSMResp response = (DeliverSMResp)deliverSm.getResponse();
response.setMessageId(getMessageId(deliverSm.getShortMessage())) ;
response.setSequenceNumber(deliverSm.getSequenceNumber()) ;
//print("sending default deliver sm " + deliverSm.getSequenceNumber() + "server response: " + response.debugString() );
session.send(response);
}

protected void bind(BindRequest bindRequest,int commandId) throws WrongLengthOfStringException , IOException, PDUException {
print("binding not done") ;
}

protected void enquireLink(EnquireLink enquireLink) throws IOException, PDUException {
EnquireLinkResp enquireLinkResponse = (EnquireLinkResp)enquireLink.getResponse() ;
//print("Enquire response " + enquireLinkResponse.debugString()) ;
session.send(enquireLinkResponse) ;
}

protected void acceptBind(BindRequest bindRequest,String ip) throws WrongLengthOfStringException , IOException, PDUException {
BindResponse bindResponse = (BindResponse) bindRequest.getResponse();
bindResponse.setSystemId("sys");
serverResponse(bindResponse);
this.username = bindRequest.getSystemId() ;
this.password = bindRequest.getPassword()  ;
this.ip = ip ;
print(username + " is bound successfully") ;
}

protected void reject(Request request,int commandStatus) throws IOException {
reject(request,commandStatus,true) ;
}

protected void rejectNoClose(Request request,int commandStatus) throws IOException {
reject(request,commandStatus,false) ;
}

protected void reject(Request request,int commandStatus,boolean close) throws IOException {
Response response = request.getResponse() ;
response.setCommandStatus(commandStatus);
try { serverResponse(response); } catch (Exception e ) {e.printStackTrace() ; }
if ( close ) session.close();
}

protected void serverResponse(Response response) throws IOException {
//print("serverResponse " + response.debugString());
try { session.send(response); } catch (PDUException pdu ) {pdu.printStackTrace() ; new IOException(pdu.getMessage()) ; }
}

protected void sendSubmitSmResp(int sequence,String messageId) {
try {
SubmitSMResp submitResponse = new SubmitSMResp() ;
submitResponse.setSequenceNumber(sequence) ;
submitResponse.setMessageId(messageId) ; 
serverResponse(submitResponse) ;
} catch (PDUException | IOException io) {
io.printStackTrace() ;
}
}

protected void sendSubmitSmResp(int sequence,int error) {
try {
SubmitSMResp submitResponse = new SubmitSMResp() ;
submitResponse.setSequenceNumber(sequence) ;
submitResponse.setCommandStatus(error);
serverResponse(submitResponse) ;
} catch (IOException io) {
io.printStackTrace() ;
}
}

protected String addr(Address a) {
return a == null ? "" : a.getAddress() ;
}

protected static int random(int min,int max) {
return (int)( Math.random() * (max - min + 1) + min ) ;
}

protected static String san(String a) {
return a ;
}

protected String getMessageId(String a) {
if ( a == null ) return "" ;
int k = a.indexOf("id:") ;
if ( k < 0 ) return "" ;
int m = a.indexOf(" ",k) ;
if ( m < k ) return a.substring(k+3) ;
else return a.substring(k+3,m) ;
}

protected int getInteger(String i,int def) {
if ( i != null ) {
try {return Integer.parseInt(i) ; } catch(NumberFormatException nfe) {}
}
return def ;
}

public String error(int x) {
switch ( x ) {
case 0x00000000 : return "DELIVRD" ;
case 0x00000001 : return "Message too long" ;
case 0x00000002 : return "Command length is invalid" ;
case 0x00000003 : return "Command ID is invalid or not supported" ;
case 0x00000004 : return "Incorrect bind status for given command" ;
case 0x00000005 : return "Already bound" ;
case 0x00000006 : return "Invalid Priority Flag" ;
case 0x00000007 : return "Invalid registered delivery flag" ;
case 0x00000008 : return "System error" ;
case 0x0000000A : return "Invalid source address" ;
case 0x0000000B : return "Invalid destination address" ;
case 0x0000000C : return "Message ID is invalid" ;
case 0x0000000D : return "Bind failed" ;
case 0x0000000E : return "Invalid password" ;
case 0x0000000F : return "Invalid System ID" ;
case 0x00000011 : return "Cancelling message failed" ;
case 0x00000013 : return "Message recplacement failed" ;
case 0x00000014 : return "Message queue full" ;
case 0x00000015 : return "Invalid service type" ;
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
return x + "" ;
}

public String print(Object x) {
	System.out.println(x) ;
	return x == null ? null : x.toString() ;
}

}

