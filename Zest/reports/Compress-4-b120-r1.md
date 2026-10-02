# Zest – Compress-4 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 228124 (valid 47.43%) |
| Cycles | 10 |
| Corpus | 66 |
| Zest branch coverage (total / valid) | 209 / 206 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 66 |
| Failing on fixed | 0 |
| Failing on buggy | 20 |
| Bug detection | Fail (triggering: 20) |
| **Fault detected** | **yes** |
| Line coverage | 110 / 614 = 17.92% |
| Branch coverage | 23 / 253 = 9.09% |
| Test suite length (statements) | 66 |
| Mutation score | 864 / 958 = 90.19% |
| Fuzz time | 122s |
| Pipeline time | 223s |

## Failing tests on buggy version

- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test22
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test30
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test33
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test36
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test40
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test41
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test45
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test46
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test47
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test48
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test49
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test50
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test51
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test52
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test53
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test54
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test62
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test65
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test5
- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test9

```
--- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test22
junit.framework.AssertionFailedError: expected:<0=[void |this=org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream:@?
1=void |this=org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream:@?]
> but was:<0=[!java.lang.NullPointerException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test.test22(CpioArchiveOutputStream_Zest_Test.java:161)
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
--- org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test::test30
junit.framework.AssertionFailedError: expected:<0=[void |this=org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream:@?
1=!java.lang.UnsupportedOperation]Exception
> but was:<0=[!java.lang.NullPointer]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream_Zest_Test.test30(CpioArchiveOutputStream_Zest_Test.java:217)
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

- Suite: `/home/user/suites/Compress/zest/4/t12001/Compress-4f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_4/b120_r1/src/org/apache/commons/compress/archivers/cpio/CpioArchiveOutputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_4/b120_r1`
