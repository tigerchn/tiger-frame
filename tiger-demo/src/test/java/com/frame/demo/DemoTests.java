package com.frame.demo;

import com.frame.tool.date.DateObscureUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
public class DemoTests {


    @Test
    public void contextLoads() {
        String s = DateObscureUtil.nowToObscureHex();
        LocalDate localDate = DateObscureUtil.obscureHexToDate(s);
        System.out.println("localDate = " + localDate.toString());

    }
}
