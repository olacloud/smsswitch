package cobbe.smsswitch ;

import lombok.extern.slf4j.Slf4j;
import java.util.List ;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import cobbe.smsswitch.service.KafKaProducerService ;
import cobbe.smsswitch.service.UDHIService ;

import cobbe.smsswitch.smpp.APPServer;
import cobbe.smsswitch.smpp.PDUProcessor;
import cobbe.smsswitch.smpp.SMPP;
import cobbe.smsswitch.client.SMPPClient;
import cobbe.smsswitch.smpp.SMSCSession;

import cobbe.smsswitch.queue.ExpiringMap ;
import cobbe.smsswitch.queue.ExpirationListener ;
import cobbe.smsswitch.model.UDHI ;

@Slf4j
@SpringBootApplication
public class Application {

//	@Autowired
//	private KafKaProducerService producerService;
	@Autowired
	private UDHIService UDHIService ;

	public static void main(String[] args) {
		System.out.println("VERSION 1.0.14032023") ;
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner run() throws Exception {
		return args -> { 

		new APPServer(8000,new SMPP() {
			public PDUProcessor pduProcessor(SMSCSession session) {
			return new SMPPClient(session,UDHIService) ;
			}
			},true) ;

				};
	}

	@Bean
	public ExpiringMap<String, List<UDHI>> uDHIQueue() throws Exception {
		ExpiringMap<String, List<UDHI>> uDHIQueue = new ExpiringMap<String, List<UDHI>>(120,ExpiringMap.DEFAULT_EXPIRATION_INTERVAL) ;
		uDHIQueue.addExpirationListener(new ExpirationListener<List<UDHI>>() {
			@Override
			public void expired(List<UDHI> expiredObject) {
				log.info("UDHI has expired " + expiredObject) ;
			}
		}) ;
		return uDHIQueue ;		
	}
	

}
