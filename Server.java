import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.awt.Dimension;
import java.awt.Font;
import java.net.*;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Server {
    private static final int port = 8080;
    private static ServerSocket serverSocket = null;
    private static Boolean MaintainSocket = true;

    private static void CloneServer() throws Exception {
        String temp = "";
        String data = "";
        try {
            Socket socket = serverSocket.accept();
            socket.setKeepAlive(true);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            while (!temp.equals("UNHOOK")) {
                temp = in.readUTF();
                if (temp != null)
                    data += temp;
                System.out.print(temp);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        Socket socket = serverSocket.accept();
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF(data);

        System.out.println(data.length());
        out.flush();
        socket.close();
    }

    private static Boolean Process_Killer(String ID) throws IOException, Exception {
        Boolean sucessBoolean = false;
        Process p = null;
        try {
            String[] Command_Full = new String[] { "cmd", "/c", "start taskkill.exe /f /pid " + ID };
            p = Runtime.getRuntime().exec(Command_Full);
        } catch (IllegalThreadStateException Il) {
        }
        return sucessBoolean;
    }

    private static Boolean Start_App(String name) throws IOException, Exception {
        Boolean sucessBoolean = false;
        Process p = null;
        try {
            String[] Command_Full = new String[] { "cmd", "/c", "start", name + ".exe" };
            p = Runtime.getRuntime().exec(Command_Full);
            // p = Runtime.getRuntime().exec(name);
        } catch (IllegalThreadStateException Il) {
        }
        return sucessBoolean;

    }

    private static void ShutDown() throws IOException, Exception {
        Process p = null;
        String[] Command_Full = new String[] { "cmd", "/c", "start Shutdown -s -t 0" };
        try {
            p = Runtime.getRuntime().exec(Command_Full);
        } catch (IllegalThreadStateException Il) {
        } finally {
            if (p != null)
                p.destroy();
        }
    }

    private static Vector<String> getAppRunning() {
        Vector<String> processes = new Vector<String>();
        try {
            String line;
            Process p = Runtime.getRuntime()
                    .exec(new String("tasklist.exe /APPS /FI \"STATUS eq RUNNING\" /fo csv /nh").split(" "));
            if (!p.isAlive()) {
                // System.out.println("Cannot open tasklist.exe");
                processes.add("Unable to get list. Try again later!");
                return processes;
            }
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                line = line.replaceAll("\"", "");
                String[] temp = line.split(",");
                temp[0] = temp[0].split(" ")[0];
                line = temp[0] + "/" + temp[1];
                processes.add(line);
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        return processes;
    }

    private static Vector<String> getTaskList() {
        Vector<String> processes = new Vector<String>();
        try {
            String line;
            Process p = Runtime.getRuntime().exec(new String("tasklist.exe /fo csv /nh").split(" "));
            if (!p.isAlive()) {
                System.out.println("Cannot open tasklist.exe");
                return processes;
            }
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {

                if (!line.trim().equals("")) {
                    line = line.replaceAll("\"", "");
                    String[] temp = line.split(",");
                    processes.add(temp[0] + "/" + temp[1]);
                }
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        return processes;
    }

    private static ByteArrayOutputStream Capture() throws Exception {
        Robot robot = new Robot();
        Rectangle area = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage Image = robot.createScreenCapture(area);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(Image, "jpg", byteArrayOutputStream);
        return byteArrayOutputStream;
    }

    public static byte[] getCommand(String command, Socket socket) throws Exception {
        String IsGot = "Wrong Command!";
        byte[] buffer = IsGot.getBytes();
        String[] Split = command.split(" ");
        switch (Split[0]) {
            case "CONNECT":
                System.out.println("Connected!");
                break;
            case "CAPTURE":
                buffer = Capture().toByteArray();
                break;
            case "PROCESSES":
                System.out.println("Command got: PROCESSES");
                Vector<String> processes = getTaskList();
                String Process_toString = null;
                for (String x : processes)
                    Process_toString += x + "\n";
                buffer = Process_toString.getBytes();
                break;
            case "APPS":
                System.out.println("Command got: APPS");
                Vector<String> apps = getAppRunning();
                String buffered = null;
                for (String x : apps)
                    buffered += x + "\n";
                buffer = buffered.getBytes();
                break;
            case "KILL":
                System.out.println("Command got: KILL");
                Process_Killer(Split[1]);
                break;
            case "START":
                if (Start_App(Split[1])) {
                    buffer = "Successfully!".getBytes();
                } else
                    buffer = "Failed!".getBytes();
                break;
            case "SHUTDOWN":
                System.out.println(command);
                // serverSocket.close();
                // ShutDown();
                break;
            case "HOOK":
                System.out.println("Command got: HOOK");
                socket.getOutputStream().write(new String("OK").getBytes());
                socket.getOutputStream().flush();
                socket.getInputStream().close();
                socket.close();
                CloneServer();
                break;
            case "QUIT":
                MaintainSocket = false;
                serverSocket.close();
                break;
            default:
                System.out.println(command);
                break;
        }
        return buffer;
    }

    private static final void SocketReception(JFrame jFrame) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port " + port);
            while (MaintainSocket) {
                Socket socket = serverSocket.accept();
                InputStream input = socket.getInputStream();
                DataInputStream reader = new DataInputStream(input);
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int total = reader.available();
                reader.read(buffer, 0, total);
                byteArr.write(buffer, 0, total);
                try {
                    byte[] result = getCommand(new String(buffer), socket);
                    output.write(result, 0, result.length);
                    output.close();
                } catch (IOException io) {
                    System.out.println(io.getMessage());
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                socket.close();
            }

        } catch (

        IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.getStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, Exception {
        // Set the frame to house everything.
        JFrame jFrame = new JFrame();
        // Set the size of the frame.
        jFrame.setSize(300, 200);
        jFrame.setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS));
        // Make it so when the frame is closed the program exits successfully.
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Title above panel.
        JLabel jlTitle = new JLabel("SERVER");
        // Change the font family, size, and style.
        jlTitle.setFont(new Font("Arial", Font.BOLD, 15));
        // Add a border around the label for spacing.
        jlTitle.setBorder(new EmptyBorder(15, 0, 0, 0));
        // Make it so the title is centered horizontally.
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Panel that contains the buttons.
        JPanel jpButton = new JPanel();

        // Border for panel that houses buttons.
        jpButton.setBorder(new EmptyBorder(5, 0, 10, 0));
        // Create send file button.
        JButton jbOpen = new JButton("OPEN");
        // Set preferred size works for layout containers.
        jbOpen.setPreferredSize(new Dimension(250, 100));
        // Change the font style, type, and size for the button.
        jbOpen.setFont(new Font("Arial", Font.BOLD, 60));
        jpButton.add(jbOpen);

        jbOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread r = new Thread(new Runnable() {
                    public void run() {
                        SocketReception(jFrame);
                    }
                });
                r.start();
            }
        });

        // Add everything to the frame and make it visible.
        jFrame.add(jlTitle);
        jFrame.add(jpButton);
        jFrame.setVisible(true);
    }

}