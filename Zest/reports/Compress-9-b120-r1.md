# Zest – Compress-9 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 195802 (valid 77.29%) |
| Cycles | 16 |
| Corpus | 40 |
| Zest branch coverage (total / valid) | 199 / 197 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 40 |
| Failing on fixed | 0 |
| Failing on buggy | 8 |
| Bug detection | Fail (triggering: 8) |
| **Fault detected** | **yes** |
| Line coverage | 45 / 111 = 40.54% |
| Branch coverage | 13 / 42 = 30.95% |
| Test suite length (statements) | 40 |
| Mutation score | 119 / 155 = 76.77% |
| Fuzz time | 123s |
| Pipeline time | 208s |

## Failing tests on buggy version

- org.apache.commons.compress.archivers.tar.TarArchiveOutputStream_Zest_Test::test15
- org.apache.commons.compress.archivers.tar.TarArchiveOutputStream_Zest_Test::test17
- org.apache.commons.compress.archivers.tar.TarArchiveOutputStream_Zest_Test::test25
- org.apache.commons.compress.archivers.tar.TarArchiveOutputStream_Zest_Test::test27
- org.apache.commons.compress.archivers.tar.TarArchiveOutputStream_Zest_Test::test30
- org.apache.commons.compress.archivers.tar.TarArchiveOutputStream_Zest_Test::test2
- org.apache.commons.compress.archivers.tar.TarArchiveOutputStream_Zest_Test::test6
- org.apache.commons.compress.archivers.tar.TarArchiveOutputStream_Zest_Test::test7

```
--- org.apache.commons.compress.archivers.tar.TarArchiveOutputStream_Zest_Test::test15
junit.framework.AssertionFailedError: expected:<0=[java.lang.Integer:0 |this=org.apache.commons.compress.archivers.tar.TarArchiveOutputStream:@?
1=new org.apache.commons.compress.archivers.tar.TarArchiveOutputStream:@?]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.tar.TarArchiveOutputStream_Zest_Test.test15(TarArchiveOutputStream_Zest_Test.java:112)
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
--- org.apache.commons.compress.archivers.tar.TarArchiveOutputStream_Zest_Test::test17
junit.framework.AssertionFailedError: expected:<0=[java.lang.Long:0 |this=org.apache.commons.compress.archivers.tar.TarArchiveOutputStream:@?
1=java.lang.Integer:0 |this=org.apache.commons.compress.archivers.tar.TarArchiveOutputStream:@?]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.tar.TarArchiveOutputStream_Zest_Test.test17(TarArchiveOutputStream_Zest_Test.java:126)
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
```

- Suite: `/home/user/suites/Compress/zest/9/t12001/Compress-9f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_9/b120_r1/src/org/apache/commons/compress/archivers/tar/TarArchiveOutputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_9/b120_r1`
