package cobbe.smsswitch.model ;

import lombok.Data ;
import lombok.AllArgsConstructor ;

@Data
@AllArgsConstructor
public class LoginUser {

private String username,
		password ,
		ip ;

}