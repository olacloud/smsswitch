package cobbe.smsswitch.service;

import java.util.List ;
import java.util.ArrayList ;
import java.util.Optional ;
import lombok.extern.slf4j.Slf4j;


import org.springframework.context.annotation.Lazy ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.smpp.pdu.SubmitSM;
import org.smpp.util.ByteBuffer ;
import org.smpp.util.NotEnoughDataInByteBufferException ;
import org.smpp.pdu.PDUException ;

import cobbe.smsswitch.model.UDHI ;
import cobbe.smsswitch.queue.ExpiringMap ;

@Slf4j
@Service
public class UDHIService {

	@Lazy
	@Autowired
	ExpiringMap<String, List<UDHI>> uDHIQueue ;
	
	public UDHI getUDHI(SubmitSM submitSm) throws PDUException , NotEnoughDataInByteBufferException {
		log.info("doing udhi") ;
		UDHI udhi = new UDHI(submitSm) ;
		List<UDHI> udhis = Optional.ofNullable( uDHIQueue.get(udhi.getId()) ).orElse( new ArrayList<UDHI>() ) ;
		udhis.add(udhi) ;
		if ( udhi.getPartNumber() == udhi.getTotalParts() ) {
			if ( udhis.size() != udhi.getTotalParts() ) throw new PDUException("Could not find the remaining UDHI in data store") ;
			ByteBuffer buffer = new ByteBuffer() ;
			udhis.stream()
			.sorted((p1, p2) -> p1.getPartNumber() - p2.getPartNumber() )
			.forEach( udhix ->  buffer.appendBytes(udhix.getMessage())  ) ;
			return udhi.reset(buffer.getBuffer()) ;
		} else {
			uDHIQueue.put(udhi.getId(),udhis) ;
			return udhi ;
		}
	}


	public boolean isUDHI(SubmitSM submitSm) {
		return (submitSm.getEsmClass() & 0xC0) == 0x40;
	}

}
