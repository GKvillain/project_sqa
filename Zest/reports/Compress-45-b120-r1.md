# Zest – Compress-45 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 270989 (valid 33.75%) |
| Cycles | 8 |
| Corpus | 111 |
| Zest branch coverage (total / valid) | 216 / 208 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 111 |
| Failing on fixed | 0 |
| Failing on buggy | 6 |
| Bug detection | Fail (triggering: 6) |
| **Fault detected** | **yes** |
| Line coverage | 91 / 171 = 53.22% |
| Branch coverage | 56 / 108 = 51.85% |
| Test suite length (statements) | 111 |
| Mutation score | 353 / 575 = 61.39% |
| Fuzz time | 120s |
| Pipeline time | 250s |

## Failing tests on buggy version

- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test104
- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test28
- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test46
- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test52
- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test75
- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test80

```
--- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test104
junit.framework.AssertionFailedError: expected:<0=[java.lang.Integer:-1000 |a1=[B:[[-128, 59]]
1=java.lang.Integer:-1000 |a1=[B:[[-128, 59]]]
> but was:<0=[!java.lang.IllegalArgumentException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test.test104(TarUtils_Zest_Test.java:735)
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
junit.framework.AssertionFailedError: expected:<0=!java.lang.[ArrayIndexOutOfBounds]Exception
> but was:<0=!java.lang.[IllegalArgument]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test.test28(TarUtils_Zest_Test.java:203)
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
--- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test46
```

- Suite: `/home/user/suites/Compress/zest/45/t12001/Compress-45f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_45/b120_r1/src/org/apache/commons/compress/archivers/tar/TarUtils_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_45/b120_r1`
