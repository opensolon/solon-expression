<h1 align="center" style="text-align:center;">
<img src="solon_icon.png" width="128" />
<br />
Solon-Expression（SnEL）
</h1>
<p align="center">
	<strong>Solon 简单求值表达式语言（SnEL）</strong>
</p>
<p align="center">
	<a href="https://solon.noear.org/article/learn-solon-snel">https://solon.noear.org/article/learn-solon-snel</a>
</p>

<p align="center">
    <a target="_blank" href="https://central.sonatype.com/search?q=org.noear%3Asolon-parent">
        <img src="https://img.shields.io/maven-central/v/org.noear/solon.svg?label=Maven%20Central" alt="Maven" />
    </a>
    <a target="_blank" href="LICENSE">
		<img src="https://img.shields.io/:License-Apache2-blue.svg" alt="Apache 2" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html">
		<img src="https://img.shields.io/badge/JDK-8-green.svg" alt="jdk-8" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-11-green.svg" alt="jdk-11" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-17-green.svg" alt="jdk-17" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-21-green.svg" alt="jdk-21" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk23-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-23-green.svg" alt="jdk-23" />
	</a>
    <br />
    <a target="_blank" href='https://gitee.com/noear/solon/stargazers'>
		<img src='https://gitee.com/noear/solon/badge/star.svg' alt='gitee star'/>
	</a>
    <a target="_blank" href='https://github.com/noear/solon/stargazers'>
		<img src="https://img.shields.io/github/stars/noear/solon.svg?style=flat&logo=github" alt="github star"/>
	</a>
    <a target="_blank" href='https://gitcode.com/opensolon/solon/star'>
		<img src='https://gitcode.com/opensolon/solon/star/badge.svg' alt='gitcode star'/>
	</a>
</p>

<hr />


## 1、描述

Solon 基础插件。为 Solon 提供了一套表达式通用接口。并内置 Solon Expression Language（简称，SnEL）“求值”表达式实现方案。纯 Java 代码实现，零依赖（可用于其它任何框架）。编译后为 40KB 多点儿。

* 运行后，内存比较省（与同类相比）
* 只作解析运行（没有编译，没有字节码。不会产生新的隐藏类）

解析后会形成一个表达式“树结构”。可做为中间 DSL，按需二次转换为其它表达式（比如 redis、milvus 的过滤表达式）

主要特点：

* 总会输出一个结果（“求值”表达式嘛）
* 通过上下文传递变量，只支持对上下文的变量求值（不支持 `new Xxx()`）
* 只能有一条表达式语句（即不能有 `;` 号）
* 不支持控制运算（即不能有 `if`、`for` 之类的），不能当脚本用。
* 对象字段、属性、方法调用。可多层嵌套，但只支持 `public`（相对更安全些）
* 支持模板表达式

如果有脚本需求，可用：Liquor！


## 2、简单示例

你好世界：

```java
System.out.println(SnEL.eval("'hello world!'"));
```

`SnEL` 是 `SnelEvaluator.getInstance()` 快捷方式。可以直接使用 SnEL ，也可以按需实例化 SnelEvaluator


## 3、能力说明


| 能力                      | 示例                                   | 备注         |
| --------------- | ---------------------- | -------- |
| 支持常量获取          | `1`, `'name'`, `true`, `[1,2,3]`    | 数字、字符串、布尔、数组 |
| 支持变量获取          | `name`                               |                  |
| 支持字典获取           |  `map['name']`                     |                   |
| 支持集合获取           |  `list[0]`                              |                  |
| 支持对象属性或字段获取     | `user.name`, `user['name']`    | 支持`.` 或 `[]`     |
| 支持对象方法获取     | `order.getUser()`, `list[0].getUser().getName()`    | 支持多级嵌套     |
| 支持对象静态方法获取     | `Math.add(1, 2)`,  `Math.add(a, b)`    |  支持多级嵌套     |
| 支持优先级小括号     |  `(`, `)`    |       |
| 支持算数操作符        |  `+`, `-`, `*`, `/`, `%`    | 加，减，乘，除，模     |
| 支持比较操作符       | `<`, `<=`, `>`, `>=`, `==`, `!=`       | 结果为布尔     |
| 支持like操作符        | `LIKE`, `NOT LIKE`（在相当于包含）       | 结果为布尔     |
| 支持in操作符          | `IN`, `NOT IN`       | 结果为布尔     |
| 支持三元逻辑操作符     | `conditionExpr ? trueExpr: falseExpr`     |      |
| 支持二元逻辑操作符     | `AND`, `OR`     |  与，或（兼容 `&&`、`||` ）    |
| 支持一元逻辑操作符     | `NOT`     |  非（兼容 `!` ）    |

虚拟变量（root）说明：

当使用 StandardContext 上下文时，支持 `root` 虚拟变量（`SnEL.eval("root == true", new StandardContext(true))`）


关键字须使用全大写（未来还可能会增多）：

`LIKE`, `NOT LIKE`, `IN`, `NOT IN` ,`AND`, `OR` ,`NOT`

数据类型与符号说明：

`1.1F`（单精度）、`1.1D`（双精度）、`1L`（长整型）。`1.1`（双精度）、`1`（整型）


预留特殊符号：

`#{   }`, `${   }`

## 4、表达式示例

* 常量与算数表达式

```java
System.out.println(SnEL.eval("1"));
System.out.println(SnEL.eval("-1"));
System.out.println(SnEL.eval("1 + 1"));
System.out.println(SnEL.eval("1 * (1 + 2)"));
System.out.println(SnEL.eval("'solon'"));
System.out.println(SnEL.eval("true"));
System.out.println(SnEL.eval("[1,2,3,-4]"));
```

* 变量，字典，集合获取

```java
Map<String, String> map = new HashMap<>();
map.put("code", "world");

List<Integer> list = new ArrayList<>();
list.add(1);

Map<String, Object> context = new HashMap<>();
context.put("name", "solon");
context.put("list", list);
context.put("map", map);

System.out.println(SnEL.eval("name.length()", context)); //顺便调用个函数
System.out.println(SnEL.eval("name.length() > 2 OR true", context)); 
System.out.println(SnEL.eval("name.length() > 2 ? 'A' : 'B'", context)); 
System.out.println(SnEL.eval("map['code']", context));
System.out.println(SnEL.eval("list[0]", context));
System.out.println(SnEL.eval("list[0] == 1", context));
```

* 带优先级的复杂逻辑表达式

```java
Map<String, Object> context = new HashMap<>();
context.put("age", 25);
context.put("salary", 4000);
context.put("isMarried", false);
context.put("label", "aa");
context.put("title", "ee");
context.put("vip", "l3");

String expression = "(((age > 18 AND salary < 5000) OR (NOT isMarried)) AND label IN ['aa','bb'] AND title NOT IN ['cc','dd']) OR vip=='l3'";
System.out.println(SnEL.eval(expression, context));
```

* 静态函数调用表达式

```java
Map<String, Object> context = new HashMap<>();
context.put("Math", Math.class);
System.out.println(SnEL.eval("Math.abs(-5) > 4 ? 'A' : 'B'", context));
```

## 5、嵌入对象（仅为示例）


```java
Map<String, Object> context = new HashMap<>();
context.put("Solon", Solon.class);
context.put("_sysProps", Solon.cfg()); //顺便别的对象（供参考）
context.put("_sysEnv", System.getenv());

//顺便用三元表达式，模拟下 if 语法
String expr = "Solon.cfg().getInt('demo.type', 0) > _sysProps.getInt('') ? Solon.context().getBean('logService').log(1) : 0";
System.out.println(SnEL.eval(expr, context));
```

## 6、模板表达式

占位符说明

| 接口 | 描述 | 
| -------- | -------- | 
| `#{...}`            | 求值表达式占位符     | 
| `${..}`             | 属性表达式占位符（参考 Solon.cfg() 的 getByExpr 接口，支持默认值表达）     | 


应用示例


```java
Map<String, Object> data = new HashMap<>();
data.put("a", 1);
data.put("b", 1);

StandardContext context = new StandardContext(data);
context.properties(Solon.cfg()); //绑定应用属性，支持 ${表过式}

SnEL.evalTmpl("sum val is #{a + b},  c prop is ${demo.c:c}");
```




## 7、SnEL 快捷方式接口



| 接口 | 描述 | 
| -------- | -------- | 
| `parse(...)`            | 解析求值表达式     | 
| `eval(...)`              | 评估求值表达式     | 
| `parseTmpl(...)`     | 解析模板表达式     | 
| `evalTmpl(...)`       | 评估模板表达式     | 



```java
/**
 * Solon 表达式语言引擎快捷方式（简称，SnEL）
 */
public interface SnEL {
    /**
     * 解析（将文本解析为一个可评估的表达式结构树，可反向转换）
     */
    static Expression parse(String expr, boolean cached) {
        return SnelEvaluateParser.getInstance().parse(expr, cached);
    }

    static Expression parse(String expr) {
        return parse(expr, true);
    }


    /// /////////////////


    /**
     * 评估
     *
     * @param expr    表达式
     * @param context 上下文
     * @param cached  是否带编译缓存
     */
    static Object eval(String expr, Function context, boolean cached) {
        return parse(expr, cached).eval(context);
    }

    /**
     * 评估
     *
     * @param expr    表达式
     * @param context 上下文
     * @param cached  是否带编译缓存
     */
    static Object eval(String expr, Map context, boolean cached) {
        return eval(expr, context::get, cached);
    }


    /**
     * 评估（带编译缓存）
     *
     * @param expr    表达式
     * @param context 上下文
     */
    static Object eval(String expr, Function context) {
        return eval(expr, context, true);
    }

    /**
     * 评估（带编译缓存）
     *
     * @param expr    表达式
     * @param context 上下文
     */
    static Object eval(String expr, Map context) {
        return eval(expr, context, true);
    }

    /**
     * 评估（带编译缓存）
     *
     * @param expr 表达式
     */
    static Object eval(String expr) {
        return eval(expr, Collections.EMPTY_MAP, true);
    }

    /// /////////////////////////////

    /**
     * 上下文中的属性键（用于支持属性表达式）
     */
    static final String CONTEXT_PROPS_KEY = "$PROPS";


    /**
     * 解析模板
     */
    static Expression<String> parseTmpl(String expr, boolean cached) {
        return SnelTemplateParser.getInstance().parse(expr, cached);
    }

    static Expression<String> parseTmpl(String expr) {
        return parseTmpl(expr, true);
    }

    /// /////////////////


    /**
     * 评估模板
     *
     * @param expr    表达式
     * @param context 上下文
     * @param cached  是否带编译缓存
     */
    static String evalTmpl(String expr, Function context, boolean cached) {
        return parseTmpl(expr, cached).eval(context);
    }

    /**
     * 评估模板
     *
     * @param expr    表达式
     * @param context 上下文
     * @param cached  是否带编译缓存
     */
    static String evalTmpl(String expr, Map context, boolean cached) {
        return evalTmpl(expr, context::get, cached);
    }


    /**
     * 评估模板（带编译缓存）
     *
     * @param expr    表达式
     * @param context 上下文
     */
    static String evalTmpl(String expr, Function context) {
        return evalTmpl(expr, context, true);
    }

    /**
     * 评估模板（带编译缓存）
     *
     * @param expr    表达式
     * @param context 上下文
     */
    static String evalTmpl(String expr, Map context) {
        return evalTmpl(expr, context, true);
    }

    /**
     * 评估模板（带编译缓存）
     *
     * @param expr 表达式
     */
    static String evalTmpl(String expr) {
        return evalTmpl(expr, Collections.EMPTY_MAP, true);
    }
}
```

## Solon 项目相关代码仓库



| 代码仓库                                                             | 描述                               | 
|------------------------------------------------------------------|----------------------------------| 
| [/opensolon/solon](../../../../opensolon/solon)                             | Solon ,主代码仓库                     | 
| [/opensolon/solon-examples](../../../../opensolon/solon-examples)           | Solon ,官网配套示例代码仓库                |
|                                                                  |                                  |
| [/opensolon/solon-expression](../../../../opensolon/solon-expression)                   | Solon Expression ,代码仓库           | 
| [/opensolon/solon-flow](../../../../opensolon/solon-flow)                   | Solon Flow ,代码仓库                 | 
| [/opensolon/solon-ai](../../../../opensolon/solon-ai)                       | Solon Ai ,代码仓库                   | 
| [/opensolon/solon-cloud](../../../../opensolon/solon-cloud)                 | Solon Cloud ,代码仓库                | 
| [/opensolon/solon-admin](../../../../opensolon/solon-admin)                 | Solon Admin ,代码仓库                | 
| [/opensolon/solon-jakarta](../../../../opensolon/solon-jakarta)             | Solon Jakarta ,代码仓库（base java21） | 
| [/opensolon/solon-integration](../../../../opensolon/solon-integration)     | Solon Integration ,代码仓库          | 
|                                                                  |                                  |
| [/opensolon/solon-gradle-plugin](../../../../opensolon/solon-gradle-plugin) | Solon Gradle ,插件代码仓库             | 
| [/opensolon/solon-idea-plugin](../../../../opensolon/solon-idea-plugin)     | Solon Idea ,插件代码仓库               | 
| [/opensolon/solon-vscode-plugin](../../../../opensolon/solon-vscode-plugin) | Solon VsCode ,插件代码仓库             | 
