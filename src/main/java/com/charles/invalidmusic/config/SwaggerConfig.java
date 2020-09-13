package com.charles.invalidmusic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * SwaggerConfig
 *
 * @author charleswang
 * @since 2020/9/13 7:40 下午
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .pathMapping("/")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.charles.invalidmusic"))
                .paths(PathSelectors.any())
                .build().apiInfo(new ApiInfoBuilder()
                        .title("Invalid Music 接口列表")
                        .description("Invalid Music 接口列表")
                        .version("1.0.1")
                        .contact(new Contact("Charles", "https://github.com/charles-wxg", "charles.wxg@gmail.com"))
                        .license("The Apache License")
                        .licenseUrl("https://github.com/charles-wxg")
                        .build());
    }
}
