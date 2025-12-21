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

import org.noear.solon.expression.Parser;
import org.noear.solon.expression.Expression;
import org.noear.solon.expression.util.LRUCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Solon 表达式语言模板解析器
 *
 * <p>
 * 支持以下示例：
 * 1."name is #{user.name}, and key is ${key}"
 * 2.支持嵌套表达式，如 "Hello, #{${user.name?:Tom}}!"
 * </p>
 * @author noear
 * @since 3.1
 * @since 3.8
 */
public class SnelTemplateParser implements Parser<String> {
    private static final SnelTemplateParser INSTANCE = new SnelTemplateParser(10000);
    private final LRUCache<String, Expression<String>> exprCached;

    public SnelTemplateParser(int cahceCapacity) {
        this(cahceCapacity, '#', '$');
    }

    public SnelTemplateParser(int cahceCapacity, char expreStartMark, char propsStartMark) {
        this(cahceCapacity, expreStartMark, propsStartMark, '{', '}');
    }

    public SnelTemplateParser(int cahceCapacity, char expreStartMark, char propsStartMark, char braceOpenMark, char braceCloseMark) {
        exprCached = new LRUCache<>(cahceCapacity);

        MARK_START_EXPRESSION = expreStartMark;
        MARK_START_PROPERTIES = propsStartMark;
        MARK_BRACE_OPEN = braceOpenMark;
        MARK_BRACE_CLOSE = braceCloseMark;
    }

    private final char MARK_START_EXPRESSION;
    private final char MARK_START_PROPERTIES;
    private final char MARK_BRACE_OPEN;
    private final char MARK_BRACE_CLOSE;

    public static SnelTemplateParser getInstance() {
        return INSTANCE;
    }

    public boolean hasMarker(String expr) {
        return expr.indexOf(MARK_START_EXPRESSION) >= 0 ||
                expr.indexOf(MARK_START_PROPERTIES) >= 0;
    }

    @Override
    public Expression<String> parse(String expr, boolean cached) {
        return cached ? exprCached.computeIfAbsent(expr, this::parseDo) : parseDo(expr);
    }

    private Expression<String> parseDo(String expr) {
        List<TemplateFragment> fragments = new ArrayList<>(8);
        boolean inExpression = false;
        char marker = 0;
        int markerLen = 0;
        int textStart = 0;
        int scanPosition = 0;
        final int length = expr.length();

        while (scanPosition < length) {
            if (!inExpression) {
                // 1. 文本模式：寻找标记起始位
                int exprStart = findExpressionStart(expr, scanPosition);
                if (exprStart != -1) {
                    // 提取前置文本
                    if (textStart < exprStart) {
                        fragments.add(new TemplateFragment(TemplateMarker.TEXT, expr.substring(textStart, exprStart)));
                    }
                    marker = expr.charAt(exprStart);
                    markerLen = (marker == MARK_BRACE_OPEN) ? 1 : 2;
                    inExpression = true;
                    textStart = scanPosition = exprStart + markerLen;
                } else {
                    // 无标记，剩余部分全为文本
                    fragments.add(new TemplateFragment(TemplateMarker.TEXT, expr.substring(textStart)));
                    textStart = length;
                    break;
                }
            } else {
                // 2. 表达式模式：寻找匹配的闭合括号
                int closePos = findExpressionEnd(expr, scanPosition);
                if (closePos != -1) {
                    String content = expr.substring(textStart, closePos);
                    TemplateMarker type = (marker == MARK_START_PROPERTIES) ? TemplateMarker.PROPERTIES : TemplateMarker.EXPRESSION;
                    fragments.add(new TemplateFragment(type, content));

                    inExpression = false;
                    textStart = scanPosition = closePos + 1;
                } else {
                    // 未闭合，执行回退处理并结束
                    fragments.add(new TemplateFragment(TemplateMarker.TEXT, expr.substring(textStart - markerLen)));
                    inExpression = false; // 标记已处理
                    textStart = length;
                    break;
                }
            }
        }

        // 3. 收尾逻辑：处理循环因达到长度限制而终止时的残余状态
        if (inExpression) {
            // 处理字符串末尾是 "#{ " 或 " { " 导致 while 终止的情况
            fragments.add(new TemplateFragment(TemplateMarker.TEXT, expr.substring(textStart - markerLen)));
        } else if (textStart < length) {
            // 处理最后的剩余文本（防御性补丁）
            fragments.add(new TemplateFragment(TemplateMarker.TEXT, expr.substring(textStart)));
        }

        return new TemplateNode(fragments);
    }

    // 快速定位表达式起始标记（"#{" 或 "${"）
    private int findExpressionStart(String s, int start) {
        int len = s.length();
        for (int i = start; i < len; i++) {
            char c = s.charAt(i);

            if (c == MARK_START_EXPRESSION || c == MARK_START_PROPERTIES) {
                // 如果是单字符标记模式（如 MARK_START_PROPERTIES 本身就是 '{'）
                if (c == MARK_BRACE_OPEN) {
                    return i;
                }
                // 如果是双字符标记模式（探测下一位是否是 '{'）
                if (i + 1 < len && s.charAt(i + 1) == MARK_BRACE_OPEN) {
                    return i;
                }
            }
        }

        return -1;
    }

    // 查找表达式结束位置，平衡大括号以支持嵌套
    private int findExpressionEnd(String s, int start) {
        int braceCount = 1; // 已经有一个开括号
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == MARK_BRACE_OPEN) {
                braceCount++;
            } else if (c == MARK_BRACE_CLOSE) {
                braceCount--;
                if (braceCount == 0) {
                    return i;
                }
            }
        }

        return -1; // 未找到匹配的结束括号
    }
}