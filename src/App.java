import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class App {
    public static void main(String[] args) throws IOException {
        try (ServerSocket ss = new ServerSocket(3000)) {
            while (true) {
                Socket s = ss.accept();
               new Handler(s).start();
            }
        }
    }


}

