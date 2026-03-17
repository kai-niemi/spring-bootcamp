package io.cockroachdb.bootcamp;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration;

@SpringBootApplication(exclude = {
        TransactionAutoConfiguration.class
})
public class CteApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(CteApplication.class)
                .logStartupInfo(true)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.CONSOLE)
                .run(args);
    }
}

