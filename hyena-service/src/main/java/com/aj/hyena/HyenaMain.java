package com.aj.hyena;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan({ "com.aj.hyena" })
@MapperScan(basePackages = { "com.aj.hyena.mapper" })
@EnableTransactionManagement
public class HyenaMain {
    private static final Logger logger = LoggerFactory.getLogger(HyenaMain.class);

    public static void main(String[] args) {
        logger.info("starting......");
        new SpringApplicationBuilder(HyenaMain.class).web(WebApplicationType.SERVLET).run(args);
        logger.info("started");
    }
}