package benchmark.expr;

import org.noear.solon.core.util.TmplUtil;
import org.noear.solon.expression.context.EnhanceContext;
import org.noear.solon.expression.snel.SnEL;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author noear 2025/6/30 created
 */
public class TmplTest {
    public static void main(String[] args) {
        case1();
        System.out.println("/////////////////////////");
        case2();
    }

    private static void case1() {
        Properties props = new Properties();
        props.setProperty("user", "solon");
        props.setProperty("lang", "java");

        int count = 10_000_000;
        execDo(count, "user", "user", props);
        execDo(count, "${user}", "#{user}", props);
        execDo(count, "${user}_${lang}", "#{user}_#{lang}", props);
        execDo(count, "${user}_${lang}_${lang}", "#{user}_#{lang}_#{lang}", props);
    }

    private static void case2() {
        Map<String, Object> model = new HashMap<>();
        model.put("user", "noear");
        model.put("label", 1);
        model.put("", model);

        int count = 10_000_000;
        execDo(count, "user=${user}", "user=#{user}", model);
    }


    private static void execDo(int count, String tmpl, String tmpl2, Map props) {
        System.out.println("----------------------------");
        System.out.println("tmpl: " + tmpl);
        System.out.println("----------------------------");

        System.out.println("TmplUtil:" + (TmplUtil.parse(tmpl, props)));
        System.out.println("SnEL:" + (SnEL.evalTmpl(tmpl2, new EnhanceContext(props))));

        for (int i = 0; i < 10; i++) {
            TmplUtil.parse(tmpl, props);
            SnEL.evalTmpl(tmpl2, new EnhanceContext(props));
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            TmplUtil.parse(tmpl, props);
        }
        long span = System.currentTimeMillis() - start;
        System.out.println("TmplUtil:" + span);


        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            SnEL.evalTmpl(tmpl2, new EnhanceContext(props));
        }
        span = System.currentTimeMillis() - start;
        System.out.println("SnEL:" + span);
    }
}
