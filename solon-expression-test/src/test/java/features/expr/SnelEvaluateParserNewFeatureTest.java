package features.expr;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.noear.solon.expression.Expression;
import org.noear.solon.expression.exception.CompilationException;
import org.noear.solon.expression.snel.EvaluateParser;
import org.noear.solon.expression.snel.SnelParser;

import java.util.HashMap;
import java.util.Map;

@DisplayName("SnelEvaluateParser 3.8 新特性专项测试")
public class SnelEvaluateParserNewFeatureTest {

    @Test
    @DisplayName("标识符判定：应当排除 $ 符号")
    public void testIdentifierExcludesDollar() {
        EvaluateParser parser = SnelParser.getInstance().forEval();
        Map<String, Object> context = new HashMap<>();
        context.put("$name", "solon");
        context.put("name", "noear");

        // 场景 A: 直接使用 $name 应当抛出异常（因为 $ 无法识别为标识符起始）
        // 解析器会因为无法识别 $ 而在 parsePrimaryExpression 的最后阶段失败
        Assertions.assertThrows(CompilationException.class, () -> {
            parser.parse("$name", false);
        }, "应当不允许以 $ 开头的标识符");

        // 场景 B: 确保普通标识符依然正常
        Expression expr = parser.parse("name", false);
        Assertions.assertEquals("noear", expr.eval(context));
    }


    @Test
    @DisplayName("包装解析：验证 #{} 的自动剥离与递归")
    public void testFullMarkerRecursive() {
        EvaluateParser parser = SnelParser.getInstance().forEval();
        Map<String, Object> context = new HashMap<>();
        context.put("a", 1);

        // 场景 A: 标准包装
        Assertions.assertEquals(2, parser.parse("#{a + 1}", false).eval(context));

        // 场景 B: 多层嵌套包装（递归剥离）
        // #{ #{ #{ a } } } -> 应被层层剥离最终解析为变量 a
        Assertions.assertEquals(1, parser.parse("#{#{#{a}}}", false).eval(context));

        // 场景 C: 包装内含复杂逻辑
        Assertions.assertEquals(true, parser.parse("#{ (a + 1) == 2 }", false).eval(context));
    }


    @Test
    @DisplayName("配置化：验证自定义属性表达式标记")
    public void testCustomPropertyMarker() {
        // 将属性标记改为 '%'，即使用 %{...}
        EvaluateParser customParser = new SnelParser(100, '#', '%').forEval();

        // 场景 A: 原有的 ${} 在此解析器中应失效（被视为非法标识符起始）
        Assertions.assertThrows(CompilationException.class, () -> {
            customParser.parse("${test}", false);
        });

        // 场景 B: 新的 %{} 应当被识别为 PropertyNode (TemplateNode)
        // 只要不报错，说明 isPropertyStart 逻辑生效
        Assertions.assertDoesNotThrow(() -> {
            customParser.parse("%{app.name}", false);
        });
    }

    @Test
    @DisplayName("配置化：验证自定义属性表达式标记")
    public void testCustomPropertyMarker2() {
        // 将属性标记改为 '%'，即使用 %{...}
        EvaluateParser customParser = new SnelParser(100, '#', '{').forEval();

        customParser.parse("{test}", false);

        // 场景 B: 新的 %{} 应当被识别为 PropertyNode (TemplateNode)
        // 只要不报错，说明 isPropertyStart 逻辑生效
        Assertions.assertDoesNotThrow(() -> {
            customParser.parse("%{app.name}", false);
        });
    }


    @Test
    @DisplayName("配置化：验证自定义表达式包装标记")
    public void testCustomExpressionMarker() {
        // 将包装标记改为 '@'，且改为方括号 '@[' ']'
        EvaluateParser customParser = new SnelParser(100, '@', '$', '[', ']').forEval();
        Map<String, Object> context = new HashMap<>();
        context.put("x", 10);

        // 场景 A: 识别 @[x + 10]
        Expression expr = customParser.parse("@[x + 10]", false);
        Assertions.assertEquals(20, expr.eval(context));

        // 场景 B: 嵌套识别 @[ @[x] ]
        Expression exprNested = customParser.parse("@[@[x]]", false);
        Assertions.assertEquals(10, exprNested.eval(context));

        // 场景 C: 原有的 #{} 应当失效
        Assertions.assertThrows(CompilationException.class, () -> {
            customParser.parse("#{x}", false);
        });
    }
}