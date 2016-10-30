package org.podpage.hap.accessory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class FileListener implements Runnable {

    private WatchService watcher;

    public FileListener(String path) {
        try {
            watcher = FileSystems.getDefault().newWatchService();
            Path dir = Paths.get(path);
            dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    @Override
    public void run() {
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return;
            }

            boolean showsyncanimation = false;

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                File file = ev.context().toFile();

                // System.out.println(file.getName());
                // if (!file.getName().contains("syncing")) {
                // if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                // create(file);
                // } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                // update(file);
                // } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                //delete(file);
                System.out.println(kind + " > " + file.getName());
                // }
                // }
            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }

    }

    private void create(File file) {
        System.out.println(file.getName());
    }

    private void delete(File file) {
        System.out.println(file.getName());
    }

    private void rename(File file) {

    }

    private void update(File file) {
        System.out.println(file.getName());
    }
}
