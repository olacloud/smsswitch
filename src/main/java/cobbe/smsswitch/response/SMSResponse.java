package cobbe.smsswitch.response ;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor ;
import static cobbe.smsswitch.util.Utilities.UUID ;

public class SMSResponse {

@Getter
private String id ;

@Getter @Setter
private int error = -1 ;

protected SMSResponse() {
id = UUID() ;
}

}