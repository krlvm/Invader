package ru.krlvm.powertunnel.system;

import ru.krlvm.powertunnel.frames.LogFrame;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This class is used to mirror system outputs
 * to LogFrame and back to terminal (console)
 */
public class MirroredOutputStream extends FilterOutputStream {

    private LogFrame LOG;
    private PrintStream SYSTEM_OUTPUT;

    public MirroredOutputStream(OutputStream out, LogFrame log, PrintStream systemOutput) {
        super(out);
        LOG = log;
        SYSTEM_OUTPUT = systemOutput;
    }

    @Override
    public void write(byte[] b) throws IOException {
        String s = new String(b);
        SYSTEM_OUTPUT.write(b);
        writeToLog(s);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        String s = new String(b, off, len);
        SYSTEM_OUTPUT.write(b, off, len);
        writeToLog(s);
    }

    private void writeToLog(String s) {
        LogFrame.print(s);
    }
}
