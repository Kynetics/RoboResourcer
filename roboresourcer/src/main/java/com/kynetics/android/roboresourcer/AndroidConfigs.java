package com.kynetics.android.roboresourcer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.kynetics.i18ncfg.ConfigBuilder.PROFILE_PATH_CONFIG_KEY;

/**
 * Created by andrex on 07/11/16.
 */

public class AndroidConfigs {

    public interface Reloader {
        void needReload();
    }

    public static AndroidConfigs fromDelegate(Config delegate, String keyPreferenceFilePath) {
        return fromDelegate(delegate, keyPreferenceFilePath, null);
    }

    public static AndroidConfigs fromDelegate(Config delegate, String keyPreferenceFilePath, Reloader reloader) {
        return new AndroidConfigs(delegate, keyPreferenceFilePath, reloader);
    }

    public Object getAnyRef(String path) {
        return delegate.getAnyRef(path);
    }

    public List<Boolean> getBooleanList(String path) {
        return delegate.getBooleanList(path);
    }

    public List<? extends ConfigObject> getObjectList(String path) {
        return delegate.getObjectList(path);
    }

    public Config withValue(String path, ConfigValue value) {
        return delegate.withValue(path, value);
    }

    public List<Long> getDurationList(String path, TimeUnit unit) {
        return delegate.getDurationList(path, unit);
    }

    public List<Number> getNumberList(String path) {
        return delegate.getNumberList(path);
    }

    public Number getNumber(String path) {
        return delegate.getNumber(path);
    }

    public Long getBytes(String path) {
        return delegate.getBytes(path);
    }

    public List<Long> getLongList(String path) {
        return delegate.getLongList(path);
    }

    public List<String> getStringList(String path) {
        return delegate.getStringList(path);
    }

    public double getDouble(String path) {
        return delegate.getDouble(path);
    }

    public List<Double> getDoubleList(String path) {
        return delegate.getDoubleList(path);
    }

    public List<Integer> getIntList(String path) {
        return delegate.getIntList(path);
    }

    public List<? extends Object> getAnyRefList(String path) {
        return delegate.getAnyRefList(path);
    }

    public int getInt(String path) {
        return delegate.getInt(path);
    }

    public boolean getBoolean(String path) {
        return delegate.getBoolean(path);
    }

    public String getString(String path) {
        return delegate.getString(path);
    }

    public List<? extends Config> getConfigList(String path) {
        return delegate.getConfigList(path);
    }

    public long getDuration(String path, TimeUnit unit) {
        return delegate.getDuration(path, unit);
    }

    public long getLong(String path) {
        return delegate.getLong(path);
    }

    public Config getConfig(String path) {
        return delegate.getConfig(path);
    }

    public List<Long> getBytesList(String path) {
        return delegate.getBytesList(path);
    }

    public Config withOnlyPath(String path) {
        return delegate.withOnlyPath(path);
    }

    public Config withoutPath(String path) {
        return delegate.withoutPath(path);
    }

    public Config atPath(String path) {
        return delegate.atPath(path);
    }

    public Config atKey(String key) {
        return delegate.atKey(key);
    }

    public File getFile(String key) {
        try {
            return new File(new URI(getString(key)));
        } catch (URISyntaxException use) {
            throw new RuntimeException(use);
        }
    }

    public Bitmap getBitmap(String key) {
        return BitmapFactory.decodeFile(getFile(key).getAbsolutePath());
    }

    public Config getDelegate() {
        return delegate;
    }

    public void savePreference(String key, Object value) {
        Config prefConfig;
        if(preferencesFile.exists()) {
            prefConfig = ConfigFactory.parseFile(preferencesFile);
        } else {
            prefConfig = ConfigFactory.empty();
        }
        final String wrapped_key = String.format("%s.%s",
                getProfileFolderName(),
                key);
        prefConfig = prefConfig.withValue(wrapped_key, ConfigValueFactory.fromAnyRef(value));
        try (FileChannel fileChannel = new FileOutputStream(preferencesFile).getChannel()) {
            FileLock fileLock = fileChannel.lock();
            fileChannel.write(ByteBuffer.wrap(prefConfig.root().render(configRenderOptions).getBytes()));
            fileLock.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(this.reloader != null) {
            this.reloader.needReload();
        }
        loadPreferences();
    }

    @NonNull
    private String getProfileFolderName() {
        return new File(delegate.getString(PROFILE_PATH_CONFIG_KEY)).getName();
    }

    private AndroidConfigs(Config delegate, String keyPreferenceFilePath, Reloader reloader) {
        this.delegate = delegate;
        this.preferencesFile = this.getFile(keyPreferenceFilePath);
        if(!preferencesFile.exists()){
            try {
                preferencesFile.getParentFile().mkdirs();
                preferencesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        this.loadPreferences();
        this.reloader = reloader;
    }

    private void loadPreferences() {
        try (FileChannel fileChannel = new FileInputStream(preferencesFile).getChannel()) {
            FileLock fileLock = fileChannel.lock(0L, Long.MAX_VALUE, true);
            final Reader reader = Channels.newReader(fileChannel, Charset.defaultCharset().name());
            final Config prefConfig = ConfigFactory.parseReader(reader);
            fileLock.release();
            final String key = getProfileFolderName();
            if(prefConfig.hasPath(key)){
                this.delegate = prefConfig.getConfig(key).withFallback(this.delegate);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reload(){
        loadPreferences();
    }


    private Config delegate;

    private final Reloader reloader;

    private final File preferencesFile;

    private final ConfigRenderOptions configRenderOptions = ConfigRenderOptions.concise().setFormatted(true).setJson(false);

}
