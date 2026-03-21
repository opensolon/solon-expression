package features.expr;

import org.junit.jupiter.api.Test;
import org.noear.solon.expression.context.EnhanceContext;
import org.noear.solon.expression.snel.SnEL;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author noear 2026/3/21 created
 *
 */
public class BeanTest {
    @Test
    public void case1() {
        Map<String, Object> user = new HashMap<>();
        user.put("name", "world");
        user.put("age", 20);
        user.put("age2", 10);

        Map<String, Object> order = new HashMap<>();
        order.put("id", 1);
        order.put("user", user);

        Map<String, Object> beans = new HashMap();
        beans.put("order", order);

        Map<String, Object> vars = new HashMap();
        vars.put("a", 1);

        EnhanceContext context = new EnhanceContext(vars);
        context.forBeans(beans::get);

        ///

        Object result = SnEL.eval("@order.user.age == 20 ? true : false", context);
        assert true == (Boolean) result;

        result = SnEL.eval("a + @order.user.age", context);
        assert 21 == (Integer) result;

    }
}
