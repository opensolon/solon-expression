

### v3.8.0

* 添加 `solon-expression` SnelParser 类，为 TemplateParser 和 EvaluateParser 提供出入口和占位符配置
* 优化 `solon-expression` EvaluateParser 支持定义占位符（可支持 `{xxx}` 表达式）
* 优化 `solon-expression` TemplateParser 支持定义占位符（可支持 `{xxx}` 表达式）
* 优化 `solon-expression` LRUCache 性能（提高缓存性能）
* 调整 `solon-expression` SnelEvaluateParser 更名为 EvaluateParser
* 调整 `solon-expression` SnelTemplateParser 更名为 TemplateParser

### v3.7.0

* 添加 属性表达式在算数操作中的自适应增强
* 修复 PropertyNode 因缓存造成相同表达式，值不同时会出错的问题


### 3.6.3

* 添加 属性表达式在算数操作中的自适应增强
* 修复 PropertyNode 因缓存造成相同表达式，值不同时会出错的问题


### v3.6.0

* 新增 Guidance 控制体系
* 添加 EnhanceContext 替代 StandardContext（后者标为弃用）
* 添加 `T(className)` 类型表达式
* 优化 ComparisonNode 处理，表达式可按需转换类型
* 优化 LogicalNode 处理，非空字符串，非 null 对象即为 true


示例：

```kotlin
fun main(){
    SnEL.evel("${yyy.enable} == false", Utils.asMap());
}
```

### 3.5.8

* 修复 PropertyNode 因缓存造成相同表达式，值不同时会出错的问题

### v3.5.3

* 添加 '${}' 和 '?:' 表达式支持

### v3.5.2

* 添加 `${}` 属性求值表达式支持
* 添加 `?:` Elvis 表达式支持

### v3.5.0

* 优化 SnelEvaluateParser:parseNumber 增强识别 "4.56e-3"（科学表示法）和 "1-3"（算数）

### v3.4.0

* 优化 solon-expression StandardContext 添加 target = null 检测