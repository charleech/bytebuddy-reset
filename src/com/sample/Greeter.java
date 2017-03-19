package com.sample;

@ToString
public class Greeter implements Greetable {

    @Override
    public String say(final String name) {
        return "Hello " + name + ".";
    }

}
