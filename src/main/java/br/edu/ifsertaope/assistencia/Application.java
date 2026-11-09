package br.edu.ifsertaope.assistencia;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static final String FUSO_HORARIO = "America/Recife";

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(FUSO_HORARIO));
        SpringApplication.run(Application.class, args);
    }
}
