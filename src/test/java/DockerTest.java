
import edu.eci.arep.docker.HttpConnection;
import edu.eci.arep.docker.RestServiceApplication;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Jaider Vargas
 */
public class DockerTest {
    private static Thread serverThread;
    private static Path filesBasePath = Paths.get("src/main/resources/static").toAbsolutePath().normalize();

    private final HttpConnection http = new HttpConnection();
    
    

    @BeforeAll
    public static void startServer() throws Exception {
        HttpConnection.port(9000);
        serverThread = new Thread(() -> {
            try {
                RestServiceApplication.main(new String[]{});
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        long start = System.currentTimeMillis();
        boolean up = false;
        while (System.currentTimeMillis() - start < 5000) {
            try {
                HttpURLConnection con = (HttpURLConnection) new java.net.URL("http://localhost:9000/").openConnection();
                con.setRequestMethod("GET");
                if (con.getResponseCode() >= 200) { up = true; break; }
            } catch (IOException e) { Thread.sleep(100); }
        }
        assertTrue(up, "The server is not running");
    }
    @Test
    public void serverIndexConnectionOK() throws IOException {
        HttpURLConnection con = http.stablishConnection("GET", "/");
        assertEquals(200, con.getResponseCode());
        assertTrue(con.getContentType().contains("text/html"));
    }
    @Test
    public void servirIndexHTMLOK() throws IOException {
        String response = http.makeRequest("GET", "/index.html");
        String expected = Files.readString(filesBasePath.resolve("index.html"));
        assertEquals(expected.replaceAll("\\s+", ""), response.replaceAll("\\s+", ""));
    }
    @Test
    public void serverCSSOK() throws IOException {
        HttpURLConnection con = http.stablishConnection("GET", "/styles.css");
        assertEquals(200, con.getResponseCode());
        assertTrue(con.getContentType().contains("text/css"), "Content-Type expected is text/css, was: " + con.getContentType());

        String body = http.makeRequest("GET", "/styles.css");
        String expected = Files.readString(filesBasePath.resolve("styles.css"));
        assertEquals(expected.replaceAll("\\s+", ""), body.replaceAll("\\s+", ""));
    }
    @Test
    public void serverJSOK() throws IOException {
        HttpURLConnection con = http.stablishConnection("GET", "/code.js");
        assertEquals(200, con.getResponseCode());
        assertTrue(con.getContentType().contains("application/javascript"), "Content-Type expected is application/javascript, was: " + con.getContentType());

        String body = http.makeRequest("GET", "/code.js");
        String expected = Files.readString(filesBasePath.resolve("code.js"));
        assertEquals(expected.replaceAll("\\s+", ""), body.replaceAll("\\s+", ""));
    }
    @Test
    public void serverJPGOK() throws IOException {
        HttpURLConnection con = http.stablishConnection("GET", "/james.jpg");
        assertEquals(200, con.getResponseCode());
        assertTrue(con.getContentType().contains("image/jpeg"), "Content-Type expected is image/jpeg, was: " + con.getContentType());

        byte[] body = http.makeRequestBytes("GET", "/james.jpg");
        byte[] expected = Files.readAllBytes(filesBasePath.resolve("james.jpg"));
        assertTrue(body.length > 0 && expected.length > 0);
    }
    @Test
    public void fileNotFound() throws IOException {
        HttpURLConnection con = http.stablishConnection("GET", "/eci.com");
        assertEquals(404, con.getResponseCode());
    }
 // -------- Framework (@GetMapping / @RequestParam) --------

    @Test
    public void greetingDefaultOK() throws IOException {
        HttpURLConnection con = http.stablishConnection("GET", "/app/greeting");
        assertEquals(200, con.getResponseCode());
        assertTrue(con.getContentType().contains("text/plain"));

        String body = http.makeRequest("GET", "/app/greeting");
        assertTrue(body.trim().equals("Hello, World!"),
                "Unexpected body: " + body);
    }

    @Test
    public void greetingWithParamOK() throws IOException {
        HttpURLConnection con = http.stablishConnection("GET", "/app/greeting?name=Jaider");
        assertEquals(200, con.getResponseCode());
        assertTrue(con.getContentType().contains("text/plain"));

        String body = http.makeRequest("GET", "/app/greeting?name=Jaider");
        assertTrue(body.contains("Jaider"),
                "Should use the provided name. Body was: " + body);
    }

}
