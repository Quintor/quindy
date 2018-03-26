package nl.quintor.studybits.student;

import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import org.apache.commons.io.FileUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

@EnableSwagger2
@SpringBootApplication
public class Main {

    @Value( "${indy.poolname}" )
    private String poolName;

    public static void main( String[] args ) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                                                      .apis(RequestHandlerSelectors.any())
                                                      .paths(PathSelectors.any())
                                                      .build();
    }

    @Bean
    public IndyPool indyPool() throws InterruptedException, ExecutionException, IndyException {
        try {
            PoolUtils.createPoolLedgerConfig();
        } catch ( Exception ex ) {
            System.err.println(ex.getMessage());
        }
        return new IndyPool(poolName);
    }

    public static void removeIndyClientDirectory() throws Exception {
        String homeDir = System.getProperty("user.home");
        File indyClientDir = Paths.get(homeDir, ".indy_client").toFile();
        FileUtils.deleteDirectory(indyClientDir);
    }
}
