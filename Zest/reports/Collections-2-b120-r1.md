# Zest – Collections-2 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 364038 (valid 70.47%) |
| Cycles | 23 |
| Corpus | 93 |
| Zest branch coverage (total / valid) | 257 / 254 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 92 |
| Failing on fixed | 0 |
| Failing on buggy | 4 |
| Bug detection | Fail (triggering: 4) |
| **Fault detected** | **yes** |
| Line coverage | 120 / 473 = 25.37% |
| Branch coverage | 59 / 274 = 21.53% |
| Test suite length (statements) | 92 |
| Mutation score | 391 / 444 = 88.06% |
| Fuzz time | 126s |
| Pipeline time | 223s |

## Failing tests on buggy version

- org.apache.commons.collections.ExtendedProperties_Zest_Test::test32
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test62
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test3
- org.apache.commons.collections.ExtendedProperties_Zest_Test::test5

```
--- org.apache.commons.collections.ExtendedProperties_Zest_Test::test32
junit.framework.AssertionFailedError: expected:<0=java.lang.String:[include |this=org.apache.commons.collections.ExtendedProperties:{}
1=java.lang.String:include] |this=org.apache.co...> but was:<0=java.lang.String:[򻪣𙭯󓡨𲚀󊆋 |this=org.apache.commons.collections.ExtendedProperties:{}
1=java.lang.String:򻪣𙭯󓡨𲚀󊆋] |this=org.apache.co...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.collections.ExtendedProperties_Zest_Test.test32(ExtendedProperties_Zest_Test.java:231)
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
--- org.apache.commons.collections.ExtendedProperties_Zest_Test::test62
junit.framework.AssertionFailedError: expected:<0=[java.lang.String:include] |this=org.apache.co...> but was:<0=[null] |this=org.apache.co...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.collections.ExtendedProperties_Zest_Test.test62(ExtendedProperties_Zest_Test.java:441)
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
--- org.apache.commons.collections.ExtendedProperties_Zest_Test::test3
junit.framework.AssertionFailedError: expected:<0=java.lang.String:[include] |this=org.apache.co...> but was:<0=java.lang.String:[򻪣𙭯] |this=org.apache.co...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
```

- Suite: `/home/user/suites/Collections/zest/2/t12001/Collections-2f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_2/b120_r1/src/org/apache/commons/collections/ExtendedProperties_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_2/b120_r1`
