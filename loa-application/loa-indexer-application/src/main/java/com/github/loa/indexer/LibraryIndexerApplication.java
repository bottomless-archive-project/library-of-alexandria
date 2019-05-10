package com.github.loa.indexer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@RequiredArgsConstructor
@SpringBootApplication(scanBasePackages = "com.github.loa")
@MapperScan(basePackages = "com.github.loa", annotationClass = Mapper.class)
public class LibraryIndexerApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryIndexerApplication.class, args);
    }
}
