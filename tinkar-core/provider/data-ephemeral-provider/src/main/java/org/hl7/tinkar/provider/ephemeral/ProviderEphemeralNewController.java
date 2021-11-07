package org.hl7.tinkar.provider.ephemeral;

import com.google.auto.service.AutoService;
import org.hl7.tinkar.common.service.DataServiceController;
import org.hl7.tinkar.common.service.DataUriOption;
import org.hl7.tinkar.common.service.LoadDataFromFileController;
import org.hl7.tinkar.common.service.PrimitiveDataService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@AutoService(DataServiceController.class)
public class ProviderEphemeralNewController implements DataServiceController<PrimitiveDataService> {

    public static String CONTROLLER_NAME = "Load Ephemeral Store";

    private DataUriOption dataUriOption;

    public List<DataUriOption> providerOptions() {
        List<DataUriOption> dataUriOptions = new ArrayList<>();
        File rootFolder = new File(System.getProperty("user.home"), "Solor");
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }
        for (File f : rootFolder.listFiles()) {
            if (isValidDataLocation(f.getName())) {
                dataUriOptions.add(new DataUriOption(f.getName(), f.toURI()));
            }
        }
        return dataUriOptions;
    }

    @Override
    public boolean isValidDataLocation(String name) {
        return name.toLowerCase().endsWith(".zip") && name.toLowerCase().contains("tink");
    }

    @Override
    public void setDataUriOption(DataUriOption dataUriOption) {
        this.dataUriOption = dataUriOption;
    }

    @Override
    public String controllerName() {
        return CONTROLLER_NAME;
    }

    @Override
    public Class<? extends PrimitiveDataService> serviceClass() {
        return PrimitiveDataService.class;
    }

    public boolean running() {
        if (ProviderEphemeral.singleton == null) {
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        try {
            ProviderEphemeral.provider();
            if (this.dataUriOption != null) {
                File file = new File(this.dataUriOption.uri());
                ServiceLoader<LoadDataFromFileController> controllerFinder = ServiceLoader.load(LoadDataFromFileController.class);
                LoadDataFromFileController loader = controllerFinder.findFirst().get();
                Future<Integer> loadFuture = (Future<Integer>) loader.load(file);
                int count = loadFuture.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        ProviderEphemeral.provider().close();
    }

    @Override
    public void save() {
        // nothing to save.
    }

    @Override
    public void reload() {
        throw new UnsupportedOperationException("Can't reload yet.");
    }

    @Override
    public PrimitiveDataService provider() {
        return ProviderEphemeral.provider();
    }

    @Override
    public String toString() {
        return CONTROLLER_NAME;
    }
}
