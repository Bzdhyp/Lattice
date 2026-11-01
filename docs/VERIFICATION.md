# Lattice 验证记录

验证日期：**2026-11-01**（本机时间）
验证主机：Windows 11，Git Bash，JDK 21.0.8 + JDK 25.0.4.1，Gradle Wrapper 9.7.1。

## 1. 构建验证

直接用 Java 21 启动 wrapper：

```bash
export JAVA_HOME=/c/Users/Administrator/.jdk/jdk21
./gradlew clean build
```

Gradle 通过 `gradle/gradle-daemon-jvm.properties` 自动选择 JDK 25 运行构建 daemon：

```text
Launcher JVM:  21.0.8
Daemon JVM:    Compatible with Java 25, any vendor
BUILD SUCCESSFUL
```

## 2. 唯一产物

`build/dist/` 中只有一个文件：

```text
lattice-0.1.0-universal.jar
```

| 属性 | 值 |
|---|---|
| 大小 | 9,756 B |
| SHA-1 | `dbc951356d609802d185a493d627e84d63033f07` |
| 内部 classfile | major 65（1.21.11 payload）与 69（26.3 payload） |
| 内部元数据 | `fabric.mod.json`、`META-INF/neoforge.mods.toml` |

`build/intermediate/` 和 `versions/*/build/libs/` 中的 JAR 是合并中间件，不是发布产物。

## 3. 单 JAR 四组合启动验证

同一个 `lattice-0.1.0-universal.jar` 被分别放入四个独立测试目录的 `mods/`，启动对应专用服务器：

| 测试目录 | Minecraft | 加载器 | 选中适配器 | 结果 |
|---|---|---|---|---|
| `verify/cross-fabric-1.21.11` | 1.21.11 | Fabric Loader 0.20.1 | `mc-1.21.11` | ✅ `Done (0.242s)`，`stop` 退出码 0 |
| `verify/cross-fabric-26.3` | 26.3 | Fabric Loader 0.20.1 | `mc-26.3` | ✅ `Done (0.239s)`，`stop` 退出码 0 |
| `verify/cross-neoforge-1.21.11` | 1.21.11 | NeoForge 21.11.48 | `mc-1.21.11` | ✅ `Done (0.370s)`，`stop` 退出码 0 |
| `verify/cross-neoforge-26.3` | 26.3 | NeoForge 26.3.0.100 | `mc-26.3` | ✅ `Done (0.309s)`，`stop` 退出码 0 |

关键日志：

```text
[lattice] loader=fabric, minecraft=1.21.11, adapter=mc-1.21.11, java=25.0.4.1
[lattice] loader=fabric, minecraft=26.3, adapter=mc-26.3, java=25.0.4.1
[lattice] loader=neoforge, minecraft=1.21.11, adapter=mc-1.21.11, java=21.0.8
[lattice] loader=neoforge, minecraft=26.3, adapter=mc-26.3, java=25.0.4.1
```

服务器日志还分别出现：

```text
Loading 5 mods:
	- lattice 0.1.0
```

或：

```text
Lattice 0.1.0 (lattice)
 - lattice (jar(mods/lattice-0.1.0-universal.jar))
```

### 关于测试中的无关报错

NeoForge 服务器在 Windows 上会打印：

```text
NoClassDefFoundError: Could not initialize class io.netty.channel.kqueue.Native
Only supported on OSX/BSD
NoClassDefFoundError: Could not initialize class io.netty.channel.epoll.Native
Only supported on Linux
```

这是 NeoForge 安装器生成的 Windows 参数文件同时包含 Linux/macOS 原生库条目导致的 log appender 报错，与 Lattice 无关；Lattice 初始化在报错前完成，服务器随后到达 `Done` 并正常停止。

## 4. 测试环境与安全边界

- 测试目录：`D:\MCHOMESRC\Lattice\verify\`（已在 `.gitignore` 中忽略）。
- 用户已明确授权在隔离测试目录写 `eula=true` 并接受 Minecraft EULA。
- 每个服务器仅绑定 `127.0.0.1`，`online-mode=false`，端口 25565–25568，`max-players=1`。
- 服务器在出现 `Done` 后通过控制台 `stop` 正常关闭，退出码 0。
- 未使用真实玩家账号、token 或生产服务器。

## 5. 静态产物校验

```bash
python scripts/verify-artifacts.py
```

输出：

```text
PASS: lattice-0.1.0-universal.jar | entries=24 | classes=9 | majors=[65, 69] | one jar only
ALL CHECKS PASSED (single universal jar)
```

脚本检查内容包括：`build/dist` 只有一个 JAR、无重复条目、Fabric/NeoForge 元数据共存、Fabric 范围是空格谓词、NeoForge 范围正确、共享入口存在、1.21.11 payload 为 classfile 65、26.3 payload 为 classfile 69。

## 6. 未验证项

- 客户端启动（只验证 dedicated server）。
- 真实物品/方块/命令/网络包/Mixin/资源。
- 1.21.11 其他 patch 版本、26.4+ 或未来 loader 版本。
- Modrinth/CurseForge 发布审核、签名、性能。
- 真实玩家连接和多人联机。

因此本仓库只能声称：“唯一单 JAR 在当前 1.21.11/26.3 × Fabric/NeoForge 四种专用服务器场景中实际加载成功”，不能声称客户端或所有未来版本通用。
