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

import org.noear.solon.expression.Expression;
import org.noear.solon.expression.exception.EvaluationException;

import java.util.function.Function;


/**
 * 算数表达式节点
 *
 * @author noear
 * @since 3.1
 */
public class ArithmeticNode implements Expression {
    private final ArithmeticOp operator;
    private final Expression left;
    private final Expression right;
    private final boolean leftIsTemplate;
    private final boolean rightIsTemplate;

    public ArithmeticNode(ArithmeticOp operator, Expression left, Expression right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
        this.leftIsTemplate = left instanceof TemplateNode;
        this.rightIsTemplate = right instanceof TemplateNode;
    }

    @Override
    public Object eval(Function context) {
        Object leftValue = left.eval(context);
        Object rightValue = right.eval(context);

        if (leftIsTemplate) {
            leftValue = getOptimizeValue(leftValue, rightValue);
        } else if (rightIsTemplate) {
            rightValue = getOptimizeValue(rightValue, leftValue);
        }

        // 处理加法中的非数值类型拼接
        if (operator == ArithmeticOp.ADD) {
            if (leftValue instanceof String || rightValue instanceof String) {
                return String.valueOf(leftValue) + rightValue;
            }
        }

        if (leftValue == null) {
            throw new EvaluationException("Arithmetic left value is null");
        }

        if (rightValue == null) {
            throw new EvaluationException("Arithmetic right value is null");
        }

        // 动态分派数值计算逻辑
        return calculateNumbers((Number) leftValue, (Number) rightValue);
    }

    private Number calculateNumbers(Number a, Number b) {
        // 优先级: double > float > long > int
        if (isDouble(a) || isDouble(b)) {
            return calculateAsDouble(a, b);
        } else if (isFloat(a) || isFloat(b)) {
            return calculateAsFloat(a, b);
        } else if (isLong(a) || isLong(b)) {
            return calculateAsLong(a, b);
        } else {
            return calculateAsInt(a, b);
        }
    }

    // 判断是否为 double
    private boolean isDouble(Number n) {
        return n instanceof Double;
    }

    // 判断是否为 float
    private boolean isFloat(Number n) {
        return n instanceof Float;
    }

    // 判断是否为 long
    private boolean isLong(Number n) {
        return n instanceof Long;
    }

    // 计算逻辑（按类型分派）
    private double calculateAsDouble(Number a, Number b) {
        double aVal = a.doubleValue();
        double bVal = b.doubleValue();
        switch (operator) {
            case ADD:
                return aVal + bVal;
            case SUB:
                return aVal - bVal;
            case MUL:
                return aVal * bVal;
            case DIV:
                return aVal / bVal;
            case MOD:
                return aVal % bVal;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }

    private float calculateAsFloat(Number a, Number b) {
        float aVal = a.floatValue();
        float bVal = b.floatValue();
        switch (operator) {
            case ADD:
                return aVal + bVal;
            case SUB:
                return aVal - bVal;
            case MUL:
                return aVal * bVal;
            case DIV:
                return aVal / bVal;
            case MOD:
                return aVal % bVal;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }

    private long calculateAsLong(Number a, Number b) {
        long aVal = a.longValue();
        long bVal = b.longValue();
        switch (operator) {
            case ADD:
                return aVal + bVal;
            case SUB:
                return aVal - bVal;
            case MUL:
                return aVal * bVal;
            case DIV:
                return aVal / bVal;
            case MOD:
                return aVal % bVal;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }

    private int calculateAsInt(Number a, Number b) {
        int aVal = a.intValue();
        int bVal = b.intValue();
        switch (operator) {
            case ADD:
                return aVal + bVal;
            case SUB:
                return aVal - bVal;
            case MUL:
                return aVal * bVal;
            case DIV:
                return aVal / bVal;
            case MOD:
                return aVal % bVal;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }

    /**
     * 获取优化值
     */
    protected Object getOptimizeValue(Object val, Object ref) {
        if (val instanceof String) {
            if (ref instanceof Number) {
                return getNumber((String) val, 0L);
            }
        }

        return val;
    }

    protected Number getNumber(String value, Number defaultValue) {
        if (value == null && value.length() == 0) {
            return defaultValue;
        }

        return Double.parseDouble(value);
    }

    @Override
    public String toString() {
        return "(" + left + " " + operator.getCode() + " " + right + ")";
    }
}