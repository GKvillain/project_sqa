# Zest – Collections-16 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 381611 (valid 68.70%) |
| Cycles | 24 |
| Corpus | 45 |
| Zest branch coverage (total / valid) | 198 / 190 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 45 |
| Failing on fixed | 0 |
| Failing on buggy | 4 |
| Bug detection | Fail (triggering: 4) |
| **Fault detected** | **yes** |
| Line coverage | 58 / 99 = 58.59% |
| Branch coverage | 16 / 24 = 66.67% |
| Test suite length (statements) | 45 |
| Mutation score | 53 / 66 = 80.30% |
| Fuzz time | 127s |
| Pipeline time | 196s |

## Failing tests on buggy version


```
--- org.apache.commons.collections.list.SetUniqueList_Zest_Test::test28
junit.framework.AssertionFailedError: expected:<0=[java.util.TreeSet:[򻴫󜜂񙝬򈃵񚆡񖧮񅧎񫦰󃇏] |this=org.apache.commons.collections.list.SetUniqueList:[򻴫󜜂񙝬򈃵񚆡񖧮񅧎񫦰󃇏]
1=!java.lang.IndexOutOfBounds]Exception
> but was:<0=[!H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.collections.list.SetUniqueList_Zest_Test.test28(SetUniqueList_Zest_Test.java:203)
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
--- org.apache.commons.collections.list.SetUniqueList_Zest_Test::test31
junit.framework.AssertionFailedError: expected:<...UniqueList:[򞰖񣄣]
1=[java.util.TreeSet:[櫎ᑏ锕ᱣ៥썕౦륅膪ⵇᲭ⫭鏰흋鮧톆괶䏕㰷䦳捼醗㪹뻠띟ࡸ筹⢇랛] |this=org.apache.commons.collections.list.SetUniqueList:[򞰖񣄣]]
> but was:<...UniqueList:[򞰖񣄣]
1=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.collections.list.SetUniqueList_Zest_Test.test31(SetUniqueList_Zest_Test.java:224)
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

- Suite: `/home/user/suites/Collections/zest/16/t12001/Collections-16f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_16/b120_r1/src/org/apache/commons/collections/list/SetUniqueList_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_16/b120_r1`
