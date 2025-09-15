/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.eci.arep.docker.controller;

import edu.eci.arep.docker.annotations.GetMapping;
import edu.eci.arep.docker.annotations.RequestParam;
import edu.eci.arep.docker.annotations.RestController;




@RestController
public class HelloRestController {

private static final String template = "Hello, %s!";

@GetMapping("/greeting")
public static String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
return String.format(template, name);
}
}