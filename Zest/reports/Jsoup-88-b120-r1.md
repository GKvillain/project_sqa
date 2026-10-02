# Zest – Jsoup-88 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 313527 (valid 93.95%) |
| Cycles | 19 |
| Corpus | 114 |
| Zest branch coverage (total / valid) | 380 / 377 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 114 |
| Failing on fixed | 0 |
| Failing on buggy | 2 |
| Bug detection | Fail (triggering: 2) |
| **Fault detected** | **yes** |
| Line coverage | 49 / 66 = 74.24% |
| Branch coverage | 22 / 50 = 44.00% |
| Test suite length (statements) | 222 |
| Mutation score | 74 / 103 = 71.84% |
| Fuzz time | 120s |
| Pipeline time | 192s |

## Failing tests on buggy version

- org.jsoup.nodes.Attribute_Zest_Test::test10
- org.jsoup.nodes.Attribute_Zest_Test::test35

```
--- org.jsoup.nodes.Attribute_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<...𲝭𤓿󧵣񒝾񞛄䔻򇦠򣔶򴸩
1=[java.lang.String:] |this=org.jsoup.nod...> but was:<...𲝭𤓿󧵣񒝾񞛄䔻򇦠򣔶򴸩
1=[null] |this=org.jsoup.nod...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.Attribute_Zest_Test.test10(Attribute_Zest_Test.java:77)
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
--- org.jsoup.nodes.Attribute_Zest_Test::test35
junit.framework.AssertionFailedError: expected:<0=[java.lang.String: |this=org.jsoup.nodes.Attribute:񡡁𚀫񢻷򊆻򲵙
񟚽򝳷񍈝򳩚􍴿󍉠ė𽢷😬l򤉈񋐧񶈳䔻򇦠򦡜򴸩
1=java.lang.String:] |this=org.jsoup.nod...> but was:<0=[null |this=org.jsoup.nodes.Attribute:񡡁𚀫񢻷򊆻򲵙
񟚽򝳷񍈝򳩚􍴿󍉠ė𽢷😬l򤉈񋐧񶈳䔻򇦠򦡜򴸩
1=null] |this=org.jsoup.nod...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.Attribute_Zest_Test.test35(Attribute_Zest_Test.java:252)
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

- Suite: `/home/user/suites/Jsoup/zest/88/t12001/Jsoup-88f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_88/b120_r1/src/org/jsoup/nodes/Attribute_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_88/b120_r1`
