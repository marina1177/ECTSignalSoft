package mpei.mdobro.diploma.configurations;

import mpei.mdobro.diploma.domain.calibrate.Normalization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NormalizationConfig {

    @Bean
    public Normalization normalization() {
        return new Normalization();
    }

}
