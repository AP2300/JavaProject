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

    public void FileWatcher() throws Exception{
        WatchService watcher = FileSystems.getDefault().newWatchService();
        Path dir = Paths.get("JavaProject");
        dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        System.out.println("observando"+ dir.getFileName());

        while(true){
            WatchKey key;

            key=watcher.take();

            List<WatchEvent<?>> ListofEvents = key.pollEvents();

            for(WatchEvent<?> event : ListofEvents){
                Kind<?> eventType = event.kind();
                Path fileName = (Path)event.context();

                System.out.println(eventType.name()+" evento para " + fileName.toString());

                if(eventType == OVERFLOW){
                    continue;
                }else if(eventType == ENTRY_CREATE){

                }else if(eventType == ENTRY_DELETE){

                }else if(eventType == ENTRY_MODIFY){

                }
            }
            boolean valid = key.reset();
            if(!valid) break;
        }
    }
    public static void main(String[] args) throws Exception {
        App app = new App();
        app.FileWatcher();
    }
}
