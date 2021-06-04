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

import jdk.internal.net.http.common.Log;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.util.*;

public class App {

    public static void main(String[] args) throws Exception {
        App app = new App();
        app.FileWatcher();
    }

    File Log = new File(System.getProperty("user.home") + "/desktop/Logs.txt");
    File FinalFile = new File(System.getProperty("user.home") + "/desktop/Factura.txt");

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

}
