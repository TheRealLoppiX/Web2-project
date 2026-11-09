package br.edu.ifsertaope.assistencia.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.edu.ifsertaope.assistencia.Application;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(Application.FUSO_HORARIO));
    }
}
