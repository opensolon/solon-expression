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
import org.noear.solon.expression.guidance.TypeGuidance;
import org.noear.solon.expression.exception.EvaluationException;

import java.util.function.Function;

/**
 * 类型表达式节点（表示 T(ClassName) 表达式，返回 Class 对象）
 *
 * @author noear
 * @since 3.1
 */
public class TypeNode implements Expression<Class<?>> {
    private final String className;
    private Class<?> type;

    public TypeNode(String className) {
        this.className = className;
    }

    @Override
    public Class<?> eval(Function context) {
        if (type == null) {
            if (context instanceof TypeGuidance) {
                type = ((TypeGuidance) context).getType(className);
            } else {
                throw new IllegalStateException("The current context is not supported: 'T(.)'");
            }
        }

        return type;
    }

    @Override
    public String toString() {
        return "T(" + className + ")";
    }
}