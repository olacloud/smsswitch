package cobbe.smsswitch.model ;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import static cobbe.smsswitch.util.Utilities.UUID ;
import org.smpp.util.ByteBuffer ;
import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.PDUException ;
import org.smpp.util.NotEnoughDataInByteBufferException ;
import static cobbe.smsswitch.util.Utilities.getAddress ;

@ToString
public class UDHI implements Serializable {

@Getter
private String id ;

@Getter
private String messageId ;

@Getter
private int partNumber ;

@Getter
private byte[] message ;

@Getter
int totalParts ;

public UDHI(SubmitSM submitSm) throws PDUException , NotEnoughDataInByteBufferException {
ByteBuffer shortMessage = submitSm.getShortMessageData() ;
int y = 0 ;
if ( ( y = shortMessage.removeByte() ) != 5 ) throw new PDUException("First Byte (length) should be 5 [" + y + "]") ;
if ( ( y = shortMessage.removeByte() ) != 0 ) throw new PDUException("Not UDHI Message [" + y + "]") ;
if ( ( y = shortMessage.removeByte() ) != 3 ) throw new PDUException("Not UDHI Message [" + y + "]") ;
int id_ = shortMessage.removeByte() ;
this.totalParts = shortMessage.removeByte() ;
this.partNumber = shortMessage.removeByte() ;
this.id = getAddress(submitSm.getDestAddr()) + getAddress(submitSm.getSourceAddr()) + id_ ;
this.messageId = UUID() ;
this.message = shortMessage.getBuffer() ;
}


public UDHI reset(byte[] message) {
this.id = null ;
this.messageId = null ;
this.message = message ;
return this ;
}

}