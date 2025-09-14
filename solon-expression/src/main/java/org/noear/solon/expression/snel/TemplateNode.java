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
import org.noear.solon.expression.guidance.PropertiesGuidance;
import org.noear.solon.expression.guidance.ReturnGuidance;

import java.util.List;
import java.util.Properties;
import java.util.function.Function;

/**
 * @author noear
 * @since 3.1
 */
public class TemplateNode implements Expression<String> {
    private final List<TemplateFragment> fragments;
    private TemplateFragment constantFragment;

    public TemplateNode(List<TemplateFragment> fragments) {
        this.fragments = fragments;

        if (fragments.size() == 1 && fragments.get(0).getMarker() == TemplateMarker.TEXT) {
            //优化常量性能
            constantFragment = fragments.get(0);
        }
    }

    @Override
    public String eval(Function context) {
        if (constantFragment != null) {
            return constantFragment.getContent();
        } else {

            boolean isReturnNull = false;
            boolean allowPropertyDefault = true;
            boolean allowPropertyNesting = false;
            Object propsObject = null;

            if (context instanceof ReturnGuidance) {
                isReturnNull = ((ReturnGuidance) context).isReturnNull();
            }

            if (context instanceof PropertiesGuidance) {
                PropertiesGuidance tmp = ((PropertiesGuidance) context);
                propsObject = tmp.getProperties();
                allowPropertyDefault = tmp.allowPropertyDefault();
                allowPropertyNesting = tmp.allowPropertyNesting();
            }

            if (propsObject == null) {
                propsObject = context;
            }

            /// ///////////////

            StringBuilder result = new StringBuilder();
            for (TemplateFragment fragment : fragments) {
                if (fragment.getMarker() == TemplateMarker.TEXT) {
                    // 如果是文本片段，直接追加
                    result.append(fragment.getContent());
                } else {
                    // 如果是变量片段，从上下文中获取值
                    Object value;
                    if (fragment.getMarker() == TemplateMarker.PROPERTIES) {
                        value = getProps(fragment, propsObject, allowPropertyDefault);

                        if (value != null && allowPropertyNesting) {
                            //模板里可能会（动态）再套模型
                            value = SnEL.evalTmpl((String) value, context);
                        }

                        if (value != null) {
                            //属性表达式，无值为空（即不入值）
                            result.append(value);
                        }
                    } else {
                        value = SnEL.eval(fragment.getContent(), context);
                        result.append(value);
                    }

                    if (isReturnNull && value == null) {
                        return null;
                    }
                }
            }

            return result.toString();
        }
    }

    private String getProps(TemplateFragment expr, Object propsObject, boolean allowPropertyDefault) {

        String value = null;
        if (propsObject instanceof Properties) {
            value = ((Properties) propsObject).getProperty(expr.getPropertyKey());
        } else if (propsObject instanceof Function) {
            Object tmp = ((Function) propsObject).apply(expr.getPropertyKey());
            if (tmp != null) {
                value = String.valueOf(tmp);
            }
        } else {
            throw new IllegalArgumentException("Unsupported properties type: " + propsObject.getClass());
        }

        if (value == null && allowPropertyDefault) {
            return expr.getPropertyDef();
        } else {
            return value;
        }
    }
}