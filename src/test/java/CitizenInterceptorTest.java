import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CitizenInterceptorTest {

CitizenInterceptor subj = new CitizenInterceptor();

    @Test
    public void onReceiveTest() {
        Citizen  expected = new Citizen(1,2,3,4);
        String param = "1,2,3,4";
        assertEquals(expected, subj.onReceive(param));
        assertNull(subj.onReceive(null));
        assertNotNull(subj.onReceive("123"));
    }
}
