package cobbe.smsswitch.client ;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional ;

import org.smpp.Data;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.DeliverSM;
import org.smpp.pdu.PDUException;
import org.smpp.SmppException ;
import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.SubmitSMResp;
import org.smpp.pdu.WrongLengthOfStringException;

import static cobbe.smsswitch.util.Utilities.UUID ;
import cobbe.smsswitch.smpp.PDUProcessor ;
import cobbe.smsswitch.smpp.SMSCSession ;
import cobbe.smsswitch.model.LoginUser ;
import cobbe.smsswitch.common.AppConstants ;
import cobbe.smsswitch.model.UDHI ;
import cobbe.smsswitch.model.SMS ;
import cobbe.smsswitch.model.Message ;
import cobbe.smsswitch.service.UDHIService ;
import cobbe.smsswitch.model.UDHI ;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class SMPPClient extends PDUProcessor {

	private String id = null ;
	private UDHIService UDHIService ;

	public SMPPClient(SMSCSession session,UDHIService UDHIService) {
		super(session) ;
		this.UDHIService = UDHIService ;
		id = UUID() ;
	}

	@Override
	protected void bind(BindRequest bindRequest,int commandId) throws WrongLengthOfStringException , IOException, PDUException {
	String username = bindRequest.getSystemId() , password = bindRequest.getPassword() , ip = session.getConnection().getAddress() ;
	log.info(this.username + " - Bindx for session Username " + username + " , Password " + password + " from " + ip);
	acceptBind(bindRequest,ip) ;
	}

	@Override
	protected void submitSM(SubmitSM submitSm) throws IOException, SmppException {
		log.info("Submit SM received -> " + submitSm) ;
		if ( UDHIService.isUDHI(submitSm) ) {
			UDHI udhi = UDHIService.getUDHI(submitSm) ;
			if ( udhi.getMessageId() != null ) sendSubmitSmResp(submitSm.getSequenceNumber(),udhi.getMessageId()) ;
			else new SMS(username,password,addr(submitSm.getSourceAddr()),addr(submitSm.getDestAddr()),ip,new Message(udhi.getMessage(),submitSm.getDataCoding())) ;
		} else new SMS(username,password,addr(submitSm.getSourceAddr()),addr(submitSm.getDestAddr()),ip,new Message(submitSm.getShortMessageData().getBuffer(),submitSm.getDataCoding())) ;		
	}


}
