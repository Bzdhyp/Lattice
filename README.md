# Lattice

Lattice 是一个 Minecraft **单 JAR、双加载器、双版本** 工程 PoC。

最终只发布一个文件：

```text
build/dist/lattice-0.1.0-universal.jar
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
> 当前只是**最小 PoC**：只包含初始化日志和版本调度，不包含物品、方块、命令、Mixin 或资源。加入真实内容后仍需重新验证。
>
> 可行性、失败边界和参考项目证据见 [docs/FEASIBILITY.md](docs/FEASIBILITY.md)，逐项启动记录见 [docs/VERIFICATION.md](docs/VERIFICATION.md)。

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
build/dist/lattice-0.1.0-universal.jar
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

## 安装

把 `build/dist/lattice-0.1.0-universal.jar` 复制到：

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
├── crossversion/               # 合并成唯一 JAR 的元数据与任务
├── scripts/verify-artifacts.py # 校验 build/dist 只有一个 JAR
└── docs/                       # 可行性、验证记录、Release 说明
```

## 校验

```bash
python scripts/verify-artifacts.py
```

期望输出：

```text
PASS: lattice-0.1.0-universal.jar | ... | one jar only
ALL CHECKS PASSED (single universal jar)
```

## 已验证矩阵

| 场景 | 结果 |
|---|---|
| 单 JAR × Fabric 1.21.11 | ✅ `Done`，初始化 `mc-1.21.11` |
| 单 JAR × Fabric 26.3 | ✅ `Done`，初始化 `mc-26.3` |
| 单 JAR × NeoForge 1.21.11 | ✅ `Done`，初始化 `mc-1.21.11` |
| 单 JAR × NeoForge 26.3 | ✅ `Done`，初始化 `mc-26.3` |

> 验证的是 **dedicated server**，不是客户端。客户端启动仍待验证。

## 限制

- 这是最小 PoC，不含真实游戏内容。
- 跨版本调度依赖当前 Fabric/NeoForge 的版本 API 与类加载行为；升级加载器后必须重新验证。
- 加入物品/方块/命令/Mixin 后，需要把版本相关代码放进对应 payload 包，并重新做 4 组启动测试。
- 不承诺未来 26.4+ 或其他 Minecraft 版本。
- 不承诺客户端、真实玩家连接、Modrinth/CurseForge 审核通过。

## 文档

- [可行性报告](docs/FEASIBILITY.md)
- [验证记录](docs/VERIFICATION.md)
- [Release 说明](docs/RELEASING.md)
- [CHANGELOG.md](CHANGELOG.md)

## 许可

MIT，见 [LICENSE](LICENSE)。
