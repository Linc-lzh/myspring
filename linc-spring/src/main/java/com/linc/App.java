package com.linc;

import com.linc.service.OrderService;
import com.linc.service.UserInterface;
import com.linc.service.UserService;
import com.spring.LincApplicationContext;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        LincApplicationContext  applicationContext = new LincApplicationContext(AppConfig.class);
//        UserService userService = (UserService)applicationContext.getBean("userService");
//        userService.test();

//        OrderService orderService = (OrderService)applicationContext.getBean("orderService");
//        orderService.test();

        UserInterface userService = (UserInterface)applicationContext.getBean("userService");
        userService.test();
        userService.testString();
        userService.testBeanName();
    }
}
