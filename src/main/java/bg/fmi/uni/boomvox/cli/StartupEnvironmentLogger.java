package bg.fmi.uni.boomvox.cli;

import bg.fmi.uni.boomvox.config.AppLogger;
import bg.fmi.uni.boomvox.config.BoomvoxProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class StartupEnvironmentLogger implements ApplicationRunner {
    private final AppLogger logger;
    private final BoomvoxProperties properties;
    private final ApplicationContext ctx;

    public StartupEnvironmentLogger(AppLogger logger, BoomvoxProperties properties, ApplicationContext ctx) {
        this.logger = logger;
        this.properties = properties;
        this.ctx = ctx;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("=== Boomvox Started ===");
        logger.info("Log level     : " + properties.getLogLevel());
        logger.info("Page size     : " + properties.getDefaultPageSize());
        logger.info("Log file      : " + properties.getLogFile());
        logger.debug("--- Project beans (bg.fmi.uni.boomvox) ---");
        Arrays.stream(ctx.getBeanDefinitionNames())
            .filter(name -> {
                try {
                    return ctx.getBean(name).getClass().getPackageName().startsWith("bg.fmi.uni.boomvox");
                } catch (Exception e) { return false; }
            })
            .forEach(name -> logger.debug("  bean: " + name));
    }
}