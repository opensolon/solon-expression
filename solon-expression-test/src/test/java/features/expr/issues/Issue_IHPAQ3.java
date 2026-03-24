package features.expr.issues;

import lombok.Data;
import org.junit.jupiter.api.Test;
import org.noear.solon.expression.context.EnhanceContext;
import org.noear.solon.expression.snel.SnEL;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author noear 2026/3/24 created
 *
 */
public class Issue_IHPAQ3 {

    @Test
    public void test() {
        UserDo1 userDo1 = new UserDo1();
        userDo1.setId(10);

        Map<String, Object> beans = new HashMap<>();
        beans.put("user", userDo1);

        Map<String, Object> vars = new HashMap();
        vars.put("uuu", userDo1);

        EnhanceContext context = new EnhanceContext(vars);
        context.forBeans(beans::get);

        System.out.println(SnEL.eval("@user.bb(#uuu)", context));
    }

    @Test
    public void test2() {
        UserDo1 userDo1 = new UserDo1();
        userDo1.setId(10);

        Map<String, Object> beans = new HashMap<>();
        beans.put("user", userDo1);

        Map<String, Object> vars = new HashMap();
        vars.put("uuu", userDo1);

        EnhanceContext context = new EnhanceContext(vars);
        context.forBeans(beans::get);

        System.out.println(SnEL.eval("@user.bb($uuu)", context));
    }


    @Data
    public class UserDo1 implements Serializable {
        public long id = 5;
        public String name = "bb";
        public double create_lng;
        public double create_lat;

        public void aa(int flag) {
            System.out.println(flag);
        }

        public void bb(UserDo1 userDo) {
            System.out.println(userDo.getId());
        }
    }
}
