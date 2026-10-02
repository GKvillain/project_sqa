# Zest – Compress-46 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 252868 (valid 75.49%) |
| Cycles | 30 |
| Corpus | 32 |
| Zest branch coverage (total / valid) | 158 / 155 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 32 |
| Failing on fixed | 0 |
| Failing on buggy | 3 |
| Bug detection | Fail (triggering: 3) |
| **Fault detected** | **yes** |
| Line coverage | 40 / 118 = 33.90% |
| Branch coverage | 18 / 98 = 18.37% |
| Test suite length (statements) | 32 |
| Mutation score | 299 / 333 = 89.79% |
| Fuzz time | 120s |
| Pipeline time | 209s |

## Failing tests on buggy version

- org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp_Zest_Test::test13
- org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp_Zest_Test::test19
- org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp_Zest_Test::test21

```
--- org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp_Zest_Test::test13
junit.framework.AssertionFailedError: expected:<0=[!java.lang.IllegalArgumentException]
> but was:<0=[org.apache.commons.compress.archivers.zip.ZipLong:ZipLong value: -445566191752151187]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp_Zest_Test.test13(X5455_ExtendedTimestamp_Zest_Test.java:98)
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
--- org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp_Zest_Test::test19
junit.framework.AssertionFailedError: expected:<...a Field: Flags=0 
1=[!java.lang.IllegalArgumentException]
> but was:<...a Field: Flags=0 
1=[org.apache.commons.compress.archivers.zip.ZipLong:ZipLong value: -4036371399229071]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp_Zest_Test.test19(X5455_ExtendedTimestamp_Zest_Test.java:140)
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

- Suite: `/home/user/suites/Compress/zest/46/t12001/Compress-46f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_46/b120_r1/src/org/apache/commons/compress/archivers/zip/X5455_ExtendedTimestamp_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_46/b120_r1`
