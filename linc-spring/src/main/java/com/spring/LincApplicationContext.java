package com.spring;

import com.linc.service.BeanNameAware;

import java.beans.Introspector;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LincApplicationContext {
    private Class configClass;
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    private Map<String, Object> singletonObjects = new HashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
    public LincApplicationContext(Class configClass) {
        this.configClass = configClass;

        // scan the component bean
        scan(configClass);

        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if(beanDefinition.getScope().equals("singleton")){
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition){
        Class clazz = beanDefinition.getType();
        Object instance = null;
        try {
            instance = clazz.getConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                if(field.isAnnotationPresent(Autowired.class)){
                    field.setAccessible(true);
                    field.set(instance, getBean(field.getName()));
                }
            }

            //  Aware
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware)instance).setBeanName(beanName);
            }

            // BeanPostProcessor
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            if(instance instanceof InitializingBean){
                ((InitializingBean)instance).afterPropertiesSet();
            }

            // BeanPostProcessor
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    public Object getBean(String beanName){
        if(!beanDefinitionMap.containsKey(beanName)){
            throw new NullPointerException();
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if(beanDefinition.getScope().equals("singleton")){
            Object singletonBean = singletonObjects.get(beanName);
            if(singletonBean == null){
                singletonBean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, singletonBean);
            }
            return singletonBean;
        }
        else{
            Object prototypeBean = createBean(beanName, beanDefinition);
            return prototypeBean;
        }
    }

    private void scan(Class configClass) {
        if(configClass.isAnnotationPresent(ComponentScan.class)){
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value();
            path = path.replace(".", "/");

            ClassLoader classLoader = LincApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);

            File file = new File(resource.getFile());
            if(file.isDirectory()){
                for (File f : file.listFiles()) {
                    String absolutePath = f.getAbsolutePath();
                    absolutePath = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
                    absolutePath = absolutePath.replace("/", ".");

                    try {
                        Class<?> clazz = classLoader.loadClass(absolutePath);
                        if(clazz.isAnnotationPresent(Component.class)){

                            if(BeanPostProcessor.class.isAssignableFrom(clazz)){
                                BeanPostProcessor instance = (BeanPostProcessor)clazz.getConstructor().newInstance();
                                beanPostProcessorList.add(instance);
                            }
                            else{
                                Component componentAnnotation = clazz.getAnnotation(Component.class);
                                String beanName = componentAnnotation.value();

                                if("".equals(beanName)){
                                    beanName= Introspector.decapitalize(clazz.getSimpleName());
                                }
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(clazz);

                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                                    String value = scopeAnnotation.value();
                                    beanDefinition.setScope(value);
                                } else {
                                    beanDefinition.setScope("singleton");
                                }

                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }
    }
}
