package io.cockroachdb.bootcamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration;

@SpringBootApplication(exclude = {
        TransactionAutoConfiguration.class
})
public class LockingApplication implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${spring.profiles.active}")
    private String profiles;

    @Override
    public void run(ApplicationArguments args)  {
        logger.info("Started lock demo app with profiles: %s".formatted(profiles));
    }

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "default,verbose,jdbclock");
//        System.setProperty("spring.profiles.active", "default,verbose,shedlock");
//        System.setProperty("spring.profiles.active", "default,verbose,memlock");

        new SpringApplicationBuilder(LockingApplication.class)
                .logStartupInfo(true)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.CONSOLE)
                .run(args);
    }
}

