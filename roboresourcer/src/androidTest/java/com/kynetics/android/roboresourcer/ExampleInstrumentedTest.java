package com.kynetics.android.roboresourcer;

import android.support.test.runner.AndroidJUnit4;

import com.kynetics.i18ncfg.ConfigBuilder;
import com.typesafe.config.Config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private final static String TEST_PATH = "/sdcard/Android/data/com.kynetics.android.roboresourcer.test";

    private final static String KEY_PREFERENCE_FILE_PATH = "preferenceFilePath";

    private final static String CFG_PATH = TEST_PATH + "/config/config.conf";

    private final static String KEY_AVAIL_VIDEOS = "application.video.availableVideos";


    @Test
    public void useAppContext() throws Exception {
        final File parent = tmp.newFolder("parent");
        final File preferencesFile = newTextFileIn(parent,"preferencesFile.conf","");
        final File rootConf = newTextFileIn(parent, "root.conf",
                "rootDir: "+parent.getAbsolutePath()+"\n"
                        +"profilePath: . //default"+" \n"
                        +KEY_PREFERENCE_FILE_PATH+": \"file://"+preferencesFile.getAbsolutePath()+"\""
        );
        final File propertiesFile = newTextFileIn(parent, "propertiesFile.properties",
                "property1: property1\n"+
                "property2: property2\n"
                );
        Config delegate =  ConfigBuilder.create().withRootDir(parent).withProfileDir(parent).build();
        AndroidConfigs cfg = AndroidConfigs.fromDelegate(delegate, "root."+KEY_PREFERENCE_FILE_PATH);
        assertEquals("property1",cfg.getString("propertiesFile.property1"));
        assertEquals("property2",cfg.getString("propertiesFile.property2"));
        assertFalse(cfg.getDelegate().hasPath("propertiesFile.property3"));
        cfg.savePreference("propertiesFile.property2", "property2 overwrite");
        cfg.savePreference("propertiesFile.property3", "property3");
        assertEquals("property1",cfg.getString("propertiesFile.property1"));
        assertEquals("property2 overwrite",cfg.getString("propertiesFile.property2"));
        assertTrue(cfg.getDelegate().hasPath("propertiesFile.property3"));
        assertEquals("property3",cfg.getString("propertiesFile.property3"));
    }

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File newTextFileIn(File folder, String fileName, String text) throws IOException {
        final File file = new File(folder, fileName);
        try(FileWriter w = new FileWriter(file)) {
            w.write(text);
        }
        return file;
    }

}
