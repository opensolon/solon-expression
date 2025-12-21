/*
 * Copyright 2017-2025 noear.org and authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.noear.solon.expression.snel;

import java.util.ArrayList;
import java.util.List;

import org.noear.solon.expression.Expression;
import org.noear.solon.expression.Parser;
import org.noear.solon.expression.exception.CompilationException;
import org.noear.solon.expression.util.LRUCache;


/**
 * Solon 表达式语言求值解析器
 *
 * <p>
 * 支持以下特性：
 * 1. 变量访问：`user.name`、`order['created']['name']` 等嵌套属性
 * 2. 方法调用：`Math.add(1, 2)`、`user.getName()` 等
 * 3. 逻辑运算：AND/OR/NOT，支持短路逻辑
 * 4. 比较运算：>、<、==、!=、IN、LIKE 等
 * 5. 算术运算：+、-、*、/、%
 * 6. 三元表达式：condition ? trueExpr : falseExpr
 * 7. 布尔常量：直接解析 true/false
 * 8. 空值安全：属性或方法不存在时返回 null，避免 NPE
 * 9. 支持 ${} 属性表达式（带默认值）
 * 10. 支持安全导航操作符 `?.`
 * 11. 支持 Elvis 操作符 `?:`
 * </p>
 *
 * @author noear
 * @since 3.1
 * @since 3.8
 * */
public class SnelEvaluateParser implements Parser {
    private static final SnelEvaluateParser INSTANCE = new SnelEvaluateParser(10000);
    private final LRUCache<String, Expression> exprCached;

    private final char MARK_START_EXPRESSION; // 默认 '#'
    private final char MARK_START_PROPERTIES; // 默认 '$'
    private final char MARK_BRACE_OPEN;       // 默认 '{'
    private final char MARK_BRACE_CLOSE;      // 默认 '}'

    public static SnelEvaluateParser getInstance() {
        return INSTANCE;
    }

    public SnelEvaluateParser(int cahceCapacity) {
        this(cahceCapacity, '#', '$');
    }

    public SnelEvaluateParser(int cahceCapacity, char expreStartMark, char propsStartMark) {
        this(cahceCapacity, expreStartMark, propsStartMark, '{', '}');
    }

    public SnelEvaluateParser(int cahceCapacity, char expreStartMark, char propsStartMark, char braceOpenMark, char braceCloseMark) {
        this.exprCached = new LRUCache<>(cahceCapacity);
        this.MARK_START_EXPRESSION = expreStartMark;
        this.MARK_START_PROPERTIES = propsStartMark;
        this.MARK_BRACE_OPEN = braceOpenMark;
        this.MARK_BRACE_CLOSE = braceCloseMark;
    }

    @Override
    public Expression parse(String expr, boolean cached) {
        if (cached) {
            return exprCached.computeIfAbsent(expr, this::parseDo);
        } else {
            return parseDo(expr);
        }
    }

    protected Expression parseDo(String expr) {
        // 检查是否是整体包装的属性表达式 (例如 "${...}")
        if (isFullMarkerExpression(expr, MARK_START_PROPERTIES)) {
            return parsePropertyExpression(expr);
        }

        // 检查是否是整体包装的解析表达式 (例如 "#{...}" 或 "{...}")
        if (isFullMarkerExpression(expr, MARK_START_EXPRESSION) || isFullMarkerExpression(expr, (char) 0)) {
            // 剥离外壳，递归解析内部内容。如果是 { 开头则偏移1，如果是 #{ 开头则偏移2
            int markerLen = (expr.charAt(0) == MARK_BRACE_OPEN) ? 1 : 2;
            return parseDo(expr.substring(markerLen, expr.length() - 1));
        }

        ParserState state = new ParserState(expr);
        Expression result = parseElvisExpression(state);
        if (state.getCurrentChar() != -1) {
            throw new CompilationException("Unexpected trailing character: " + (char) state.getCurrentChar());
        }
        return result;
    }

    /**
     * 检查是否是整体被标记包裹的表达式
     */
    private boolean isFullMarkerExpression(String expr, char marker) {
        int len = expr.length();
        if (len < 2) return false;

        if (marker != 0 && expr.charAt(0) == marker) {
            // 情况：#{...} 或 ${...}
            return len > 2 && expr.charAt(1) == MARK_BRACE_OPEN && expr.charAt(len - 1) == MARK_BRACE_CLOSE;
        } else if (marker == 0 && expr.charAt(0) == MARK_BRACE_OPEN) {
            // 情况：{...}
            return expr.charAt(len - 1) == MARK_BRACE_CLOSE;
        }
        return false;
    }

    /**
     * 解析 Elvis 操作符 ?:
     */
    private Expression parseElvisExpression(ParserState state) {
        Expression left = parseTernaryExpression(state);
        state.skipWhitespace();

        // 检查 Elvis 操作符 ?: (注意：这里要区分三元表达式中的 ?)
        if (state.getCurrentChar() == '?' && state.peekNextChar() == ':') {
            state.nextChar(); // 跳过 ?
            state.nextChar(); // 跳过 :
            Expression right = parseElvisExpression(state);
            return new ElvisNode(left, right);
        }
        return left;
    }

    /**
     * 解析安全导航操作符 ?.
     */
    private Expression parseSafeNavigationExpression(ParserState state) {
        Expression left = parsePrimaryExpression(state);

        // 处理安全导航操作符 ?.
        while (state.getCurrentChar() == '?' && state.peekNextChar() == '.') {
            state.nextChar(); // 跳过 ?
            state.nextChar(); // 跳过 .
            state.skipWhitespace();

            String identifier = parseIdentifier(state);
            left = new SafeNavigationNode(left, identifier);

            // 处理安全导航后的方法调用
            left = parsePostfixAfterSafeNavigation(state, left);
        }
        return left;
    }

    /**
     * 解析安全导航后的后缀操作（方法调用等）
     */
    private Expression parsePostfixAfterSafeNavigation(ParserState state, Expression expr) {
        while (true) {
            state.skipWhitespace();
            if (eat(state, '(')) {
                List<Expression> args = parseMethodArguments(state);
                require(state, ')', "Expected ')' after arguments");
                expr = new MethodNode((SafeNavigationNode) expr, args);
            } else if (eat(state, '[')) {
                Expression indexExpr = parseLogicalOrExpression(state);
                require(state, ']', "Expected ']' after index");
                expr = new PropertyNode((SafeNavigationNode) expr, indexExpr);
            } else {
                break;
            }
        }
        return expr;
    }

    /**
     * 检查是否是属性表达式（用于 Primary 内部判定）
     */
    private boolean isPropertyStart(ParserState state) {
        return state.getCurrentChar() == MARK_START_PROPERTIES && state.peekNextChar() == MARK_BRACE_OPEN;
    }

    /**
     * 检查是否是包装表达式起始
     */
    private boolean isExpressionStart(ParserState state) {
        return (state.getCurrentChar() == MARK_START_EXPRESSION && state.peekNextChar() == MARK_BRACE_OPEN)
                || state.getCurrentChar() == MARK_BRACE_OPEN;
    }

    /**
     * 解析属性表达式内容（剥离外壳，交给 TemplateNode）
     */
    private Expression parsePropertyExpression(String expr) {
        // 自动识别偏移量：如果是 { 则偏移1，如果是 ${ 则偏移2
        int offset = (expr.charAt(0) == MARK_BRACE_OPEN) ? 1 : 2;
        String content = expr.substring(offset, expr.length() - 1);

        // 使用模板解析器来处理属性表达式（支持默认值）
        List<TemplateFragment> fragments = new ArrayList<>();
        fragments.add(new TemplateFragment(TemplateMarker.PROPERTIES, content));

        return new TemplateNode(fragments);
    }

    // 以下为递归下降解析器的核心方法 ----------------------------

    /**
     * 解析三元表达式：condition ? trueExpr : falseExpr
     */
    private Expression parseTernaryExpression(ParserState state) {
        Expression condition = parseLogicalOrExpression(state);
        state.skipWhitespace();

        // 检查三元操作符 ? :
        if (state.getCurrentChar() == '?' && state.peekNextChar() != '.' && state.peekNextChar() != ':') {
            state.nextChar(); // 跳过 ?
            state.skipWhitespace();
            Expression<Object> trueExpr = parseTernaryExpression(state);
            require(state, ':', "Expected ':' in ternary expression");
            Expression<Object> falseExpr = parseTernaryExpression(state);
            return new TernaryNode((Expression<Boolean>) condition, trueExpr, falseExpr);
        }
        return condition;
    }

    /**
     * 解析逻辑 OR（|| 或 OR）
     */
    private Expression parseLogicalOrExpression(ParserState state) {
        Expression left = parseLogicalAndExpression(state);
        state.skipWhitespace();
        while (eat(state, "OR") || eat(state, "||")) {
            left = new LogicalNode(LogicalOp.OR, left, parseLogicalAndExpression(state));
        }
        return left;
    }

    /**
     * 解析逻辑 AND（&& 或 AND）
     */
    private Expression parseLogicalAndExpression(ParserState state) {
        Expression left = parseLogicalNotExpression(state);
        state.skipWhitespace();
        while (eat(state, "AND") || eat(state, "&&")) {
            left = new LogicalNode(LogicalOp.AND, left, parseLogicalNotExpression(state));
        }
        return left;
    }

    /**
     * 解析逻辑 NOT（前置 NOT 运算符）
     */
    private Expression parseLogicalNotExpression(ParserState state) {
        if (eat(state, "NOT") || eat(state, "!")) {
            return new LogicalNode(LogicalOp.NOT, parseComparisonExpression(state), null);
        }
        return parseComparisonExpression(state);
    }

    /**
     * 解析比较表达式（包括 IN/LIKE 等高级操作符）
     */
    private Expression parseComparisonExpression(ParserState state) {
        Expression left = parseAdditiveExpression(state);
        state.skipWhitespace();

        if (isComparisonOperatorStart(state.getCurrentChar())) {
            String op = parseComparisonOperator(state);
            return new ComparisonNode(ComparisonOp.parse(op), left, parseAdditiveExpression(state));
        } else if (eat(state, "IN")) {
            return new ComparisonNode(ComparisonOp.in, left, parseListExpression(state));
        } else if (eat(state, "LIKE")) {
            return new ComparisonNode(ComparisonOp.lk, left, parseAdditiveExpression(state));
        } else if (eat(state, "NOT")) {
            if (eat(state, "IN")) {
                return new ComparisonNode(ComparisonOp.nin, left, parseListExpression(state));
            } else if (eat(state, "LIKE")) {
                return new ComparisonNode(ComparisonOp.nlk, left, parseAdditiveExpression(state));
            }
            throw new CompilationException("Invalid NOT expression");
        }
        return left;
    }

    /**
     * 解析加减法表达式
     */
    private Expression parseAdditiveExpression(ParserState state) {
        Expression left = parseMultiplicativeExpression(state);
        while (true) {
            if (eat(state, '+')) {
                left = new ArithmeticNode(ArithmeticOp.ADD, left, parseMultiplicativeExpression(state));
            } else if (eat(state, '-')) {
                left = new ArithmeticNode(ArithmeticOp.SUB, left, parseMultiplicativeExpression(state));
            } else {
                break;
            }
        }
        return left;
    }

    /**
     * 解析乘除法表达式
     */
    private Expression parseMultiplicativeExpression(ParserState state) {
        Expression left = parseSafeNavigationExpression(state);
        while (true) {
            if (eat(state, '*')) {
                left = new ArithmeticNode(ArithmeticOp.MUL, left, parseSafeNavigationExpression(state));
            } else if (eat(state, '/')) {
                left = new ArithmeticNode(ArithmeticOp.DIV, left, parseSafeNavigationExpression(state));
            } else if (eat(state, '%')) {
                left = new ArithmeticNode(ArithmeticOp.MOD, left, parseSafeNavigationExpression(state));
            } else {
                break;
            }
        }
        return left;
    }

    /**
     * 解析基本表达式单元：
     * 1. 括号表达式 ( ... )
     * 2. 数字字面量（如 123, 45.67）
     * 3. 字符串字面量（如 'hello'）
     * 4. 布尔常量（true/false）
     * 5. 变量或属性访问（如 user.name）
     * 6. 方法调用（如 Math.add(1, 2)）
     * 7. ${} 属性表达式 或 #{} 包装表达式
     */
    private Expression parsePrimaryExpression(ParserState state) {
        state.skipWhitespace();
        Expression expr;

        if (eat(state, "T(")) {
            // 检查是否是 T(...) 类型表达式
            String className = parseClassName(state);
            require(state, ')', "Expected ')' after class name in T(...) expression");
            expr = new TypeNode(className);
        } else if (isPropertyStart(state)) {
            // 检查是否是 ${} 属性表达式
            String propertyExpr = parseMarkerExpressionContent(state);
            expr = parsePropertyExpression(propertyExpr);
        } else if (isExpressionStart(state)) {
            // 检查是否是 #{} 或 {} 包装表达式
            String innerExprStr = parseMarkerExpressionContent(state);
            // 核心修正：根据起始字符剥离外壳，递归解析内部纯算术/逻辑内容
            int markerLen = (innerExprStr.charAt(0) == MARK_BRACE_OPEN) ? 1 : 2;
            String rawContent = innerExprStr.substring(markerLen, innerExprStr.length() - 1);
            expr = parseDo(rawContent);
        } else if (eat(state, '(')) {
            expr = parseElvisExpression(state);
            require(state, ')', "Expected ')' after expression");
        } else if (state.isNumber()) {
            expr = new ConstantNode(parseNumber(state));
        } else if (state.isString()) {
            expr = new ConstantNode(parseString(state));
        } else if (state.isArray()) {
            expr = parseListExpression(state);
        } else if (checkKeyword(state, "true")) {
            expr = new ConstantNode(true);
        } else if (checkKeyword(state, "false")) {
            expr = new ConstantNode(false);
        } else if (checkKeyword(state, "null")) {
            expr = new ConstantNode(null);
        } else {
            String identifier = parseIdentifier(state);
            expr = new VariableNode(identifier);
        }

        return parsePostfix(state, expr);
    }

    /**
     * 解析类名（允许点分隔的标识符）
     */
    private String parseClassName(ParserState state) {
        StringBuilder sb = new StringBuilder();
        while (state.isIdentifierStart() || state.getCurrentChar() == '.') {
            sb.append((char) state.getCurrentChar());
            state.nextChar();
        }
        return sb.toString();
    }

    /**
     * 解析带标记的表达式（如 ${xxx} 或 #{xxx} 或 {xxx}）
     */
    private String parseMarkerExpressionContent(ParserState state) {
        StringBuilder sb = new StringBuilder();
        int first = state.getCurrentChar();
        sb.append((char) first); // marker char (# / $ / {)
        state.nextChar();

        if (first != MARK_BRACE_OPEN) {
            sb.append((char) state.getCurrentChar()); // { char
            state.nextChar();
        }

        int braceCount = 1; // 已经进入一个大括号
        while (state.getCurrentChar() != -1 && braceCount > 0) {
            char c = (char) state.getCurrentChar();
            sb.append(c);
            state.nextChar();

            if (c == MARK_BRACE_OPEN) {
                braceCount++;
            } else if (c == MARK_BRACE_CLOSE) {
                braceCount--;
            }
        }

        return sb.toString();
    }

    /**
     * 处理表达式后的点、方括号和方法调用（支持安全导航）
     * */
    private Expression parsePostfix(ParserState state, Expression expr) {
        while (true) {
            state.skipWhitespace();
            if (eat(state, '.')) {
                String prop = parseIdentifier(state);
                expr = new PropertyNode(expr, prop);
            } else if (eat(state, '[')) {
                Expression indexExpr = parseLogicalOrExpression(state);
                require(state, ']', "Expected ']' after index");
                expr = new PropertyNode(expr, indexExpr);
            } else if (eat(state, '(')) {
                List<Expression> args = parseMethodArguments(state);
                require(state, ')', "Expected ')' after arguments");
                if (expr instanceof PropertyNode) {
                    PropertyNode propNode = (PropertyNode) expr;
                    expr = new MethodNode(propNode.getTarget(), propNode.getPropertyName(), args);
                } else if (expr instanceof VariableNode) {
                    VariableNode varNode = (VariableNode) expr;
                    expr = new MethodNode(varNode, varNode.getName(), args);
                } else {
                    throw new CompilationException("Invalid method call target: " + expr);
                }
            } else {
                break;
            }
        }
        return expr;
    }

    /**
     * 解析变量、属性访问或方法调用
     */
    private Expression parseVariableOrMethodCall(ParserState state) {
        String identifier = parseIdentifier(state);
        Expression expr = new VariableNode(identifier);

        // 循环处理后续的属性访问操作（. 或 [] 或 ()）
        while (true) {
            state.skipWhitespace();
            if (eat(state, '.')) {
                // 解析点号属性访问：obj.property
                String prop = parseIdentifier(state);
                expr = new PropertyNode(expr, prop);
            } else if (eat(state, '[')) {
                // 解析方括号属性访问：obj['property'] 或 obj[0]
                Expression propExpr = parseLogicalOrExpression(state);
                eat(state, ']');
                expr = new PropertyNode(expr, propExpr);
            } else if (eat(state, '(')) {
                // 解析方法调用：obj.method()
                List<Expression> args = parseMethodArguments(state);
                eat(state, ')');

                // 确保 target 是属性访问节点，而不是方法名
                if (expr instanceof PropertyNode) {
                    PropertyNode propertyNode = (PropertyNode) expr;
                    expr = new MethodNode(propertyNode.getTarget(), propertyNode.getPropertyName(), args);
                } else if (expr instanceof VariableNode) {
                    // 如果 expr 是变量节点，直接使用方法名
                    expr = new MethodNode(expr, identifier, args);
                } else {
                    throw new CompilationException("Invalid method call target: " + expr);
                }
            } else {
                break;
            }
        }
        return expr;
    }

    /**
     * 解析方法参数列表
     */
    private List<Expression> parseMethodArguments(ParserState state) {
        List<Expression> args = new ArrayList<>();
        while (state.getCurrentChar() != ')') {
            args.add(parseLogicalOrExpression(state));
            if (eat(state, ',')) continue;
        }
        return args;
    }

    // 以下为工具方法 --------------------------------

    /**
     * 检查字符是否是比较操作符的起始字符（>、<、=、!）
     */
    private boolean isComparisonOperatorStart(int c) {
        return c == '>' || c == '<' || c == '=' || c == '!';
    }

    /**
     * 解析比较操作符（支持 ==、!=、>=、<=）
     */
    private String parseComparisonOperator(ParserState state) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) state.getCurrentChar());
        state.nextChar();
        if (state.getCurrentChar() == '=') {
            sb.append((char) state.getCurrentChar());
            state.nextChar();
        }
        return sb.toString();
    }

    /**
     * 解析列表（用于 IN 操作符）
     */
    private Expression parseListExpression(ParserState state) {
        if (eat(state, '[')) {
            List<Object> list = new ArrayList<>();
            while (state.getCurrentChar() != ']') {
                list.add(parseValue(state));
                if (eat(state, ',')) continue;
            }
            eat(state, ']');
            return new ConstantNode(list);
        } else {
            return parseTernaryExpression(state);
        }
    }

    /**
     * 解析值（数字、字符串、变量）
     */
    private Object parseValue(ParserState state) {
        state.skipWhitespace();
        if (state.isString()) {
            return parseString(state);
        } else if (state.isNumber()) {
            return parseNumber(state);
        } else if (checkKeyword(state, "true")) {
            return true;
        } else if (checkKeyword(state, "false")) {
            return false;
        } else if (checkKeyword(state, "null")) {
            return null;
        } else {
            return parseVariableOrMethodCall(state); // 简化处理
        }
    }

    /**
     * 解析字符串
     */
    private String parseString(ParserState state) {
        char quote = (char) state.getCurrentChar();
        state.nextChar();
        StringBuilder sb = new StringBuilder();
        while (state.getCurrentChar() != quote) {
            sb.append((char) state.getCurrentChar());
            state.nextChar();
        }
        state.nextChar();
        return sb.toString();
    }

    /**
     * 解析数字
     */
    private Number parseNumber(ParserState state) {
        StringBuilder sb = new StringBuilder();
        boolean isFloat = false;
        boolean isDouble = false;
        boolean isLong = false;

        while (state.isNumber() || state.getCurrentChar() == '.') {
            if (state.getCurrentChar() == '.') {
                isDouble = true;
            }

            if (state.getCurrentChar() == '-') {
                if (sb.length() > 0) {
                    //识别："4.56e-3"（科学表示法） 或 "1-3"（算数）
                    char c2 = sb.charAt(sb.length() - 1);
                    if ((c2 != 'E' || c2 != 'e')) {
                        break;
                    }
                }
            }

            sb.append((char) state.getCurrentChar());
            state.nextChar();
        }

        if (Character.toUpperCase(state.getCurrentChar()) == 'L') {
            isLong = true;
            state.nextChar();
        } else if (Character.toUpperCase(state.getCurrentChar()) == 'F') {
            isFloat = true;
            isDouble = false;
            state.nextChar();
        } else if (Character.toUpperCase(state.getCurrentChar()) == 'D') {
            isDouble = true;
            state.nextChar();
        }

        String numberStr = sb.toString();
        try {
            if (isDouble) {
                return Double.parseDouble(numberStr);
            } else if (isFloat) {
                return Float.parseFloat(numberStr);
            } else if (isLong) {
                return Long.parseLong(numberStr);
            } else {
                return Integer.parseInt(numberStr);
            }
        } catch (NumberFormatException e) {
            throw new CompilationException("Invalid number format: " + numberStr, e);
        }
    }

    /**
     * 解析标识符 (排除 $)
     */
    private String parseIdentifier(ParserState state) {
        StringBuilder sb = new StringBuilder();
        while (state.isIdentifierStart()) {
            sb.append((char) state.getCurrentChar());
            state.nextChar();
        }
        return sb.toString();
    }

    /**
     * 检查并跳过指定字符串
     */
    private boolean eat(ParserState state, String expected) {
        state.skipWhitespace();
        for (int i = 0; i < expected.length(); i++) {
            if (state.getCurrentChar() != expected.charAt(i)) {
                return false;
            }
            state.nextChar();
        }
        return true;
    }

    /**
     * 检查并跳过指定字符
     */
    private boolean eat(ParserState state, char expected) {
        state.skipWhitespace();
        if (state.getCurrentChar() == expected) {
            state.nextChar();
            return true;
        }
        return false;
    }

    /**
     * 检查并跳过指定字符，否则抛出异常
     */
    private void require(ParserState state, char expected, String errorMessage) {
        state.skipWhitespace();
        if (state.getCurrentChar() != expected) {
            throw new CompilationException(errorMessage);
        }
        state.nextChar();
    }

    /**
     * 检查当前是否是关键字（如 true/false）
     */
    private boolean checkKeyword(ParserState state, String keyword) {
        state.mark();
        for (int i = 0; i < keyword.length(); i++) {
            if (state.getCurrentChar() != keyword.charAt(i)) {
                state.reset();
                return false;
            }
            state.nextChar();
        }
        if (state.isIdentifierStart()) {
            state.reset();
            return false;
        }
        return true;
    }

    // 内部类：封装解析状态 ----------------------------

    /**
     * 解析器状态跟踪器
     */
    private static class ParserState {
        private final String reader;
        private int ch;      // 当前字符
        private int position = 0;
        private int markedCh = 0;
        private int markedPosition = 0;

        public ParserState(String reader) {
            this.reader = reader;
            nextChar(); // 初始化读取第一个字符
        }

        /**
         * 获取当前字符
         */
        public int getCurrentChar() {
            return ch;
        }

        /**
         * 前进到下一个字符
         */
        public void nextChar() {
            if (position < reader.length()) {
                ch = reader.charAt(position);
                position++;
            } else {
                ch = -1;
            }
        }

        /**
         * 查看下一个字符（不移动指针）
         */
        public int peekNextChar() {
            if (position < reader.length()) {
                return reader.charAt(position);
            } else {
                return -1;
            }
        }

        /**
         * 跳过空白字符
         */
        public void skipWhitespace() {
            while (Character.isWhitespace(ch)) nextChar();
        }

        /**
         * 检查当前是否是字符串起始字符（' 或 "）
         */
        public boolean isString() {
            return ch == '\'' || ch == '"';
        }

        /**
         * 检查当前是否是数字起始字符
         */
        public boolean isNumber() {
            return Character.isDigit(ch) || ch == '-';
        }

        /**
         * 检查当前是否是数字字符
         */
        public boolean isDigit() {
            return Character.isDigit(ch);
        }

        /**
         * 检查当前是否是数组起始字符（[）
         */
        public boolean isArray() {
            return ch == '[';
        }

        /**
         * 检查当前是否是标识符字符（排除 $，仅限字母/数字/下划线）
         */
        public boolean isIdentifierStart() {
            return Character.isLetterOrDigit(ch) || ch == '_';
        }

        /**
         * 获取当前读取位置
         */
        public void mark() {
            markedCh = ch;
            markedPosition = position;
        }

        /**
         * 设置读取位置（用于回滚）
         */
        public void reset() {
            ch = markedCh;
            position = markedPosition;
        }

        @Override
        public String toString() {
            return "ParserState{" +
                    "ch='" + (char) ch + "'" +
                    ", position=" + position +
                    '}';
        }
    }
}