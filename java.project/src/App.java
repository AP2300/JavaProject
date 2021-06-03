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
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.xml.parsers.DocumentBuilder;

// import org.graalvm.compiler.code.DataSection;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.util.*;

public class App {

    public static void main(String[] args) throws Exception {
        App app = new App();
        app.FileWatcher();
    }

    File Log = new File(System.getProperty("user.home") + "/desktop/Logs.txt");

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
                        Logger("Archivo " + fileName.toString() + " Creado");
                        TranslateXML(fileName.toString());
                    } else if (eventType == ENTRY_DELETE) {
                        Logger("Archivo " + fileName.toString() + " Eliminado");
                    } else if (eventType == ENTRY_MODIFY) {
                        // Logger("Archivo " + fileName.toString() + " Modificado");
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

    // public void TranslateXML(String name) throws Exception {
    //     File XML = new File(System.getProperty("user.home") + "/downloads/" + name);
    //     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    //     DocumentBuilder db = dbf.newDocumentBuilder();
    //     Document doc = db.parse(XML);
    //     doc.getDocumentElement().normalize();
    //     NodeList headerList = doc.getElementsByTagName("header");

    //     for (int itr = 0; itr < headerList.getLength(); itr++) {
    //         System.out.println("Nro de Iteracion: "+itr);
    //         Node node = headerList.item(itr);
    //         System.out.println("\nNode Name: " + node.getNodeName());
    //         if (node.getNodeType() == Node.ELEMENT_NODE) {
    //             Element eElement = (Element) node;
    //             System.out.println("Razon Social: " + eElement.getElementsByTagName("razonSocial").item(0).getTextContent());
    //             System.out.println("Rif: " + eElement.getElementsByTagName("rifEmpresa").item(0).getTextContent());
    //             System.out.println("Direccion: " + eElement.getElementsByTagName("direccionEmpresa").item(0).getTextContent());
    //             System.out.println("Nro Control: " + eElement.getElementsByTagName("nroControl").item(0).getTextContent());
    //             System.out.println("Cliente: " + eElement.getElementsByTagName("cliente").item(0).getTextContent());
    //             System.out.println("Cedula: " + eElement.getElementsByTagName("cedula").item(0).getTextContent());
    //             System.out.println("Fecha: " + eElement.getElementsByTagName("fecha").item(0).getTextContent());
    //         }
    //     }

    //     NodeList detailsList = doc.getElementsByTagName("producto");
    //     System.out.println(detailsList.getLength());

    //     for (int itr = 0; itr < detailsList.getLength(); itr++) {
    //         System.out.println("Nro de Iteracion: "+itr);
    //         Node node = detailsList.item(itr);
    //         System.out.println("\nNode Name: " + node.getNodeName());
    //         if (node.getNodeType() == Node.ELEMENT_NODE) {
    //             Element eElement = (Element) node;
    //             System.out.println("Codigo: " + eElement.getElementsByTagName("codigo").item(0).getTextContent());
    //             System.out.println("Descripcion: " + eElement.getElementsByTagName("descripcion").item(0).getTextContent());
    //             System.out.println("Precio Unitario: " + eElement.getElementsByTagName("precioUnitario").item(0).getTextContent());
    //             System.out.println("Cantidad: " + eElement.getElementsByTagName("cantidad").item(0).getTextContent());
    //             System.out.println("total: " + eElement.getElementsByTagName("total").item(0).getTextContent());
    //         }
    //     }
    // }

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

    public void TranslateXML(String name) throws Exception {
        File XML = new File(System.getProperty("user.home") + "/downloads/" + name);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(XML);
        doc.getDocumentElement().normalize();
        NodeList HeaderList = doc.getElementsByTagName("header");
        NodeList ProductsList = doc.getElementsByTagName("producto");

            Node Headernode = HeaderList.item(0);
            if (Headernode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) Headernode;
                NodeList ChildElement = eElement.getChildNodes();

                for (int j = 0; j < ChildElement.getLength(); j++) {
                    Node Cnode = ChildElement.item(j);
                    
                    if(Cnode.getNodeType() == Node.ELEMENT_NODE){
                        System.out.println( Cnode.getNodeName() + " " + Cnode.getTextContent());
                    }
                }
            }

        for (int i = 0; i < ProductsList.getLength(); i++) {
            Node Productonode = ProductsList.item(i);

            if (Productonode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) Productonode;
                NodeList ChildElement = eElement.getChildNodes();

                for (int j = 0; j < ChildElement.getLength(); j++) {
                    Node Cnode = ChildElement.item(j);
                    
                    if(Cnode.getNodeType() == Node.ELEMENT_NODE){
                        System.out.println( Cnode.getNodeName() + " " + Cnode.getTextContent());
                    }
                }
            }
        }
    }
}
