
package selenium.tests;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.GET;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.routeAndCall;
import org.junit.Test;
import play.mvc.Content;
import play.mvc.Result;

/**
 * SimpleTest.
 * @author vladutb
 * @version 1.0
 * @since 21.02.2013
 */
public class SimpleTest {
    /**
     * 
     */
    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertThat(a).isEqualTo(2);
    }
    /**
     * 
     */
    @Test
    public void renderTemplate() {
        Content html = views.html.index.render("Welcome");
        assertThat(contentType(html)).isEqualTo("text/html");
        assertThat(contentAsString(html)).contains("Welcome");
    }
    /**
     * 
     */
    @Test
    public void badRoute() {
        Result result = routeAndCall(fakeRequest(GET, "/xx/Kiki"));
        assertThat(result).isNull();
    }
}