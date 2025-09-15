/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.eci.arep.docker;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.lang.annotation.Annotation;
import java.nio.file.Files;

/**
 *
 * @author User
 */
public class FindControllers {

    public static Set<Class<?>> find(String basePackage, Class<? extends Annotation> ann) throws Exception {
    Set<Class<?>> found = new HashSet<>();
    String path = basePackage.replace('.', '/');
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Enumeration<URL> urls = cl.getResources(path);

    while (urls.hasMoreElements()) {
        URL url = urls.nextElement();
        String proto = url.getProtocol();

        if ("file".equals(proto)) {
            Path dir = Paths.get(url.toURI());
            try (var stream = Files.walk(dir)) {
                stream.filter(p -> p.toString().endsWith(".class")).forEach(p -> {
                    String cn = basePackage + "." +
                            dir.relativize(p).toString()
                               .replace(File.separatorChar, '.')
                               .replaceAll("\\.class$", "");
                    try {
                        Class<?> c = Class.forName(cn);
                        if (c.isAnnotationPresent(ann)) found.add(c);
                    } catch (Throwable ignore) {}
                });
            }
        }
    }
    return found;
}

}
