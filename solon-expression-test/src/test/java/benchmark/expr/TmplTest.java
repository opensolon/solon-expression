package benchmark.expr;

import org.noear.solon.core.util.TmplUtil;
import org.noear.solon.expression.context.StandardContext;
import org.noear.solon.expression.snel.SnEL;

import java.util.Map;
import java.util.Properties;

/**
 * @author noear 2025/6/30 created
 */
public class TmplTest {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("user", "solon");
        props.setProperty("lang", "java");

        int count = 10_000_000;
        case1(count, "user", props);
        case1(count, "${user}", props);
        case1(count, "${user:noear}", props);
        case1(count, "${user}_${lang}", props);
    }

    private static void case1(int count, String tmpl, Properties props) {
        System.out.println("----------------------------");
        System.out.println("tmpl: " + tmpl);
        System.out.println("----------------------------");

        System.out.println("TmplUtil:" + (TmplUtil.parse(tmpl, (Map) props)));
        System.out.println("SnEL:" + (SnEL.evalTmpl(tmpl, new StandardContext(null, props))));

        for (int i = 0; i < 10; i++) {
            TmplUtil.parse(tmpl, (Map) props);
            SnEL.evalTmpl(tmpl, new StandardContext(null, props));
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            TmplUtil.parse(tmpl, (Map) props);
        }
        long span = System.currentTimeMillis() - start;
        System.out.println("TmplUtil:" + span);


        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            SnEL.evalTmpl(tmpl, new StandardContext(null, props));
        }
        span = System.currentTimeMillis() - start;
        System.out.println("SnEL:" + span);
    }
}
