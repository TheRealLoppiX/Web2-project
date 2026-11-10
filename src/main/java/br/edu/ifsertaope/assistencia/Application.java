package br.edu.ifsertaope.assistencia;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import br.edu.ifsertaope.assistencia.config.RelogioAssistencia;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(RelogioAssistencia.FUSO_HORARIO));
        SpringApplication.run(Application.class, args);
    }
}
