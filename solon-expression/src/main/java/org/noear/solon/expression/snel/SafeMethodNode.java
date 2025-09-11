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

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

/**
 * 安全方法调用节点（?. 操作符的方法调用版本）
 *
 * @author noear
 * @since 3.1
 */
public class SafeMethodNode implements Expression {
    private final SafeNavigationNode safeNode;
    private final String methodName;
    private final List<Expression> args;

    public SafeMethodNode(SafeNavigationNode safeNode, String methodName, List<Expression> args) {
        this.safeNode = safeNode;
        this.methodName = methodName;
        this.args = args;
    }

    @Override
    public Object eval(Function context) {
        Object targetValue = safeNode.getTarget().eval(context);
        if (targetValue == null) {
            return null;
        }

        // 使用 MethodNode 来调用方法
        MethodNode methodNode = new MethodNode(new ConstantNode(targetValue), methodName, args);
        return methodNode.eval(context);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(safeNode);
        buf.append(".");
        buf.append(methodName);
        buf.append("(");

        for (Expression arg1 : args) {
            buf.append(arg1).append(",");
        }

        if (args.size() > 0) {
            buf.setLength(buf.length() - 1);
        }
        buf.append(")");

        return buf.toString();
    }
}