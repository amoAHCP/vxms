package org.jacpfx.beans;

import org.springframework.stereotype.Component;

/**
 * Created by Andy Moncsek on 28.01.16.
 */
@Component
public class HelloWorldBean {
    public String sayHallo(){
        return "hello world";
    }

    public String sayHallo(String name){
        System.out.println("got name: "+name);
        return "hello world "+name;
    }

    public String seyHelloWithException() {
        throw new NullPointerException("stupid exception");
    }
}
