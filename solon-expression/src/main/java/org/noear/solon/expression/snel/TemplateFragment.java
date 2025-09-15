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

/**
 * 模板片段
 *
 * @author noear
 * @since 3.1
 */
public class TemplateFragment {
    private final TemplateMarker marker;
    private final String content;

    private String propertyKey;
    private String propertyDef;

    /**
     * 标记
     */
    public TemplateMarker getMarker() {
        return marker;
    }

    /**
     * 片段内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 属性键
     */
    public String getPropertyKey() {
        return propertyKey;
    }

    /**
     * 属性默认值
     */
    public String getPropertyDef() {
        return propertyDef;
    }

    public TemplateFragment(TemplateMarker marker, String content) {
        this.marker = marker;
        this.content = content;

        resolvePropertyName();
    }

    private void resolvePropertyName() {
        if (marker == TemplateMarker.EXPRESSION) {
            return;
        }

        //兼容 `:` 默认值
        int colonIdx = content.lastIndexOf(':');

        if (colonIdx < 0) {
            propertyDef = null;
            propertyKey = content;
        } else {
            propertyDef = content.substring(colonIdx + 1);
            propertyKey = content.substring(0, colonIdx);
        }
    }
}