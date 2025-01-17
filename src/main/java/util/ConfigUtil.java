package util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigUtil {

    private File file;
    private FileConfiguration config;
    private String version = "1.0";

    public ConfigUtil(Plugin plugin, String path){
        this(plugin.getDataFolder().getAbsolutePath() + "/" + path);

    }

    public ConfigUtil(String path) {
        this.file = new File(path);
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }


    public void save() {
        try {
            this.config.save(this.file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   public void reload() {
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }


    public File getFile(){
        return this.file;
    }

    public FileConfiguration getConfig(){
        return this.config;
    }

    public String getVersion(){
        return this.version;
    }
}
