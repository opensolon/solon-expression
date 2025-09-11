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
 * 安全属性访问节点（安全导航操作符的方括号版本）
 *
 * @author noear
 * @since 3.1
 */
public class SafePropertyNode implements Expression {
    private final SafeNavigationNode safeNode;
    private final Expression property;

    public SafePropertyNode(SafeNavigationNode safeNode, Expression property) {
        this.safeNode = safeNode;
        this.property = property;
    }

    @Override
    public Object eval(Function context) {
        Object targetValue = safeNode.getTarget().eval(context);
        if (targetValue == null) {
            return null;
        }

        // 使用 PropertyNode 来访问属性
        PropertyNode propertyNode = new PropertyNode(new ConstantNode(targetValue), property);
        return propertyNode.eval(context);
    }

    @Override
    public String toString() {
        return safeNode + "[" + property + "]";
    }
}