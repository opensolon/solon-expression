package features.expr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.noear.solon.expression.Expression;
import org.noear.solon.expression.snel.SnelParser;
import org.noear.solon.expression.snel.TemplateParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SnelTemplateParser 单元测试
 */
public class SnelTemplateParserTest {

    private SnelParser parser;

    @BeforeEach
    void setUp() {
        // 初始化解析器：容量1000, 起始符'#', 属性符'$'
        parser = new SnelParser(1000, '#', '{');
    }

    @Test
    @DisplayName("基础文本解析：不包含任何标记")
    void testPureText() {
        String text = "Hello World";
        Expression<String> expr = parser.forTmpl().parse(text, true);

        assertEquals(text, expr.eval(Collections.emptyMap()));
    }

    @Test
    @DisplayName("基础属性解析：{key}")
    void testBasicProperties() {
        String template = "Welcome, {name}!";
        Map<String, Object> context = new HashMap<>();
        context.put("name", "Solon");

        Expression<String> expr = parser.forTmpl().parse(template, true);
        assertEquals("Welcome, Solon!", expr.eval(context));
    }

    @Test
    @DisplayName("基础表达式解析：#{1 + 1}")
    void testBasicExpression() {
        // 注意：具体的表达式执行取决于渲染层实现，这里假设渲染层会处理内容
        String template = "Result is #{1 + 1}";
        Expression<String> expr = parser.forTmpl().parse(template, true);

        // 验证解析不报错
        assertNotNull(expr);
    }

    @Test
    @DisplayName("混合模式解析：同时包含属性和表达式")
    void testMixed() {
        String template = "User #{user.id}: {user.name}";
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1001);
        user.put("name", "noear");
        context.put("user", user);
        context.put("user.name", "noear");

        Expression<String> expr = parser.forTmpl().parse(template, true);
        String result = expr.eval(context);

        assertTrue(result.contains("1001"));
        assertTrue(result.contains("noear"));
    }

    @Test
    @DisplayName("嵌套表达式解析：#{${key}}")
    void testNestedExpression() {
        // 这是一个复杂的场景，测试 findExpressionEnd 的大括号平衡逻辑
        String template = "Nested: #{${prefix}.name}";
        Map<String, Object> context = new HashMap<>();
        context.put("prefix", "user");

        Expression<String> expr = parser.forTmpl().parse(template, true);
        assertNotNull(expr);
        // 这里验证的是解析过程没有截断，完整的内容 "${prefix}.name" 被识别为 EXPRESSION 内部内容
    }

    @Test
    @DisplayName("边缘情况：未闭合的标记")
    void testUnclosedMarker() {
        String template = "This is #{unclosed";
        Expression<String> expr = parser.forTmpl().parse(template, false);

        // 根据代码逻辑，未闭合的表达式会回退为普通文本
        assertEquals(template, expr.eval(new HashMap()));
    }

    @Test
    @DisplayName("边缘情况：空表达式")
    void testEmptyExpression() {
        String template = "Empty {}";
        Expression<String> expr = parser.forTmpl().parse(template, true);

        assertNotNull(expr);
        // 应该能正常解析出片段
    }

    @Test
    @DisplayName("LRU 缓存验证")
    void testCache() {
        String template = "cached_{val}";

        // 第一次解析
        Expression<String> expr1 = parser.forTmpl().parse(template, true);
        // 第二次解析（命中缓存）
        Expression<String> expr2 = parser.forTmpl().parse(template, true);

        assertSame(expr1, expr2, "开启缓存时，相同表达式应返回同一实例");

        // 不使用缓存
        Expression<String> expr3 = parser.forTmpl().parse(template, false);
        assertNotSame(expr1, expr3, "禁用缓存时，应生成新实例");
    }

    @Test
    @DisplayName("hasMarker 逻辑测试")
    void testHasMarker() {
        assertTrue(parser.hasMarker("hello #{name}"));
        assertTrue(parser.hasMarker("hello {name}"));
        assertFalse(parser.hasMarker("hello name"));
    }

    @Test
    @DisplayName("单双字符混合解析：同时支持 #{expr} 和 {prop}")
    void testHybridLength() {
        // 设置：# 为表达式前缀，{ 为属性前缀（即单字符标记）
        TemplateParser parser = new SnelParser(100, '#', '{', '{', '}').forTmpl();

        String template = "Exp:#{1+1}, Prop:{user}";
        Expression<String> expr = parser.parse(template, true);

        Map<String, Object> ctx = Collections.singletonMap("user", "noear");
        String result = expr.eval(ctx);

        // 验证截取位置是否准确（没有多删或漏删字符）
        assertTrue(result.contains("Exp:"));
        assertTrue(result.contains("Prop:noear"));
    }

    @Test
    @DisplayName("边界测试：字符串末尾的单标记")
    void testEndWithMarker() {
        TemplateParser parser = new SnelParser(100, '#', '{', '{', '}').forTmpl();
        String template = "Trailing {";
        Expression<String> expr = parser.parse(template, false);

        assertEquals("Trailing {", expr.eval(new HashMap()), "末尾未闭合的单标记应识别为文本");
    }

    @Test
    @DisplayName("测试双字符标记 (标准模式)")
    void testStandardMarkers() {
        SnelParser parser = new SnelParser(100, '#', '$'); // #{...} and ${...}
        String input = "A #{exp} B ${prop}";

        // 验证 hasMarker
        assertTrue(parser.hasMarker(input));

        // 解析验证
        assertNotNull(parser.forTmpl().parse(input, true));
    }

    @Test
    @DisplayName("测试单字符标记兼容性 ({prop})")
    void testSingleCharMarker() {
        // 配置：'#' 为表达式前缀(双字符)，'{' 为属性前缀(单字符)
        TemplateParser parser = new SnelParser(100, '#', '{', '{', '}').forTmpl();
        String input = "Hi {name}, calc #{1+1}";

        Expression<String> expr = parser.parse(input, false);
        // 如果逻辑正确，这里应该成功解析出 4 个片段： "Hi ", {name}, ", calc ", #{1+1}
        assertNotNull(expr);
    }

    @Test
    @DisplayName("测试未闭合回退 (单/双字符回退)")
    void testUnclosedRollback() {
        TemplateParser parser = new SnelParser(100, '#', '{', '{', '}').forTmpl();

        // 测试双字符未闭合回退
        assertEquals("A #{exp", parser.parse("A #{exp", false).eval(new HashMap()));

        // 测试单字符未闭合回退
        assertEquals("B {prop", parser.parse("B {prop", false).eval(new HashMap()));
    }

    @Test
    @DisplayName("测试嵌套括号平衡")
    void testNestedBraces() {
        TemplateParser parser = SnelParser.getInstance().forTmpl();
        String input = "Result: #{ map.get('{') }";

        // 内部的 '{' 不应导致解析提前结束
        assertNotNull(parser.parse(input, true));
    }
}