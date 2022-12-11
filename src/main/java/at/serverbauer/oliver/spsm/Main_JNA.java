package at.serverbauer.oliver.spsm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.management.OperatingSystemMXBean;

/**
 * JavaDoc this file!
 * Created: 26.01.2021
 * 13:54
 * 26.01.2021
 * <p>
 * 13:54
 * <p>
 * SPSM_Windows
 * at.serverbauer.oliver.spsm
 *
 * @author Serverbauer | GermanRPGBrothers.eu Inhaber and Oliver
 */

public class Main_JNA {

    public interface CLibrary extends Library {
        Main_JNA.CLibrary INSTANCE = (Main_JNA.CLibrary) Native.loadLibrary("kernel32", Main_JNA.CLibrary.class);

        // Diese Methode gibt die aktuelle GPU-Auslastung des Systems in Prozent zurück.
        int GetGPULoad();
    }
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            // Bean für den Betriebssystemmanager abrufen
            OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

            while (true) {
                // Warten auf eingehende Verbindungen
                Socket clientSocket = serverSocket.accept();

                // Streams für die Kommunikation mit dem Client erstellen
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Nachricht vom Client empfangen
                String request = in.readLine();

                // CPU-Auslastung ermitteln
                double cpuLoad = osBean.getSystemCpuLoad();

                // GPU-Auslastung ermitteln
                int gpuload = Main_JNA.CLibrary.INSTANCE.GetGPULoad();

                // RAM-Auslastung ermitteln
                long totalMemory = osBean.getTotalPhysicalMemorySize();

                // Disk-Auslastung ermitteln
                long freeDiskSpace = osBean.getFreePhysicalMemorySize();

                // Netzwerk-Auslastung ermitteln
                long totalNetwork = osBean.getTotalSwapSpaceSize();

                // Antwort an den Client senden
                out.println("Der request wurde empfangen. Die CPU-Auslastung beträgt: " + cpuLoad + "Die GPU-Auslastung beträgt: " + gpuload + " Die RAM-Auslastung beträgt: " + totalMemory + " Die Disk-Auslastung beträgt: " + freeDiskSpace + " Die Netzwerk-Auslastung beträgt: " + totalNetwork);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}