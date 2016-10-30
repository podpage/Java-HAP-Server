package org.podpage.hap.config;

import com.beowulfe.hap.HomekitAuthInfo;
import com.beowulfe.hap.HomekitServer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.podpage.hap.HAPServer;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Config implements HomekitAuthInfo {

    private String label;
    private String manufacturer;
    private String model;
    private String serialNumber;

    private String pin;
    private short port;

    private String mac;
    private BigInteger salt;
    private byte[] privateKey;
    private ConcurrentMap<String, byte[]> userKeyMap = new ConcurrentHashMap<>();

    private Config() {

    }

    private static Config createConfig() {
        Config config = new Config();
        config.setPin((new SecureRandom().nextInt(100000000) + "").replaceFirst("(\\w{3})(\\w{2})(\\w{3})", "$1-$2-$3"));
        config.setSalt(HomekitServer.generateSalt());
        config.setMac(HomekitServer.generateMac());

        config.setPort((short) 9123);

        try {
            config.setPrivateKey(HomekitServer.generateKey());
        } catch (Exception e) {
            e.printStackTrace();
        }

        config.setLabel("HAPServer-Bridge");
        config.setManufacturer("@podpage");
        config.setSerialNumber("1.3.3.7");
        config.setModel("HAP-Java");
        return config;
    }

    public static Config loadConfig() {
        try {

            File file = new File(new File(HAPServer.class
                    .getProtectionDomain().getCodeSource().getLocation()
                    .toURI().getPath()).getParentFile(), "config.json");
            if (file.exists()) {
                Scanner scan = new Scanner(file);
                String json = "";
                while (scan.hasNext()) {
                    json += scan.next();
                }
                scan.close();

                JsonObject jsonobject = new JsonParser().parse(json).getAsJsonObject();

                Config config = new Config();

                config.setLabel(jsonobject.get("label").getAsString());
                config.setManufacturer(jsonobject.get("manufacturer").getAsString());
                config.setSerialNumber(jsonobject.get("model").getAsString());
                config.setModel(jsonobject.get("serialNumber").getAsString());

                config.setPin(jsonobject.get("pin").getAsString());
                config.setMac(jsonobject.get("mac").getAsString());

                config.setPort(jsonobject.get("port").getAsShort());

                config.setSalt(DatatypeConverter.parseInteger(jsonobject.get("salt").getAsString()));
                config.setPrivateKey(DatatypeConverter.parseBase64Binary(jsonobject.get("privateKey").getAsString()));

                Type type = new TypeToken<ConcurrentMap<String, byte[]>>() {
                }.getType();
                ConcurrentMap<String, byte[]> map = new Gson().fromJson(jsonobject.get("userKeyMap").getAsString(), type);
                config.setUserKeyMap(map);
                return config;
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        Config config = createConfig();
        config.saveConfig();
        return config;
    }

    public ConcurrentMap<String, byte[]> getUserKeyMap() {
        return userKeyMap;
    }

    public void setUserKeyMap(ConcurrentMap<String, byte[]> userKeyMap) {
        this.userKeyMap = userKeyMap;
    }

    @Override
    public String getPin() {
        return pin;
    }

    private void setPin(String pin) {
        this.pin = pin;
    }

    @Override
    public String getMac() {
        return mac;
    }

    private void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public BigInteger getSalt() {
        return salt;
    }

    private void setSalt(BigInteger salt) {
        this.salt = salt;
    }

    @Override
    public byte[] getPrivateKey() {
        return privateKey;
    }

    private void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public void createUser(String username, byte[] publicKey) {
        userKeyMap.putIfAbsent(username, publicKey);
        saveConfig();
    }

    @Override
    public void removeUser(String username) {
        userKeyMap.remove(username);
        saveConfig();
    }

    @Override
    public byte[] getUserPublicKey(String username) {
        return userKeyMap.get(username);
    }

    public String getLabel() {
        return label;
    }

    private void setLabel(String label) {
        this.label = label;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    private void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    private void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    private void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public short getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public void saveConfig() {
        try {
            JsonObject innerObject = new JsonObject();
            innerObject.addProperty("label", getLabel());
            innerObject.addProperty("manufacturer", getManufacturer());
            innerObject.addProperty("model", getModel());
            innerObject.addProperty("serialNumber", getSerialNumber());

            innerObject.addProperty("pin", getPin());
            innerObject.addProperty("mac", getMac());
            innerObject.addProperty("port", getPort());
            innerObject.addProperty("salt", DatatypeConverter.printInteger(getSalt()));
            innerObject.addProperty("privateKey", DatatypeConverter.printBase64Binary(getPrivateKey()));
            innerObject.addProperty("userKeyMap", new Gson().toJson(getUserKeyMap()));

            String json = innerObject.toString();
            File file = new File(new File(HAPServer.class
                    .getProtectionDomain().getCodeSource().getLocation()
                    .toURI().getPath()).getParentFile(), "config.json");
            file.delete();
            file.createNewFile();
            PrintWriter pw = new PrintWriter(file, "utf-8");
            pw.write(json);
            pw.flush();
            pw.close();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
