package ctfmodell.simulation;

/*
  Ich war zum Thread-Unterricht leider nicht da, musste deshalb recherchieren
  https://stackoverflow.com/questions/16758346/how-pause-and-then-resume-a-thread
*/

import ctfmodell.model.Landscape;
import ctfmodell.model.PoliceOfficer;
import ctfmodell.provider.SoundProvider;
import ctfmodell.simulation.exception.SimulationException;
import javafx.application.Platform;

import java.util.Observable;
import java.util.Observer;

public class SimulationRunner implements Runnable, Observer {

    private final Object pauseLock = new Object();
    public volatile boolean running = false;
    public volatile boolean paused = false;
    private double speed;
    private Landscape landscape;

    public void setLandscape(Landscape landscape) {
        this.landscape = landscape;
    }

    public SimulationRunner(double speed, Landscape landscape) {
        this.speed = speed;
        this.landscape = landscape;
        landscape.addObserver(this);
        landscape.getPoliceOfficer().addObserver(this);
    }

    @Override
    public void run() {
        this.running = true;
        try {
            landscape.getPoliceOfficer().main();
            if (landscape.getPoliceOfficer().hasWon()) {

            }
            this.running = false;
            this.paused = false;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            SoundProvider.beep();
        }
        this.running = false;
        this.paused = false;
    }


    @Override
    public void update(Observable o, Object arg) {
        if (!(arg instanceof PoliceOfficer))
            this.landscape = (Landscape) o;

        try {
            if (!Platform.isFxApplicationThread()) {
                if (!running) throw new SimulationException("Simulation wurde beendet");
                synchronized (pauseLock) {
                    if (paused) {
                        try {
                            synchronized (pauseLock) {
                                pauseLock.wait();
                            }
                        } catch (InterruptedException ex) {
                            throw new SimulationException("Simulation wurde beendet");
                        }
                    }
                    if (!running) throw new SimulationException("Simulation wurde beendet");
                }
                Thread.sleep((long) this.speed);
            }
        } catch (InterruptedException e) {
            System.err.println("Simulations-Thread wurde gestoppt");
        }
    }

    public void stop() {
        running = false;
        synchronized (pauseLock) {
            this.pauseLock.notify();
        }
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
