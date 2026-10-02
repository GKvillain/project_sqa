# Zest – Compress-8 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 160108 (valid 68.02%) |
| Cycles | 9 |
| Corpus | 49 |
| Zest branch coverage (total / valid) | 182 / 179 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 49 |
| Failing on fixed | 0 |
| Failing on buggy | 5 |
| Bug detection | Fail (triggering: 5) |
| **Fault detected** | **yes** |
| Line coverage | 51 / 81 = 62.96% |
| Branch coverage | 26 / 48 = 54.17% |
| Test suite length (statements) | 49 |
| Mutation score | 189 / 283 = 66.78% |
| Fuzz time | 123s |
| Pipeline time | 223s |

## Failing tests on buggy version

- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test16
- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test25
- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test28
- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test34
- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test43

```
--- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test16
junit.framework.AssertionFailedError: expected:<0=[!java.lang.IllegalArgumentException]
> but was:<0=[java.lang.Long:0 |a0=[B:[[-48, -12, -48, -48, -48, -48, -15, -97]]]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test.test16(TarUtils_Zest_Test.java:119)
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
--- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test25
junit.framework.AssertionFailedError: expected:<...ils:@?
1=!java.lang.[Array]IndexOutOfBoundsExce...> but was:<...ils:@?
1=!java.lang.[String]IndexOutOfBoundsExce...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test.test25(TarUtils_Zest_Test.java:182)
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
--- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test28
junit.framework.AssertionFailedError: expected:<...ils:@?
```

- Suite: `/home/user/suites/Compress/zest/8/t12001/Compress-8f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_8/b120_r1/src/org/apache/commons/compress/archivers/tar/TarUtils_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_8/b120_r1`
