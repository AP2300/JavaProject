import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.TrayIcon.MessageType;
import javax.swing.*;

public class App extends JFrame implements ActionListener{
    File Log = new File(System.getProperty("user.home") + "/desktop/Logs.txt");
    File FinalFile = new File(System.getProperty("user.home") + "/desktop/Factura.txt");
    static JFrame f = new JFrame();
    private PopupMenu popup = new PopupMenu();
    private final Image image = new ImageIcon(getClass().getResource("up.ico")).getImage();
    private final TrayIcon trayIcon = new TrayIcon(image, "App_Name esperando archivos", popup);
    private Timer timer;

    final JMenuBar ToolBar;
    final JMenu Menu1;
    final JMenu Menu2;
    final JMenuItem mi1, mi2, mi3;

    public App(JFrame f) {
        App.f = f;

        ToolBar=new JMenuBar();
        f.add(ToolBar);
        ToolBar.setBounds(0,0,400,20);
        Menu1=new JMenu("Archivo");
        ToolBar.add(Menu1);
        Menu2=new JMenu("Configuracion");
        ToolBar.add(Menu2);
        mi1=new JMenuItem("Ver descargas");
        mi1.addActionListener(this);
        Menu1.add(mi1);
        mi2=new JMenuItem("Ver reportes");
        mi2.addActionListener(this);
        Menu1.add(mi2);
        mi3=new JMenuItem("Salir");
        mi3.addActionListener(this);
        Menu1.add(mi3); 

        JLabel ActiveText;
        ActiveText = new JLabel("Esperando archivos...");
        ActiveText.setBounds(40, 30, 300, 100);
        ActiveText.setFont(ActiveText.getFont().deriveFont(28.0f));
        f.add(ActiveText);

        if (SystemTray.isSupported()) {
            SystemTray systemtray = SystemTray.getSystemTray();
            trayIcon.setImageAutoSize(true);

            MouseListener mouseListener = new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent evt) {
                    // Si se presiona con el boton izquierdo en el icono
                    // y la aplicacion esta minimizada se muestra una frase
                    if (evt.getButton() == MouseEvent.BUTTON1 && f.getExtendedState() == JFrame.ICONIFIED) {
                        if (evt.getClickCount() % 2 == 0 && !evt.isConsumed()) {
                            evt.consume();
                            if (f.getExtendedState() == JFrame.ICONIFIED) {
                                f.setVisible(true);
                                f.setExtendedState(JFrame.NORMAL);
                                f.repaint();
                                if (timer != null)
                                    timer.cancel();
                            }
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent evt) {
                    /* nada x aqui circulen... */}

                @Override
                public void mouseExited(MouseEvent evt) {
                    /* nada x aqui circulen... */}

                @Override
                public void mousePressed(MouseEvent evt) {
                    /* nada x aqui circulen... */}

                @Override
                public void mouseReleased(MouseEvent evt) {
                    /* nada x aqui circulen... */}
            };

            ActionListener exitListener = (ActionEvent e) -> {
                System.exit(0);
            };

            ActionListener restoreListener = (ActionEvent e) -> {
                // si esta minimizado restaura JFrame
                if (f.getExtendedState() == JFrame.ICONIFIED) {
                    f.setVisible(true);
                    f.setExtendedState(JFrame.NORMAL);
                    f.repaint();
                    if (timer != null)
                        timer.cancel();
                }
            };

            MenuItem restoreAppItem = new MenuItem("Restaurar");
            restoreAppItem.addActionListener(restoreListener);
            popup.add(restoreAppItem);

            MenuItem exitAppItem = new MenuItem("Salir");
            exitAppItem.addActionListener(exitListener);
            popup.add(exitAppItem);

            trayIcon.addMouseListener(mouseListener);

            // Añade el TrayIcon al SystemTray
            try {
                systemtray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("Error:" + e.getMessage());
            }
        } else {
            System.err.println("Error: SystemTray no es soportado");
            return;
        }

        // Cuando se minimiza JFrame, se oculta para que no aparesca en la barra de
        // tareas
        f.addWindowListener(new WindowAdapter() {
            public void windowIconified(WindowEvent e) {
                f.setVisible(false);// Se oculta JFrame
                MessageTray("Seguira funcionando en segundo plano", MessageType.INFO);
            }


        });

    }

    public static void main(String[] args) throws Exception {
        App app = new App(f);
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                f.setExtendedState(JFrame.ICONIFIED);
            }
        });

        f.setSize(400, 200);// 400 width and 500 height
        f.setResizable(false);
        f.setLocationRelativeTo(null);
        f.setLayout(null);// using no layout managers
        f.setVisible(true);// making the frame visible
        app.FileWatcher();
    }

    public void MessageTray(String text, MessageType type) {
        trayIcon.displayMessage("App_Name", text, type);
    }

    public void FileWatcher() throws Exception {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        Path dir = Paths.get(System.getProperty("user.home") + "/downloads");
        dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        System.out.println("observando " + dir.getFileName());

        while (true) {
            WatchKey key;

            key = watcher.take();

            List<WatchEvent<?>> ListofEvents = key.pollEvents();

            for (WatchEvent<?> event : ListofEvents) {
                Kind<?> eventType = event.kind();
                Path fileName = (Path) event.context();

                if (CheckXML(fileName)) {
                    if (eventType == OVERFLOW) {
                        continue;
                    } else if (eventType == ENTRY_CREATE) {
                        // System.out.println(fileName.getFileName());
                        Logger("Archivo " + fileName.toString() + " Creado");
                        Await(1);
                    } else if (eventType == ENTRY_DELETE) {
                        Logger("Archivo " + fileName.toString() + " Eliminado");
                    } else if (eventType == ENTRY_MODIFY) {
                        Logger("Archivo " + fileName.toString() + " Creado");
                        TranslateXML(fileName.toString());
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    watcher.close();
                    break;
                }
            }
            key = null;
            ListofEvents = null;
            System.gc();
        }
    }

    private void Await(int segundos) {
        try {
            Thread.sleep(segundos * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public Boolean CheckXML(Path filename) {
        String fe = "";
        int i = filename.toString().lastIndexOf('.');
        if (i > 0) {
            fe = filename.toString().substring(i + 1);
        }
        Boolean result = fe.equals("xml");
        return result;
    }

    public void Logger(String str) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(Log, true));
        Date FH = new Date();
        DateFormat fFH = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        String LOGS = fFH.format(FH) + "  " + str;
        bw.write(LOGS);
        bw.newLine();
        bw.write("--------------------------------------------------------------------------------------");
        bw.newLine();
        bw.close();
    }

    public String Spacing(HashMap<String, String[]> products, String comparator) {
        String spc = "";

        String ref = products.get("producto1")[3] + " x  " + products.get("producto1")[1];
        for (String[] i : products.values()) {
            if (ref.length() < (i[3] + " x  " + i[1]).length()) {
                ref = i[3] + " x  " + i[1];
            }
        }

        int itr = comparator.equals("") ? 19 : (ref.length() - comparator.length()) + 4;

        for (int i = 0; i < itr; i++) {
            spc += " ";
        }

        return spc;
    }

    public void toText(HashMap<String, String> data, HashMap<String, String[]> products, HashMap<String, String> total)
            throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(FinalFile, true));
        bw.write(Spacing(products, "") + data.get("razonSocial"));
        bw.newLine();
        bw.write(Spacing(products, "") + "     " + data.get("direccionEmpresa"));
        bw.newLine();
        bw.write(Spacing(products, "") + "  J-" + data.get("rifEmpresa"));
        bw.newLine();
        bw.newLine();
        bw.write("Nro de control: \t\t\t      " + data.get("nroControl"));
        bw.newLine();
        bw.write("Cliente: \t\t\t\t      " + data.get("cliente"));
        bw.newLine();
        bw.write("C.I: \t\t\t\t\t      " + data.get("cedula"));
        bw.newLine();
        bw.write("Fecha: \t\t\t\t\t      " + data.get("fecha"));
        bw.newLine();
        bw.write("-----------------------------------------------------------");
        bw.newLine();
        for (int i = 0; i < products.size(); i++) {

            bw.write(products.get("producto" + i)[3] + " x  " + products.get("producto" + i)[1]
                    + Spacing(products, products.get("producto" + i)[3] + " x  " + products.get("producto" + i)[1])
                    + products.get("producto" + i)[4]);
            bw.newLine();
        }
        bw.write("------------------------------------------------------------");
        bw.newLine();
        bw.write("Sub-Total:\t\t\t\t      " + total.get("subtotal"));
        bw.newLine();
        bw.write("IVA (16%):\t\t\t\t      " + total.get("iva"));
        bw.newLine();
        bw.write("Total:\t\t\t\t\t      " + total.get("total"));
        bw.newLine();
        bw.close();
    }

    public void TranslateXML(String name) throws Exception {
        File XML = new File(System.getProperty("user.home") + "/downloads/" + name);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(XML);

        doc.getDocumentElement().normalize();
        NodeList HeaderList = doc.getElementsByTagName("header");
        NodeList ProductsList = doc.getElementsByTagName("producto");
        NodeList TotalList = doc.getElementsByTagName("totales");

        HashMap<String, String> TicketData = new HashMap<String, String>();
        HashMap<String, String[]> TicketProducts = new HashMap<String, String[]>();
        HashMap<String, String> TicketTotal = new HashMap<String, String>();

        int pos = 0;

        Node Headernode = HeaderList.item(0);
        if (Headernode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) Headernode;
            NodeList ChildElement = eElement.getChildNodes();

            for (int j = 0; j < ChildElement.getLength(); j++) {
                Node Cnode = ChildElement.item(j);

                if (Cnode.getNodeType() == Node.ELEMENT_NODE) {
                    TicketData.put(Cnode.getNodeName(), Cnode.getTextContent());
                }
            }
        }

        for (int i = 0; i < ProductsList.getLength(); i++) {
            Node Productonode = ProductsList.item(i);
            String[] temp = new String[5];

            if (Productonode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) Productonode;
                NodeList ChildElement = eElement.getChildNodes();

                for (int j = 0; j < ChildElement.getLength(); j++) {
                    Node Cnode = ChildElement.item(j);

                    if (Cnode.getNodeType() == Node.ELEMENT_NODE && pos < 5) {
                        temp[pos] = Cnode.getTextContent();
                        pos++;
                    }
                }
                TicketProducts.put("producto" + i, temp);
            }
            temp = null;
            pos = 0;
        }

        Node Totalnode = TotalList.item(0);
        if (Totalnode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) Totalnode;
            NodeList ChildElement = eElement.getChildNodes();

            for (int j = 0; j < ChildElement.getLength(); j++) {
                Node Cnode = ChildElement.item(j);

                if (Cnode.getNodeType() == Node.ELEMENT_NODE) {
                    TicketTotal.put(Cnode.getNodeName(), Cnode.getTextContent());
                }
            }
        }

        toText(TicketData, TicketProducts, TicketTotal);
        TicketData = null;
        TicketProducts = null;
        TicketTotal = null;
        dbf = null;
        db = null;
        doc = null;
        HeaderList = null;
        ProductsList = null;
        XML = null;
        System.gc();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==mi1) {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + System.getProperty("user.home") + "/downloads");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        if (e.getSource()==mi2) {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + System.getProperty("user.home") + "/desktop");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            
        }
        if (e.getSource()==mi3) {
            System.exit(0);
        }  
        
    }

}
