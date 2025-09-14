package features.expr;

import org.junit.jupiter.api.Test;
import org.noear.solon.expression.context.StandardContext;
import org.noear.solon.expression.snel.SnEL;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SnEL 新特性单元测试
 *
 * @author noear
 * @since 3.1
 */
public class SnelElvisTest {

    // 测试数据
    private Map<String, Object> createTestContext() {
        Map<String, Object> context = new HashMap<>();

        // 用户对象
        Map<String, Object> user = new HashMap<>();
        user.put("name", "John");
        user.put("age", 25);
        user.put("email", null);

        // 地址对象
        Map<String, Object> address = new HashMap<>();
        address.put("city", "Beijing");
        address.put("street", null);

        user.put("address", address);

        context.put("user", user);
        context.put("nullValue", null);
        context.put("emptyString", "");

        return context;
    }

    // 1. 测试安全导航操作符 - 正常情况
    @Test
    public void testSafeNavigation_NormalCase() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("user?.name", context);
        assertEquals("John", result);
    }

    // 2. 测试安全导航操作符 - null 情况
    @Test
    public void testSafeNavigation_NullCase() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("nullValue?.name", context);
        assertNull(result);
    }

    // 3. 测试安全导航操作符 - 链式调用正常
    @Test
    public void testSafeNavigation_ChainedNormal() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("user?.address?.city", context);
        assertEquals("Beijing", result);
    }

    // 4. 测试安全导航操作符 - 链式调用中间为null
    @Test
    public void testSafeNavigation_ChainedNull() {
        Map<String, Object> context = createTestContext();
        context.put("user", null);
        Object result = SnEL.eval("user?.address?.city", context);
        assertNull(result);
    }

    // 5. 测试安全导航操作符 - 链式调用末端为null
    @Test
    public void testSafeNavigation_ChainedEndNull() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("user?.address?.street", context);
        assertNull(result);
    }

    // 6. 测试Elvis操作符 - 正常情况（左侧不为null）
    @Test
    public void testElvisOperator_LeftNotNull() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("user.name ?: 'Guest'", context);
        assertEquals("John", result);


        result = SnEL.eval("user.name ?: 'Guest'");
        assertEquals("Guest", result);

        result = SnEL.eval("user.name?:'Guest'");
        assertEquals("Guest", result);
    }

    // 7. 测试Elvis操作符 - null情况（使用默认值）
    @Test
    public void testElvisOperator_LeftNull() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("user.email ?: 'default@email.com'", context);
        assertEquals("default@email.com", result);


        result = SnEL.eval("user.email ?: 'default@email.com'");
        assertEquals("default@email.com", result);


        result = SnEL.eval("user.email?:'default@email.com'");
        assertEquals("default@email.com", result);
    }

    // 8. 测试Elvis操作符 - 数字默认值
    @Test
    public void testElvisOperator_NumericDefault() {
        Map<String, Object> context = createTestContext();
        context.put("user", null);
        Object result = SnEL.eval("user?.age ?: 18", context);
        assertEquals(18, result);

        result = SnEL.eval("user?.age ?: 18");
        assertEquals(18, result);

        result = SnEL.eval("user?.age?:18");
        assertEquals(18, result);
    }

    // 9. 测试安全导航和Elvis组合使用
    @Test
    public void testSafeNavigationWithElvis() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("user?.email ?: 'no-email'", context);
        assertEquals("no-email", result);

        result = SnEL.eval("user?.email ?: 'no-email'");
        assertEquals("no-email", result);

        result = SnEL.eval("user?.email?:'no-email'");
        assertEquals("no-email", result);
    }

    // 添加括号测试
    @Test
    public void testElvisOperatorWithParentheses() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("(user?.age ?: 18) > 20", context);
        assertEquals(true, result); // 25 > 20

        result = SnEL.eval("(user?.age ?: 18) > 20");
        assertEquals(false, result); // 25 > 20

        result = SnEL.eval("(user?.age?:18) > 20");
        assertEquals(false, result); // 25 > 20
    }


    // 10. 测试多层安全导航和Elvis组合
    @Test
    public void testChainedSafeNavigationWithElvis() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("user?.address?.street ?: 'Unknown Street'", context);
        assertEquals("Unknown Street", result);
    }

    // 11. 测试安全导航方法调用 - 正常情况
    @Test
    public void testSafeNavigationMethodCall_Normal() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("user?.name?.length()", context);
        assertEquals(4, result); // "John".length() = 4
    }

    // 测试字符串拼接
    @Test
    public void testStringConcatenation() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("'Hello ' + user?.name + '!'", context);
        assertEquals("Hello John!", result);
    }


    // 12. 测试安全导航方法调用 - null情况
    @Test
    public void testSafeNavigationMethodCall_Null() {
        Map<String, Object> context = createTestContext();
        context.put("user", null);
        Object result = SnEL.eval("user?.name?.length()", context);
        assertNull(result);
    }

    // 测试数字和字符串混合拼接
    @Test
    public void testMixedConcatenation() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("'Age: ' + (user?.age ?: 0)", context);
        assertEquals("Age: 25", result);
    }

    // 13. 测试复杂表达式中的安全导航
    @Test
    public void testSafeNavigationInComplexExpression() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("(user?.age ?: 0) > 18", context);
        assertEquals(true, result);
    }

    // 14. 测试复杂表达式中的安全导航和Elvis
    @Test
    public void testSafeNavigationAndElvisInComplexExpression() {
        Map<String, Object> context = createTestContext();
        context.put("user", null);
        Object result = SnEL.eval("(user?.age ?: 0) > 18 ? 'Adult' : 'Child'", context);
        assertEquals("Child", result);
    }

    // 15. 测试${}属性表达式 - 正常情况
    @Test
    public void testPropertyExpression_Normal() {
        Map<String, Object> context = createTestContext();
        context.put("user.name", "John");
        Object result = SnEL.eval("${user.name}", context);
        assertEquals("John", result);
    }

    // 16. 测试${}属性表达式 - 带默认值
    @Test
    public void testPropertyExpression_WithDefault() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("${user.email:default@email.com}", context);
        assertEquals("default@email.com", result);

        result = SnEL.eval("${user.email:default@email.com} == 'default@email.com'", context);
        assertEquals(true, result);

        result = SnEL.eval("${user.email:default@email.com} != 'default@email.com'", context);
        assertEquals(false, result);
    }

    // 17. 测试${}属性表达式在复杂表达式中使用
    @Test
    public void testPropertyExpressionInComplexExpression() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("${user.name:Guest} + ' welcome!'", context);
        assertEquals("Guest welcome!", result);

        context.put("user.name", "John");
        result = SnEL.eval("${user.name:Guest} + ' welcome!'", context);
        assertEquals("John welcome!", result);
    }

    // 18. 测试向后兼容性 - 原有的三元表达式
    @Test
    public void testBackwardCompatibility_Ternary() {
        Map<String, Object> context = createTestContext();
        context.put("Math", Math.class);
        Object result = SnEL.eval("Math.abs(-5) > 4 ? 'A' : 'B'", context);
        assertEquals("A", result);
    }

    // 19. 测试向后兼容性 - 原有的方法调用
    @Test
    public void testBackwardCompatibility_MethodCall() {
        Map<String, Object> context = createTestContext();
        Object result = SnEL.eval("'hello'.toUpperCase()", context);
        assertEquals("HELLO", result);
    }

    // 20. 测试混合使用所有新特性
    @Test
    public void testAllNewFeaturesMixed() {
        Map<String, Object> context = createTestContext();
        // 复杂的混合表达式：安全导航 + Elvis + 三元表达式 + 方法调用
        Object result = SnEL.eval("(user?.address?.city ?: 'Unknown') + ' - ' + (user?.age ?: 0)", context);
        assertEquals("Beijing - 25", result);
    }

    // 21. 额外测试：测试空字符串在Elvis操作符中的行为
    @Test
    public void testElvisWithEmptyString() {
        Map<String, Object> context = createTestContext();
        // 空字符串不是null，所以不会使用默认值
        Object result = SnEL.eval("emptyString ?: 'default'", context);
        assertEquals("", result);
    }

    // 22. 额外测试：测试StandardContext中的属性表达式
    @Test
    public void testPropertyExpressionWithStandardContext() {
        Map<String, Object> data = createTestContext();
        StandardContext context = new StandardContext(data);

        Object result = SnEL.eval("${user.name:Guest}", context);
        assertEquals("Guest", result);


        data.put("user.name", "John");
        result = SnEL.eval("${user.name:Guest}", context);
        assertEquals("John", result);

        result = SnEL.eval("${user.name:1} == '1'");
        assertEquals(true, result);

        result = SnEL.eval("T(java.lang.Integer).parseInt(${user.name:1}) == 1", new StandardContext(null));
        assertEquals(true, result);
    }

    @Test
    public void testPropertyExpressionCompare() {
        StandardContext context = new StandardContext(null);
        Object result = SnEL.eval("T(java.lang.Integer).parseInt(${user.name:1}) > 1", context);
        assertEquals(false, result);

        result = SnEL.eval("T(java.lang.Integer).parseInt(${user.name:1}) >= 1", context);
        assertEquals(true, result);

        result = SnEL.eval("T(java.lang.Integer).parseInt(${user.name:2}) > 1", context);
        assertEquals(true, result);

        result = SnEL.eval("T(java.lang.Integer).parseInt(${user.name:2}) >= 1", context);
        assertEquals(true, result);

        /// /////////////


        result = SnEL.eval("T(java.lang.Integer).parseInt(${user.name:1}) < 1", context);
        assertEquals(false, result);

        result = SnEL.eval("T(java.lang.Integer).parseInt(${user.name:1}) <= 1", context);
        assertEquals(true, result);

        result = SnEL.eval("T(java.lang.Integer).parseInt(${user.name:2}) < 1", context);
        assertEquals(false, result);

        result = SnEL.eval("T(java.lang.Integer).parseInt(${user.name:2}) <= 1", context);
        assertEquals(false, result);
    }

    // 23. 测试安全导航数组访问
    @Test
    public void testSafeNavigationArrayAccess() {
        Map<String, Object> context = new HashMap<>();
        String[] array = {"a", "b", "c"};
        context.put("array", array);
        context.put("nullArray", null);

        Object result1 = SnEL.eval("array?.[1]", context);
        assertEquals("b", result1);

        Object result2 = SnEL.eval("nullArray?.[0]", context);
        assertNull(result2);
    }

    // 24. 测试嵌套的${}表达式
    @Test
    public void testNestedPropertyExpression() {
        Map<String, Object> context = createTestContext();
        // 注意：这里需要确保SnelTemplateParser也支持嵌套
        Object result = SnEL.evalTmpl("Hello, #{${user.name:Tom}}!", context);
        assertEquals("Hello, Tom!", result);
    }
}