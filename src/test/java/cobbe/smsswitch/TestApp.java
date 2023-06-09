package cobbe.smsswitch;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll ;
import cobbe.smsswitch.gateway.SMPPGateway ;
import org.springframework.boot.test.context.SpringBootTest;
import org.smpp.util.ByteBuffer ;


@SpringBootTest
public class TestApp
{
    private static SMPPGateway gateway ;
    @BeforeAll
    public static void createGateway() {
	gateway = new SMPPGateway("localhost",8000,"test","test") ;
    }

    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( gateway.bind() );
    }

    @Test
    public void shouldAnswerWith() throws Exception 
    {
	String bb = "Ecstatic advanced and procured civility not absolute put continue.Ecstatic advanced and procured civility not absolute put continue.Ecstatic advanced and procured civility not absolute put continue.Ecstatic advanced and procured civility not absolute put continue." ;
	gateway.submitMessage(1,"1234","7890",new ByteBuffer(bb.getBytes()),(byte)0,(byte)0,"") ;
	Thread.sleep(30000) ;
        //assertTrue( gateway.bind() );
    }
}
