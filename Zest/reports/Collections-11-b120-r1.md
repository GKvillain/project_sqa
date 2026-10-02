# Zest – Collections-11 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 460801 (valid 79.34%) |
| Cycles | 33 |
| Corpus | 39 |
| Zest branch coverage (total / valid) | 175 / 172 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 39 |
| Failing on fixed | 0 |
| Failing on buggy | 3 |
| Bug detection | Fail (triggering: 3) |
| **Fault detected** | **yes** |
| Line coverage | 33 / 37 = 89.19% |
| Branch coverage | 10 / 12 = 83.33% |
| Test suite length (statements) | 55 |
| Mutation score | 13 / 21 = 61.90% |
| Fuzz time | 126s |
| Pipeline time | 200s |

## Failing tests on buggy version


```
--- org.apache.commons.collections.keyvalue.MultiKey_Zest_Test::test14
junit.framework.AssertionFailedError: expected:<0=[org.apache.commons.collections.keyvalue.MultiKey:MultiKey[𞤿󾣪𜱵𦲴𫓩] |this=org.apache.commons.collections.keyvalue.MultiKey:MultiKey[𞤿󾣪𜱵𦲴𫓩]
1=org.apache.commons.collections.keyvalue.MultiKey:MultiKey[𞤿󾣪𜱵𦲴𫓩] |this=org.apache.commons.collections.keyvalue.MultiKey:MultiKey[𞤿󾣪𜱵𦲴𫓩]]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.collections.keyvalue.MultiKey_Zest_Test.test14(MultiKey_Zest_Test.java:105)
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
--- org.apache.commons.collections.keyvalue.MultiKey_Zest_Test::test34
junit.framework.AssertionFailedError: expected:<...?󟉭ᝌ򳁥񪄩񣎴󦩈򢬲...
1=[org.apache.commons.collections.keyvalue.MultiKey:MultiKey[򫓕򀶒򎆿񕳒򕀫𚅽󂱻󳅈󾇐򦟅󌳌󶱜񽂱򋟦򫝬擄򚼾񩒧񊏧񮩈񶥸󄓏񖊲񹳳񠬃򕆉񰛼󗷊񽣙󾐞񱧋󆳇󮯹𪆕񏂨򁟚𴳩𫲂恎ᗱ𮨌󒩓𸀉򂢨󵫷񚋣𵋊􍚓󥭀󟉭ᝌ򳁥񪄩񣎴󦩈򢬲񖭯񴤍󋾢񅯸򲪢𕻩񺽦򯔊򅢞􉼺񢦾𤔴񿘇󪁴񹧞󷊖𶧰󜤶𬬦𷯮󯛰] |this=org.apache.commons.collections.keyvalue.MultiKey:MultiKey[򫓕򀶒򎆿񕳒򕀫𚅽󂱻󳅈󾇐򦟅󌳌󶱜񽂱򋟦򫝬擄򚼾񩒧񊏧񮩈񶥸󄓏񖊲񹳳񠬃򕆉񰛼󗷊񽣙󾐞񱧋󆳇󮯹𪆕񏂨򁟚𴳩𫲂恎ᗱ𮨌󒩓𸀉򂢨󵫷񚋣𵋊􍚓󥭀󟉭ᝌ򳁥񪄩񣎴󦩈򢬲񖭯񴤍󋾢񅯸򲪢𕻩񺽦򯔊򅢞􉼺񢦾𤔴񿘇󪁴񹧞󷊖𶧰󜤶𬬦𷯮󯛰]]
> but was:<...?󟉭ᝌ򳁥񪄩񣎴󦩈򢬲...
1=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.collections.keyvalue.MultiKey_Zest_Test.test34(MultiKey_Zest_Test.java:245)
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

- Suite: `/home/user/suites/Collections/zest/11/t12001/Collections-11f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Collections_11/b120_r1/src/org/apache/commons/collections/keyvalue/MultiKey_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Collections_11/b120_r1`
