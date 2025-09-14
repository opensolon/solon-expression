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
package org.noear.solon.expression.context;

import org.noear.solon.expression.exception.EvaluationException;
import org.noear.solon.expression.guidance.PropertiesGuidance;
import org.noear.solon.expression.guidance.ReturnGuidance;
import org.noear.solon.expression.guidance.TypeGuidance;
import org.noear.solon.expression.guidance.TypeGuidanceUnsafety;
import org.noear.solon.expression.snel.PropertyHolder;
import org.noear.solon.expression.snel.ReflectionUtil;

import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

/**
 * 增强上下文
 *
 * @author noear
 * @since 3.2
 * @since 3.6
 */
public class EnhanceContext implements Function<String, Object>, TypeGuidance, PropertiesGuidance, ReturnGuidance {
    private final Object target;
    private final boolean isMap;

    private TypeGuidance typeGuidance = TypeGuidanceUnsafety.INSTANCE;
    private Properties properties;
    private boolean allowPropertyDefault = true;
    private boolean isReturnNull;

    public EnhanceContext(Object target) {
        this.target = target;
        this.isMap = target instanceof Map;
    }

    public EnhanceContext forProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    public EnhanceContext forAllowPropertyDefault(boolean allowPropertyDefault) {
        this.allowPropertyDefault = allowPropertyDefault;
        return this;
    }

    public EnhanceContext forTypeGuidance(TypeGuidance typeGuidance) {
        this.typeGuidance = typeGuidance;
        return this;
    }

    public EnhanceContext forReturnNull(boolean isReturnNull) {
        this.isReturnNull = isReturnNull;
        return this;
    }

    private Object lastValue;

    @Override
    public Object apply(String name) {
        if (target == null) {
            return null;
        }

        if ("root".equals(name)) {
            return target;
        }

        if ("this".equals(name)) {
            if (lastValue == null) {
                return target;
            } else {
                return lastValue;
            }
        }

        if (isMap) {
            lastValue = ((Map) target).get(name);
        } else {
            PropertyHolder tmp = ReflectionUtil.getProperty(target.getClass(), name);

            try {
                lastValue = tmp.getValue(target);
            } catch (Throwable e) {
                throw new EvaluationException("Failed to access property: " + name, e);
            }
        }

        return lastValue;
    }

    //TypeGuidance
    @Override
    public Class<?> getType(String typeName) throws EvaluationException {
        if (typeGuidance == null) {
            throw new IllegalStateException("The current context is not supported: 'T(.)'");
        } else {
            return typeGuidance.getType(typeName);
        }
    }

    //PropertiesGuidance
    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public boolean allowPropertyDefault() {
        return allowPropertyDefault;
    }

    //ReturnGuidance
    @Override
    public boolean isReturnNull() {
        return isReturnNull;
    }
}