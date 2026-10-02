# Zest – Collections-5 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 416923 (valid 73.34%) |
| Cycles | 31 |
| Corpus | 44 |
| Zest branch coverage (total / valid) | 198 / 190 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 44 |
| Failing on fixed | 0 |
| Failing on buggy | 4 |
| Bug detection | Fail (triggering: 4) |
| **Fault detected** | **yes** |
| Line coverage | 58 / 86 = 67.44% |
| Branch coverage | 15 / 22 = 68.18% |
| Test suite length (statements) | 44 |
| Mutation score | 46 / 59 = 77.97% |
| Fuzz time | 126s |
| Pipeline time | 198s |

## Failing tests on buggy version

- org.apache.commons.collections.list.SetUniqueList_Zest_Test::test16
- org.apache.commons.collections.list.SetUniqueList_Zest_Test::test21
- org.apache.commons.collections.list.SetUniqueList_Zest_Test::test25
- org.apache.commons.collections.list.SetUniqueList_Zest_Test::test32

```
--- org.apache.commons.collections.list.SetUniqueList_Zest_Test::test16
junit.framework.AssertionFailedError: expected:<....SetUniqueList:[]
1=[!java.lang.IndexOutOfBoundsException]
> but was:<....SetUniqueList:[]
1=[java.lang.Boolean:true |this=org.apache.commons.collections.list.SetUniqueList:[󡏩𖍬󐳅󶯇󄷳]]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.collections.list.SetUniqueList_Zest_Test.test16(SetUniqueList_Zest_Test.java:119)
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
--- org.apache.commons.collections.list.SetUniqueList_Zest_Test::test21
junit.framework.AssertionFailedError: expected:<...?路򲾇󋷭󫔌񒏻󂪌𚮔𚜃]
1=[!java.lang.IndexOutOfBoundsException]
> but was:<...?路򲾇󋷭󫔌񒏻󂪌𚮔𚜃]
1=[java.lang.Boolean:true |this=org.apache.commons.collections.list.SetUniqueList:[𵩅񚳛󐳅򤓛򀷍, 򋗝򞷼𵄸𹻼路򲾇󋷭󫔌񒏻󂪌𚮔𚜃, 𵩅񚳛󐳅򤓛򀷍]]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.collections.list.SetUniqueList_Zest_Test.test21(SetUniqueList_Zest_Test.java:154)
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
```

- Suite: `/home/user/suites/Collections/zest/5/t12001/Collections-5f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_5/b120_r1/src/org/apache/commons/collections/list/SetUniqueList_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_5/b120_r1`
