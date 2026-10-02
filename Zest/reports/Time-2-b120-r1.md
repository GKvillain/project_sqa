# Zest – Time-2 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 368793 (valid 49.98%) |
| Cycles | 53 |
| Corpus | 49 |
| Zest branch coverage (total / valid) | 600 / 288 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 49 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 94 / 301 = 31.23% |
| Branch coverage | 28 / 118 = 23.73% |
| Test suite length (statements) | 67 |
| Mutation score | 344 / 389 = 88.43% |
| Fuzz time | 124s |
| Pipeline time | 235s |

## Failing tests on buggy version

- org.joda.time.field.UnsupportedDurationField_Zest_Test::test5

```
--- org.joda.time.field.UnsupportedDurationField_Zest_Test::test5
junit.framework.AssertionFailedError: expected:<0=[!]java.lang.NullPointe...> but was:<0=[java.lang.Integer:0 |this=org.joda.time.field.UnsupportedDurationField:!toString:]java.lang.NullPointe...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.joda.time.field.UnsupportedDurationField_Zest_Test.test5(UnsupportedDurationField_Zest_Test.java:42)
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

- Suite: `/home/user/suites/Time/zest/2/t12001/Time-2f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Time_2/b120_r1/src/org/joda/time/field/UnsupportedDurationField_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Time_2/b120_r1`
