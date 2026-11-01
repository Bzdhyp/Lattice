# Lattice

Lattice 是一个 Minecraft **单 JAR、双加载器、双版本** 工程 PoC。

最终只发布一个文件：

```text
build/dist/lattice-1.21.11-26.3-0.1.0-universal.jar
```

这一个 JAR 同时覆盖：

| 项目 | 支持 |
|---|---|
| Minecraft | **1.21.11**、**26.3** |
| 加载器 | **Fabric**、**NeoForge** |
| 安装方式 | 同一个 JAR 直接放入对应客户端/服务端的 `mods/` |

> **真实性说明**
>
> 这个单 JAR 已在 4 种真实专用服务器组合中启动验证：Fabric 1.21.11、Fabric 26.3、NeoForge 1.21.11、NeoForge 26.3。  
> 当前只是**最小 PoC**：只包含初始化日志、版本调度、资源目录骨架和 `lang` 示例，不包含真实物品、方块、命令或 Mixin。加入真实内容后仍需重新验证。

## 构建

直接在项目根目录运行：

```bat
gradlew.bat build
```

Linux/macOS：

```bash
./gradlew build
```

构建完成后：

```text
build/dist/lattice-1.21.11-26.3-0.1.0-universal.jar
```

`gradlew build` 只会把**这一个 JAR** 放进 `build/dist/`。  
`build/intermediate/` 和 `versions/*/build/libs/` 里的文件是构建中间件，不是发布产物。

### 关于 Java 版本

- Gradle Wrapper 可以通过 `gradle/gradle-daemon-jvm.properties` 自动选择 JDK 25 运行构建 daemon。
- 1.21.11 编译 toolchain：JDK 21。
- 26.3 编译 toolchain：JDK 25。
- 如果你看到 `Dependency requires at least JVM runtime version 25`，先执行 `gradlew.bat --stop`，再运行 `gradlew.bat build`。
- 仍失败时临时指定 JDK 25：

```bat
set JAVA_HOME=C:\path\to\jdk-25
gradlew.bat build
```

## 在 IDEA 中启动客户端

Gradle/IDEA 同步（点击大象刷新）时，Loom 的 `ideaSyncTask` 会自动生成：

- `Minecraft Client (:versions:fabric:v1_21_11)`（Gradle 类型）
- `Minecraft Client (:versions:fabric:v26_3)`（Gradle 类型）
- `fabric - Client 1.21.11`（Application 类型）
- `fabric - Client 26.3`（Application 类型）

NeoForge 侧由 ModDevGradle 生成 `neoforge - Client` / `neoforge - Server`。

这些配置写入 `.idea/runConfigurations/`，属于本地 IDE 配置，已被 Git 忽略；**不需要 `.run` 文件夹**。  
本项目在 `buildSrc` 中用 `GenerateIdeaApplicationRunTask` 挂到 `ideaSyncTask`，所以同步时会自动创建 Fabric Application 配置。

Application 配置使用 Loom 的 `.gradle/loom-cache/projects/.../launch.cfg`；同步任务会同时生成该文件。

也可以从 Gradle 工具窗口运行：

```text
Lattice > Tasks > run > runClientFabric12111
Lattice > Tasks > run > runClientNeoForge12111
Lattice > Tasks > run > runClientFabric263
Lattice > Tasks > run > runClientNeoForge263
```

第一次启动会下载客户端资源，之后会复用缓存。  
注意：`runClient` 加载的是开发环境中的项目源码，不是 `build/dist/lattice-1.21.11-26.3-0.1.0-universal.jar`；要验证最终单 JAR，仍需把它放进 `mods/`。

## 安装

把 `build/dist/lattice-1.21.11-26.3-0.1.0-universal.jar` 复制到：

```text
.minecraft/mods/
```

或服务器：

```text
server/mods/
```

然后使用对应环境启动：

- Minecraft 1.21.11 + Fabric
- Minecraft 1.21.11 + NeoForge
- Minecraft 26.3 + Fabric
- Minecraft 26.3 + NeoForge

日志中应出现：

```text
[lattice] Lattice 0.1.0 initialized
[lattice] loader=fabric, minecraft=1.21.11, adapter=mc-1.21.11, java=...
```

## 单 JAR 原理

```text
fabric.mod.json / neoforge.mods.toml
        │
        ▼
共享入口（不引用 Minecraft 类）
  Fabric:  FabricLoader 读取 minecraft 版本
  NeoForge: ModList 读取 minecraft 版本
        │
        ├── 1.21.11 → dev.lattice.v12111.<loader>.Payload
        └── 26.3    → dev.lattice.v263.<loader>.Payload
```

- 入口类只使用稳定的加载器 API，不引用 Minecraft 类。
- 每个版本 + 加载器的 payload 放在独立包中。
- 1.21.11 Fabric payload 经过 intermediary remap；26.3 使用官方非混淆名。
- 运行时只加载当前版本对应的 payload，不会加载另一个版本的类。
- `fabric.mod.json` 使用 Fabric 谓词 `>=1.21.11 <26.4`；NeoForge 使用 `[1.21.11,26.4)`。

## 工程结构

```text
Lattice/
├── buildSrc/                    # Gradle 构建逻辑：合并 JAR + 自动生成 IDEA 运行配置
├── common/
│   ├── src/main/java/           # 无 MC / 无 loader 依赖的公共代码
│   └── src/main/resources/
│       └── assets/lattice/      # 共享大资源：font / shaders / textures / icon.png
├── bootstrap/
│   ├── fabric/                  # 运行时 Fabric 入口，不是 buildSrc
│   └── neoforge/                # 运行时 NeoForge 入口，不是 buildSrc
├── versions/
│   ├── common/                  # 版本专属代码 + lang
│   │   ├── 1.21.11/src/main/java/
│   │   ├── 1.21.11/src/main/resources/
│   │   ├── 26.3/src/main/java/
│   │   └── 26.3/src/main/resources/
│   ├── fabric/                  # 只放 Fabric 元数据 / Mixin
│   │   ├── 1.21.11/
│   │   └── 26.3/
│   └── neoforge/                # 只放 NeoForge 元数据 / Mixin
│       ├── 1.21.11/
│       └── 26.3/
├── release/                     # 合并四个 loader JAR -> 唯一单 JAR
└── scripts/verify-artifacts.py  # 校验 build/dist 只有一个 JAR
```

`buildSrc` 只放 Gradle 构建逻辑，不参与游戏运行时；`bootstrap` 仍是运行时入口，会被打进 mod JAR。  
四个 loader 子工程各自有独立 toolchain、依赖图和源码目录；根 wrapper 统一编排，`release` 负责最终合并。

### 资源放哪里

| 资源 | 路径 |
|---|---|
| 字体、shader、贴图、音效、图标等共享资源 | `common/src/main/resources/assets/lattice/` |
| TTF 字体（原版 provider） | `common/src/main/resources/assets/lattice/font/your_font.ttf` + 同名 `.json` |
| 版本专属 `lang` | `versions/common/<版本>/src/main/resources/assets/lattice/lang/` |
| 版本专属贴图 / data | `versions/common/<版本>/src/main/resources/assets/lattice/` 或 `.../data/lattice/` |
| 加载器元数据、Mixin 配置 | `versions/fabric/<版本>/src/main/resources/`、`versions/neoforge/<版本>/src/main/resources/` |

原版 TTF provider 示例：

```text
common/src/main/resources/assets/lattice/font/your_font.ttf
common/src/main/resources/assets/lattice/font/your_font.json
```

```json
{
  "providers": [
    {
      "type": "ttf",
      "file": "lattice:font/your_font.ttf",
      "size": 11.0,
      "oversample": 2.0,
      "shift": [0.0, 0.0],
      "skip": ""
    }
  ]
}
```

如果你使用 Epsilon / Muonium 那种自绘 TTF 渲染器，字体可以放在 `common/src/main/resources/lattice/fonts/`，由你的字体加载器通过 classpath 读取；这不是原版字体 provider 路径。

版本 `lang` 会在合并单 JAR 时按 JSON 合并：26.3 对同名 key 优先。需要两个版本同时存在的文案请使用带版本后缀的 key，避免被覆盖。

## 校验

```bash
python scripts/verify-artifacts.py
```

期望输出：

```text
PASS: lattice-1.21.11-26.3-0.1.0-universal.jar | ... | one jar only
ALL CHECKS PASSED (single universal jar)
```

## Skija 2D 渲染

Lattice 已接入 **Skija 0.143.17** 作为 2D 渲染后端：

- 共享代码：`common/src/main/java/dev/lattice/render/SkijaRenderer.java`
- Fabric 客户端入口：`bootstrap/fabric/.../LatticeFabricClientEntry.java`
- NeoForge 客户端初始化：在 `LatticeNeoForgeEntry` 中按 `Dist.CLIENT` 调用
- 原生库：`io.github.humbleui:skija-windows-x64:0.143.17` 已打进最终单 JAR
- 最终 JAR 使用 `Multi-Release: true`，保证 Java 21/25 下走 Skija 的 `META-INF/versions/9` 实现

当前提供：

```java
SkijaRenderer.initialize();
SkijaRenderer.isAvailable();

try (SkijaFrame frame = SkijaRenderer.beginFrame(480, 240)) {
    SkijaShapes.rect(0, 0, 480, 240, 0xFF101018);
    SkijaShapes.roundedRect(40, 40, 400, 160, 24, 0xFF202838);
    SkijaShapes.roundedOutline(40, 40, 400, 160, 24, 2, 0xFF70C0FF);
    SkijaEffects.dropShadow(40, 40, 400, 160, 24, 0, 8, 18, 0xAA000000);
    SkijaEffects.backdropBlurRounded(40, 40, 400, 160, 24, 12, 0.8f);
    byte[] png = frame.encodePng();
}
```

API 分层：

| 类 | 用途 |
|---|---|
| `SkijaRenderer` | 初始化、`beginFrame()`、预览图 |
| `SkijaFrame` | 一帧离屏 Surface/Canvas，`encodePng()` |
| `SkijaGfx` | Paint 缓存、状态栈、translate/scale/rotate |
| `SkijaShapes` | rect、圆角矩形、描边、圆、线性渐变 |
| `SkijaEffects` | drop shadow、shape blur、backdrop blur |

注意：

- 当前 API 是 **离屏 Canvas API**；Minecraft GUI framebuffer 的 Host 仍需按 1.21.11 / 26.3 各写一份（参考 MuoniumPlayer 的 `SkijaHost` 分层）。
- 目前只打包 **Windows x64** 原生库；其他系统会捕获加载失败并禁用 Skija，不会让游戏崩溃。
- 当前是 2D 渲染层，已支持 blur / drop shadow；还没有 3D 世界阴影。
- Skija 使用 Apache-2.0，Skia 使用 BSD-3-Clause；发布前需要补充第三方许可文本。

## 已验证矩阵

| 场景 | 结果 |
|---|---|
| 单 JAR × Fabric 1.21.11 | ✅ `Done`，初始化 `mc-1.21.11` |
| 单 JAR × Fabric 26.3 | ✅ `Done`，初始化 `mc-26.3` |
| 单 JAR × NeoForge 1.21.11 | ✅ `Done`，初始化 `mc-1.21.11` |
| 单 JAR × NeoForge 26.3 | ✅ `Done`，初始化 `mc-26.3` |

> 上表四组服务器矩阵来自资源分层重构前的同一功能版本。本次重构后已重新通过 `gradlew build`、静态校验，并重新启动 Fabric 1.21.11 客户端；NeoForge/26.3 服务器未在本次重构后重跑。

## 限制

- 这是最小 PoC，不含真实游戏内容。
- 跨版本调度依赖当前 Fabric/NeoForge 的版本 API 与类加载行为；升级加载器后必须重新验证。
- 加入物品/方块/命令/Mixin 后，需要把版本相关代码放进对应 payload 包，并重新做 4 组启动测试。
- 不承诺未来 26.4+ 或其他 Minecraft 版本。
- 不承诺客户端、真实玩家连接、Modrinth/CurseForge 审核通过。

## 许可

MIT，见 [LICENSE](LICENSE)。
