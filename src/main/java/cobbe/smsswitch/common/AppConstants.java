package cobbe.smsswitch.common;

public class AppConstants 
{

//    	@Value(value = "${spring.kafka.consumer.bootstrap-servers}")
	public static String bootstrapAddress = "localhost:9092" ;
	public static final String TOPIC_BIND_NAME = "BINDS";
	public static final String TOPIC_LOGIN_USER = "LOGIN_USER";
	public static final String GROUP_ID = "group_id";
}
