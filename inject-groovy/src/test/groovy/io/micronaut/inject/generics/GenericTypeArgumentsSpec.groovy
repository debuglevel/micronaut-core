/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.inject.generics

import io.micronaut.AbstractBeanDefinitionSpec
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod

import java.util.function.Function

class GenericTypeArgumentsSpec extends AbstractBeanDefinitionSpec {

    void "test type arguments for interface"() {
        given:
        BeanDefinition definition = buildBeanDefinition('test.GenericsTest1','''\
package test;

import io.micronaut.inject.annotation.*;
import io.micronaut.context.annotation.*;

@javax.inject.Singleton
class GenericsTest1 implements java.util.function.Function<String, Integer>{

    public Integer apply(String str) {
        return 10;
    }
}

class Foo {}
''')
        expect:
        definition != null
        definition.getTypeArguments(Function).size() == 2
        definition.getTypeArguments(Function)[0].name == 'T'
        definition.getTypeArguments(Function)[1].name == 'R'
        definition.getTypeArguments(Function)[0].type == String
        definition.getTypeArguments(Function)[1].type == Integer
    }

    void "test type arguments for inherited interface"() {
        given:
        BeanDefinition definition = buildBeanDefinition('test.GenericsTest2','''\
package test;

import io.micronaut.inject.annotation.*;
import io.micronaut.context.annotation.*;

@javax.inject.Singleton
class GenericsTest2 implements Foo {

    public Integer apply(String str) {
        return 10;
    }
}

interface Foo extends java.util.function.Function<String, Integer> {}
''')
        expect:
        definition != null
        definition.getTypeArguments(Function).size() == 2
        definition.getTypeArguments(Function)[0].name == 'T'
        definition.getTypeArguments(Function)[1].name == 'R'
        definition.getTypeArguments(Function)[0].type == String
        definition.getTypeArguments(Function)[1].type == Integer
    }



    void "test type arguments for superclass with interface"() {
        given:
        BeanDefinition definition = buildBeanDefinition('test.GenericsTest3','''\
package test;

import io.micronaut.inject.annotation.*;
import io.micronaut.context.annotation.*;

@javax.inject.Singleton
class GenericsTest3 extends Foo {

    public Integer apply(String str) {
        return 10;
    }
}

abstract class Foo implements java.util.function.Function<String, Integer> {}
''')
        expect:
        definition != null
        definition.getTypeArguments(Function).size() == 2
        definition.getTypeArguments(Function)[0].name == 'T'
        definition.getTypeArguments(Function)[1].name == 'R'
        definition.getTypeArguments(Function)[0].type == String
        definition.getTypeArguments(Function)[1].type == Integer
    }

    void "test type arguments for superclass"() {
        given:
        BeanDefinition definition = buildBeanDefinition('test.GenericsTest4','''\
package test;

import io.micronaut.inject.annotation.*;
import io.micronaut.context.annotation.*;

@javax.inject.Singleton
class GenericsTest4 extends Foo<String, Integer> {

    public Integer apply(String str) {
        return 10;
    }
}

abstract class Foo<T, R> {

    abstract R apply(T t);
}
''')
        expect:
        definition != null
        definition.getTypeArguments('test.Foo').size() == 2
        definition.getTypeArguments('test.Foo')[0].name == 'T'
        definition.getTypeArguments('test.Foo')[1].name == 'R'
        definition.getTypeArguments('test.Foo')[0].type == String
        definition.getTypeArguments('test.Foo')[1].type == Integer
    }

    void "test type arguments for factory"() {
        given:
        BeanDefinition definition = buildBeanDefinition('test.TestFactory$MyFunc0','''\
package test;

import io.micronaut.inject.annotation.*;
import io.micronaut.context.annotation.*;

@Factory
class TestFactory {

    @Bean
    java.util.function.Function<String, Integer> myFunc() {
        return { String str -> 10 };
    }
}

''')
        expect:
        definition != null
        definition.getTypeArguments(Function).size() == 2
        definition.getTypeArguments(Function)[0].name == 'T'
        definition.getTypeArguments(Function)[1].name == 'R'
        definition.getTypeArguments(Function)[0].type == String
        definition.getTypeArguments(Function)[1].type == Integer
    }

    void "test type arguments for methods"() {
        BeanDefinition definition = buildBeanDefinition('test.StatusController', '''
package test;

import io.micronaut.http.annotation.*;

class GenericController<T> {

    @Post
    T save(@Body T entity) {
        return entity;
    }
}

@Controller
class StatusController extends GenericController<String> {

}
''')
        List<ExecutableMethod> methods = definition.getExecutableMethods().toList()

        expect:
        definition != null
        methods.size() == 1
        methods[0].getArguments()[0].type == String
        methods[0].getReturnType().getFirstTypeVariable().get().type == String
    }
}
