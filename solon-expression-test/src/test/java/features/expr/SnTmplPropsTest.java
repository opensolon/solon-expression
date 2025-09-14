package features.expr;

import org.junit.jupiter.api.Test;
import org.noear.solon.expression.context.StandardContext;
import org.noear.solon.expression.snel.SnEL;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SnTmplPropsTest {
    @Test
    public void case11() {
        String template = "Hello, ${user.name}!";

        Map<String, Object> context = new HashMap<>();
        context.put("user.name", "Tom");

        String result = SnEL.evalTmpl(template, context);
        assertEquals("Hello, Tom!", result);
    }

    @Test
    public void case12() {
        String template = "Hello, ${user.name:Tom}!";

        Map<String, Object> context = new HashMap<>();

        String result = SnEL.evalTmpl(template, context);
        assertEquals("Hello, Tom!", result);


        result = SnEL.evalTmpl("${user.name:}");
        assertEquals("", result);


        result = SnEL.evalTmpl("${user.name}");
        assertEquals("", result);

        result = SnEL.evalTmpl("${user.name:a}");
        assertEquals("a", result);
    }


    @Test
    public void case13() {
        String template = "Hello, ${user.name?:Ddd}!"; //不支持 `?:` 所以是 ddd

        Map<String, Object> context = new HashMap<>();
        context.put("user.name", "Tom");

        String result = SnEL.evalTmpl(template, context);
        assertEquals("Hello, Ddd!", result);
    }


    @Test
    public void case21() {
        String template = "Hello, #{${user.name:Tom}}!";

        Map<String, Object> context = new HashMap<>();

        String result = SnEL.evalTmpl(template, context);
        assertEquals("Hello, Tom!", result);
    }

    @Test
    public void case22() {
        String template = "#{${test:Hello, }}#{${user.name:Tom}}!";

        Map<String, Object> context = new HashMap<>();

        String result = SnEL.evalTmpl(template, context);
        assertEquals("Hello, Tom!", result);
    }

    @Test
    public void case23() {
        StandardContext context = new StandardContext(null).isReturnNull(true);

        String result = SnEL.evalTmpl("${user}", context);
        assertNull(result);
    }
}
