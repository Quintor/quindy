package nl.quintor.studybits.university;

import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.concurrent.ExecutionException;

@Configuration
@ComponentScan
@EnableSwagger2
public class AppConfig implements WebMvcConfigurer {

    @Value( "${indy.poolname}" )
    private String poolName;

    @Autowired
    private RequestInterceptor requestInterceptor;

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
            System.out.println(ex.getMessage());
        }
        return new IndyPool(poolName);
    }

    @Bean
    public UserContext userContext() {
        return new UserContext();
    }

    @Override
    public void addInterceptors( InterceptorRegistry registry ) {
        registry.addInterceptor(requestInterceptor);
    }
}