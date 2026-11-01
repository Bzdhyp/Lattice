# Lattice

Lattice 是一个 Minecraft **多加载器 / 多版本** 工程骨架与可行性 PoC：

- 加载器：Fabric + NeoForge
- 游戏版本：Minecraft **1.21.11**（Java 21）与 **26.3**（Java 25）
- mod id：`lattice`
- 构建：Gradle Wrapper 9.7.1，Java toolchain 21 / 25

> **真实性说明**
>
> 仓库内的 `universal` JAR 不是“所有版本永久通用”的承诺。
> - 同版本双加载器 JAR：已实际放入 Fabric / NeoForge 专用服务器 `mods/` 并启动成功。
> - 跨 1.21.11 / 26.3 单 JAR：已用当前最小 PoC 在 4 种组合下实际启动成功；它采用“共享无 MC 类调度器 + 版本隔离 payload”，**不自动保证未来加入物品、方块、Mixin、资源后仍然可用**。
> - 详细限制、参考项目证据和建议路径见 [docs/FEASIBILITY.md](docs/FEASIBILITY.md)；逐项启动记录见 [docs/VERIFICATION.md](docs/VERIFICATION.md)。

## 已验证产物

构建命令：

```bash
./gradlew build
```

产物位于 `build/dist/`：

| 文件 | Minecraft | Fabric | NeoForge | 验证状态 |
|---|---|---|---|---|
| `lattice-1.21.11-0.1.0-universal.jar` | 1.21.11 | ✅ | ✅ | 两个专用服务器均识别并初始化 |
| `lattice-26.3-0.1.0-universal.jar` | 26.3 | ✅ | ✅ | 两个专用服务器均识别并初始化 |
| `lattice-1.21.11-26.3-0.1.0-universal.jar` | 1.21.11 + 26.3 | ✅ | ✅ | 4 种组合均识别并初始化（实验性） |

单独加载器产物在各版本模块的 `build/libs/`：

```text
versions/1.21.11/fabric/build/libs/lattice-fabric-1.21.11-0.1.0.jar
versions/1.21.11/neoforge/build/libs/lattice-neoforge-1.21.11-0.1.0.jar
versions/26.3/fabric/build/libs/lattice-fabric-26.3-0.1.0.jar
versions/26.3/neoforge/build/libs/lattice-neoforge-26.3-0.1.0.jar
```

## 构建环境

- 运行 Gradle 的 JVM：**JDK 25**（本机其他 JDK 不影响 Gradle toolchain）
- 编译 1.21.11：JDK **21**
- 编译 26.3：JDK **25**
- 不需要系统安装 Gradle；仓库自带 wrapper。

Windows 示例：

```bat
set JAVA_HOME=C:\path\to\jdk-25
gradlew.bat build
```

Linux/macOS 示例：

```bash
JAVA_HOME=/path/to/jdk-25 ./gradlew build
```

Gradle 会自动探测本机 JDK；CI 通过 `JAVA_HOME_21_X64` / `JAVA_HOME_25_X64` 提供两个 toolchain。缺少 JDK 时 foojay resolver 可自动下载。

## 单独构建

```bash
./gradlew :v1_21_11:fabric:build
./gradlew :v1_21_11:neoforge:build
./gradlew :v26_3:fabric:build
./gradlew :v26_3:neoforge:build
./gradlew :v1_21_11:universalJar
./gradlew :v26_3:universalJar
./gradlew :crossversion:crossVersionJar
```

## 安装

把对应版本的 `universal` JAR 放入客户端的 `mods/` 即可：

- 1.21.11 Fabric / NeoForge：`lattice-1.21.11-0.1.0-universal.jar`
- 26.3 Fabric / NeoForge：`lattice-26.3-0.1.0-universal.jar`
- 跨版本实验：`lattice-1.21.11-26.3-0.1.0-universal.jar`

启动后在日志中应看到：

```text
[lattice] Lattice 0.1.0 initialized
[lattice] loader=fabric, minecraft=1.21.11, adapter=mc-1.21.11, java=...
```

## 工程结构

```text
Lattice/
├── core/                       # 无 MC / 无 loader 依赖的公共层
├── bootstrap/
│   ├── fabric/                 # 单 Fabric 入口，按 MC 版本反射调度
│   └── neoforge/               # 单 NeoForge 入口，按 MC 版本反射调度
├── versions/
│   ├── 1.21.11/
│   │   ├── common/             # 1.21.11 版本适配层
│   │   ├── fabric/             # Fabric payload + fabric.mod.json
│   │   └── neoforge/           # NeoForge payload + neoforge.mods.toml
│   └── 26.3/
│       ├── common/
│       ├── fabric/
│       └── neoforge/
├── crossversion/               # 跨版本合并与合并后的元数据
├── scripts/verify-artifacts.py # 产物结构静态校验
└── docs/                       # 可行性、验证记录
```

## 技术结论（摘要）

1. **同一 MC 版本内 Fabric + NeoForge 单 JAR 可行**：两个元数据文件可以共存，入口类按加载器隔离。
2. **跨 1.21.11 / 26.3 单 JAR 对当前最小 PoC 可行**：共享调度器不引用 MC 类，各版本 payload 放在独立包中，运行时只加载正确版本。
3. **不能把“同版本 universal”误写成“跨所有版本 universal”**：NDPR 的 universal JAR 是每个 MC 版本一个，不是跨版本。
4. **生产化前必须重做架构**：真正的物品/方块/命令/Mixin 会引入映射、注册表和 Mixin 配置差异；推荐按版本分别发布 universal JAR，或采用 AutoModpack 式“按目标完整构建 + 外层 trampoline”的方案。

## 许可

MIT，见 [LICENSE](LICENSE)。
