# Zest – Compress-7 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 225261 (valid 48.03%) |
| Cycles | 9 |
| Corpus | 69 |
| Zest branch coverage (total / valid) | 186 / 183 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 69 |
| Failing on fixed | 0 |
| Failing on buggy | 3 |
| Bug detection | Fail (triggering: 3) |
| **Fault detected** | **yes** |
| Line coverage | 52 / 64 = 81.25% |
| Branch coverage | 33 / 38 = 86.84% |
| Test suite length (statements) | 69 |
| Mutation score | 154 / 230 = 66.96% |
| Fuzz time | 123s |
| Pipeline time | 231s |

## Failing tests on buggy version

- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test38
- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test45
- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test66

```
--- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test38
junit.framework.AssertionFailedError: expected:<0=java.lang.String:I[ð] |a0=[B:[[73, -16, 0...> but was:<0=java.lang.String:I[￰] |a0=[B:[[73, -16, 0...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test.test38(TarUtils_Zest_Test.java:273)
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
--- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test45
junit.framework.AssertionFailedError: expected:<0=java.lang.String:[þ^8 |a0=[B:[[-99, 76, -55, 75, -2, 94, 56, 0]]
1=java.lang.String:þ]^8 |a0=[B:[[-99, 76,...> but was:<0=java.lang.String:[￾^8 |a0=[B:[[-99, 76, -55, 75, -2, 94, 56, 0]]
1=java.lang.String:￾]^8 |a0=[B:[[-99, 76,...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test.test45(TarUtils_Zest_Test.java:322)
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
--- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test66
junit.framework.AssertionFailedError: expected:<0=java.lang.String:[LÉKþ^8 |a0=[B:[[-99, 76, -55, 75, -2, 94, 56, 0]]
1=java.lang.String:LÉKþ]^8 |a0=[B:[[-99, 76,...> but was:<0=java.lang.String:[ﾝL￉K￾^8 |a0=[B:[[-99, 76, -55, 75, -2, 94, 56, 0]]
1=java.lang.String:ﾝL￉K￾]^8 |a0=[B:[[-99, 76,...>
```

- Suite: `/home/user/suites/Compress/zest/7/t12001/Compress-7f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_7/b120_r1/src/org/apache/commons/compress/archivers/tar/TarUtils_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_7/b120_r1`
