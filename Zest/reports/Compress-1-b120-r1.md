# Zest – Compress-1 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 223224 (valid 46.53%) |
| Cycles | 7 |
| Corpus | 81 |
| Zest branch coverage (total / valid) | 221 / 218 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 81 |
| Failing on fixed | 0 |
| Failing on buggy | 4 |
| Bug detection | Fail (triggering: 4) |
| **Fault detected** | **yes** |
| Line coverage | 66 / 165 = 40.00% |
| Branch coverage | 24 / 59 = 40.68% |
| Test suite length (statements) | 81 |
| Mutation score | 323 / 395 = 81.77% |
| Fuzz time | 122s |
| Pipeline time | 220s |

## Failing tests on buggy version

- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test23
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test31
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test47
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test8

```
--- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test23
junit.framework.AssertionFailedError: expected:<0=[!java.lang.NullPointerException]
> but was:<0=[void |this=org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test.test23(CpioArchiveOutputStream_Zest_Test.java:168)
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
--- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test31
junit.framework.AssertionFailedError: expected:<...veOutputStream:@?
1=[!java.lang.NullPointerException]
> but was:<...veOutputStream:@?
1=[void |this=org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test.test31(CpioArchiveOutputStream_Zest_Test.java:224)
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

- Suite: `/home/user/suites/Compress/zest/1/t12001/Compress-1f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_1/b120_r1/src/org/apache/commons/compress/archivers/cpio/CpioArchiveOutputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_1/b120_r1`
