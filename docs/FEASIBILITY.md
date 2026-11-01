# Lattice 可行性报告

## 1. 官方版本事实（2026-11-01 核实）

数据来源：

- Mojang 版本清单：`https://piston-meta.mojang.com/mc/game/version_manifest_v2.json`
- Fabric meta：`https://meta.fabricmc.net/`
- NeoForge Maven：`https://maven.neoforged.net/releases/`
- Modrinth Fabric API：`https://api.modrinth.com/v2/project/fabric-api`

| 项目 | 1.21.11 | 26.3 |
|---|---|---|
| Mojang 类型 | release | release |
| 发布时间 | 2025-12-09 | 2026-09-15 |
| 官方 Java | 21（`java-runtime-delta`） | 25（`java-runtime-epsilon`） |
| Fabric Loader | 0.20.1 | 0.20.1 |
| Fabric API | 0.141.6+1.21.11 | 0.165.0+26.3 |
| Yarn | 1.21.11+build.6 | 无 Yarn 结果；26.x 使用官方名/非混淆工具链 |
| NeoForge | 21.11.48 | 26.3.0.100 |

结论：**26.3 与 1.21.11 都真实存在，Fabric/NeoForge 均有对应构建。**

## 2. 参考项目结论

### 2.1 NDPR Mod（NDPReforged/NDPR-mod）

- README 明确写“每个 MC 平台版本一个构建目录，patch 版本由 minecraft_version_range 覆盖”。
- 仓库根提交了 9 个 JAR：`ndpr-1.21.11-...-universal.jar`、`ndpr-26.3-...-universal.jar` 等。
- 每个版本目录是独立 Gradle 构建；`mergeJar` 把 `fabric`、`forge`、`neoforge` 三个平台 JAR 解压合并。
- 为了让三套映射命名空间共存，它把 common 源码复制到 `org.ndpr.platform.<loader>.*` 后分别编译。
- **可借鉴**：同版本内多 loader 合并、元数据共存、按 loader 隔离包名。
- **不可照搬**：它不是“一个 JAR 跨 1.21.11/26.3”；它把 9 份源码和 9 个 JAR 提交进仓库，且无 CI/测试；它的 `fabric.mod.json` 使用了 `[1.21.10,26.1)` 这种 Maven 范围，而 Fabric 的版本谓词实际按空格拆分谓词，不能直接照抄（本项目已改为 `>=1.21.11 <1.21.12`）。

参考路径（本地审计副本）：
`NDPR-mod/README.md`、`NDPR-mod/1.21.11/build.gradle`、`NDPR-mod/1.21.11/build-logic/src/main/groovy/multiloader-loader.gradle`、`NDPR-mod/26.3/build.gradle`。

### 2.2 jaredlll08/MultiLoader-Template

- 分支 `26.3` 是 `common + fabric + neoforge` 三模块。
- `common` 只依赖 NeoForm，避免 loader API 泄漏；loader 模块通过 `commonJava` 配置把 common 源码编进自己的 JAR。
- 输出是 `examplemod-fabric-26.3-...jar` 与 `examplemod-neoforge-26.3-...jar`，**没有合并 JAR**。
- **可借鉴**：公共层与加载器适配层分离、源码包含方式、ServiceLoader 平台接口。
- **不可照搬**：它不解决多 MC 版本；一个分支只对应一个 MC 版本。

### 2.3 stonecutter-versioning/stonecutter-template-multiloader

- `settings.gradle.kts` 用 `match("1.21.1", "fabric", "neoforge")` 等声明 8 个节点，覆盖 1.21.1 / 1.21.11 / 26.2 / 26.3。
- 每个节点输出 `template-fabric-...` 或 `template-neoforge-...`，仍是**按版本、按加载器分别构建**。
- **可借鉴**：版本矩阵、每版本属性表、`processResources` 排除另一加载器元数据、26.x 的 Java 25 规则。
- **不可照搬**：单源码树的 `//? if` 指令在功能变大后可读性差；它的 Loom 版本用了 SNAPSHOT，不适合作为可复现依赖。

### 2.4 Saphyra（StudioSaphira/projet-saphyra）

- 结构是 `common + fabric + neoforge`，常规双产物；README 甚至只写 NeoForge 安装。
- 唯一的 `buildJar` 任务试图把两个 loader JAR 放进 `META-INF/jars`，但：
  - 根元数据目录 `distribution/` 被 `.gitignore` 且仓库中不存在；
  - `fabric.mod.json` 没有声明 `jars`；
  - NeoForge 需要 `META-INF/jarjar/metadata.json`，不是 `META-INF/jars`。
- **结论**：Saphyra 当前代码不能作为“单 JAR 全加载器成功”的证据，只能作为反例（嵌套 JAR 必须显式声明）。

### 2.5 AutoModpack（Skidamek/AutoModpack）

- 文档和 CI 声称并校验“一个 JAR 覆盖 1.18.2 → 26.3，Fabric/Forge/NeoForge”。
- 实现：Stonecutter 为每个目标构建完整 impl JAR，再用自定义 `OneJarTask` 压成 zstd 载荷，外层 Java 17 trampoline 通过 loader SPI 注入对应版本。
- **结论**：跨大版本单 JAR 在理论上和工程上**可以做到**，但代价是：
  - 每目标完整构建；
  - 自定义打包/解包；
  - 依赖 Fabric/NeoForge/Forge 的 loader 内部 SPI；
  - 需要针对 loader 代际做兼容。
- 这是“可行但重”的参考，不是简单 merge 能复制的。

### 2.6 pvpOption（LunixiaLIVE/pvpOption）

- README 明确分支策略：`multi_1.21.11` 单独；`multi_26.1-3` 覆盖 26.1–26.3。
- 1.21.11 使用 jar-in-jar bundle；26.x 使用 no-remap 合并 JAR。
- **结论**：连这个项目也把 1.21.11 与 26.x 分开，说明大版本边界是现实存在的。

### 2.7 FabricMultiVersionHelper / gXLg/MultiVersion

- 都是 Fabric-only 的运行时/反射/条件 Mixin 方案，不是双加载器方案。
- `FabricMultiVersionHelper` 用 `IMixinConfigPlugin` + 版本条件 JSON；`MultiVersion` 用反射和 ByteBuddy。
- **可借鉴**：版本探测、反射桥、条件 Mixin。
- **不可照搬**：不能解决 NeoForge，也不能替代编译期映射差异。

## 3. 技术约束

### 3.1 Java 与 classfile

- 1.21.11 官方 Java 21，classfile major 65。
- 26.3 官方 Java 25，classfile major 69。
- JVM 只验证实际加载的类；因此一个 JAR 可以同时包含 65 和 69 的类，只要运行时只加载匹配版本。
- 但这依赖 loader 不在启动时强制加载/解析所有类。当前 PoC 已在 Fabric 0.20.1 与 NeoForge 21.11.48 / 26.3.0.100 上验证通过；未来 loader 若加强全量扫描，需要重新验证。

### 3.2 映射

- 1.21.11 Fabric：开发期 mojmap → `remapJar` 后为 intermediary；NeoForge 为 mojmap。
- 26.x：官方非混淆名，Fabric 使用非 remap Loom，NeoForge 使用官方名。
- **同一份引用 MC 类的字节码不能同时服务 1.21.11 Fabric 和 NeoForge**，因此：
  - 同版本 universal 需要按 loader 隔离类/包；
  - 跨版本 universal 需要按版本 + loader 隔离 payload。

### 3.3 元数据

- Fabric 读根 `fabric.mod.json`；NeoForge 读 `META-INF/neoforge.mods.toml`，两者可共存。
- Fabric 版本谓词：空格分隔，例如 `>=1.21.11 <26.4`；**不支持** `[1.21.11,26.4)` 这种 Maven 范围。
- NeoForge 使用 Maven `VersionRange`，例如 `[1.21.11,26.4)`。
- 本项目的同版本 Jar 使用严格版本范围；跨版本 Jar 使用同时覆盖两个版本的联合范围。

### 3.4 Multi-Release JAR 为什么不用

Multi-Release JAR 的 `META-INF/versions/N` 按 **Java 运行时版本**选择，不是按 Minecraft 版本选择。1.21.11 完全可以在 Java 25 上启动，因此不能用 Java 版本区分 MC 版本；loader 元数据和 Mixin refmap 也不遵循 MR-JAR 语义。

### 3.5 嵌套 JAR 为什么单独不够

- Fabric 的 `META-INF/jars` 需要 `fabric.mod.json` 的 `jars` 声明。
- NeoForge 需要 `META-INF/jarjar/metadata.json`。
- 即使正确声明，嵌套 JAR 也只解决“同一 MC 版本下的 loader 轴”，不能自动选择 1.21.11 vs 26.3；错误版本的嵌套 mod 仍会被依赖检查拒绝。
- 详见 Saphyra 反例与 pvpOption 的 jar-in-jar 用法。

## 4. Lattice 采用的 PoC 方案

```text
fabric.mod.json / neoforge.mods.toml
        │
        ▼
共享入口（不引用 MC 类）
  Fabric:  FabricLoader 读取 minecraft 版本
  NeoForge: ModList 读取 minecraft 版本
        │
        ├── 1.21.11 → dev.lattice.v12111.<loader>.Payload
        └── 26.3    → dev.lattice.v263.<loader>.Payload
```

- 入口类由两个版本共同编译/合并，保留 classfile 65 版本，兼容 1.21.11 Java 21 和 26.3 Java 25。
- payload 按版本 + loader 放独立包；Fabric 1.21.11 的 payload 经过 remap，26.3 payload 不 remap。
- 唯一发布 JAR 的元数据使用联合范围；按版本生成的 JAR 仅作为合并中间件，不进入 `build/dist`。
- 当前 payload 只写日志，不引用 Minecraft 类型；这是“最小 PoC”，不是通用业务代码方案。

## 5. 已验证与未验证

**已验证**

- `gradlew build` 成功，并且 `build/dist` 只生成一个 `lattice-0.1.0-universal.jar`。
- 这一个 JAR 在 1.21.11/26.3 × Fabric/NeoForge 四种组合均被识别并初始化。
- Fabric 与 NeoForge 元数据可共存；Fabric 入口不会在 NeoForge 上被错误调用，反之亦然。
- JAR 中 26.3 payload 的 classfile 69 没有阻止 1.21.11 服务器启动。
- 四次验证均通过服务器 `stop` 正常关闭，退出码 0。

**未验证 / 不承诺**

- 客户端启动（只验证了 dedicated server）。
- 物品、方块、命令、网络包、数据生成、Mixin、资源包、access widener/class tweaker。
- 1.21.11 的其他 patch 版本、26.4+、未来 Fabric/NeoForge 版本。
- 生产环境性能、混淆/签名、Modrinth/CurseForge 发布审核。
- 真实玩家连接、多人联机、服务端兼容性矩阵。

## 6. 推荐路径

1. **发布策略**：默认构建只产出 `build/dist/lattice-0.1.0-universal.jar`，该文件已通过四种加载器/版本组合验证。
2. **生产化**：当前单 JAR 仍是最小 PoC。加入真实内容后，优先保持“共享无 MC 类入口 + 版本/加载器隔离 payload”，并为每个版本独立测试；如果代码量或 Mixin 复杂度超过可控范围，再回退到“按版本 universal JAR”或 AutoModpack 式“每目标完整构建 + 外层 trampoline”方案。
3. **加入真实内容时**：优先按版本目录放置版本相关注册代码；每个 loader 适配层只做入口/事件注册；公共层保持无 MC 引用。
4. **CI**：构建唯一 JAR 并做元数据静态检查；有 Windows runner 时可加入专用服务器 smoke test。
5. **Mixin**：每个 loader/版本使用独立 mixin 配置和 refmap；不要假设一个 mixin 配置能跨映射空间。

## 7. 参考链接

- https://github.com/NDPReforged/NDPR-mod
- https://github.com/jaredlll08/MultiLoader-Template
- https://github.com/stonecutter-versioning/stonecutter-template-multiloader
- https://github.com/StudioSaphira/projet-saphyra
- https://github.com/Skidamek/AutoModpack
- https://github.com/LunixiaLIVE/pvpOption
- https://github.com/Klotzi111/FabricMultiVersionHelper
- https://github.com/gXLg/MultiVersion
