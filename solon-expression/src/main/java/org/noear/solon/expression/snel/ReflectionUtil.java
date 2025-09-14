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

import org.noear.solon.expression.exception.EvaluationException;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射工具
 *
 * @author noear
 * @since 3.1
 * */
public class ReflectionUtil {
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP = new HashMap<>();

    static {
        PRIMITIVE_WRAPPER_MAP.put(byte.class, Byte.class);
        PRIMITIVE_WRAPPER_MAP.put(short.class, Short.class);
        PRIMITIVE_WRAPPER_MAP.put(int.class, Integer.class);
        PRIMITIVE_WRAPPER_MAP.put(long.class, Long.class);
        PRIMITIVE_WRAPPER_MAP.put(float.class, Float.class);
        PRIMITIVE_WRAPPER_MAP.put(double.class, Double.class);
        PRIMITIVE_WRAPPER_MAP.put(boolean.class, Boolean.class);
        // 可以添加更多的基本类型和包装类型映射
    }

    private final Map<MethodKey, Method> cache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Method[]> methodsCache = new ConcurrentHashMap<>();

    private Method[] getMethods(Class<?> clazz) {
        return methodsCache.computeIfAbsent(clazz, Class::getMethods);
    }

    public Method getMethod(Class<?> clazz, String methodName, Class<?>[] argTypes) {
        MethodKey key = new MethodKey(clazz, methodName, argTypes);
        return cache.computeIfAbsent(key, k -> findMethod(clazz, methodName, argTypes));
    }


    // 优化参数类型匹配逻辑
    private Method findMethod(Class<?> clazz, String methodName, Class<?>[] argTypes) {
        // 使用流处理并并行查找（如果线程安全）
        Method method = Arrays.stream(getMethods(clazz))
                .filter(m -> m.getName().equals(methodName))
                .filter(m -> isMethodMatch(m, argTypes))
                .findFirst()
                .orElse(null);

        if (method != null) {
            accessibleAsTrue(method);
        }

        return method;
    }

    private boolean isMethodMatch(Method method, Class<?>[] argTypes) {
        Class<?>[] paramTypes = method.getParameterTypes();

        // 处理可变参数方法
        if (method.isVarArgs()) {
            return isVarArgsMatch(method, paramTypes, argTypes);
        }

        // 处理普通方法
        if (paramTypes.length != argTypes.length) return false;

        for (int i = 0; i < paramTypes.length; i++) {
            if (!isAssignable(paramTypes[i], argTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isVarArgsMatch(Method method, Class<?>[] paramTypes, Class<?>[] argTypes) {
        // 可变参数方法至少需要一个固定参数
        if (paramTypes.length == 0) {
            return false;
        }

        // 最后一个参数应该是数组类型
        Class<?> varArgType = paramTypes[paramTypes.length - 1];
        if (!varArgType.isArray()) {
            return false;
        }

        // 获取可变参数的元素类型
        Class<?> varArgComponentType = varArgType.getComponentType();

        // 检查参数匹配 - 允许零个可变参数
        if (argTypes.length < paramTypes.length - 1) {
            // 参数数量不足
            return false;
        }

        // 检查固定参数
        for (int i = 0; i < paramTypes.length - 1; i++) {
            if (!isAssignable(paramTypes[i], argTypes[i])) {
                return false;
            }
        }

        // 检查可变参数 - 如果没有可变参数，也是有效的
        if (argTypes.length > paramTypes.length - 1) {
            for (int i = paramTypes.length - 1; i < argTypes.length; i++) {
                if (!isAssignable(varArgComponentType, argTypes[i])) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isAssignable(Class<?> targetType, Class<?> sourceType) {
        // 处理原始类型与包装类型的兼容性
        if (targetType.isPrimitive()) {
            Class<?> wrapperType = PRIMITIVE_WRAPPER_MAP.get(targetType);
            return wrapperType != null && wrapperType.isAssignableFrom(sourceType);
        } else if (sourceType.isPrimitive()) {
            Class<?> targetWrapper = PRIMITIVE_WRAPPER_MAP.get(sourceType);
            return targetType.isAssignableFrom(targetWrapper);
        }

        if (targetType.isAssignableFrom(sourceType)) {
            return true;
        }

        if (sourceType == Void.class) {//表示 source-val 为 null
            return true;
        }

        return false;
    }

    /**
     * 准备方法调用参数，处理可变参数的情况
     */
    public Object[] prepareInvokeArgs(Method method, Object[] argValues) {
        if (!method.isVarArgs()) {
            return argValues;
        }

        Class<?>[] paramTypes = method.getParameterTypes();
        int fixedParamCount = paramTypes.length - 1;

        // 如果参数数量正好匹配固定参数，不需要特殊处理
        if (argValues.length == fixedParamCount) {
            return argValues;
        }

        // 准备调用参数
        Object[] invokeArgs = new Object[paramTypes.length];

        // 复制固定参数
        System.arraycopy(argValues, 0, invokeArgs, 0, Math.min(fixedParamCount, argValues.length));

        // 处理可变参数
        Class<?> varArgType = paramTypes[fixedParamCount];
        Class<?> varArgComponentType = varArgType.getComponentType();

        if (argValues.length >= fixedParamCount) {
            int varArgCount = argValues.length - fixedParamCount;

            // 创建可变参数数组
            Object varArgsArray = java.lang.reflect.Array.newInstance(varArgComponentType, varArgCount);
            for (int i = 0; i < varArgCount; i++) {
                java.lang.reflect.Array.set(varArgsArray, i, argValues[fixedParamCount + i]);
            }

            invokeArgs[fixedParamCount] = varArgsArray;
        } else {
            // 如果没有可变参数，创建一个空数组
            Object emptyArray = java.lang.reflect.Array.newInstance(varArgComponentType, 0);
            invokeArgs[fixedParamCount] = emptyArray;
        }

        return invokeArgs;
    }


    /// //////////////////////////////
    private static final Map<String, PropertyHolder> PROPERTY_CACHE = new ConcurrentHashMap<>();


    /**
     * 获取属性
     */
    public static PropertyHolder getProperty(Class<?> clazz, String propName) {
        String key = clazz.getName() + ":" + propName;

        return PROPERTY_CACHE.computeIfAbsent(key, k -> {
            try {
                String name = "get" + capitalize(propName);
                Method method = clazz.getMethod(name);
                accessibleAsTrue(method);

                return new PropertyHolder(method, null);
            } catch (NoSuchMethodException e) {
                try {
                    Field field = clazz.getField(propName);
                    accessibleAsTrue(field);

                    return new PropertyHolder(null, field);
                } catch (NoSuchFieldException ex) {
                    throw new EvaluationException("Missing property: " + propName, e);
                }
            }
        });
    }

    /**
     * 将字符串首字母大写
     */
    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }


    /**
     * 尝试设置访问权限
     */
    private static void accessibleAsTrue(AccessibleObject method) {
        try {
            if (method.isAccessible() == false) {
                method.setAccessible(true);
            }
        } catch (Throwable ignore) {
            //略过
        }
    }

    private static class MethodKey {
        private final Class<?> clazz;
        private final String methodName;
        private final Class<?>[] argTypes;

        public MethodKey(Class<?> clazz, String methodName, Class<?>[] argTypes) {
            this.clazz = clazz;
            this.methodName = methodName;
            this.argTypes = argTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodKey methodKey = (MethodKey) o;
            return clazz.equals(methodKey.clazz) &&
                    methodName.equals(methodKey.methodName) &&
                    java.util.Arrays.equals(argTypes, methodKey.argTypes);
        }

        @Override
        public int hashCode() {
            int result = clazz.hashCode();
            result = 31 * result + methodName.hashCode();
            result = 31 * result + java.util.Arrays.hashCode(argTypes);
            return result;
        }
    }
}