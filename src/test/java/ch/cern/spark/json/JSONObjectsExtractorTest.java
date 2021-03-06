package ch.cern.spark.json;

import org.junit.Assert;
import org.junit.Test;

public class JSONObjectsExtractorTest {

    @Test
    public void extractElement(){
        
        String json = "{\"data\":{\"service_name\":\"timtest_s.cern.ch\",\"return_code\":0}}";
    
        try {
            JSONObject listenerEvent = new JSONObjectsExtractor("data").call(new JSONObject.Parser().parse(json.getBytes()));
            
            Assert.assertNotNull(listenerEvent.getProperty("service_name"));
            Assert.assertEquals("timtest_s.cern.ch", listenerEvent.getProperty("service_name"));
            
            Assert.assertNotNull(listenerEvent.getProperty("return_code"));
            Assert.assertEquals("0", listenerEvent.getProperty("return_code"));
    
        } catch (Exception e) {
            e.printStackTrace();
            
            Assert.fail();
        }
        
    }
    
}
