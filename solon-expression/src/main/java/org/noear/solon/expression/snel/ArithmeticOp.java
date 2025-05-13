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
 * 算数操作符
 *
 * @author noear
 * @since 3.1
 */
public enum ArithmeticOp {
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    MOD("%"),
    ;

    ArithmeticOp(String code) {
        this.code = code;
    }

    private String code;

    public String getCode() {
        return code;
    }

    /**
     * 解析
     */
    public static ArithmeticOp parse(String op) {
        switch (op) {
            case "+":
                return ADD;
            case "-":
                return SUB;
            case "*":
                return MUL;
            case "/":
                return DIV;
            case "%":
                return MOD;
            default:
                throw new IllegalArgumentException("Invalid arithmetic operator: " + op);
        }
    }
}
