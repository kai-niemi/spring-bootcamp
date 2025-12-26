package io.cockroachdb.bootcamp;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration;

@SpringBootApplication(exclude = {
        TransactionAutoConfiguration.class
})
public class ContentionApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ContentionApplication.class)
                .logStartupInfo(true)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.CONSOLE)
                .run(args);
    }
}

