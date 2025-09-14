package features.expr;

import org.junit.jupiter.api.Test;
import org.noear.solon.expression.context.EnhanceContext;
import org.noear.solon.expression.snel.SnEL;

import java.util.HashMap;
import java.util.Map;

/**
 * @author noear 2025/5/2 created
 */
public class ContextTest {
    @Test
    public void case1() {
        Map<String, Object> user = new HashMap<>();
        user.put("name", "world");
        user.put("age", 20);
        user.put("age2", 10);

        Map<String, Object> order = new HashMap<>();
        order.put("id", 1);
        order.put("user", user);

        Map<String, Object> context = new HashMap();
        context.put("order", order);

        Object result = SnEL.eval("order.user.age == 20 ? true : false", new EnhanceContext(context));
        assert true == (Boolean) result;


        result = SnEL.eval("order.user.age == order.user.age2 ? true : false", new EnhanceContext(context));
        assert false == (Boolean) result;


        //支持 root 变量
        result = SnEL.eval("root.order.user.age == order.user.age2 ? true : false", new EnhanceContext(context));
        assert false == (Boolean) result;
    }


    @Test
    public void case2() {
        User user = new User("world", 20);

        Object result = SnEL.eval("age == 20 ? true : false", new EnhanceContext(user));
        assert true == (Boolean) result;


        //支持 root 变量
        result = SnEL.eval("root.age == 20 ? true : false", new EnhanceContext(user));
        assert true == (Boolean) result;

        result = SnEL.eval("root['age'] == 20 ? true : false", new EnhanceContext(user));
        assert true == (Boolean) result;
    }

    @Test
    public void case3() {
        Object result = SnEL.eval("root == true", new EnhanceContext(true));
        assert true == (Boolean) result;

        result = SnEL.eval("root == true", new EnhanceContext(false));
        assert false == (Boolean) result;
    }


    @Test
    public void case4() {
        Object result = SnEL.eval("root.substring(1, this.length() - 1)", new EnhanceContext("abcde"));
        assert "bcd".equals(result);

        Map<String, Object> map = new HashMap<>();
        map.put("name", "world");

        result = SnEL.eval("name.substring(1, this.length() - 1)", new EnhanceContext(map));
        assert "orl".equals(result);
    }


    public static class User {
        public String name;
        public int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
}
