package com.linc.service;

import com.spring.Autowired;
import com.spring.Component;
import com.spring.InitializingBean;
import com.spring.Scope;

@Component("userService")
@Scope("prototype")
public class UserService implements InitializingBean, UserInterface, BeanNameAware {

    @Autowired
    private OrderService orderService;
    @LincValue("test linc string")
    private String testString;

    private String beanName;

    public void  test()
    {
        System.out.println("test UserService");
    }

    public void  testBeanName()
    {
        System.out.println(beanName);
    }

    public void  testString()
    {
        System.out.println(testString);
    }

    public void  testAutowired()
    {
        System.out.println(orderService);
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("initializing...");
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}
