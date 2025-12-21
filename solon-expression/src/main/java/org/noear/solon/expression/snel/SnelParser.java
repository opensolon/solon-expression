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

/**
 * Solon 表达式语言解析器
 *
 * @author noear
 * @since 3.8
 */
public class SnelParser {
    private static final SnelParser instance = new SnelParser(2048);

    public static SnelParser getInstance() {
        return instance;
    }

    private final EvaluateParser evaluateParser;
    private final TemplateParser templateParser;

    protected final char MARK_START_EXPRESSION; // 默认 '#'
    protected final char MARK_START_PROPERTIES; // 默认 '$'
    protected final char MARK_BRACE_OPEN;       // 默认 '{'
    protected final char MARK_BRACE_CLOSE;      // 默认 '}'

    public SnelParser(int cahceCapacity) {
        this(cahceCapacity, '#', '$');
    }

    public SnelParser(int cahceCapacity, char expreStartMark, char propsStartMark) {
        this(cahceCapacity, expreStartMark, propsStartMark, '{', '}');
    }

    public SnelParser(int cahceCapacity, char expreStartMark, char propsStartMark, char braceOpenMark, char braceCloseMark) {
        //先
        this.MARK_START_EXPRESSION = expreStartMark;
        this.MARK_START_PROPERTIES = propsStartMark;
        this.MARK_BRACE_OPEN = braceOpenMark;
        this.MARK_BRACE_CLOSE = braceCloseMark;

        //后
        this.evaluateParser = new EvaluateParser(this, cahceCapacity);
        this.templateParser = new TemplateParser(this, cahceCapacity);
    }

    /// /////////////////

    /**
     * 求值表达式解析器
     */
    public EvaluateParser forEval() {
        return evaluateParser;
    }

    /**
     * 模板表达式解析器
     */
    public TemplateParser forTmpl() {
        return templateParser;
    }

    /**
     * 是否有占位符
     */
    public boolean hasMarker(String expr) {
        return expr.indexOf(MARK_START_EXPRESSION) >= 0 ||
                expr.indexOf(MARK_START_PROPERTIES) >= 0;
    }
}