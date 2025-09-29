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
 * 逻辑表达式节点（如 AND, OR, NOT）
 *
 * @author noear
 * @since 3.1
 */
public class LogicalNode implements Expression<Boolean> {
    private LogicalOp operator; // 逻辑运算符，如 "AND", "OR"
    private Expression left;  // 左子节点
    private Expression right; // 右子节点

    /**
     * 获取操作符
     */
    public LogicalOp getOperator() {
        return operator;
    }

    /**
     * 获取左节点
     */
    public Expression getLeft() {
        return left;
    }

    /**
     * 获取右节点
     */
    public Expression getRight() {
        return right;
    }

    public LogicalNode(LogicalOp operator, Expression left, Expression right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public Boolean eval(Function context) {
        boolean leftValue = getBoolean(left, context);
        boolean rightValue = getBoolean(right, context);

        if (operator == LogicalOp.AND) {
            return leftValue && rightValue;
        } else if (operator == LogicalOp.OR) {
            return leftValue || rightValue;
        } else {
            return leftValue == false;
        }
    }

    protected boolean getBoolean(Expression expression, Function context) {
        if (expression == null) {
            return false;
        }

        Object value = expression.eval(context);

        if (value instanceof Boolean) {
            //布尔
            return (Boolean) value;
        } else if (value instanceof String) {
            //非空字符串
            return ((String) value).length() > 0;
        } else {
            //非null对象
            return value != null;
        }
    }

    @Override
    public String toString() {
        return "(" + left + " " + operator.getCode() + " " + right + ")";
    }
}