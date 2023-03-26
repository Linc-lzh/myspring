package com.linc.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.Field;
@Component
public class LincValueBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {

        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(LincValue.class)) {
                field.setAccessible(true);
                try {
                    field.set(bean, field.getAnnotation(LincValue.class).value());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return bean;
    }
}
