package cobbe.smsswitch.util ;
import java.util.Optional ;
import java.util.UUID ;
import org.smpp.pdu.Address ;

public class Utilities {

public static String getAddress(Address addr) {
return addr == null ? "" : addr.getAddress() ;
}

public static String UUID() {
return UUID.randomUUID().toString() ;
}

}