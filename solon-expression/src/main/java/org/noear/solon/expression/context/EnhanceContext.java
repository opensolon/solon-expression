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
public class EnhanceContext<T extends Object, Slf extends EnhanceContext> implements Function<String, Object>, TypeGuidance, PropertiesGuidance, ReturnGuidance {
    protected final T target;
    protected final boolean isMap;

    private TypeGuidance typeGuidance = TypeGuidanceUnsafety.INSTANCE;
    private Properties properties;

    private boolean allowPropertyDefault = true;
    private boolean allowPropertyNesting = false;
    private boolean allowTextAsProperty = false;
    private boolean allowReturnNull = false;

    public EnhanceContext(T target) {
        this.target = target;
        this.isMap = target instanceof Map;
    }

    public Slf forProperties(Properties properties) {
        this.properties = properties;
        return (Slf) this;
    }

    public Slf forAllowPropertyDefault(boolean allowPropertyDefault) {
        this.allowPropertyDefault = allowPropertyDefault;
        return (Slf) this;
    }

    public Slf forAllowPropertyNesting(boolean allowPropertyNesting) {
        this.allowPropertyNesting = allowPropertyNesting;
        return (Slf) this;
    }

    public Slf forAllowTextAsProperty(boolean allowTextAsProperty) {
        this.allowTextAsProperty = allowTextAsProperty;
        return (Slf) this;
    }

    public Slf forAllowReturnNull(boolean allowReturnNull) {
        this.allowReturnNull = allowReturnNull;
        return (Slf) this;
    }

    public Slf forTypeGuidance(TypeGuidance typeGuidance) {
        this.typeGuidance = typeGuidance;
        return (Slf) this;
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
            PropertyHolder tmp = ReflectionUtil.getInstance().getProperty(target.getClass(), name);

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
            throw new EvaluationException("The current context is not supported: 'T(.)'");
        } else {
            return typeGuidance.getType(typeName);
        }
    }

    public T getTarget() {
        //方便单测用
        return target;
    }

    public TypeGuidance getTypeGuidance() {
        //方便单测用
        return typeGuidance;
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

    @Override
    public boolean allowPropertyNesting() {
        return allowPropertyNesting;
    }

    @Override
    public boolean allowTextAsProperty() {
        return allowTextAsProperty;
    }

    //ReturnGuidance
    @Override
    public boolean allowReturnNull() {
        return allowReturnNull;
    }
}