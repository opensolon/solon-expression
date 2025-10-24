
### v3.7.0

* 添加 属性表达式在算数操作中的自适应增强

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

### v3.5.2

* 添加 `${}` 属性求值表达式支持
* 添加 `?:` Elvis 表达式支持

### v3.5.0

* 优化 SnelEvaluateParser:parseNumber 增强识别 "4.56e-3"（科学表示法）和 "1-3"（算数）

### v3.4.0

* 优化 solon-expression StandardContext 添加 target = null 检测