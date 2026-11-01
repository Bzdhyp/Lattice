# Lattice 验证记录

验证日期：**2026-11-01**（本机时间）
验证主机：Windows 11，Git Bash，JDK 21.0.8 + JDK 25.0.4.1，Gradle Wrapper 9.7.1。

## 1. 构建验证

命令（本机）：

```bash
export JAVA_HOME=/c/Users/Administrator/.jdks/azul-25.0.4.1
./gradlew build
```

结果：

```text
BUILD SUCCESSFUL
21 actionable tasks: 10 executed, 11 up-to-date
```

`./gradlew build` 会同时构建：

- `:v1_21_11:fabric:build`
- `:v1_21_11:neoforge:build`
- `:v26_3:fabric:build`
- `:v26_3:neoforge:build`
- `:v1_21_11:universalJar`
- `:v26_3:universalJar`
- `:crossversion:crossVersionJar`

## 2. 产物

| 文件 | 大小 | SHA-1 |
|---|---:|---|
| `lattice-1.21.11-0.1.0-universal.jar` | 7,841 B | `e28f9e1164c1abf8d15ca0ff2ad64af67a7dd305` |
| `lattice-26.3-0.1.0-universal.jar` | 7,781 B | `208c9681b333f62f30d2ad0af9cc656f4eabe206` |
| `lattice-1.21.11-26.3-0.1.0-universal.jar` | 9,756 B | `dbc951356d609802d185a493d627e84d63033f07` |

上述 JAR 都很小，因为当前 PoC 只包含初始化日志和版本调度，不含资源或真实游戏内容。

## 3. 同版本双加载器验证

每个 JAR 都被实际复制到独立测试目录的 `mods/`，启动对应专用服务器：

| 测试目录 | JAR | 启动器 | 日志中的初始化 | 结果 |
|---|---|---|---|---|
| `verify/fabric-1.21.11` | `lattice-1.21.11-...-universal.jar` | Fabric Loader 0.20.1 | `loader=fabric, minecraft=1.21.11, adapter=mc-1.21.11` | ✅ `Done (2.711s)` |
| `verify/fabric-26.3` | `lattice-26.3-...-universal.jar` | Fabric Loader 0.20.1 | `loader=fabric, minecraft=26.3, adapter=mc-26.3` | ✅ `Done (1.941s)` |
| `verify/neoforge-1.21.11` | `lattice-1.21.11-...-universal.jar` | NeoForge 21.11.48 | `loader=neoforge, minecraft=1.21.11, adapter=mc-1.21.11` | ✅ `Done (2.571s)` |
| `verify/neoforge-26.3` | `lattice-26.3-...-universal.jar` | NeoForge 26.3.0.100 | `loader=neoforge, minecraft=26.3, adapter=mc-26.3` | ✅ `Done (1.922s)` |

Fabric 日志还显示：

```text
Loading Minecraft 1.21.11 with Fabric Loader 0.20.1
Loading 5 mods:
	- lattice 0.1.0
[lattice] Lattice 0.1.0 initialized
```

NeoForge 日志还显示：

```text
Lattice 0.1.0 (lattice)
 - lattice (jar(mods/lattice-1.21.11-0.1.0-universal.jar))
[lattice] Lattice 0.1.0 initialized
```

## 4. 跨版本单 JAR 验证

同一个 `lattice-1.21.11-26.3-0.1.0-universal.jar` 分别放入四个测试目录：

| 测试目录 | Minecraft | 加载器 | 选中适配器 | 结果 |
|---|---|---|---|---|
| `verify/cross-fabric-1.21.11` | 1.21.11 | Fabric 0.20.1 | `mc-1.21.11` | ✅ `Done (2.261s)` |
| `verify/cross-fabric-26.3` | 26.3 | Fabric 0.20.1 | `mc-26.3` | ✅ `Done (1.887s)` |
| `verify/cross-neoforge-1.21.11` | 1.21.11 | NeoForge 21.11.48 | `mc-1.21.11` | ✅ `Done (2.379s)` |
| `verify/cross-neoforge-26.3` | 26.3 | NeoForge 26.3.0.100 | `mc-26.3` | ✅ `Done (1.757s)` |

关键日志：

```text
[lattice] loader=fabric, minecraft=1.21.11, adapter=mc-1.21.11, java=25.0.4.1
[lattice] loader=fabric, minecraft=26.3, adapter=mc-26.3, java=25.0.4.1
[lattice] loader=neoforge, minecraft=1.21.11, adapter=mc-1.21.11, java=21.0.8
[lattice] loader=neoforge, minecraft=26.3, adapter=mc-26.3, java=25.0.4.1
```

### 关于测试中的无关报错

NeoForge 服务器在 Windows 上会打印类似：

```text
NoClassDefFoundError: Could not initialize class io.netty.channel.kqueue.Native
Only supported on OSX/BSD
NoClassDefFoundError: Could not initialize class io.netty.channel.epoll.Native
Only supported on Linux
```

这是 NeoForge 安装器生成的 Windows 参数文件同时包含 Linux/macOS 原生库条目导致的 log appender 报错，与 Lattice 无关；四次 NeoForge 测试都在其后正常到达 `Done`，且 Lattice 初始化日志出现在这些报错之前。

## 5. 测试环境与安全边界

- 测试目录：`D:\MCHOMESRC\Lattice\verify\`（已在 `.gitignore` 中忽略）。
- 用户已明确授权在隔离测试目录写 `eula=true` 并接受 Minecraft EULA。
- 每个服务器仅绑定 `127.0.0.1`，`online-mode=false`，端口 25561–25568，`max-players=1`。
- 服务器在出现 `Done` 后由 `timeout` 终止；退出码 `124` 是主动超时，不是启动失败。
- 未使用真实玩家账号、token 或生产服务器。

## 6. 静态产物校验

```bash
python scripts/verify-artifacts.py
```

输出：

```text
PASS: lattice-1.21.11-0.1.0-universal.jar | entries=18 | classes=6 | majors=[65] | fabric=>=1.21.11 <1.21.12
PASS: lattice-26.3-0.1.0-universal.jar | entries=18 | classes=6 | majors=[69] | fabric=>=26.3 <26.4
PASS: lattice-1.21.11-26.3-0.1.0-universal.jar | entries=24 | classes=9 | majors=[65, 69] | fabric=>=1.21.11 <26.4
ALL CHECKS PASSED (3 jars)
```

脚本检查内容包括：JAR 内无重复条目、Fabric/NeoForge 元数据共存、入口类存在、Fabric 范围不是 Maven 范围、NeoForge `modId`/版本范围正确、classfile major 与目标 Java 匹配、跨版本 JAR 同时包含 65/69 两个版本 payload。

## 7. 未验证项

- 客户端启动（只验证 dedicated server）。
- 真实物品/方块/命令/网络包/Mixin/资源。
- 1.21.11 其他 patch 版本、26.4+ 或未来 loader 版本。
- Modrinth/CurseForge 发布审核、签名、性能。
- 真实玩家连接和多人联机。

因此本仓库只能声称：“当前最小 PoC 在所列 8 个专用服务器场景中实际加载成功”，不能声称“所有版本/所有功能都单 JAR 通用”。
