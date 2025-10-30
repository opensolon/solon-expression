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

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

/**
 * 比较表达式节点（如 >, <, ==）
 *
 * @author noear
 * @since 3.1
 */
public class ComparisonNode implements Expression<Boolean> {
    private final ComparisonOp operator; // 比较运算符，如 ">", "<", "=="
    private final Expression left;
    private final Expression right;
    private final boolean leftIsTemplate;
    private final boolean rightIsTemplate;

    /**
     * 获取操作符
     */
    public ComparisonOp getOperator() {
        return operator;
    }

    /**
     * 获取左侧
     */
    public Expression getLeft() {
        return left;
    }

    /**
     * 获取右侧
     */
    public Expression getRight() {
        return right;
    }

    public ComparisonNode(ComparisonOp operator, Expression left, Expression right) {
        this.operator = operator;
        this.left = left;
        this.right = right;

        this.leftIsTemplate = left instanceof TemplateNode;
        this.rightIsTemplate = right instanceof TemplateNode;
    }

    @Override
    public Boolean eval(Function context) {
        Object leftValue = left.eval(context);
        Object rightValue = right.eval(context);

        if (leftIsTemplate) {
            leftValue = getOptimizeValue(leftValue, rightValue);
        } else if (rightIsTemplate) {
            rightValue = getOptimizeValue(rightValue, leftValue);
        }

        if (operator == ComparisonOp.eq) {
            // ==
            if (leftValue instanceof Number && rightValue instanceof Number) {
                return ((Number) leftValue).doubleValue() == ((Number) rightValue).doubleValue();
            } else {
                return Objects.equals(leftValue, rightValue);
            }
        } else if (operator == ComparisonOp.neq) {
            // !=
            if (leftValue instanceof Number && rightValue instanceof Number) {
                return ((Number) leftValue).doubleValue() != ((Number) rightValue).doubleValue();
            } else {
                return Objects.equals(leftValue, rightValue) == false;
            }
        } else if (operator == ComparisonOp.in) {
            if (rightValue instanceof Collection) {
                return ((Collection) rightValue).contains(leftValue);
            } else {
                return false;
            }
        } else if (operator == ComparisonOp.nin) {
            if (rightValue instanceof Collection) {
                return ((Collection) rightValue).contains(leftValue) == false;
            } else {
                return false;
            }
        } else {
            if (leftValue == null || rightValue == null) {
                return false;
            }

            switch (operator) {
                case gt:
                    return ((Number) leftValue).doubleValue() > ((Number) rightValue).doubleValue();
                case gte:
                    return ((Number) leftValue).doubleValue() >= ((Number) rightValue).doubleValue();
                case lt:
                    return ((Number) leftValue).doubleValue() < ((Number) rightValue).doubleValue();
                case lte:
                    return ((Number) leftValue).doubleValue() <= ((Number) rightValue).doubleValue();
                case lk:
                    return (leftValue.toString()).contains(rightValue.toString());
                case nlk:
                    return (leftValue.toString()).contains(rightValue.toString()) == false;
                default:
                    throw new IllegalArgumentException("Unknown operator: " + operator);
            }
        }
    }

    /**
     * 获取优化值
     */
    protected Object getOptimizeValue(Object val, Object ref) {
        if (val instanceof String) {
            if (ref instanceof Boolean) {
                return getBoolean((String) val, false);
            } else if (ref instanceof Number) {
                return getNumber((String) val, 0L);
            }
        }

        return val;
    }

    protected boolean getBoolean(String value, boolean defaultValue) {
        if (value == null && value.length() == 0) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
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