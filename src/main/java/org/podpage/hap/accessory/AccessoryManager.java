package org.podpage.hap.accessory;

import com.beowulfe.hap.HomekitAccessory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.podpage.hap.HAPServer;
import org.podpage.hap.accessory.annotation.LoadableAccessory;
import org.podpage.hap.accessory.annotation.PluginConfig;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AccessoryManager {

    private HAPServer hapServer;

    public AccessoryManager(HAPServer hapServer) {
        this.hapServer = hapServer;
    }

    public void loadPlugins() {
        try {
            File file = new File(new File(HAPServer.class
                    .getProtectionDomain().getCodeSource().getLocation()
                    .toURI().getPath()).getParentFile(), "plugins");
            if (file.exists() && file.isDirectory()) {
                for (File f : file.listFiles()) {
                    if (f.isFile() && f.getName().endsWith(".jar")) {
                        loadPlugin(f);
                    } else if (f.isDirectory()) {
                        for (File cmm : f.listFiles()) {
                            if (cmm.isFile() && cmm.getName().endsWith(".jar")) {
                                loadPlugin(cmm);
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

    public void loadPlugin(File file) {
        try {
            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> e = jarFile.entries();

            URL[] urls = {new URL("jar:file:" + file.getPath() + "!/")};
            URLClassLoader cl = URLClassLoader.newInstance(urls);

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
                            config(homekitAccessory);
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

    public void config(HomekitAccessory homekitAccessory) {
        try {
            File configfolder = new File(new File(HAPServer.class
                    .getProtectionDomain().getCodeSource().getLocation()
                    .toURI().getPath()).getParentFile(), "config");
            File folder = new File(new File(new File(HAPServer.class
                    .getProtectionDomain().getCodeSource().getLocation()
                    .toURI().getPath()).getParentFile(), "config"), homekitAccessory.getManufacturer());
            if (!configfolder.exists()) {
                configfolder.mkdir();
            }
            if (!folder.exists()) {
                folder.mkdir();
            }
            File config = new File(folder, homekitAccessory.getLabel() + ".json");
            if (!config.exists()) {
                try {
                    config.createNewFile();
                    saveConfig(homekitAccessory);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                loadConfig(homekitAccessory);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig(HomekitAccessory homekitAccessory) {
        try {
            File folder = new File(new File(new File(HAPServer.class
                    .getProtectionDomain().getCodeSource().getLocation()
                    .toURI().getPath()).getParentFile(), "config"), homekitAccessory.getManufacturer());

            File config = new File(folder, homekitAccessory.getLabel() + ".json");

            Field[] fields = homekitAccessory.getClass().getFields();
            String json = "{";
            for (Field field : fields) {
                if (field.isAnnotationPresent(PluginConfig.class)) {
                    try {
                        json += "\"" + field.getName() + "\":"
                                + new Gson().toJson(field.get(homekitAccessory)) + ",";
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            json = json.substring(0, json.length() - 1) + "}";
            if (fields.length == 0 || json.length() < 2) {
                json = "{}";
            }
            try {
                PrintWriter writer = new PrintWriter(config, "UTF-8");
                writer.write(json);
                writer.close();
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig(HomekitAccessory homekitAccessory) {
        String json = "";
        try {
            File folder = new File(new File(new File(HAPServer.class
                    .getProtectionDomain().getCodeSource().getLocation()
                    .toURI().getPath()).getParentFile(), "config"), homekitAccessory.getManufacturer());

            File config = new File(folder, homekitAccessory.getLabel() + ".json");

            if (config.exists()) {
                Scanner scan = new Scanner(config);
                while (scan.hasNext()) {
                    json += scan.next();
                }
                scan.close();
            }
        } catch (Exception e) {
            json = "{}";
        }

        if (json.length() > 2) {
            JsonObject config = new Gson().fromJson(json, JsonObject.class);
            Field[] fields = homekitAccessory.getClass().getFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(PluginConfig.class)) {
                    try {
                        field.setAccessible(true);
                        field.set(homekitAccessory, new Gson().fromJson(config.get(field.getName()), field.getType()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public void addAccessory(HomekitAccessory homekitAccessory) {
        hapServer.getBridge().addAccessory(homekitAccessory);
        System.out.println("Loading: " + homekitAccessory.getLabel());
    }

    public void checkPlugins() {
        loadPlugins();
    }
}
