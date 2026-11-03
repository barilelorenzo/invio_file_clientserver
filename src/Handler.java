import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


class Handler extends Thread {
    private final Socket s;

    public Handler(Socket s) {
        this.s = s;
    }
    public void run(){
          try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
                Socket alias = s;) {

            String testo = in.readLine();
            if (testo == null)
                return;
            String[] pezzi = testo.trim().split(" ");
            if (pezzi.length != 2) {
                out.println("ERR SYNTAX");
                return;
            }
            if (!pezzi[0].equals("LEGGI")) {
                out.println("ERR UNKNOWNCMD");
            }
            if (pezzi[1].contains("\\") || pezzi[1].contains("/") || pezzi[1].contains("..")) {
                out.println("ERR SYNTAX");
                return;
            }
            if (!pezzi[1].endsWith(".txt")) {
                out.println("ERR SYNTAX");
                return;
            }

            File cartella = new File("documenti");
            File file = new File(cartella, pezzi[1]);

            System.out.println(file.getAbsolutePath()); // Utile durante le prove
            boolean presente = file.isFile();

            if (presente) {
                try (BufferedReader lettoreFile = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                    out.println("OK FILE");
                    out.println();

                    String riga;
                    while ((riga = lettoreFile.readLine()) != null) {
                        out.println(riga);
                    }
                } catch (IOException e) {
                    out.println("ERR READ");
                    return;
                }
            } else {
                out.println("ERR NOTFOUND");
            }

        } catch (IOException e) {
            System.out.println("Errore: " + e.toString());
        }
        

    }
}

