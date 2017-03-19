package com.sample;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

public class MyTransformation {
    public static void main(final String[] args) {
        ByteBuddyAgent.install();
        final AgentBuilder bldr = new AgentBuilder.Default(new ByteBuddy()).
                                             ignore(ElementMatchers.none()).
//                                             with(AgentBuilder.Listener.
//                                                               StreamWriting.
//                                                               toSystemOut()).
                                             with(RedefinitionStrategy.
                                                     RETRANSFORMATION).
                                             disableClassFormatChanges().
                                             type(ElementMatchers.isSubTypeOf(
                                                       Greetable.class)).
// ====> If use the matcher with concrete class,
//       the transformation is worked properly.
//                                             type(ElementMatchers.isSubTypeOf(
//                                                     Greeter.class)).
                                             transform(
    new AgentBuilder.Transformer() {
    @Override
    public DynamicType.Builder<?> transform(final DynamicType.Builder<?> builder,
                                            final TypeDescription        typeDescription,
                                            final ClassLoader            classloader,
                                            final JavaModule             module) {
      return builder.method(ElementMatchers.named("say")).
                    intercept(FixedValue.value("transformed"));
    }
    });

        final ResettableClassFileTransformer resetter =
                                          bldr.installOnByteBuddyAgent();
        final Greetable g1 = new Greeter();
        System.out.println("====> MyTransformation: " + g1.say("001"));

        //remove the transformation
        resetter.reset(ByteBuddyAgent.getInstrumentation(),
                       RedefinitionStrategy.RETRANSFORMATION);
        final Greetable g2 = new Greeter();
        System.out.println("====> MyTransformation: " + g2.say("002"));

        //re-apply the transformation
        bldr.installOn(ByteBuddyAgent.getInstrumentation());
        final Greetable g3 = new Greeter();
        System.out.println("====> MyTransformation: " + g3.say("003"));

    }
}
