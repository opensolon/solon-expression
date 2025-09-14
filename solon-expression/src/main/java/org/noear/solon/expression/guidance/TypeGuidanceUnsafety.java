package org.noear.solon.expression.guidance;

import org.noear.solon.expression.exception.EvaluationException;

/**
 * 类型指导非安全实现
 *
 * @author noear
 * @since 3.6
 */
public class TypeGuidanceUnsafety implements TypeGuidance {
    public static final TypeGuidance INSTANCE = new TypeGuidanceUnsafety();

    @Override
    public Class<?> getType(String typeName) throws EvaluationException {
        try {
            return Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            throw new EvaluationException("Class not found: " + typeName, e);
        }
    }
}