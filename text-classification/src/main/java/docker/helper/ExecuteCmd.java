package docker.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * A helper class that executed cmd line commands.
 *
 * @author Dominik Fuchss
 */
public final class ExecuteCmd {
    private static final Logger logger = LoggerFactory.getLogger(ExecuteCmd.class);
    private static final int DEFAULT_WAIT_IN_SECONDS = 15;

    private ExecuteCmd() {
        throw new IllegalAccessError();
    }

    /**
     * Run a command on the command line.
     *
     * @param command the command
     * @return the result of the execution
     */
    public static ExecuteResult runCommand(String command) {
        return runCommand(command, DEFAULT_WAIT_IN_SECONDS);
    }

    /**
     * Run a command on the command line.
     *
     * @param command                  the command
     * @param waitForResponseInSeconds the time to wait until canceling
     * @return the result of the execution
     */
    @SuppressWarnings("java:S2142")
    public static ExecuteResult runCommand(String command, int waitForResponseInSeconds) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            Pair<Pointer<String>, Pointer<String>> outputs = new Pair<>(new Pointer<>(), new Pointer<>());
            var threads = createConsumerThreads(outputs, process);

            boolean exited = process.waitFor(waitForResponseInSeconds, TimeUnit.SECONDS);

            if (!exited) {
                threads.first().interrupt();
                threads.second().interrupt();
                logger.error("Timout in #runCommand(String, int)");
                return new ExecuteResult(Integer.MIN_VALUE, null, "Timeout! Cancelling request!");
            }

            threads.first().join();
            threads.second().join();

            return new ExecuteResult(process.exitValue(), outputs.first().getP(), outputs.second().getP());
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage(), e);
            return new ExecuteResult(Integer.MIN_VALUE, null, e.getMessage());
        }
    }

    private static Pair<Thread, Thread> createConsumerThreads(Pair<Pointer<String>, Pointer<String>> outputs, Process process) {
        var outputThread = createConsumerThread(outputs.first(), process.getInputStream());
        var errorThread = createConsumerThread(outputs.second(), process.getErrorStream());
        return new Pair<>(outputThread, errorThread);
    }

    private static Thread createConsumerThread(Pointer<String> outputs, InputStream inputStream) {
        Thread runner = new Thread(() -> {
            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                char[] buffer = new char[512];
                int size;
                while ((size = reader.read(buffer)) != -1) {
                    builder.append(buffer, 0, size);
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            outputs.setP(builder.toString());
        });
        runner.setDaemon(true);
        runner.start();
        return runner;
    }

    /**
     * The result of an execution of a command.
     *
     * @param exitCode the exit code of the process (will be set to {@link Integer#MIN_VALUE} on timeout)
     * @param stdOut   the content from the output stream
     * @param stdErr   the content from the error stream
     * @author Dominik Fuchss
     */
    public record ExecuteResult(int exitCode, String stdOut, String stdErr) {
        public boolean success() {
            return exitCode == 0;
        }
    }
}
