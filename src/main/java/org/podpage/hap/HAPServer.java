package org.podpage.hap;

import com.beowulfe.hap.HomekitRoot;
import com.beowulfe.hap.HomekitServer;
import org.podpage.hap.accessory.AccessoryManager;
import org.podpage.hap.config.Config;

import java.net.InetAddress;

public class HAPServer {

    private static HAPServer hapServer;
    private AccessoryManager accessoryManager;
    private Config config;
    private HomekitServer homekit;
    private HomekitRoot bridge;

    private HAPServer() {
        hapServer = this;
        config = Config.loadConfig();
        accessoryManager = new AccessoryManager(this);

        System.out.println("Your PIN: " + config.getPin());

        try {
            if (config.getIP().equals("") || config.getIP().equals("127.0.0.1")) {
                homekit = new HomekitServer(config.getPort());
            } else {
                homekit = new HomekitServer(InetAddress.getByName(config.getIP()), config.getPort());
            }
            bridge = homekit.createBridge(config, config.getLabel(), config.getManufacturer(), config.getModel(), config.getSerialNumber());
            bridge.start();

            accessoryManager.checkPlugins();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HAPServer getInstance() {
        if (hapServer != null) {
            return hapServer;
        }
        return new HAPServer();
    }

    public AccessoryManager getAccessoryManager() {
        return accessoryManager;
    }

    public Config getConfig() {
        return config;
    }

    public HomekitServer getHomekit() {
        return homekit;
    }

    public HomekitRoot getBridge() {
        return bridge;
    }
}
