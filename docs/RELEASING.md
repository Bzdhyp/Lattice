# Release 说明

## 版本与标签

当前 PoC 版本：`0.1.0`。发布时使用语义化标签，例如：

```bash
git tag -a v0.1.0 -m "Lattice 0.1.0"
git push origin main
git push origin v0.1.0
```

> 本仓库默认不自动 push；以上命令需要在确认后手动执行。

## 产物矩阵

运行：

```bash
JAVA_HOME=/path/to/jdk-25 ./gradlew build
python scripts/verify-artifacts.py
```

得到：

| 产物 | 适用环境 | 发布定位 |
|---|---|---|
| `build/dist/lattice-1.21.11-0.1.0-universal.jar` | 1.21.11 + Fabric/NeoForge | **推荐正式发布** |
| `build/dist/lattice-26.3-0.1.0-universal.jar` | 26.3 + Fabric/NeoForge | **推荐正式发布** |
| `build/dist/lattice-1.21.11-26.3-0.1.0-universal.jar` | 1.21.11/26.3 + Fabric/NeoForge | 实验性 PoC，标注清楚 |

不要发布成“单个 JAR 覆盖所有 Minecraft 版本”。跨版本 JAR 只覆盖本仓库实际测试过的 1.21.11 与 26.3。

## GitHub Release

```bash
gh release create v0.1.0 \
  build/dist/lattice-1.21.11-0.1.0-universal.jar \
  build/dist/lattice-26.3-0.1.0-universal.jar \
  build/dist/lattice-1.21.11-26.3-0.1.0-universal.jar \
  --title "Lattice 0.1.0" \
  --notes-file CHANGELOG.md
```

建议同时上传 SHA-1/SHA-256 校验文件：

```bash
sha1sum build/dist/*.jar > build/dist/SHA1SUMS.txt
sha256sum build/dist/*.jar > build/dist/SHA256SUMS.txt
```

## 发布前检查

1. `./gradlew build` 成功。
2. `python scripts/verify-artifacts.py` 输出 `ALL CHECKS PASSED`。
3. 至少在本机专用服务器验证对应版本 + 加载器能出现 Lattice 初始化日志。
4. 如果改了 bootstrap、元数据、Mixin 或 payload 包结构，重新跑全部 8 个运行场景。
5. 不要把 `verify/`、日志、token、私钥或本地路径提交进仓库。

## Modrinth / CurseForge（后续）

- 1.21.11 与 26.3 分开发布，各自声明 Fabric + NeoForge。
- 跨版本 JAR 作为单独的实验性文件，不要伪装成“全版本通用”。
- 需要补充 icon、banner、项目描述、源码链接和 issue tracker。
- 当前没有真实游戏内容，不建议直接提交平台审核；先完成物品/命令/Mixin 等实际功能。
