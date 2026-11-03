# Archivio di file – Client/server TCP in Java

## Obiettivo

Realizzare due programmi Java, un **server** e un **client**, che permettano di consultare a distanza un file di testo. Il client richiede un file indicando il suo nome; il server lo cerca sul proprio disco e restituisce il contenuto oppure uno stato di errore.

Il servizio sarà un piccolo archivio di classe: un utente potrà consultare avvisi e orari senza avere una copia dei file sul proprio computer. **Il nome viaggia dal client al server; il testo del file viaggia dal server al client.** Il client non deve aprire direttamente la cartella del server.

Ripartite dalle istruzioni usate nella verifica: `ServerSocket`, `Socket`, `BufferedReader`, `PrintWriter`, `Scanner`, `readLine()`, `println()`, `split()`, `if` e `switch`. La novità principale è leggere dal disco e inviare una risposta composta da più righe.

## 1. Organizzazione del lavoro

Il server ascolta sulla porta **3000**. Nella directory di avvio del server create una cartella `documenti` con questi piccoli file, salvati in UTF-8:

| File | Contenuto da preparare |
| --- | --- |
| `saluto.txt` | Due righe di saluto |
| `orario.txt` | Alcune righe con giorni e orari, una riga vuota e una parola accentata |
| `vuoto.txt` | Nessun carattere |

Non create `assente.txt`: servirà per provare il caso di errore. Non occorre mettere questi file nel progetto del client.

Per questa esercitazione trasferiamo solo **testo**, non immagini, PDF o altri file binari. Il client deve visualizzare il testo ricevuto; non è richiesto riprodurre esattamente i byte o i terminatori di riga del file originale.

La versione base serve un client alla volta, ma deve restare attiva per accettare nuove connessioni. La gestione contemporanea di più client con i thread è facoltativa.

Procedete per tappe, verificando ogni risultato prima di continuare:

1. **Collegamento:** il client invia una richiesta e il server la stampa sulla propria console. Controllate di aver ricevuto esattamente ciò che avete inviato.
2. **Risposta di prova:** il server restituisce `OK FILE`, una riga vuota e due righe di testo scritte nel codice, poi chiude il socket. Il client deve mostrare entrambe le righe e terminare la lettura.
3. **Accesso al disco:** sostituite il testo di prova con le righe del file richiesto e gestite il file assente.
4. **Completamento:** aggiungete i controlli sulle richieste, provate file vuoti e righe vuote, poi permettete all'utente di effettuare una nuova richiesta.

Le prime due tappe sono passaggi intermedi: la consegna finale deve leggere davvero i file dal disco.

## 2. Regole del dialogo

Ogni connessione serve per **una sola richiesta e una sola risposta**:

1. Il client si collega.
2. Il client invia una riga di richiesta.
3. Il server invia la risposta.
4. Il server chiude il socket di quel client.

A differenza del gioco “Indovina Numero”, qui il server **non invia un messaggio di benvenuto**: aspetta la richiesta. Per richiedere un altro file, il client apre una nuova connessione.

### Richiesta

La forma prevista è:

```text
LEGGI nomefile.txt
```

Il comando `LEGGI` deve essere maiuscolo. Ignorate gli spazi iniziali e finali e accettate più spazi tra comando e nome. Non modificate maiuscole e minuscole del nome del file.

Sono ammessi soltanto nomi semplici, senza spazi, `/`, `\` o la sequenza `..`, e con estensione `.txt`. Non sono previste sottocartelle. Il server deve cercare i file esclusivamente nella cartella `documenti`.

### Risposta

Ogni risposta è composta da:

1. **Una riga di stato**, che indica l'esito.
2. **Una riga vuota**, che separa lo stato dal contenuto.
3. **Il contenuto**, presente soltanto in caso di successo.

La riga di stato serve al programma client per decidere che cosa fare; il contenuto è il testo da mostrare all'utente. La riga vuota segna l'inizio del contenuto, **non la sua fine**.

| Situazione | Riga di stato |
| --- | --- |
| File trovato e aperto correttamente | `OK FILE` |
| Richiesta vuota, argomenti mancanti o in eccesso, nome non ammesso | `ERR SYNTAX` |
| Comando diverso da `LEGGI` | `ERR UNKNOWNCMD` |
| Nome valido, ma file assente o percorso che non indica un file | `ERR NOTFOUND` |
| File presente, ma impossibile da aprire in lettura | `ERR READ` |

Per decidere quale errore inviare, controllate nell'ordine: richiesta vuota, comando riconosciuto, numero di argomenti e nome, presenza del file, apertura in lettura.

Anche in caso di errore inviate la riga vuota, poi chiudete la connessione. **Non inviate una parola speciale come `FINE` per terminare il contenuto**: anche quella parola potrebbe essere una riga del file. Il client riconosce la fine della risposta quando `readLine()` restituisce `null`, dopo la chiusura del socket da parte del server.

### Esempi di scambio

Se `saluto.txt` contiene due righe:

```text
Benvenuti in laboratorio!
Oggi condividiamo un file.
```

lo scambio è il seguente. Le etichette `CLIENT →` e `SERVER →` sono illustrative e non devono essere trasmesse.

```text
CLIENT → LEGGI saluto.txt
SERVER → OK FILE
SERVER →
SERVER → Benvenuti in laboratorio!
SERVER → Oggi condividiamo un file.
```

Il server chiude quindi il socket. Per un file assente:

```text
CLIENT → LEGGI inesistente.txt
SERVER → ERR NOTFOUND
SERVER →
```

Anche in questo caso il server chiude il socket. Un file vuoto produce `OK FILE`, una riga vuota e la chiusura: non è un errore.

## 3. Il server

Realizzate un programma che:

- crei un `ServerSocket` sulla porta 3000 e usi `accept()` dentro un ciclo;
- legga la richiesta dal socket e la analizzi;
- controlli la richiesta prima di accedere agli elementi dell'array prodotto da `split()`;
- cerchi il file nella cartella `documenti` e lo apra prima di comunicare `OK FILE`;
- invii stato, separatore e, se previsto, tutte le righe del file;
- chiuda file e socket del client, lasciando aperto il `ServerSocket` per la richiesta successiva.

Se il client si scollega senza inviare una richiesta, `readLine()` restituisce `null`: terminate quella gestione senza analizzare la stringa. Un errore relativo a un client non deve terminare tutto il server. Collocate quindi la gestione degli errori della singola connessione all'interno del ciclo che accetta i client.

Tenete distinti i due oggetti: chiudere il `Socket` termina il dialogo corrente; chiudere il `ServerSocket` impedisce di accettare nuove connessioni.

Se la lettura del file fallisce dopo l'invio di `OK FILE`, registrate l'errore sulla console del server e chiudete il socket: non inserite un secondo stato nel contenuto. In questa versione il client non può distinguere un trasferimento interrotto da uno completo: riprenderemo questo limite nella discussione finale.

## 4. Il client

Realizzate un programma che:

- chieda all'utente il nome del file usando `Scanner`;
- si colleghi a `localhost` sulla porta 3000 e invii `LEGGI ` seguito dal nome;
- legga la riga di stato e poi la riga vuota di separazione;
- in caso di `OK FILE`, legga e mostri tutte le righe successive fino a `null`;
- in caso di errore, mostri un messaggio comprensibile;
- chiuda il socket e permetta di richiedere un altro file, aprendo una nuova connessione, oppure di uscire.

La scelta di uscire riguarda l'interfaccia del client: non occorre inviare un comando `QUIT` al server. Se la connessione termina prima dello stato o del separatore, segnalate una risposta incompleta. Se il separatore ricevuto non è una stringa vuota, segnalate una risposta non conforme alle regole concordate.

Per confrontare lo stato usate `equals()`, per esempio `"OK FILE".equals(stato)`, non `==`. Non mostrate all'utente la riga vuota di separazione come se appartenesse al file.

## 5. Promemoria e suggerimenti

I frammenti seguenti sono aiuti da integrare nei vostri programmi, non una soluzione completa.

### Stream testuali sul socket

Riprendete lo schema già usato nella verifica. Per leggere correttamente anche le lettere accentate, impostate UTF-8 su entrambi i programmi:

```java
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

BufferedReader in = new BufferedReader(
    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
PrintWriter out = new PrintWriter(
    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
```

`out.println(testo)` invia una riga all’altro programma; `System.out.println(testo)` scrive soltanto sulla console locale. Il parametro `true` fa inviare subito i dati dopo ogni `println()`.

### Analizzare una richiesta

```java
String richiesta = in.readLine();
// Se richiesta è null, terminate la gestione di questo client.
// Altrimenti, se richiesta.trim().isEmpty(), rispondete ERR SYNTAX.
// Solo dopo questi controlli:
String[] parti = richiesta.trim().split("\\s+");
```

`split("\\s+")` separa usando uno o più spazi o altri caratteri di spaziatura. Verificate `parti.length` prima di leggere `parti[1]`. Per controllare il nome potete usare `contains()`, `endsWith()` e `equals()`; per indicare una barra inversa in una stringa Java si scrive `"\\"`.

### Cercare il file sul disco del server

```java
File cartella = new File("documenti");
File file = new File(cartella, nomeFile);

System.out.println(file.getAbsolutePath()); // Utile durante le prove
boolean presente = file.isFile();
```

`isFile()` restituisce `true` se il percorso indica un file esistente, non una cartella. Il controllo va eseguito dopo aver validato il nome.

**Attenzione alla cartella di avvio:** `documenti` è relativa alla directory da cui parte il programma, non alla posizione del sorgente `.java`. Usate il percorso assoluto stampato per capire dove il server sta cercando.

### Aprire, leggere e inviare un file

Un file può essere letto riga per riga con un altro `BufferedReader`, proprio come un socket:

```java
BufferedReader lettoreFile = new BufferedReader(
    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
```

L'apertura può generare una `IOException`: gestitela con `try/catch`. Solo dopo l'apertura riuscita inviate lo stato di successo e il separatore:

```java
out.println("OK FILE");
out.println(); // Riga vuota

String riga;
while ((riga = lettoreFile.readLine()) != null) {
    out.println(riga);
}
```

Per chiudere il file anche in caso di errore potete usare `try-with-resources`: la risorsa dichiarata tra parentesi viene chiusa automaticamente all'uscita dal blocco.

```java
try (BufferedReader lettoreFile = new BufferedReader(
        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
    // Qui inserite l'invio dello stato, del separatore e delle righe.
} catch (IOException e) {
    // Gestite l'errore distinguendo se lo stato è già stato inviato.
}
```

Questo frammento sostituisce l'apertura precedente: non aprite due volte lo stesso file. Potete usare una variabile `boolean` per ricordare se avete già inviato `OK FILE`. Alla fine della risposta chiudete il socket del client.

### Ricevere tutto il contenuto

Dopo aver letto stato e separatore, il client può usare:

```java
String riga;
while ((riga = in.readLine()) != null) {
    System.out.println(riga);
}
```

Una riga vuota del file è una stringa `""`: **non è `null`** e non deve interrompere il ciclo. Se il server non chiude il socket, il client rimane in attesa della prossima riga.

### Se il programma sembra bloccato

| Sintomo | Controllo da fare |
| --- | --- |
| Nessuno dei due programmi procede dopo il collegamento | Il client deve inviare la richiesta prima di leggere la risposta. |
| Il server riceve la richiesta, ma il client non vede nulla | State usando `out.println()` sul socket? Il `PrintWriter` ha il parametro `true`? |
| Il client mostra il testo ma non termina | Il server ha chiuso il socket di quel client dopo l'ultima riga? |
| Il client perde la prima riga del file | Avete letto esattamente una riga di stato e una riga di separazione? |
| Un file presente risulta assente | Stampate `file.getAbsolutePath()` e controllate directory di avvio e maiuscole. |
| La seconda richiesta non funziona | Il server torna su `accept()`? Il client apre un nuovo socket? |

## 6. Prove da eseguire

Verificate il server anche inviando richieste a mano con:

```bash
nc localhost 3000
```

Avviate prima il server, poi `nc`; scrivete la richiesta e premete Invio. Avviate nuovamente `nc` per ogni richiesta. Poi ripetete le prove possibili con il vostro client.

| Prova | Risultato atteso |
| --- | --- |
| File esistente con più righe | `OK FILE` e tutto il testo |
| File con righe vuote, accenti e una riga contenente `FINE` | Contenuto visualizzato senza interruzioni anticipate |
| File esistente ma vuoto | Successo, nessuna riga di contenuto |
| Nome valido di un file assente | `ERR NOTFOUND` |
| `LEGGI` senza nome oppure con due nomi | `ERR SYNTAX` |
| `CIAO saluto.txt` | `ERR UNKNOWNCMD` |
| Richiesta vuota | `ERR SYNTAX` |
| `LEGGI ../saluto.txt` | `ERR SYNTAX` |
| `LEGGI orario.pdf` | `ERR SYNTAX` |
| `  LEGGI   saluto.txt  ` | `OK FILE` e contenuto |
| Due richieste consecutive dal client Java | Due nuove connessioni, entrambe completate |
| Nuova richiesta dopo un errore, su una nuova connessione | Il server risponde ancora |

Prima di consegnare, scambiatevi il client con un compagno: **il vostro client deve funzionare con il suo server e viceversa**, senza modificare le regole del dialogo. Potete eseguire i programmi sullo stesso computer. Se usate due computer, sostituite `localhost` con l'indirizzo IP del computer che esegue il server.

## 7. Consegna e riflessione

Consegnate i sorgenti del client e del server, i file di esempio e brevi istruzioni per avviarli. Indicate le prove effettuate e le eventuali difficoltà rimaste.

Rispondete inoltre a queste domande:

1. Perché conviene separare lo stato dal contenuto? Come distinguiamo un file vuoto da un file inesistente?
2. Che cosa succede se client e server aspettano entrambi di leggere prima di scrivere?
3. Perché una riga vuota non può indicare la fine del file?
4. Quale informazione aggiungereste alla risposta per verificare che il contenuto sia arrivato tutto?
5. Come potrebbe il server comunicare anche il tipo di file? Dove inserireste questa informazione?
6. Se volessimo richiedere più file sulla stessa connessione, come potremmo riconoscere la fine di ogni risposta senza chiudere il socket?

Saranno considerati soprattutto la comprensione del dialogo client/server, il trasferimento completo del testo, la distinzione tra successo ed errore e la corretta gestione delle connessioni. Come estensioni facoltative potete gestire più client con i thread o salvare il testo ricevuto in una cartella del client.