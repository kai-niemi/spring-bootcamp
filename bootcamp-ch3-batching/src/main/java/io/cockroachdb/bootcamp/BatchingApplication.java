package io.cockroachdb.bootcamp;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration;

@SpringBootApplication(exclude = {
        TransactionAutoConfiguration.class
})
public class BatchingApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(BatchingApplication.class)
                .logStartupInfo(true)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.CONSOLE)
                .run(args);
    }
}

