package org.podpage.hap;

import com.beowulfe.hap.HomekitRoot;
import com.beowulfe.hap.HomekitServer;
import org.podpage.hap.accessory.AccessoryManager;
import org.podpage.hap.config.Config;

public class HAPServer {

    public AccessoryManager accessoryManager;
    private Config config;
    private HomekitServer homekit;
    private HomekitRoot bridge;

    public HAPServer() {
        config = Config.loadConfig();
        accessoryManager = new AccessoryManager(this);

        int PORT = 9123;

        try {
            homekit = new HomekitServer(PORT);
            bridge = homekit.createBridge(config, config.getLabel(), config.getManufacturer(), config.getModel(), config.getSerialNumber());
            bridge.start();

            accessoryManager.checkPlugins();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
