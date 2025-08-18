# Java环境手动升级指南

## 当前状态
- ❌ 系统Java版本: Java 8 (OpenJDK 1.8.0_392)
- ❌ Android Gradle Plugin 8.2.2需要Java 11+
- ✅ 项目配置已优化，等待Java环境升级

## 推荐解决方案

### 方案1: 快速安装 Eclipse Temurin 17 (推荐)

1. **下载链接**:
   https://adoptium.net/temurin/releases/?version=17

2. **选择版本**:
   - Operating System: Windows
   - Architecture: x64
   - Package Type: JDK
   - Version: 17 (LTS)

3. **下载文件**:
   `OpenJDK17U-jdk_x64_windows_hotspot_17.0.13_11.msi`

4. **安装步骤**:
   - 双击MSI文件运行安装程序
   - 选择"Add to PATH"选项
   - 选择"Set JAVA_HOME variable"选项
   - 完成安装

5. **验证安装**:
   ```batch
   java -version
   ```
   应显示 `openjdk version "17.0.x"`

### 方案2: 使用Chocolatey安装

```powershell
# 如果已安装Chocolatey
choco install openjdk17

# 或者
choco install temurin17
```

### 方案3: 如果有Android Studio

检查Android Studio是否安装了JBR (JetBrains Runtime):
- 路径通常为: `C:\Program Files\Android\Android Studio\jbr`
- 如果存在，可以使用该JDK

## 安装后配置

### 1. 验证Java版本
```batch
java -version
javac -version
```

### 2. 检查环境变量
```batch
echo %JAVA_HOME%
echo %PATH%
```

### 3. 重启终端并测试项目构建
```bash
./gradlew --stop
./gradlew clean build
```

## 如果仍然有问题

### 临时解决方案: 降级项目配置
修改 `gradle/libs.versions.toml`:
```toml
agp = "7.3.1"  # 支持Java 8
kotlin = "1.7.21"
```

但是**不推荐**，因为会失去新功能和安全更新。

## 一键升级脚本

运行项目目录下的 `upgrade_environment.bat` 脚本，它会:
1. 下载Java 17
2. 自动安装
3. 配置环境变量
4. 验证构建

## 支持信息

如果遇到问题:
1. 重启计算机确保环境变量生效
2. 检查Windows PATH变量顺序
3. 确保新的Java版本在PATH最前面