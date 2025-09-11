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
 * 安全导航表达式节点（?. 操作符）
 *
 * @author noear
 * @since 3.1
 */
public class SafeNavigationNode implements Expression {
    private final Expression target;
    private final String propertyName;

    public SafeNavigationNode(Expression target, String propertyName) {
        this.target = target;
        this.propertyName = propertyName;
    }

    public Expression getTarget() {
        return target;
    }

    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public Object eval(Function context) {
        Object targetValue = target.eval(context);
        if (targetValue == null) {
            return null;
        }

        // 使用 PropertyNode 来访问属性
        PropertyNode propertyNode = new PropertyNode(new ConstantNode(targetValue), propertyName);
        return propertyNode.eval(context);
    }

    @Override
    public String toString() {
        return target + "?." + propertyName;
    }
}