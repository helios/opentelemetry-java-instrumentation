package io.opentelemetry.javaagent.instrumentation.demo;

import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.named;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class DemoInstrumentation implements TypeInstrumentation {
  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("helios.com.test.heliosjavademo.HeliosJavaDemoApplication");
  }

  @Override
  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        isMethod().and(isStatic()).and(named("main")), this.getClass().getName() + "$Hook");
  }

  public static class Hook {
    @Advice.OnMethodEnter
    public static void enter() {
      System.out.println("OnMethodEnter!!!!1");
    }

    @Advice.OnMethodExit
    public static void exit() {
      System.out.println("OnMethodExit!!!!");
    }
  }
}
