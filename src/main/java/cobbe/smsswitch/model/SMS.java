package cobbe.smsswitch.model ;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor ;

@AllArgsConstructor
public class SMS {

@Getter
private String username ,
		password ,
		sourceNumber ,
		destinationNumber ,
		sourceIp ;

@Getter
private Message message ;

}