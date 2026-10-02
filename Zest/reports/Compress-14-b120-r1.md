# Zest – Compress-14 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 360116 (valid 44.00%) |
| Cycles | 12 |
| Corpus | 90 |
| Zest branch coverage (total / valid) | 208 / 205 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 90 |
| Failing on fixed | 0 |
| Failing on buggy | 2 |
| Bug detection | Fail (triggering: 2) |
| **Fault detected** | **yes** |
| Line coverage | 80 / 96 = 83.33% |
| Branch coverage | 48 / 62 = 77.42% |
| Test suite length (statements) | 90 |
| Mutation score | 223 / 378 = 58.99% |
| Fuzz time | 122s |
| Pipeline time | 239s |

## Failing tests on buggy version

- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test69
- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test73

```
--- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test69
junit.framework.AssertionFailedError: expected:<0=[java.lang.Long:0 |a0=[B:[[78, 0, 53, 89, -85, -118, -92, -19]]
1=]!java.lang.ArrayInde...> but was:<0=[]!java.lang.ArrayInde...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test.test69(TarUtils_Zest_Test.java:490)
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
--- org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test::test73
junit.framework.AssertionFailedError: expected:<0=[java.lang.Long:0 |a0=[B:[[78, 0, 53, 89, -85, -118, -92, -19]]
1=java.lang.Long:0 |a0=[B:[[78, 0, 53, 89, -85, -118, -92, -19]]]
> but was:<0=[!java.lang.ArrayIndexOutOfBoundsException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.tar.TarUtils_Zest_Test.test73(TarUtils_Zest_Test.java:518)
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

- Suite: `/home/user/suites/Compress/zest/14/t12001/Compress-14f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_14/b120_r1/src/org/apache/commons/compress/archivers/tar/TarUtils_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_14/b120_r1`
