# Zest – Compress-34 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 211178 (valid 91.44%) |
| Cycles | 16 |
| Corpus | 48 |
| Zest branch coverage (total / valid) | 198 / 195 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 48 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 62 / 71 = 87.32% |
| Branch coverage | 6 / 14 = 42.86% |
| Test suite length (statements) | 48 |
| Mutation score | 106 / 114 = 92.98% |
| Fuzz time | 122s |
| Pipeline time | 223s |

## Failing tests on buggy version

- org.apache.commons.compress.archivers.zip.X7875_NewUnix_Zest_Test::test8

```
--- org.apache.commons.compress.archivers.zip.X7875_NewUnix_Zest_Test::test8
junit.framework.AssertionFailedError: expected:<...ort:ZipShort value: [0 |this=org.apache.commons.compress.archivers.zip.X7875_NewUnix:0x7875 Zip Extra Field: UID=1000 GID=1000
1=org.apache.commons.compress.archivers.zip.ZipShort:ZipShort value: 0] |this=org.apache.co...> but was:<...ort:ZipShort value: [7 |this=org.apache.commons.compress.archivers.zip.X7875_NewUnix:0x7875 Zip Extra Field: UID=1000 GID=1000
1=org.apache.commons.compress.archivers.zip.ZipShort:ZipShort value: 7] |this=org.apache.co...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.compress.archivers.zip.X7875_NewUnix_Zest_Test.test8(X7875_NewUnix_Zest_Test.java:63)
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

- Suite: `/home/user/suites/Compress/zest/34/t12001/Compress-34f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_34/b120_r1/src/org/apache/commons/compress/archivers/zip/X7875_NewUnix_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_34/b120_r1`
