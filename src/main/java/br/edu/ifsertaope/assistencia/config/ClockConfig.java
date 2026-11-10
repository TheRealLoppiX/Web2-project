package br.edu.ifsertaope.assistencia.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return RelogioAssistencia.getInstancia().clock();
    }
}
