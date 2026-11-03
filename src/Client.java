import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket s = new Socket("localhost",3000);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
        PrintWriter out = new PrintWriter(
            new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
        Scanner scanner = new Scanner(System.in);
        
        out.println(scanner.nextLine());

        String res = in.readLine();
        if (res.equals("OK FILE")) {
            in.readLine();

            String riga;
            while ((riga = in.readLine()) != null) {
                System.out.println(riga);
            }
        } else {
            System.out.println(res);
        }

        scanner.close();
        in.close();
        out.close();
        s.close();
    }
    
    
}
