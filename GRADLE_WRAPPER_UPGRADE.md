# 需要手动做的一步：升级 Gradle Wrapper

你没有上传 `gradle/wrapper/gradle-wrapper.properties`，所以这一步我没法帮你自动改，
必须你自己动手，否则 `./gradlew build` 会直接报错退出。

`fabric-loom:1.11-SNAPSHOT`（对应 1.21.8）要求 Gradle **至少 8.14**。
如果你现在用的 wrapper 版本更低，打开：

```
gradle/wrapper/gradle-wrapper.properties
```

把这一行：

```
distributionUrl=https\://services.gradle.org/distributions/gradle-8.12.1-bin.zip
```

改成：

```
distributionUrl=https\://services.gradle.org/distributions/gradle-8.14-bin.zip
```

（具体版本号以你原来那一行里的数字为准，只需要把版本号部分换成 8.14 或更高。）

改完之后跑一次：

```
./gradlew --refresh-dependencies
./gradlew genSources
./gradlew idea    # 如果用 IntelliJ
```

如果不改这一步，Gradle 会直接报：
`Plugin fabric-loom:1.11-SNAPSHOT requires at least Gradle 8.14, but this build uses X.X.X.`
