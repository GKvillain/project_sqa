# Zest – Collections-4 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 422090 (valid 92.38%) |
| Cycles | 29 |
| Corpus | 55 |
| Zest branch coverage (total / valid) | 240 / 237 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 55 |
| Failing on fixed | 0 |
| Failing on buggy | 9 |
| Bug detection | Fail (triggering: 9) |
| **Fault detected** | **yes** |
| Line coverage | 93 / 112 = 83.04% |
| Branch coverage | 35 / 48 = 72.92% |
| Test suite length (statements) | 55 |
| Mutation score | 56 / 73 = 76.71% |
| Fuzz time | 126s |
| Pipeline time | 212s |

## Failing tests on buggy version

- org.apache.commons.collections.map.MultiValueMap_Zest_Test::test14
- org.apache.commons.collections.map.MultiValueMap_Zest_Test::test24
- org.apache.commons.collections.map.MultiValueMap_Zest_Test::test30
- org.apache.commons.collections.map.MultiValueMap_Zest_Test::test32
- org.apache.commons.collections.map.MultiValueMap_Zest_Test::test35
- org.apache.commons.collections.map.MultiValueMap_Zest_Test::test36
- org.apache.commons.collections.map.MultiValueMap_Zest_Test::test37
- org.apache.commons.collections.map.MultiValueMap_Zest_Test::test38
- org.apache.commons.collections.map.MultiValueMap_Zest_Test::test2

```
--- org.apache.commons.collections.map.MultiValueMap_Zest_Test::test14
junit.framework.AssertionFailedError: expected:<0=java.lang.Boolean:[tru]e |this=org.apache.c...> but was:<0=java.lang.Boolean:[fals]e |this=org.apache.c...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.collections.map.MultiValueMap_Zest_Test.test14(MultiValueMap_Zest_Test.java:105)
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
--- org.apache.commons.collections.map.MultiValueMap_Zest_Test::test24
junit.framework.AssertionFailedError: expected:<0=[java.lang.String:渟] |this=org.apache.co...> but was:<0=[null] |this=org.apache.co...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.collections.map.MultiValueMap_Zest_Test.test24(MultiValueMap_Zest_Test.java:175)
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
--- org.apache.commons.collections.map.MultiValueMap_Zest_Test::test30
junit.framework.AssertionFailedError: expected:<0=java.lang.Boolean:[tru]e |this=org.apache.c...> but was:<0=java.lang.Boolean:[fals]e |this=org.apache.c...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.collections.map.MultiValueMap_Zest_Test.test30(MultiValueMap_Zest_Test.java:217)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
```

- Suite: `/home/user/suites/Collections/zest/4/t12001/Collections-4f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_4/b120_r1/src/org/apache/commons/collections/map/MultiValueMap_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_4/b120_r1`
