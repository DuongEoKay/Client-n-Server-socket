import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JFileChooser;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.event.*;

public class Client {
    private final static String Text_ip = "192.168.0.112"; // jbTypeIP.getText();
    private final static int port = 8080;

    private static class CloneSocket {
        private Socket socket = null;

        private void CloneSocket() {
        }

        private void connect(String id, int port) throws Exception {
            socket = new Socket(id, port);
        }

        private void disconnect() throws Exception {
            if (socket != null)
                socket.close();
        }

        private OutputStream getOutputStream() throws Exception {
            return socket.getOutputStream();
        }

        private InputStream getInputStream() throws Exception {
            return socket.getInputStream();
        }

        private void close() throws Exception {
            if (socket != null)
                socket.close();
        }

    }

    private static class Picture {
        private BufferedImage picture;

        private void Picture() {

        }

        private void setImage(BufferedImage x) {
            picture = x;
        }

        private BufferedImage getImage() {
            return picture;
        }
    }

    private static String getKeyPressed(ByteArrayOutputStream rawData) throws Exception {
        String data = new String(rawData.toByteArray());
        return data;
    }

    private static void KeyStokeControl(JButton jbKeystroke) throws Exception {
        final ByteArrayOutputStream rawData = new ByteArrayOutputStream();
        final String[] columnNames = { "Key Text", "Key Code" };
        CloneSocket keyStroke = new CloneSocket();
        jbKeystroke.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    JFrame frame = new JFrame("Keystroke");
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.setSize(600, 400);
                    frame.setLocationRelativeTo(null);

                    JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                    JButton Hook = new JButton("Hook");
                    JButton Unhook = new JButton("Unhook");
                    JButton Display = new JButton("Display");
                    JButton Delete = new JButton("Delete");

                    JTextArea textArea = new JTextArea();
                    textArea.setPreferredSize(new Dimension(550, 300));

                    JFrame ChildFrame = new JFrame("Key Listener");
                    ChildFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    ChildFrame.setSize(600, 200);
                    ChildFrame.setLocationRelativeTo(null);

                    Hook.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                keyStroke.connect(Text_ip, port);
                                DataOutputStream out = new DataOutputStream(keyStroke.getOutputStream());
                                out.write("HOOK ".getBytes());
                                out.flush();
                                out.close();
                                keyStroke.connect(Text_ip, port);
                                DataOutputStream outClone = new DataOutputStream(keyStroke.getOutputStream());
                                ChildFrame.setVisible(true);
                                ChildFrame.addKeyListener(new KeyListener() {
                                    @Override
                                    public void keyTyped(KeyEvent e1) {
                                    }

                                    @Override
                                    public void keyPressed(KeyEvent e1) {
                                        try {
                                            outClone.writeUTF(e1.getKeyText(e1.getKeyCode()) + " ");
                                        } catch (Exception ex1) {
                                        }

                                    }

                                    @Override
                                    public void keyReleased(KeyEvent e1) {
                                    }
                                });
                            } catch (Exception ex) {
                            }
                        }
                    });

                    Unhook.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                DataOutputStream outClone = new DataOutputStream(keyStroke.getOutputStream());
                                outClone.writeUTF(new String("UNHOOK"));
                                keyStroke.close();
                                keyStroke.connect(Text_ip, port);
                                DataInputStream inClone = new DataInputStream(keyStroke.getInputStream());
                                byte[] buffer = new byte[4096];
                                int total = inClone.available();
                                inClone.read(buffer, 0, total);
                                rawData.write(buffer, 0, total);
                                System.out.println(rawData.size());
                                ChildFrame.dispatchEvent(new WindowEvent(ChildFrame, WindowEvent.WINDOW_CLOSING));
                            } catch (Exception ex) {
                            }

                        }
                    });

                    Display.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                String data = getKeyPressed(rawData);
                                ////////////////////////////////////
                                System.out.println(data);
                                textArea.setText(data);
                                ///////////////////////////////////
                            } catch (Exception ex) {
                            }
                        }
                    });

                    Delete.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String ex = " ";
                            textArea.setText(ex);
                        }
                    });

                    panel.setLayout(new FlowLayout());

                    panel.add(Hook);
                    panel.add(Unhook);
                    panel.add(Display);
                    panel.add(Delete);
                    panel.add(textArea);
                    frame.add(panel);
                    frame.setVisible(true);

                } catch (Exception io) {
                }

            }
        });

        keyStroke.close();
    }

    private static String[][] getApprunning() throws Exception {
        byte[] buffer = new byte[4096];
        int total = -1;
        Socket socket = new Socket(Text_ip, port);
        OutputStream out = new BufferedOutputStream(socket.getOutputStream());
        out.write(new String("APPS ").getBytes());
        out.flush();
        InputStream in = socket.getInputStream();
        ByteArrayOutputStream a = new ByteArrayOutputStream();
        while ((total = in.read(buffer, 0, 4096)) > 0) {
            a.write(buffer, 0, total);
        }
        String message = new String(a.toByteArray());
        List<String> splitStrings = splitString(message);
        String[][] data = new String[splitStrings.size() / 2][2];
        for (int i = 0; i < splitStrings.size(); i++) {
            data[i / 2][i % 2] = splitStrings.get(i);
        }
        socket.close();
        return data;
    }

    private static String[][] getProcessRunning() throws Exception {
        byte[] buffer = new byte[4096];
        int total = -1;
        Socket socket = new Socket(Text_ip, port);
        OutputStream out = new BufferedOutputStream(socket.getOutputStream());
        out.write(new String("PROCESSES ").getBytes());
        out.flush();
        InputStream in = socket.getInputStream();
        ByteArrayOutputStream a = new ByteArrayOutputStream();
        while ((total = in.read(buffer, 0, 4096)) > 0) {
            a.write(buffer, 0, total);
        }
        String message = new String(a.toByteArray());
        List<String> splitStrings = splitString(message);
        String[][] data = new String[splitStrings.size() / 2][2];
        for (int i = 0; i < splitStrings.size(); i++) {
            data[i / 2][i % 2] = splitStrings.get(i);
        }
        in.close();
        socket.close();
        return data;

    }

    private static List<String> splitString(String input) {
        List<String> result = new ArrayList<>();
        // Split the input string by endl
        String[] splitByEndl = input.split("\n");
        for (String s : splitByEndl) {
            // Split each string by '/'
            String[] splitBySlash = s.split("/");
            result.add(splitBySlash[0]);
            result.add(splitBySlash[1]);
        }
        return result;
    }

    private static BufferedImage Capture() throws Exception {
        byte[] buffer = new byte[4096];
        int total = -1;
        Socket socket = new Socket(Text_ip, port);
        OutputStream out = new BufferedOutputStream(socket.getOutputStream());
        out.write(new String("CAPTURE ").getBytes());
        out.flush();
        InputStream in = socket.getInputStream();
        ByteArrayOutputStream a = new ByteArrayOutputStream();
        while ((total = in.read(buffer, 0, 4096)) > 0) {
            a.write(buffer, 0, total);
        }

        socket.close();
        BufferedImage Image = ImageIO.read(new ByteArrayInputStream(a.toByteArray()));
        return Image;
    }

    public static void main(String[] args) throws Exception {
        JFrame jFrame = new JFrame("Client");
        jFrame.setSize(450, 750);
        jFrame.setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel jlTitle = new JLabel("CLIENT");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jlTitle.setBorder(new EmptyBorder(10, 0, 10, 0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel jlFileName = new JLabel("Select ur option: ");
        jlFileName.setFont(new Font("Arial", Font.BOLD, 20));
        jlFileName.setBorder(new EmptyBorder(20, 0, 0, 0));
        jlFileName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpButton = new JPanel();
        jpButton.setBorder(new EmptyBorder(30, 0, 10, 0));

        JTextField jbTypeIP = new JTextField();
        jbTypeIP.setPreferredSize(new Dimension(250, 75));
        jbTypeIP.setFont(new Font("Arial", Font.BOLD, 25));

        JButton jbConnect = new JButton("Connect");
        jbConnect.setPreferredSize(new Dimension(150, 75));
        jbConnect.setFont(new Font("Arial", Font.BOLD, 20));

        JButton jbKeystroke = new JButton("Keystroke");
        jbKeystroke.setPreferredSize(new Dimension(400, 75));
        jbKeystroke.setFont(new Font("Arial", Font.BOLD, 20));

        JButton jbPrintScreen = new JButton("Print Screen");
        jbPrintScreen.setPreferredSize(new Dimension(400, 75));
        jbPrintScreen.setFont(new Font("Arial", Font.BOLD, 20));

        JButton jbProcessRunning = new JButton("Process Running");
        jbProcessRunning.setPreferredSize(new Dimension(400, 75));
        jbProcessRunning.setFont(new Font("Arial", Font.BOLD, 20));

        JButton jbAppRunning = new JButton("App Running");
        jbAppRunning.setPreferredSize(new Dimension(400, 75));
        jbAppRunning.setFont(new Font("Arial", Font.BOLD, 20));

        JButton jbShutdown = new JButton("Shutdown");
        jbShutdown.setPreferredSize(new Dimension(400, 75));
        jbShutdown.setFont(new Font("Arial", Font.BOLD, 20));

        JButton jbQuit = new JButton("Quit");
        jbQuit.setPreferredSize(new Dimension(400, 75));
        jbQuit.setFont(new Font("Arial", Font.BOLD, 20));

        jpButton.add(jbTypeIP);
        jpButton.add(jbConnect);
        jpButton.add(jbKeystroke);
        jpButton.add(jbPrintScreen);
        jpButton.add(jbProcessRunning);
        jpButton.add(jbAppRunning);
        jpButton.add(jbShutdown);
        jpButton.add(jbQuit);

        jbConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Socket socket = new Socket(Text_ip, 8080);
                    if (socket.isConnected() == true) {
                        OutputStream out = socket.getOutputStream();
                        out.write(new String("CONNECT ").getBytes());
                        JOptionPane.showMessageDialog(null, "Connect Sucessfully", "Announcement",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    socket.close();
                } catch (Exception a) {

                    JOptionPane.showMessageDialog(null, "Can't connect, try another IP !!!", "Announcement",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        KeyStokeControl(jbKeystroke);

        jbPrintScreen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Picture picture = new Picture();
                    picture.setImage(Capture());
                    BufferedImage Image = picture.getImage();
                    ImageIcon Icon = new ImageIcon(Image);
                    JFrame frame = new JFrame();
                    frame.setLayout(new FlowLayout());
                    frame.setSize((int) Image.getWidth(), (int) Image.getHeight());
                    JLabel lbl = new JLabel();
                    JPanel panel = new JPanel();
                    JButton saveImage = new JButton("Save");
                    saveImage.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            BufferedImage Image = picture.getImage();
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setDialogTitle("Save Image");
                            fileChooser.setSelectedFile(new File("screenshot.jpg"));
                            int userSelection = fileChooser.showSaveDialog(null);
                            if (userSelection == JFileChooser.APPROVE_OPTION) {
                                try {
                                    File fileToSave = fileChooser.getSelectedFile();
                                    ImageIO.write(Image, "jpg", fileToSave);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });
                    panel.add(saveImage);
                    JButton Again = new JButton("Again");
                    Again.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            try {
                                picture.setImage(Capture());
                                BufferedImage Image = picture.getImage();
                                ImageIcon Icon = new ImageIcon(Image);
                                lbl.setIcon(Icon);
                                lbl.setPreferredSize(new Dimension((int) Image.getWidth(), (int) Image.getHeight()));
                                frame.remove(lbl);
                                frame.add(lbl);
                                frame.setVisible(true);
                                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            } catch (Exception ie) {
                                System.out.println(ie.getMessage());
                            }

                        }
                    });
                    panel.add(Again);
                    frame.add(panel);
                    lbl.setIcon(Icon);
                    lbl.setPreferredSize(new Dimension((int) Image.getWidth(), (int) Image.getHeight()));
                    frame.add(lbl);
                    frame.setVisible(true);
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                } catch (Exception io) {
                    System.out.println(io.getMessage());
                }
            }
        });

        jbAppRunning.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String[] columnNames = { "Name Application", "ID Application" };
                    String[][] data = getApprunning();
                    JPanel panel = new JPanel();
                    JTable table = new JTable(data, columnNames);
                    JScrollPane scrollPane = new JScrollPane(table);
                    JFrame frame = new JFrame("App Running");
                    JButton Kill = new JButton("Kill");

                    Kill.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            String input = JOptionPane.showInputDialog(null, "Enter ID application:");
                            try {
                                Socket kill = new Socket(Text_ip, port);
                                OutputStream out = new BufferedOutputStream(kill.getOutputStream());

                                out.write(new String("KILL " + input + " ").getBytes());
                                out.flush();
                            } catch (Exception hihi) {
                                System.out.println(hihi.getMessage());
                            }
                        }
                    });
                    JButton Display = new JButton("Display");
                    Display.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            try {
                                String[][] Re_data = null;
                                Re_data = getApprunning();
                                JTable Newtable = new JTable(Re_data, columnNames);
                                JScrollPane NewscrollPane = new JScrollPane(Newtable);
                                Component[] map = frame.getContentPane().getComponents();
                                frame.remove(map[1]);
                                frame.add(NewscrollPane, BorderLayout.CENTER);
                                frame.pack();
                            } catch (Exception io) {

                            }

                        }
                    });
                    JButton Delete = new JButton("Delete");
                    Delete.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            String[][] empty = { { null, null } };
                            JTable Newtable = new JTable(empty, columnNames);
                            JScrollPane NewscrollPane = new JScrollPane(Newtable);
                            Component[] map = frame.getContentPane().getComponents();
                            frame.remove(map[1]);
                            frame.add(NewscrollPane, BorderLayout.CENTER);
                            frame.pack();
                        }
                    });
                    JButton Start = new JButton("Start");
                    Start.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            // start
                            System.out.println("Start");
                            String input = JOptionPane.showInputDialog(null, "Enter ID application:");
                            try {
                                Socket start = new Socket(Text_ip, port);
                                OutputStream out = new BufferedOutputStream(start.getOutputStream());
                                out.write(new String("START " + input + " ").getBytes());
                                out.flush();
                            } catch (Exception hihi) {
                                System.out.println(hihi.getMessage());
                            }
                        }
                    });

                    panel.add(Kill);
                    panel.add(Display);
                    panel.add(Delete);
                    panel.add(Start);

                    frame.add(panel, BorderLayout.NORTH);
                    frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
                    frame.pack();
                    frame.setVisible(true);
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                } catch (Exception io) {
                    System.out.println(io.getMessage());
                }
            }
        });

        jbProcessRunning.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String[] columnNames = { "Name Process", "ID Process" };
                    String[][] data = getProcessRunning();
                    JPanel panel = new JPanel();
                    JTable table = new JTable(data, columnNames);
                    JScrollPane scrollPane = new JScrollPane(table);
                    JFrame frame = new JFrame("Process Running");
                    JButton Kill = new JButton("Kill");

                    Kill.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            String input = JOptionPane.showInputDialog(null, "Enter ID application:");
                            try {
                                Socket kill = new Socket(Text_ip, port);
                                OutputStream out = new BufferedOutputStream(kill.getOutputStream());
                                out.write(new String("KILL " + input + " ").getBytes());
                                out.flush();
                                out.close();
                                kill.close();
                            } catch (Exception hihi) {
                                System.out.println(hihi.getMessage());
                            }
                        }
                    });
                    JButton Display = new JButton("Display");
                    Display.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            try {
                                String[][] Re_data = null;
                                Re_data = getProcessRunning();
                                JTable Newtable = new JTable(Re_data, columnNames);
                                JScrollPane NewscrollPane = new JScrollPane(Newtable);
                                Component[] map = frame.getContentPane().getComponents();
                                frame.remove(map[1]);
                                frame.add(NewscrollPane, BorderLayout.CENTER);
                                frame.pack();
                            } catch (Exception io) {

                            }

                        }
                    });
                    JButton Delete = new JButton("Delete");
                    Delete.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            String[][] empty = { { " ", " " } };

                            JTable Newtable = new JTable(empty, columnNames);
                            JScrollPane NewscrollPane = new JScrollPane(Newtable);
                            Component[] map = frame.getContentPane().getComponents();

                            frame.remove(map[1]);
                            // frame.add(panel);
                            frame.add(NewscrollPane, BorderLayout.CENTER);
                            frame.pack();
                        }
                    });
                    JButton Start = new JButton("Start");
                    Start.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            // start
                            System.out.println("Start");
                            String input = JOptionPane.showInputDialog(null, "Enter ID application:");
                            try {
                                Socket start = new Socket(Text_ip, port);
                                OutputStream out = new BufferedOutputStream(start.getOutputStream());
                                out.write(new String("START " + input + " ").getBytes());
                                out.flush();
                                start.close();
                            } catch (Exception hihi) {
                                System.out.println(hihi.getMessage());
                            }
                        }
                    });
                    panel.add(Kill);
                    panel.add(Display);
                    panel.add(Delete);
                    panel.add(Start);

                    frame.add(panel, BorderLayout.NORTH, 0);
                    frame.add(scrollPane, BorderLayout.CENTER, 1);
                    frame.pack();
                    frame.setVisible(true);
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                } catch (Exception io) {
                    System.out.println(io.getMessage());
                }
            }
        });

        jbShutdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Socket kill = new Socket(Text_ip, port);
                    OutputStream out = new BufferedOutputStream(kill.getOutputStream());
                    out.write(new String("SHUTDOWN ").getBytes());
                    out.flush();
                    out.close();
                    kill.close();
                } catch (Exception io) {

                }

            }
        });

        jbQuit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.dispose();
            }
        });

        jFrame.add(jlTitle);
        jFrame.add(jlFileName);
        jFrame.add(jpButton);
        jFrame.setVisible(true);
    }
}
