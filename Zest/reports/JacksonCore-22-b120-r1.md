# Zest – JacksonCore-22 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 408652 (valid 68.43%) |
| Cycles | 58 |
| Corpus | 15 |
| Zest branch coverage (total / valid) | 138 / 135 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 15 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 25 / 388 = 6.44% |
| Branch coverage | 6 / 283 = 2.12% |
| Test suite length (statements) | 15 |
| Mutation score | 349 / 369 = 94.58% |
| Fuzz time | 127s |
| Pipeline time | 204s |

## Failing tests on buggy version

- com.fasterxml.jackson.core.filter.FilteringParserDelegate_Zest_Test::test11

```
--- com.fasterxml.jackson.core.filter.FilteringParserDelegate_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<...ParserDelegate:@?
1=[java.lang.Boolean:true |this=com.fasterxml.jackson.core.filter.FilteringParserDelegate:@?]
> but was:<...ParserDelegate:@?
1=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.core.filter.FilteringParserDelegate_Zest_Test.test11(FilteringParserDelegate_Zest_Test.java:84)
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

- Suite: `/home/user/suites/JacksonCore/zest/22/t12001/JacksonCore-22f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_22/b120_r1/src/com/fasterxml/jackson/core/filter/FilteringParserDelegate_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_22/b120_r1`
