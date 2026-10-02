# Zest – Compress-33 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 207417 (valid 59.70%) |
| Cycles | 20 |
| Corpus | 40 |
| Zest branch coverage (total / valid) | 188 / 185 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 40 |
| Failing on fixed | 0 |
| Failing on buggy | 19 |
| Bug detection | Fail (triggering: 19) |
| **Fault detected** | **yes** |
| Line coverage | 21 / 99 = 21.21% |
| Branch coverage | 14 / 78 = 17.95% |
| Test suite length (statements) | 40 |
| Mutation score | 113 / 133 = 84.96% |
| Fuzz time | 123s |
| Pipeline time | 232s |

## Failing tests on buggy version

- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test16
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test18
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test19
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test22
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test23
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test24
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test26
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test28
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test29
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test30
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test32
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test33
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test34
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test35
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test36
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test37
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test38
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test39
- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test2

```
--- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test16
junit.framework.AssertionFailedError: expected:<...rStreamFactory:@?
1=[java.lang.Boolean:false]
> but was:<...rStreamFactory:@?
1=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test.test16(CompressorStreamFactory_Zest_Test.java:119)
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
--- org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test::test18
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:false |a0=[B:[[-16, -126]]
1=java.lang.Boolean:false |this=org.apache.commons.compress.compressors.CompressorStreamFactory:@?]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.compressors.CompressorStreamFactory_Zest_Test.test18(CompressorStreamFactory_Zest_Test.java:133)
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
```

- Suite: `/home/user/suites/Compress/zest/33/t12001/Compress-33f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_33/b120_r1/src/org/apache/commons/compress/compressors/CompressorStreamFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_33/b120_r1`
