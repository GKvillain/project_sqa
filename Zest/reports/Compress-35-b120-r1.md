# Zest – Compress-35 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 270759 (valid 37.65%) |
| Cycles | 9 |
| Corpus | 80 |
| Zest branch coverage (total / valid) | 204 / 201 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 80 |
| Failing on fixed | 0 |
| Failing on buggy | 2 |
| Bug detection | Fail (triggering: 2) |
| **Fault detected** | **yes** |
| Line coverage | 80 / 170 = 47.06% |
| Branch coverage | 42 / 104 = 40.38% |
| Test suite length (statements) | 80 |
| Mutation score | 393 / 557 = 70.56% |
| Fuzz time | 122s |
| Pipeline time | 225s |

## Failing tests on buggy version

- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test42
- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test5

```
--- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test42
junit.framework.AssertionFailedError: expected:<... 86, 98, 21, -7]]
1=[!java.lang.ArrayIndexOutOfBoundsException]
> but was:<... 86, 98, 21, -7]]
1=[java.lang.Boolean:false |a0=[B:[[76, -103, 115, -73, 86, 98, 21, -7]]]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test.test42(TarUtils_Zest_Test.java:301)
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
--- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test5
junit.framework.AssertionFailedError: expected:<0=[!java.lang.ArrayIndexOutOfBoundsException]
> but was:<0=[java.lang.Boolean:false |a0=[B:[[106, 21]]]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test.test5(TarUtils_Zest_Test.java:42)
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

- Suite: `/home/user/suites/Compress/zest/35/t12001/Compress-35f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_35/b120_r1/src/org/apache/commons/compress/archivers/tar/TarUtils_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_35/b120_r1`
