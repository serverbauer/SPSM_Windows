package at.serverbauer.oliver.spsm;

import at.serverbauer.oliver.spsm.old_and_new.Main_JNA_Beta;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.management.OperatingSystemMXBean;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 * JavaDoc this file!
 * Created: 12.12.2022
 * 21:00
 * <p>
 * SPSM_Windows
 * at.serverbauer.oliver.spsm
 *
 * @author Serverbauer | GermanRPGBrothers.eu Inhaber
 */
public class Main {

    public interface CLibrary extends Library {
        Main.CLibrary INSTANCE = (Main.CLibrary) Native.loadLibrary("kernel32", Main.CLibrary.class);

        // Diese Methode gibt die aktuelle GPU-Auslastung des Systems in Prozent zurück.
        int GetGPULoad();
    }
    public static void main(String[] args) throws IOException {

        Properties config = new Properties();

        int portdefault = 5000;

        try {
            String configFile = "Config/config.properties";

            config.load(new FileInputStream(configFile));

        } catch (IOException e) {
            File configFile = new File("Config/config.properties");
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
            e.printStackTrace();
        } finally {
            config.setProperty("port", String.valueOf(portdefault));
            config.setProperty("ipAddress", "127.0.0.1");
            try (FileOutputStream out = new FileOutputStream("Config/config.properties")) {
                config.store(out, "Config");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*
        //Für die Config

        //Laden der Config
        try (FileInputStream in = new FileInputStream("Config/config.properties")) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
         */

        //Port aus der Config laden
        int port = Integer.parseInt(config.getProperty("port"));

        //IP aus der Config laden
        String ipAddress = config.getProperty("ipAddress");

        //Debug Nachricht
        System.out.println("Folgende Daten in der Config gefunden:");
        System.out.println("Port: " + port + " | IP: " + ipAddress);

        ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ipAddress));

        // Warten auf eine Verbindung vom Master
        Socket masterSocket = serverSocket.accept();

        // Bean für den Betriebssystemmanager abrufen
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Sobald eine Verbindung vom Master empfangen wird, wird die CPU-Auslastung ermittelt
        // und an den Master gesendet
        while (true) {
            // CPU-Auslastung ermitteln

            double cpuLoad = osBean.getSystemCpuLoad();

            // GPU-Auslastung ermitteln
            int gpuload = CLibrary.INSTANCE.GetGPULoad();

            // RAM-Auslastung ermitteln
            long totalMemory = osBean.getTotalPhysicalMemorySize();

            // Disk-Auslastung ermitteln
            //long freeDiskSpace = osBean.getFreePhysicalMemorySize();
            long freeDiskSpace = osBean.getFreeMemorySize();

            // Netzwerk-Auslastung ermitteln
            long totalNetwork = osBean.getTotalSwapSpaceSize();

            // Senden der CPU-Auslastung an den Master
            DataOutputStream out = new DataOutputStream(masterSocket.getOutputStream());
            out.writeDouble(cpuLoad + gpuload + totalMemory + freeDiskSpace + totalNetwork);
        }
    }
}