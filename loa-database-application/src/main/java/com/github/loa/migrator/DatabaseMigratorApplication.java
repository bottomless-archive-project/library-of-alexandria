package com.github.loa.migrator;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.loa")
@MapperScan(basePackages = "com.github.loa", annotationClass = Mapper.class)
public class DatabaseMigratorApplication {

    public static void main(final String[] args) {
        SpringApplication.run(DatabaseMigratorApplication.class, args);
    }
}
