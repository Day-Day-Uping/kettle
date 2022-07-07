package com.kettle.mykettle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MykettleApplication {

    public static void main(String[] args) {
        SpringApplication.run(MykettleApplication.class, args);
    }

}
