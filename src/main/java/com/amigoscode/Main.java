package com.amigoscode;

import com.amigoscode.customer.Customer;
import com.amigoscode.customer.CustomerRepository;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication

public class Main {

    public static void main(String[] args) {
       ConfigurableApplicationContext applicationContext =  SpringApplication.run(Main.class, args);
       //printBeans(applicationContext);
    }
    @Bean
    CommandLineRunner runner(CustomerRepository customerRepository) {
        return args -> {
            Customer alex = new Customer(
                    "Alex",
                    "alex@gmail.com",
                    21
            );
            Customer jamila = new Customer(
                    "Jamila",
                    "jamila@gmail.com",
                    19
            );
            List<Customer> customers = List.of(alex, jamila);
            customerRepository.saveAll(customers);
        };
    }

    /*
    @Bean("foo")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    // @RequestScope()
    public Foo getFoo() {
        return new Foo("bar");
    }

    record Foo(String name){}

    private static void printBeans(ConfigurableApplicationContext ctx) {
        String[] beanDefinitionNames =
                ctx.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            System.out.println(beanDefinitionName);
        }
    }

     */

}