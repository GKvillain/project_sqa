# Zest – Compress-44 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 358731 (valid 0.00%) |
| Cycles | 77 |
| Corpus | 7 |
| Zest branch coverage (total / valid) | 108 / 0 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 7 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 3 / 21 = 14.29% |
| Branch coverage | 1 / 10 = 10.00% |
| Test suite length (statements) | 7 |
| Mutation score | 24 / 25 = 96.00% |
| Fuzz time | 120s |
| Pipeline time | 215s |

## Failing tests on buggy version

- org.apache.commons.compress.utils.ChecksumCalculatingInputStream_Zest_Test::test3

```
--- org.apache.commons.compress.utils.ChecksumCalculatingInputStream_Zest_Test::test3
junit.framework.AssertionFailedError: expected:<0=[!java.lang.NullPointerException]
> but was:<0=[new org.apache.commons.compress.utils.ChecksumCalculatingInputStream:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.utils.ChecksumCalculatingInputStream_Zest_Test.test3(ChecksumCalculatingInputStream_Zest_Test.java:28)
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

- Suite: `/home/user/suites/Compress/zest/44/t12001/Compress-44f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_44/b120_r1/src/org/apache/commons/compress/utils/ChecksumCalculatingInputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_44/b120_r1`
