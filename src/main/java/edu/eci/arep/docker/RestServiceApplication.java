/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.eci.arep.docker;

import edu.eci.arep.docker.annotations.RestController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class RestServiceApplication {

public static void main(String[] args) throws Exception{
        System.out.println("Running Microspringboot");
        String base = "edu.eci.arep.docker.controller";
        Set<Class<?>> setControllers = FindControllers.find(base, RestController.class);
        List<Class<?>> controllers = new ArrayList<>(setControllers);
        String[] controllerNames = new String[controllers.size()];
        for (int i = 0; i < controllers.size(); i++) {
            controllerNames[i] = controllers.get(i).getName();
        }
        HttpServer.staticfiles("static");
        HttpServer.port(getPort());
        HttpServer.runServer(controllerNames);
}

private static int getPort() {
    if (System.getenv("PORT") != null) {
        return Integer.parseInt(System.getenv("PORT"));
    }
    return 9000;
}

}
