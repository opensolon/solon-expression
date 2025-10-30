package features.expr;

import org.junit.jupiter.api.Test;
import org.noear.solon.expression.context.EnhanceContext;
import org.noear.solon.expression.guidance.TypeGuidance;
import org.noear.solon.expression.guidance.TypeGuidanceUnsafety;
import org.noear.solon.expression.snel.SnEL;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EnhanceContext 在 SnEL.eval 中的运行表现测试
 *
 * @author noear
 * @since 3.6
 */
public class EnhanceContextSnELTest {

    // 测试对象类
    public static class TestUser {
        private String name;
        private int age;

        public TestUser(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public boolean isAdult() {
            return age >= 18;
        }
    }

    // 测试 PropertiesGuidance 特性
    @Test
    void testPropertiesGuidanceWithProperties() {
        Properties props = new Properties();
        props.setProperty("app.name", "MyApp");
        props.setProperty("app.version", "1.0.0");

        EnhanceContext context = new EnhanceContext<>(new Object())
                .forProperties(props);

        // 测试属性访问
        Object result = SnEL.eval("${app.name}", context);
        assertEquals("MyApp", result);

        result = SnEL.eval("${app.version}", context);
        assertEquals("1.0.0", result);
    }

    @Test
    void testPropertiesGuidanceWithDefaultValue() {
        Properties props = new Properties();
        props.setProperty("app.name", "MyApp");
        // app.version 未设置，使用默认值

        EnhanceContext context = new EnhanceContext<>(new Object())
                .forProperties(props)
                .forAllowPropertyDefault(true);

        // 测试带默认值的属性表达式
        Object result = SnEL.eval("${app.version:2.0.0}", context);
        assertEquals("2.0.0", result);

        // 已设置的属性不使用默认值
        result = SnEL.eval("${app.name:DefaultApp}", context);
        assertEquals("MyApp", result);
    }

    @Test
    void testPropertiesGuidanceWithoutDefaultValue() {
        Properties props = new Properties();
        // app.name 未设置

        EnhanceContext context = new EnhanceContext<>(new Object())
                .forProperties(props)
                .forAllowPropertyDefault(false)
                .forAllowReturnNull(true);

        // 不允许默认值时返回 null
        Object result = SnEL.eval("${app.name:DefaultApp}", context);
        assertNull(result);
    }

    @Test
    void testPropertiesGuidanceNesting() {
        Properties props = new Properties();
        props.setProperty("user.name", "noear");
        props.setProperty("greeting", "Hello, ${user.name}!");

        EnhanceContext context = new EnhanceContext<>(new Object())
                .forProperties(props)
                .forAllowPropertyNesting(true);

        // 测试属性嵌套解析
        Object result = SnEL.eval("${greeting}", context);
        assertEquals("Hello, noear!", result);
    }

    @Test
    void testPropertiesGuidanceWithoutNesting() {
        Properties props = new Properties();
        props.setProperty("user.name", "noear");
        props.setProperty("greeting", "Hello, ${user.name}!");

        EnhanceContext context = new EnhanceContext<>(new Object())
                .forProperties(props)
                .forAllowPropertyNesting(false);

        // 不允许嵌套时返回原始值
        Object result = SnEL.eval("${greeting}", context);
        assertEquals("Hello, ${user.name}!", result);
    }

    @Test
    void testAllowTextAsProperty() {
        Properties props = new Properties();
        props.setProperty("text", "property_value");

        EnhanceContext context = new EnhanceContext<>(new Object())
                .forProperties(props)
                .forAllowTextAsProperty(true);

        // 文本作为属性表达式
        Object result = SnEL.eval("${text}", context);
        assertEquals("property_value", result);
    }

    @Test
    void testDisallowTextAsProperty() {
        Properties props = new Properties();
        props.setProperty("text", "property_value");

        EnhanceContext context = new EnhanceContext<>(new Object())
                .forProperties(props)
                .forAllowTextAsProperty(false);

        // 文本不作为属性表达式
        Object result = SnEL.evalTmpl("plain_text", context);
        assertEquals("plain_text", result);
    }

    // 测试 ReturnGuidance 特性
    @Test
    void testAllowReturnNull() {
        Map<String, Object> data = new HashMap<>();
        data.put("existing", "value");
        // nonExisting 键不存在

        EnhanceContext context = new EnhanceContext<>(data)
                .forAllowReturnNull(true);

        // 允许返回 null
        Object result = SnEL.eval("nonExisting", context);
        assertNull(result);
    }


    @Test
    void testReturnNullInTemplate() {
        Properties props = new Properties();
        // missing.property 未设置

        EnhanceContext context = new EnhanceContext<>(new Object())
                .forProperties(props)
                .forAllowPropertyDefault(false)
                .forAllowReturnNull(true);

        // 模板中属性缺失时返回 null
        Object result = SnEL.evalTmpl("${missing.property}", context);
        assertNull(result);
    }

    // 测试 TypeGuidance 特性
    @Test
    void testTypeGuidanceWithTExpression() {
        EnhanceContext context = new EnhanceContext<>(new Object())
                .forTypeGuidance(new TypeGuidance() {
                    @Override
                    public Class<?> getType(String typeName) {
                        if ("java.lang.String".equals(typeName)) {
                            return String.class;
                        }
                        throw new RuntimeException("Type not found: " + typeName);
                    }
                });

        // 测试 T() 表达式
        Object result = SnEL.eval("T(java.lang.String)", context);
        assertEquals(String.class, result);
    }

    @Test
    void testTypeGuidanceWithStaticMethod() {
        EnhanceContext context = new EnhanceContext<>(new Object())
                .forTypeGuidance(TypeGuidanceUnsafety.INSTANCE);

        // 测试静态方法调用
        Object result = SnEL.eval("T(java.lang.String).valueOf(123)", context);
        assertEquals("123", result);
    }

    @Test
    void testTypeGuidanceWithoutSupport() {
        // 没有类型指导时抛出异常
        Throwable err = null;

        try {
            EnhanceContext context = new EnhanceContext<>(new Object())
                    .forTypeGuidance(null);

            SnEL.eval("T(java.lang.String)", context, false);
        } catch (Throwable e) {
            err = e;
        }

        assert err != null;
    }

    // 测试对象属性访问
    @Test
    void testObjectPropertyAccess() {
        TestUser user = new TestUser("noear", 25);
        EnhanceContext<TestUser, ?> context = new EnhanceContext<>(user);

        // 测试属性访问
        Object result = SnEL.eval("name", context);
        assertEquals("noear", result);

        result = SnEL.eval("age", context);
        assertEquals(25, result);

        result = SnEL.eval("root.isAdult()", context);
        assertEquals(true, result);
    }

    @Test
    void testMapPropertyAccess() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "noear");
        data.put("age", 25);
        data.put("scores", new int[]{90, 85, 95});

        EnhanceContext context = new EnhanceContext<>(data);

        // 测试 Map 访问
        Object result = SnEL.eval("name", context);
        assertEquals("noear", result);

        result = SnEL.eval("age", context);
        assertEquals(25, result);

        result = SnEL.eval("scores[0]", context);
        assertEquals(90, result);
    }

    // 测试复杂表达式组合
    @Test
    void testComplexExpressionWithAllFeatures() {
        Properties props = new Properties();
        props.setProperty("minAge", "18");
        props.setProperty("message", "User: ${user.name}, Status: ${user.status}");

        TestUser user = new TestUser("noear", 25);

        EnhanceContext<TestUser, ?> context = new EnhanceContext<>(user)
                .forProperties(props)
                .forAllowPropertyDefault(true)
                .forAllowPropertyNesting(true)
                .forAllowTextAsProperty(false)
                .forAllowReturnNull(true)
                .forTypeGuidance(TypeGuidanceUnsafety.INSTANCE);

        // 测试复杂表达式
        Object result = SnEL.eval("age > T(java.lang.Integer).parseInt(${minAge:16}) && root.isAdult()", context);
        assertEquals(true, result);

        // 测试模板表达式
        String templateResult = SnEL.evalTmpl("${message}", context);
        assertNull(templateResult);
    }

    @Test
    void testSafeNavigationWithNullReturn() {
        Map<String, Object> data = new HashMap<>();
        data.put("user", null); // user 为 null

        EnhanceContext context = new EnhanceContext<>(data)
                .forAllowReturnNull(true);

        // 安全导航操作符
        Object result = SnEL.eval("user?.name", context);
        assertNull(result);
    }

    @Test
    void testElvisOperatorWithProperties() {
        Properties props = new Properties();
        // userName 未设置

        EnhanceContext context = new EnhanceContext<>(new Object())
                .forProperties(props)
                .forAllowPropertyDefault(false)
                .forAllowReturnNull(true);

        // Elvis 操作符与属性表达式结合
        Object result = SnEL.eval("${userName} ?: 'guest'", context);
        assertEquals("guest", result);
    }

    // 测试边界情况
    @Test
    void testEmptyProperties() {
        Properties props = new Properties();

        EnhanceContext context = new EnhanceContext<>(new Object())
                .forProperties(props)
                .forAllowPropertyDefault(true);

        // 空属性集使用默认值
        Object result = SnEL.eval("${missing:default}", context);
        assertEquals("default", result);
    }

    @Test
    void testNullTarget() {
        EnhanceContext context = new EnhanceContext<>(null)
                .forAllowReturnNull(true);

        // null 目标对象
        Object result = SnEL.eval("anyProperty", context);
        assertNull(result);

        result = SnEL.eval("root", context);
        assertNull(result);

        result = SnEL.eval("this", context);
        assertNull(result);
    }

    @Test
    void testThisKeyword() {
        TestUser user = new TestUser("noear", 25);
        EnhanceContext<TestUser, ?> context = new EnhanceContext<>(user);

        // 测试 this 关键字
        Object result = SnEL.eval("this.name", context);
        assertEquals("noear", result);

        // 在表达式中使用 this
        result = SnEL.eval("this.age > 20 ? this.name : 'young'", context);
        assertEquals("noear", result);
    }

    @Test
    void testRootKeyword() {
        TestUser user = new TestUser("noear", 25);
        EnhanceContext<TestUser, ?> context = new EnhanceContext<>(user);

        // 测试 root 关键字
        Object result = SnEL.eval("root.name", context);
        assertEquals("noear", result);
        assertEquals(user, SnEL.eval("root", context));
    }
}