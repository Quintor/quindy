package nl.quintor.studybits.university.config;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import nl.quintor.studybits.university.RequestInterceptor;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.ExecutionException;

@Slf4j
@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Value("${indy.poolname}")
    private String poolName;

    private final RequestInterceptor requestInterceptor;

    @Autowired
    public AppConfig(RequestInterceptor requestInterceptor) {
        this.requestInterceptor = requestInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("Disabling cors for all requests");
        registry.addMapping("/**")
                .allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH");
    }

    @Bean
    public IndyPool indyPool() throws InterruptedException, ExecutionException, IndyException {
        try {
            PoolUtils.createPoolLedgerConfig();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return new IndyPool(poolName);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor);
    }
}