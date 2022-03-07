/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Spawns another process and provides a way to send strings to the input stream of the spawned process while
 * simultaneously allowing reading from the output stream of the spawned process. Instances of this class keep alive the
 * spawned process alongside its input and output stream until {@link #close()} is called.
 */
public class CLIProcess implements AutoCloseable {

    private final Process process;
    private final InputStreamReader reader;
    private final OutputStreamWriter writer;
    private final StringBuilder readBuffer = new StringBuilder();

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
        this.reader = new InputStreamReader(this.process.getInputStream());
        this.writer = new OutputStreamWriter(this.process.getOutputStream());

        Runtime.getRuntime().addShutdownHook(new Thread(this.process::destroyForcibly));
    }

    /**
     * Sends the given string to the input stream of the spawned process. No newline is appended to the given string.
     *
     * @param string the string to send
     * @throws IOException if sending the string to the process fails
     */
    public void sendToInput(String string) throws IOException {
        this.writer.write(string);
        this.writer.flush();
    }

    /**
     * Sends the given line along with the system line separator to the input stream of the spawned process.
     *
     * @param line the line to send
     * @throws IOException if sending the line to the process fails
     */
    public void sendLineToInput(String line) throws IOException {
        this.sendToInput(line + System.lineSeparator());
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
        this.readBuffer.delete(0, this.readBuffer.length());

        for (int i = 0; i < 10000; i++) {
            int intChar = this.reader.read();

            if (intChar == -1) { System.out.println("EOL"); break; }

            char character = (char) intChar;

            if (character == '\n' || character == '\r') {
                break;
            }

            this.readBuffer.append(character);
        }

        return this.readBuffer.toString().trim();
    }

    /**
     * Forcibly destroys the spawned process. If the process is not alive, no action is taken.
     */
    @Override public void close() {
        this.process.destroyForcibly();
    }

}
