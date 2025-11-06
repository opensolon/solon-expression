

### 3.5.8

* 修复 PropertyNode 因缓存造成相同表达式，值不同时会出错的问题

### v3.5.3

* 添加 '${}' 和 '?:' 表达式支持

### v3.5.0

* 优化 SnelEvaluateParser:parseNumber 增强识别 "4.56e-3"（科学表示法）和 "1-3"（算数）

### v3.4.0

* 优化 solon-expression StandardContext 添加 target = null 检测