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
 * Created: 12.12.2022
 * 21:00
 * <p>
 * SPSM_Windows
 * at.serverbauer.oliver.spsm
 *
 * @author Serverbauer | GermanRPGBrothers.eu Inhaber
 */
public class Main {

    public static void main(String[] args) {
        SocketBackground task = new SocketBackground();
        task.start();
    }
}