# Zest – Compress-43 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 279695 (valid 57.32%) |
| Cycles | 14 |
| Corpus | 40 |
| Zest branch coverage (total / valid) | 196 / 193 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 40 |
| Failing on fixed | 0 |
| Failing on buggy | 15 |
| Bug detection | Fail (triggering: 15) |
| **Fault detected** | **yes** |
| Line coverage | 63 / 533 = 11.82% |
| Branch coverage | 17 / 300 = 5.67% |
| Test suite length (statements) | 40 |
| Mutation score | 866 / 944 = 91.74% |
| Fuzz time | 120s |
| Pipeline time | 234s |

## Failing tests on buggy version

- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test13
- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test22
- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test23
- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test24
- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test25
- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test26
- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test27
- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test29
- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test31
- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test35
- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test36
- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test37
- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test39
- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test2
- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test5

```
--- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test13
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:false |this=org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream:@?]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test.test13(ZipArchiveOutputStream_Zest_Test.java:98)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:298)
	at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:292)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.base/java.lang.Thread.run(Thread.java:829)
--- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test22
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:false |this=org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream:@?
1=java.lang.Boolean:false |this=org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream:@?]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test.test22(ZipArchiveOutputStream_Zest_Test.java:161)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:298)
	at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:292)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.base/java.lang.Thread.run(Thread.java:829)
--- org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream_Zest_Test::test23
```

- Suite: `/home/user/suites/Compress/zest/43/t12001/Compress-43f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_43/b120_r1/src/org/apache/commons/compress/archivers/zip/ZipArchiveOutputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_43/b120_r1`
