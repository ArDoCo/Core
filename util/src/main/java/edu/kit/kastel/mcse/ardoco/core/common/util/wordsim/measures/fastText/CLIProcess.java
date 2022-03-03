/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Spawns another process and provides a way to send strings to the input stream of the spawned process while
 * simultaneously allowing reading from the output stream of the spawned process. Instances of this class keep alive the
 * spawned process alongside its input and output stream until {@link #close()} is called.
 */
public class CLIProcess implements AutoCloseable {

    private final Process process;
    private final BufferedReader input;
    private final DataOutputStream output;

    /**
     * Constructs a new {@link CLIProcess}.
     *
     * @param builder the builder used to spawn the process
     * @throws IOException if starting the process fails
     */
    public CLIProcess(ProcessBuilder builder) throws IOException {
        builder.redirectInput(ProcessBuilder.Redirect.PIPE);
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);

        this.process = builder.start();
        this.input = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
        this.output = new DataOutputStream(this.process.getOutputStream());

        Runtime.getRuntime().addShutdownHook(new Thread(this.process::destroyForcibly));
    }

    /**
     * Sends the given string to the input stream of the spawned process. No newline is appended to the given string.
     *
     * @param string the string to send
     * @throws IOException if sending the string to the process fails
     */
    public void sendToInput(String string) throws IOException {
        this.output.writeUTF(string);
        this.output.flush();
    }

    /**
     * Waits for and reads the next line that comes from the output stream of the spawned process. A line is considered
     * to be terminated by any one of a line feed ({@code '\n'}), a carriage return ({@code '\r'}), a carriage return
     * followed immediately by a line feed, or by reaching the end-of-file ({@code EOF}). This is a blocking operation.
     *
     * @return the read line, or {@code null} if the end of stream has been reached
     * @throws IOException if reading from the output stream fails
     */
    public String readLineFromOutput() throws IOException {
        return this.input.readLine();
    }

    /**
     * Forcibly destroys the spawned process. If the process is not alive, no action is taken.
     */
    @Override
    public void close() {
        this.process.destroyForcibly();
    }

}
