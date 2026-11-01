# Changelog

## 0.1.0

- 创建 Gradle + Wrapper 多项目工程骨架。
- 支持 Minecraft 1.21.11（Java 21）与 26.3（Java 25）。
- 支持 Fabric 与 NeoForge。
- 默认构建只输出一个发布 JAR：
  `build/dist/lattice-0.1.0-universal.jar`。
- 该单 JAR 同时覆盖 1.21.11 / 26.3 与 Fabric / NeoForge。
- 在 4 个本地专用服务器场景中验证加载：
  Fabric 1.21.11、Fabric 26.3、NeoForge 1.21.11、NeoForge 26.3。
- 增加 Gradle daemon JDK 25 自动选择，避免 Java 21 启动时报 Loom 版本错误。
- 增加产物结构静态校验和 GitHub Actions 构建。
- 增加可行性、验证记录和 Release 文档。
