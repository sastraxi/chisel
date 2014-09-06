package com.lyricfind.dpgscript;

import com.lyricfind.dpgscript.exceptions.CannotPauseException;

import java.util.Set;

public abstract class BaseProcess {

    /**
     * Called to reset the object and set its parameters.
     *
     * Note: not actually defined as you can set its signature to anything;
     * Java reflection is used to determine what parameters need to be passed-in.
     *
     * Note: must be public; protected or private setup(?...) methods are ignored
     * when scanning for entry points.
     */
    // public final void setup();

    /**
     * Called to start the job.
     */
    public final void start() throws Exception {
        start(null);
    }

    /**
     * Called if the script execution failed and restartPoint was not null.
     * Entry point of the job. Jobs are always called in new Threads.
     */
    private Thread thread = null;
    public abstract void doStart(final Object restartPoint) throws Exception;
    public final void start(final Object restartPoint) throws Exception
    {
        lastException = null;
        didFinish = false;
        isActive = true;
        new Thread() {
            @Override
            public void run() {
                try {
                    doStart(restartPoint);
                } catch (Exception e) {
                    lastException = e;
                    cleanup();
                }
                didFinish = true;
            }
        }.start();
    }

    /**
     * Called if start(?...) throws an exception, this gives a chance to clean-up anything that needs to be done.
     * Also called right before start(?...) is called subsequent times
     */
    public abstract void doCleanup();
    public final void cleanup()
    {
        stop();
        doCleanup();
    }

    /**
     * Called if the user wants to forceably stop, or right-before cleanup occurs.
     * Threads must periodically Thread.sleep() or poll Thread.interrupted() and throw an InterruptedException
     * themselves if an interrupt has been called.
     */
    public final void stop()
    {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        isActive = false;
        didFinish = false;
    }

    /**
     * The object returned has toString() called on it and is used to notify the user of job status
     * through the web interface/email updates.
     *
     * doGetMessage() is called unless an Exception bubbled up and the Process has stopped because of it.
     * In this case, that Exception is returned.
     */
    public abstract Object doGetMessage();
    public final Object getMessage() {
        if (lastException != null) return lastException;
        return doGetMessage();
    }

    private Object lastException = null;
    public final boolean wasException() {
        return (lastException != null);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns true iff the job just finished successfully
     */
    private boolean didFinish = false;
    public final boolean didFinish() {
        return didFinish;
    }

    /**
     * Returns true iff the job is in the middle of a run right now, it might be paused as well.
     * See isPaused()
     */
    private boolean isActive = false;
    public final boolean isActive() {
        return isActive;
    }

    /**
     * If your process isPauseable(), must continually poll isPaused().
     */
    public abstract boolean isPauseable();

    protected void doPause() { /* by default, the thread just sleep-polls */ }
    protected void doResume() { /* by default, the thread just stops sleep-polling */ }
    public final void pause() throws CannotPauseException
    {
        if (didFinish() || isPaused() || !isActive() || !isPauseable())
            throw new CannotPauseException();

        isPaused = true;
        doPause();
    }

    /**
     * @return true iff isPaused() was true upon calling this function.
     */
    public final boolean resume()
    {
        boolean wasPaused = isPaused;
        if (!didFinish()) {
           doResume();
        }
        isPaused = false;
        return wasPaused;
    }

    private boolean isPaused = false;
    public final boolean isPaused() {
        return isPaused;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Set<String> emailAddresses;
    public final void addEmail(String address) {
        emailAddresses.add(address);

    }

    protected Set<String> getEmailAddresses() {
        return emailAddresses;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * An optional point to re-start the script, in case of any errors that can be fixed out-of-band.
     * @return an Object that can be passed into start(), or null if the script must be started from the
     * beginning.
     */
    public abstract Object getRestartPoint();

}
