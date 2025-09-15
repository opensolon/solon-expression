package features.expr;

import org.junit.jupiter.api.Test;
import org.noear.solon.expression.context.EnhanceContext;
import org.noear.solon.expression.guidance.TypeGuidance;
import org.noear.solon.expression.guidance.TypeGuidanceUnsafety;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EnhanceContext 单元测试
 *
 * @author noear
 * @since 3.6
 */
class EnhanceContextTest {

    @Test
    void testForProperties() {
        Properties props = new Properties();
        props.setProperty("key1", "value1");

        EnhanceContext<String, ?> context = new EnhanceContext<>("test");
        context.forProperties(props);

        assertSame(props, context.getProperties());
    }

    @Test
    void testForPropertiesWithNull() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");
        context.forProperties(null);

        assertNull(context.getProperties());
    }

    @Test
    void testForPropertiesWithEmpty() {
        Properties props = new Properties();

        EnhanceContext<String, ?> context = new EnhanceContext<>("test");
        context.forProperties(props);

        assertSame(props, context.getProperties());
        assertTrue(context.getProperties().isEmpty());
    }

    @Test
    void testForAllowPropertyDefault() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");

        // Test true
        context.forAllowPropertyDefault(true);
        assertTrue(context.allowPropertyDefault());

        // Test false
        context.forAllowPropertyDefault(false);
        assertFalse(context.allowPropertyDefault());
    }

    @Test
    void testForAllowPropertyDefaultTrue() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");
        context.forAllowPropertyDefault(true);

        assertTrue(context.allowPropertyDefault());
    }

    @Test
    void testForAllowPropertyDefaultFalse() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");
        context.forAllowPropertyDefault(false);

        assertFalse(context.allowPropertyDefault());
    }

    @Test
    void testForAllowPropertyNesting() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");

        // Test true
        context.forAllowPropertyNesting(true);
        assertTrue(context.allowPropertyNesting());

        // Test false
        context.forAllowPropertyNesting(false);
        assertFalse(context.allowPropertyNesting());
    }

    @Test
    void testForAllowPropertyNestingTrue() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");
        context.forAllowPropertyNesting(true);

        assertTrue(context.allowPropertyNesting());
    }

    @Test
    void testForAllowPropertyNestingFalse() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");
        context.forAllowPropertyNesting(false);

        assertFalse(context.allowPropertyNesting());
    }

    @Test
    void testForAllowTextAsProperty() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");

        // Test true
        context.forAllowTextAsProperty(true);
        assertTrue(context.allowTextAsProperty());

        // Test false
        context.forAllowTextAsProperty(false);
        assertFalse(context.allowTextAsProperty());
    }

    @Test
    void testForAllowTextAsPropertyTrue() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");
        context.forAllowTextAsProperty(true);

        assertTrue(context.allowTextAsProperty());
    }

    @Test
    void testForAllowTextAsPropertyFalse() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");
        context.forAllowTextAsProperty(false);

        assertFalse(context.allowTextAsProperty());
    }

    @Test
    void testForAllowReturnNull() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");

        // Test true
        context.forAllowReturnNull(true);
        assertTrue(context.allowReturnNull());

        // Test false
        context.forAllowReturnNull(false);
        assertFalse(context.allowReturnNull());
    }

    @Test
    void testForAllowReturnNullTrue() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");
        context.forAllowReturnNull(true);

        assertTrue(context.allowReturnNull());
    }

    @Test
    void testForAllowReturnNullFalse() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");
        context.forAllowReturnNull(false);

        assertFalse(context.allowReturnNull());
    }

    @Test
    void testForTypeGuidance() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");
        TypeGuidance customGuidance = new TypeGuidance() {
            @Override
            public Class<?> getType(String typeName) {
                return String.class;
            }
        };

        context.forTypeGuidance(customGuidance);

        assertSame(customGuidance, context.getTypeGuidance());
    }

    @Test
    void testForTypeGuidanceWithUnsafety() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");
        context.forTypeGuidance(TypeGuidanceUnsafety.INSTANCE);

        assertSame(TypeGuidanceUnsafety.INSTANCE, context.getTypeGuidance());
    }

    @Test
    void testForTypeGuidanceWithNull() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");
        context.forTypeGuidance(null);

        assertNull(context.getTypeGuidance());
    }

    @Test
    void testFluentInterface() {
        Properties props = new Properties();
        props.setProperty("test", "value");

        EnhanceContext<String, ?> context = new EnhanceContext<>("test")
                .forProperties(props)
                .forAllowPropertyDefault(true)
                .forAllowPropertyNesting(false)
                .forAllowTextAsProperty(true)
                .forAllowReturnNull(false)
                .forTypeGuidance(TypeGuidanceUnsafety.INSTANCE);

        assertSame(props, context.getProperties());
        assertTrue(context.allowPropertyDefault());
        assertFalse(context.allowPropertyNesting());
        assertTrue(context.allowTextAsProperty());
        assertFalse(context.allowReturnNull());
        assertSame(TypeGuidanceUnsafety.INSTANCE, context.getTypeGuidance());
    }

    @Test
    void testChainedConfiguration() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");

        // Test chaining
        EnhanceContext<String, ?> returnedContext = context
                .forAllowPropertyDefault(true)
                .forAllowPropertyNesting(false)
                .forAllowTextAsProperty(true);

        assertSame(context, returnedContext);
        assertTrue(context.allowPropertyDefault());
        assertFalse(context.allowPropertyNesting());
        assertTrue(context.allowTextAsProperty());
    }

    @Test
    void testDefaultValues() {
        EnhanceContext<String, ?> context = new EnhanceContext<>("test");

        // Test default values
        assertTrue(context.allowPropertyDefault());
        assertFalse(context.allowPropertyNesting());
        assertFalse(context.allowTextAsProperty());
        assertFalse(context.allowReturnNull());
        assertSame(TypeGuidanceUnsafety.INSTANCE, context.getTypeGuidance());
    }
}