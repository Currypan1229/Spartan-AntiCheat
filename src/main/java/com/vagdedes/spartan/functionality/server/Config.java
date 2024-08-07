package com.vagdedes.spartan.functionality.server;

import com.vagdedes.spartan.Register;
import com.vagdedes.spartan.abstraction.check.Check;
import com.vagdedes.spartan.abstraction.configuration.implementation.Compatibility;
import com.vagdedes.spartan.abstraction.configuration.implementation.Messages;
import com.vagdedes.spartan.abstraction.configuration.implementation.SQLFeature;
import com.vagdedes.spartan.abstraction.configuration.implementation.Settings;
import com.vagdedes.spartan.abstraction.player.SpartanPlayer;
import com.vagdedes.spartan.functionality.connection.cloud.CloudBase;
import com.vagdedes.spartan.functionality.connection.cloud.CrossServerInformation;
import com.vagdedes.spartan.functionality.connection.cloud.IDs;
import com.vagdedes.spartan.functionality.moderation.Wave;
import com.vagdedes.spartan.functionality.notifications.AwarenessNotifications;
import com.vagdedes.spartan.functionality.performance.ResearchEngine;
import me.vagdedes.spartan.api.API;
import me.vagdedes.spartan.api.SpartanReloadEvent;
import me.vagdedes.spartan.system.Enums;
import me.vagdedes.spartan.system.Enums.HackType;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {

    public static final String checksFileName = "checks.yml";

    private static YamlConfiguration configuration = null;
    private static String construct = null;
    public static Settings settings = new Settings();
    public static SQLFeature sql = new SQLFeature();
    public static Messages messages = new Messages();
    public static Compatibility compatibility = new Compatibility();

    static {
        refreshFields(false);
    }

    // Separator

    public static File getFile() {
        return new File(Register.plugin.getDataFolder() + "/" + checksFileName);
    }

    public static YamlConfiguration getConfiguration() { // Synchronise it in all uses
        if (configuration == null) {
            File file = getFile();

            if (file.exists()) {
                configuration = YamlConfiguration.loadConfiguration(file);
            } else {
                try {
                    if (file.createNewFile()) {
                        configuration = YamlConfiguration.loadConfiguration(file);
                    } else {
                        configuration = null;
                    }
                } catch (Exception ignored) {
                    configuration = null;
                }
            }
        }
        return configuration;
    }

    public static String getConstruct() {
        return construct;
    }

    // Separator

    public static Check getCheckByName(String s) {
        for (HackType hackType : Enums.HackType.values()) {
            Check check = hackType.getCheck();
            String checkName = check.getName();

            if (checkName != null && checkName.equals(s)) {
                return check;
            }
        }
        return null;
    }

    // Separator

    public static void refreshFields(boolean clearChecksCache) {
        // Configuration
        File file = getFile();

        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    configuration = YamlConfiguration.loadConfiguration(file);
                } else {
                    configuration = null;
                }
            } catch (Exception ignored) {
                configuration = null;
            }
        } else {
            configuration = YamlConfiguration.loadConfiguration(file);
        }

        // Identification & Labelling
        construct = "[Spartan " + API.getVersion() + "/" + IDs.hide(IDs.user()) + "/" + IDs.hide(IDs.file()) + "] ";
        CloudBase.clear(false);
        CrossServerInformation.refresh();

        // Check Cache
        if (clearChecksCache) {
            for (HackType hackType : Enums.HackType.values()) {
                hackType.resetCheck();
            }
        }
    }

    public static void createConfigurations(boolean local) {
        if (!local) { // Always first
            File file = getFile();

            if (file.exists()) {
                CrossServerInformation.sendConfiguration(file);
            }
        }
        settings.create(local); // Always Second (Research Engine File Logs)
        sql.create(local); // Always Third (Research Engine SQL Logs)
        messages.create(local);
        Compatibility.create(local);
        Wave.create(local);
    }

    // Separator

    public static void create() {
        boolean enabledPlugin = Register.isPluginEnabled();

        // Utilities
        refreshFields(true);

        if (enabledPlugin) {
            // Configuration
            createConfigurations(false); // Always First

            // System
            AwarenessNotifications.refresh();
        } else {
            // Configuration
            settings.clear();
            sql.refreshConfiguration();
            messages.clear();
            compatibility.clearCache();
            Wave.clearCache();

            // System
            AwarenessNotifications.clear();
        }

        // System
        ResearchEngine.refresh(enabledPlugin);
    }

    public static void reload(Object sender) {
        if (Config.settings.getBoolean("Important.enable_developer_api")) {
            SpartanReloadEvent event = new SpartanReloadEvent();
            Register.manager.callEvent(event);

            if (event.isCancelled()) {
                return;
            }
        }
        if (sender != null) {
            String message = Config.messages.getColorfulString("config_reload");

            if (sender instanceof CommandSender) {
                ((CommandSender) sender).sendMessage(message);
            } else if (sender instanceof SpartanPlayer) {
                ((SpartanPlayer) sender).sendMessage(message);
            }
        }
        create();
    }

    // Separator

    public static void enableChecks() {
        for (HackType hackType : Enums.HackType.values()) {
            hackType.getCheck().setEnabled(null, true);
        }
    }

    public static void disableChecks() {
        for (HackType hackType : Enums.HackType.values()) {
            hackType.getCheck().setEnabled(null, false);
        }
    }

    // Separator

    public static void enableSilentChecking() {
        for (HackType hackType : Enums.HackType.values()) {
            hackType.getCheck().setSilent(true);
        }
    }

    public static void disableSilentChecking() {
        for (HackType hackType : Enums.HackType.values()) {
            hackType.getCheck().setSilent(false);
        }
    }
}
