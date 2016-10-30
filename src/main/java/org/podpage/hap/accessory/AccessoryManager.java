package org.podpage.hap.accessory;

import com.beowulfe.hap.HomekitAccessory;
import org.podpage.hap.HAPServer;
import org.podpage.hap.accessory.annotation.LoadableAccessory;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AccessoryManager {

    private HAPServer hapServer;

    public AccessoryManager(HAPServer hapServer) {
        this.hapServer = hapServer;
    }

    public void loadModuls() {
        try {
            File file = new File(new File(HAPServer.class
                    .getProtectionDomain().getCodeSource().getLocation()
                    .toURI().getPath()).getParentFile(), "plugins");
            if (file.exists() && file.isDirectory()) {
                for (File f : file.listFiles()) {
                    if (f.isFile() && f.getName().endsWith(".jar")) {
                        loadModul(f);
                    } else if (f.isDirectory()) {
                        for (File cmm : f.listFiles()) {
                            if (cmm.isFile() && cmm.getName().endsWith(".jar")) {
                                loadModul(cmm);
                            }
                        }
                    }
                }
            } else {
                file.mkdirs();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    // Taken from stackoverflow:
    // http://stackoverflow.com/questions/26949333/load-a-class-in-a-jar-just-with-his-name

    public void loadModul(File file) {
        try {
            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> e = jarFile.entries();

            URL[] urls = {new URL("jar:file:" + file.getPath() + "!/")};
            URLClassLoader cl = URLClassLoader.newInstance(urls);

            HashMap<String, String> lang = new HashMap<>();

            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();

                if (!je.isDirectory() && je.getName().endsWith(".class")) {

                    InputStream inputStream = jarFile.getInputStream(je);

                    inputStream.close();

                    String className = je.getName().substring(0,
                            je.getName().length() - 6);
                    className = className.replace('/', '.');
                    Class<?> c = cl.loadClass(className);

                    if (c.isAnnotationPresent(LoadableAccessory.class)) {

                        if (HomekitAccessory.class.isAssignableFrom(c)) {
                            HomekitAccessory homekitAccessory = (HomekitAccessory) c.newInstance();
                            addAccessory(homekitAccessory);
                        }
                    }
                }
            }
            jarFile.close();
            cl.close();
           /* if (mainclass != null) {

                if (firstinstall) {
                    File folder = new File(file.getParent(), em.getName());
                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    file.renameTo(new File(file.getParent(), em.getName() + "/"
                            + em.getName() + ".cmm"));
                }
                loaded = true;
            }
            if (!loaded) {

            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addAccessory(HomekitAccessory homekitAccessory) {
        hapServer.getBridge().addAccessory(homekitAccessory);
        System.out.println("Loading: " + homekitAccessory.getLabel());
    }

    public void checkPlugins() {
        loadModuls();
    }
}
