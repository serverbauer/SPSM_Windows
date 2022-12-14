package at.serverbauer.oliver.spsm;

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
 * Created: 13.12.2022
 * 22:13
 * <p>
 * SPSM_Windows
 * at.serverbauer.oliver.spsm
 *
 * @author Serverbauer | GermanRPGBrothers.eu Inhaber
 */
public class SocketBackground extends Thread {

    public interface CLibrary extends Library {
        SocketBackground.CLibrary INSTANCE = (SocketBackground.CLibrary) Native.loadLibrary("kernel32", SocketBackground.CLibrary.class);

        // Diese Methode gibt die aktuelle GPU-Auslastung des Systems in Prozent zurück.
        int GetGPULoad();
    }

    public void run() {
        Properties config = new Properties();

        int portdefault = 5000;

        File configFile = new File("Config/config.properties");

        if (configFile.exists()) {
            try {
                config.load(new FileInputStream(configFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            configFile.getParentFile().mkdirs();
            try {
                configFile.createNewFile();
                config.setProperty("port", String.valueOf(portdefault));
                config.setProperty("ipAddress", "127.0.0.1");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        //Port aus der Config laden
        int port = Integer.parseInt(config.getProperty("port"));

        //IP aus der Config laden
        String ipAddress = config.getProperty("ipAddress");

        //Debug Nachricht
        System.out.println("Folgende Daten in der Config gefunden:");
        System.out.println("Port: " + port + " | IP: " + ipAddress);

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ipAddress));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Warten auf eine Verbindung vom Master
        Socket masterSocket = null;
        try {
            masterSocket = serverSocket.accept();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Bean für den Betriebssystemmanager abrufen
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Sobald eine Verbindung vom Master empfangen wird, wird die CPU-Auslastung ermittelt
        // und an den Master gesendet
        while (true) {
            // CPU-Auslastung ermitteln

            double cpuLoad = osBean.getSystemCpuLoad();

            // GPU-Auslastung ermitteln
            int gpuload = SocketBackground.CLibrary.INSTANCE.GetGPULoad();

            // RAM-Auslastung ermitteln
            long totalMemory = osBean.getTotalPhysicalMemorySize();

            // Disk-Auslastung ermitteln
            //long freeDiskSpace = osBean.getFreePhysicalMemorySize();
            long freeDiskSpace = osBean.getFreeMemorySize();

            // Netzwerk-Auslastung ermitteln
            long totalNetwork = osBean.getTotalSwapSpaceSize();

            // Senden der CPU-Auslastung an den Master
            DataOutputStream out = null;
            try {
                out = new DataOutputStream(masterSocket.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                out.writeDouble(cpuLoad + gpuload + totalMemory + freeDiskSpace + totalNetwork);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
