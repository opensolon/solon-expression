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

import java.util.function.Function;

/**
 * Elvis 操作符表达式节点（?: 操作符）
 *
 * @author noear
 * @since 3.1
 */
public class ElvisNode implements Expression {
    private final Expression left;
    private final Expression right;

    public ElvisNode(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public Object eval(Function context) {
        Object leftValue = left.eval(context);
        if (leftValue != null) {
            return leftValue;
        }
        return right.eval(context);
    }

    @Override
    public String toString() {
        return left + " ?: " + right;
    }
}