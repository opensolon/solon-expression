package features.expr;

import org.junit.jupiter.api.Test;
import org.noear.solon.expression.snel.SnEL;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SnTmplPropsTest {
    @Test
    public void case1() {
        String template = "Hello, ${user.name}!";

        Map<String, Object> context = new HashMap<>();
        context.put("user.name", "Tom");

        String result = SnEL.evalTmpl(template, context);
        assertEquals("Hello, Tom!", result);
    }

    @Test
    public void case2() {
        String template = "Hello, ${user.name:Tom}!";

        Map<String, Object> context = new HashMap<>();

        String result = SnEL.evalTmpl(template, context);
        assertEquals("Hello, Tom!", result);
    }

    @Test
    public void case3() {
        String template = "Hello, ${user.name?:Tom}!";

        Map<String, Object> context = new HashMap<>();

        String result = SnEL.evalTmpl(template, context);
        assertEquals("Hello, Tom!", result);
    }
}
