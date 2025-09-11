package features.expr;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.noear.solon.expression.snel.SnEL;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author noear 2025/8/14 created
 *
 */
public class MathTest {
    @Test
    public void case1() {
        // 数学运算 (Long)
        Integer result = (Integer) SnEL.eval("1+2+3");
        System.out.println(result); // 6
        assert 6 == result;

        // 数学运算 (Double)
        Double result2 = (Double) SnEL.eval("1.1+2.2+3.3");
        System.out.println(result2); // 6.6
        assert 6.6D == result2;
    }

    @Test
    public void case1_2() {
        // 数学运算 (Long)
        Integer result = (Integer) SnEL.eval("1 + 2 + 3");
        System.out.println(result); // 6
        assert 6 == result;

        // 数学运算 (Double)
        Double result2 = (Double) SnEL.eval("1.1 + 2.2 + 3.3");
        System.out.println(result2); // 6.6
        assert 6.6D == result2;
    }

    @Test
    public void case2() {
        // 数学运算 (Long)
        Integer result = (Integer) SnEL.eval("1-2-3");
        System.out.println(result); // 6
        assert -4 == result;

        // 数学运算 (Double)
        Double result2 = (Double) SnEL.eval("1.1-2.2-3.3");
        System.out.println(result2); // 6.6
        assert -4.4D == result2;
    }

    @Test
    public void case2_2() {
        // 数学运算 (Long)
        Integer result = (Integer) SnEL.eval("1 - 2 - 3");
        System.out.println(result); // 6
        assert -4 == result;

        // 数学运算 (Double)
        Double result2 = (Double) SnEL.eval("1.1 - 2.2 - 3.3");
        System.out.println(result2); // 6.6
        assert -4.4D == result2;
    }

    @Test
    public void case2_3() {
        // 数学运算 (Long)
        Integer result = (Integer) SnEL.eval("-1 - 2 - 3");
        System.out.println(result); // 6
        assert -6 == result;

        // 数学运算 (Double)
        Double result2 = (Double) SnEL.eval("-1.1 - 2.2 - 3.3");
        System.out.println(result2); // 6.6
        assert -6.6D == result2;
    }

    @Test
    public void case3() {
        // 数学运算 (Long)
        Integer result = (Integer) SnEL.eval("1*2*3");
        System.out.println(result); // 6
        assert 6 == result;

        // 数学运算 (Double)
        Double result2 = (Double) SnEL.eval("1.1*2.2*3.3");
        System.out.println(result2); // 6.6
        assert 7.986000000000001D == result2;
    }

    @Test
    public void case3_2() {
        // 数学运算 (Long)
        Integer result = (Integer) SnEL.eval("1 * 2 * 3");
        System.out.println(result); // 6
        assert 6 == result;

        // 数学运算 (Double)
        Double result2 = (Double) SnEL.eval("1.1 * 2.2 * 3.3");
        System.out.println(result2); // 6.6
        assert 7.986000000000001D == result2;
    }

    @Test
    public void case6() {
        Map<String, Object> context = new HashMap();
        context.put("a", 1);
        context.put("b", 2);

        Integer result = (Integer) SnEL.eval("(a + b) * 2", context);
        assert result == 6;
    }

    @Test
    public void case7() {
        Map<String, Object> context = new HashMap();
        context.put("Math", Math.class);

        Object result = null;

        result = SnEL.eval("Math.abs(-5)", context);
        Assertions.assertEquals(5, result);

        result = SnEL.eval("Math.abs(-5) > 4 ? 'A' : 'B'", context);
        Assertions.assertEquals("A", result);
    }

    @Test
    public void case8() {
        Map<String, Object> context = new HashMap();
        context.put("abs", Math.class);

        Object result = null;

        result = SnEL.eval("abs(-5)", context);
        Assertions.assertEquals(5, result);

        result = SnEL.eval("abs(-5) > 4 ? 'A' : 'B'", context);
        Assertions.assertEquals("A", result);
    }

    @Test
    public void case9() {
        Map<String, Object> context = new HashMap();
        context.put("sum", MathPlus.class);

        Object result = null;

        result = SnEL.eval("sum(1,2)", context);
        Assertions.assertEquals(3, result);
    }

    public static class MathPlus {
        public static int sum(int a, int b) {
            return a + b;
        }
    }
}
