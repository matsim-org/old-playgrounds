package playground.sebhoerl.mexec.local;

import org.apache.commons.io.FileUtils;
import playground.polettif.publicTransitMapping.workbench.Run;
import playground.sebhoerl.mexec.Config;
import playground.sebhoerl.mexec.ConfigUtils;
import playground.sebhoerl.mexec.Simulation;
import playground.sebhoerl.mexec.data.ControllerData;
import playground.sebhoerl.mexec.generic.AbstractSimulation;
import playground.sebhoerl.mexec.local.data.LocalSimulationData;
import playground.sebhoerl.mexec.placeholders.PlaceholderUtils;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LocalSimulation extends AbstractSimulation<LocalSimulationData> implements Simulation {
    final private LocalEnvironment environment;

    final private File path;
    final private File scenarioPath;
    final private File controllerPath;
    final private File outputPath;

    final private ControllerData controllerData;

    private Config config = null;

    public LocalSimulation(LocalEnvironment environment, LocalSimulationData data, File path, File scenarioPath, File controllerPath, ControllerData controllerData) {
        super(data);

        this.environment = environment;
        this.path = path;
        this.scenarioPath = scenarioPath;
        this.controllerPath = controllerPath;
        this.controllerData = controllerData;
        this.outputPath = new File(path, "output");
    }

    @Override
    public void save() {
        if (config != null) {
            ConfigUtils.saveConfig(new File(path, "config.xml"), config);
        }

        environment.save();
    }

    @Override
    public Config getConfig() {
        if (config == null) {
            File source = new File(path, "config.xml");
            config = ConfigUtils.loadConfig(source);
        }

        return config;
    }

    @Override
    public boolean isActive() {
        try {
            Process process = Runtime.getRuntime().exec("ps " + data.pid);
            process.waitFor();

            if (process.exitValue() == 0) {
                return true;
            }
        } catch (InterruptedException e) {
        } catch (IOException e) {}

        return false;
    }

    @Override
    public void start() {
        if (isActive()) {
            throw new RuntimeException("Cannot start an active simulation.");
        }

        reset();

        File pidPath = new File(path, "matsim.pid");
        File runScriptPath = new File(path, "run.sh");
        File outputLogPath = new File(path, "o.log");
        File errorLogPath = new File(path, "e.log");

        // Configuration creation
        Map<String, String> placeholders = new HashMap<>();
        placeholders.putAll(environment.getScenario(data.scenarioId).getPlaceholders());
        placeholders.putAll(getPlaceholders());

        placeholders.put("scenario", scenarioPath.toString());
        placeholders.put("output", outputPath.toString());

        Config config = ConfigUtils.loadConfig(new File(path, "config.xml"));
        Config transformed = PlaceholderUtils.transformConfig(config, placeholders);
        ConfigUtils.saveConfig(new File(path, "run_config.xml"), transformed);

        // Build Java Command
        List<String> command = new LinkedList<>();
        command.add("java");
        if (getMemory() != null) command.add("-Xmx" + getMemory());
        command.add("-cp");
        command.add(new File(controllerPath, controllerData.classPath).toString());
        command.add(controllerData.className);
        command.add(new File(path, "run_config.xml").toString());
        command.add("1>");
        command.add(new File(path, "o.log").toString());
        command.add("2>");
        command.add(new File(path, "e.log").toString());
        command.add("&");
        String javaCommand = String.join(" ", command);

        // Build Run Script
        String runScript = "cd " + path + "\n" + javaCommand + "\necho $! > " + pidPath;

        try {
            OutputStream outputStream = new FileOutputStream(runScriptPath);
            OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
            streamWriter.write(runScript);
            streamWriter.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error while creating the run file.");
        } catch (IOException e) {
            throw new RuntimeException("Error while writing the run file.");
        }

        // Start.
        try {
            Runtime.getRuntime().exec("sh " + runScriptPath);
        } catch (IOException e) {
            throw new RuntimeException("Error while running the run script.");
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}

        try {
            InputStream inputStream = new FileInputStream(pidPath);
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream));
            data.pid = Long.parseLong(streamReader.readLine());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("No PID file found.");
        } catch (IOException e) {
            throw new RuntimeException("Error while reading PID.");
        }

        save();
    }

    @Override
    public void stop() {
        if (!isActive()) {
            throw new RuntimeException("Cannot start an inactive simulation.");
        }

        try {
            Runtime.getRuntime().exec("kill " + data.pid);
        } catch (IOException e) {
            throw new RuntimeException("Error while killing process.");
        }

        while (isActive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
    }

    @Override
    public void reset() {
        if (isActive()) {
            throw new RuntimeException("Cannot reset an active simulation.");
        }

        File outputLogPath = new File(path, "o.log");
        File errorLogPath = new File(path, "e.log");

        FileUtils.deleteQuietly(outputLogPath);
        FileUtils.deleteQuietly(errorLogPath);
        FileUtils.deleteQuietly(outputPath);
    }

    @Override
    public Long getIteration() {
        File scorestatsPath = new File(path, "output/scorestats.txt");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(scorestatsPath)));

            String line;
            long iteration = 0;

            reader.readLine();
            while ((line = reader.readLine()) != null) {
                try {
                    iteration = Math.max(iteration, Long.parseLong(line.substring(0, line.indexOf('\t'))));
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {}
            }

            return iteration;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public InputStream getOutputFile(String suffix) {
        File source = new File(outputPath, suffix);

        try {
            return new FileInputStream(source);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error file not found:" + source);
        }
    }

    @Override
    public String getOutputPath(String suffix) {
        return new File(outputPath, suffix).toString();
    }
}
