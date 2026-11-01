# Release 说明

## 版本与标签

当前 PoC 版本：`0.1.0`。发布时使用语义化标签，例如：

```bash
git tag -a v0.1.0 -m "Lattice 0.1.0"
git push origin main
git push origin v0.1.0
```

> 本仓库默认不自动 push；以上命令需要在确认后手动执行。

## 唯一产物

运行：

```bash
./gradlew clean build
python scripts/verify-artifacts.py
```

得到**一个**发布文件：

```text
build/dist/lattice-0.1.0-universal.jar
```

它同时适用：

- Minecraft 1.21.11 / 26.3
- Fabric / NeoForge

`build/intermediate/` 与 `versions/*/build/libs/` 中的文件是构建中间件，不要上传到 Release。

## GitHub Release

```bash
gh release create v0.1.0 \
  build/dist/lattice-0.1.0-universal.jar \
  --title "Lattice 0.1.0" \
  --notes-file CHANGELOG.md
```

建议同时上传校验文件：

```bash
sha1sum build/dist/lattice-0.1.0-universal.jar > build/dist/SHA1SUMS.txt
sha256sum build/dist/lattice-0.1.0-universal.jar > build/dist/SHA256SUMS.txt
```

## 发布前检查

1. `./gradlew clean build` 成功。
2. `build/dist` 只有 `lattice-0.1.0-universal.jar`。
3. `python scripts/verify-artifacts.py` 输出 `ALL CHECKS PASSED (single universal jar)`。
4. 用同一个 JAR 跑四种专用服务器 smoke test：Fabric 1.21.11、Fabric 26.3、NeoForge 1.21.11、NeoForge 26.3，均出现 Lattice 初始化日志并正常停止。
5. 如果改了 bootstrap、元数据、Mixin 或 payload 包结构，重新跑全部四种场景，并补客户端验证。
6. 不要把 `verify/`、日志、token、私钥或本地路径提交进仓库。

## Modrinth / CurseForge（后续）

- 上传同一个 `lattice-0.1.0-universal.jar`，游戏版本勾选 1.21.11 与 26.3，加载器勾选 Fabric 与 NeoForge。
- 当前是最小 PoC，不含真实游戏内容；不建议直接提交平台审核。
- 需要补充 icon、banner、项目描述、源码链接和 issue tracker。
- 不要宣称支持 26.4+ 或其他未测试版本。
