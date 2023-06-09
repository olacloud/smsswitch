package cobbe.smsswitch.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import cobbe.smsswitch.common.AppConstants;

@Slf4j
@Service
public class KafKaProducerService 
{
	
	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	public void sendMessage(String topic,String message) 
	{
		log.info(String.format("Saving message -> %s", message));
		this.kafkaTemplate.send(topic, message);
	}
	

}
