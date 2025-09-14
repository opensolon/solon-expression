package features.expr;

import org.junit.jupiter.api.Test;
import org.noear.solon.expression.Expression;
import org.noear.solon.expression.context.EnhanceContext;
import org.noear.solon.expression.exception.EvaluationException;
import org.noear.solon.expression.snel.SnEL;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author noear 2025/9/14 created
 *
 */
public class SnelTypeExpressionTest {
    EnhanceContext context = new EnhanceContext(null);

    @Test
    public void testVarargsMethod() {
        Expression expr = SnEL.parse("T(java.util.Arrays).asList('a', 'b', 'c')");
        Object result = expr.eval(context);

        assertNotNull(result);
        assertTrue(result instanceof java.util.List);
        java.util.List<?> list = (java.util.List<?>) result;
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    public void testVarargsMethodWithMixedTypes() {
        Expression expr = SnEL.parse("T(java.util.Arrays).asList(1, 'hello', 3.14)");
        Object result = expr.eval(context);

        assertNotNull(result);
        assertTrue(result instanceof java.util.List);
        java.util.List<?> list = (java.util.List<?>) result;
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals("hello", list.get(1));
        assertEquals(3.14, list.get(2));
    }

    @Test
    public void testVarargsMethodWithSingleArg() {
        Expression expr = SnEL.parse("T(java.util.Arrays).asList(42)");
        Object result = expr.eval(context);

        assertNotNull(result);
        assertTrue(result instanceof java.util.List);
        java.util.List<?> list = (java.util.List<?>) result;
        assertEquals(1, list.size());
        assertEquals(42, list.get(0));
    }

    @Test
    public void testVarargsMethodWithNoArgs() {
        Expression expr = SnEL.parse("T(java.util.Arrays).asList()");
        Object result = expr.eval(context);

        assertNotNull(result);
        assertTrue(result instanceof java.util.List);
        java.util.List<?> list = (java.util.List<?>) result;
        assertTrue(list.isEmpty());
    }

    @Test
    public void testComplexVarargsExpression() {
        Expression expr = SnEL.parse("T(java.util.Arrays).asList(T(java.lang.Integer).valueOf(1), T(java.lang.Integer).valueOf(2), T(java.lang.Integer).valueOf(3))");
        Object result = expr.eval(context);

        assertNotNull(result);
        assertTrue(result instanceof java.util.List);
        java.util.List<?> list = (java.util.List<?>) result;
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
    }


    @Test
    public void testBasicTypeExpression() {
        Expression expr = SnEL.parse("T(java.lang.String)");
        Object result = expr.eval(context);

        assertEquals(String.class, result);
        assertEquals("T(java.lang.String)", expr.toString());
    }

    @Test
    public void testStaticMethodCall() {
        Expression expr = SnEL.parse("T(java.lang.Math).random()");
        Object result = expr.eval(context);

        assertNotNull(result);
        assertTrue(result instanceof Double);
        double value = (Double) result;
        assertTrue(value >= 0.0 && value < 1.0);
    }

    @Test
    public void testStaticMethodCallWithArgs() {
        Expression expr = SnEL.parse("T(java.lang.Long).parseLong('123')");
        Object result = expr.eval(context);

        assertEquals(123L, result);
    }

    @Test
    public void testStaticMethodCallInArithmetic() {
        Expression expr = SnEL.parse("T(java.lang.Math).random() * 100");
        Object result = expr.eval(context);

        assertNotNull(result);
        assertTrue(result instanceof Double);
        double value = (Double) result;
        assertTrue(value >= 0.0 && value < 100.0);
    }

    @Test
    public void testStaticFieldAccess() {
        Expression expr = SnEL.parse("T(java.lang.Math).PI");
        Object result = expr.eval(context);

        assertEquals(Math.PI, result);
    }

    @Test
    public void testStaticFieldInExpression() {
        Expression expr = SnEL.parse("T(java.lang.Math).PI * 2");
        Object result = expr.eval(context);

        assertEquals(Math.PI * 2, (Double) result, 0.0001);
    }

    @Test
    public void testMultipleTypeExpressions() {
        Expression expr = SnEL.parse("T(java.lang.Integer).parseInt('456') + T(java.lang.Double).parseDouble('7.89')");
        Object result = expr.eval(context);

        assertEquals(456 + 7.89, (Double) result, 0.0001);
    }

    @Test
    public void testTypeExpressionWithComplexClass() {
        Expression expr = SnEL.parse("T(java.util.Collections).emptyList()");
        Object result = expr.eval(context);

        assertNotNull(result);
        assertTrue(result instanceof java.util.List);
        assertTrue(((java.util.List<?>) result).isEmpty());
    }

    @Test
    public void testTypeExpressionInTernary() {
        Expression expr = SnEL.parse("T(java.lang.Boolean).TRUE ? T(java.lang.Math).PI : T(java.lang.Math).E");
        Object result = expr.eval(context);

        assertEquals(Math.PI, result);
    }

    @Test
    public void testNonExistentClass() {
        Expression expr = SnEL.parse("T(non.existent.Class).someMethod()");
        EvaluationException exception = assertThrows(EvaluationException.class,
                () -> expr.eval(context));

        assertTrue(exception.getMessage().contains("Class not found"));
        assertTrue(exception.getMessage().contains("non.existent.Class"));
    }

    @Test
    public void testPrimitiveWrapperType() {
        Expression expr = SnEL.parse("T(java.lang.Integer).valueOf(42)");
        Object result = expr.eval(context);

        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    public void testStringClassMethods() {
        Expression expr = SnEL.parse("T(java.lang.String).valueOf(123)");
        Object result = expr.eval(context);

        assertEquals("123", result);
    }

    @Test
    public void testSystemClass() {
        Expression expr = SnEL.parse("T(java.lang.System).currentTimeMillis()");
        Object result = expr.eval(context);

        assertNotNull(result);
        assertTrue(result instanceof Long);
        assertTrue((Long) result > 0);
    }

    @Test
    public void testTypeExpressionInComparison() {
        Expression expr = SnEL.parse("T(java.lang.Math).PI > 3.0");
        Object result = expr.eval(context);

        assertEquals(true, result);
    }

    @Test
    public void testNestedTypeExpressions() {
        Expression expr = SnEL.parse("T(java.util.Arrays).asList(T(java.lang.Integer).valueOf(1), T(java.lang.Integer).valueOf(2))");
        Object result = expr.eval(context);

        assertNotNull(result);
        assertTrue(result instanceof java.util.List);
        java.util.List<?> list = (java.util.List<?>) result;
        assertEquals(2, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
    }
}
