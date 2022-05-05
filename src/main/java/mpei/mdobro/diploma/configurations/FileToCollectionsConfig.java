package mpei.mdobro.diploma.configurations;

import mpei.mdobro.diploma.domain.calibrate.Normalization;
import mpei.mdobro.diploma.domain.parse.ExcelFileFilter;
import mpei.mdobro.diploma.domain.parse.FileToCollections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class FileToCollectionsConfig {

    @Value("${spring.fileType}")
    private String fileType;

    @Bean
    public ExcelFileFilter filter() {
        return new ExcelFileFilter(fileType);
    }

    @Bean
    public Normalization normalization() {
        return new Normalization();
    }

    @Bean
    public FileToCollections fileToCollections() {
        return new FileToCollections(normalization());
    }


}
