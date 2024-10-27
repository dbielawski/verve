package com.example.demo;

import com.example.demo.service.VerveService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private VerveService verveService;

    @Test
    void contextLoads() {
        // Test that the context loads and beans are injected
        assert(verveService != null);
    }
}

