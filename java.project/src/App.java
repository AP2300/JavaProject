import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.NoSuchFileException;
import java.util.*;

public class App {

    public void FileWatcher() throws Exception {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        Path dir = Paths.get("JavaProject");
        dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        System.out.println("observando" + dir.getFileName());

        while (true) {
            WatchKey key;

            key = watcher.take();

            List<WatchEvent<?>> ListofEvents = key.pollEvents();

            for (WatchEvent<?> event : ListofEvents) {
                Kind<?> eventType = event.kind();
                Path fileName = (Path) event.context();

                if (eventType == OVERFLOW) {
                    continue;
                } else if (eventType == ENTRY_CREATE) {
                    String fe = "";
                    int i = fileName.toString().lastIndexOf('.');
                    if (i > 0) {
                        fe = fileName.toString().substring(i+1);
                    }
                    System.out.println(fe);
                    if(fe == "xml"){
                        System.out.println("archivo xml añadido");
                    }
                } else if (eventType == ENTRY_DELETE) {

                } else if (eventType == ENTRY_MODIFY) {

                }
                
                boolean valid = key.reset();
                if (!valid)
                    break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        App app = new App();
        app.FileWatcher();
    }
}
