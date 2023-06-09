package cobbe.smsswitch.model ;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

@Slf4j
public class Message {

@Getter
private String message ;

@Getter
private byte[] message_ ;

@Getter
private byte datacoding ;

public Message(String message,byte datacoding) {
this.message = message ;
this.datacoding = datacoding ;
}

public Message(byte[] message_,byte datacoding) {
this.message_ = message_ ;
this.datacoding = datacoding ;
log.info("Message is " + new String(message_) ) ;
}

}