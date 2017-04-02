/*
 * Copyright [2017] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
       // System.out.println("got name: "+name);
        return "hello world "+name;
    }

    public String seyHelloWithException() {
        throw new NullPointerException("stupid exception");
    }
}
