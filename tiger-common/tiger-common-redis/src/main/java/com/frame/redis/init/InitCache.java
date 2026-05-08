package com.frame.redis.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InitCache implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void run(String... args) throws Exception {
        Map<String, AbstractCache> beanMap = applicationContext.getBeansOfType(AbstractCache.class);
        if (beanMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, AbstractCache> entry : beanMap.entrySet()) {
            entry.getValue().initCache();
        }
    }

}
