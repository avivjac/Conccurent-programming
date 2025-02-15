package bgu.spl.mics.application.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * TimeService is responsible for handling global system timer and clock ticks.
 * It sends TickBroadcast messages at each tick and stops after the specified
 * duration.
 */
public class TimeService extends MicroService {

    // TimeService fields
    private final int tickTime;
    private final int duration;
    private int currentTick;

    //constructor
    public TimeService(int tickTime, int duration) {
        super("TimeService");
        this.tickTime = tickTime;
        this.duration = duration;
        this.currentTick = 0;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified
     * duration.
     */
    @Override
    protected void initialize() {

        System.out.println("TimeService starting...");
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Tick: " + currentTick);
                if (currentTick < duration && !terminated) {
                    sendBroadcast(new TickBroadcast(currentTick));
                    StatisticalFolder.getInstance().setSystemRuntime(currentTick);
                    currentTick++;
                } else if (!terminated) {
                    System.out.println("TimeService reached duration limit");
                    StatisticalFolder.getInstance().setSystemRuntime(currentTick);
                    sendBroadcast(new TickBroadcast(-1));
                    Thread.sleep(2000);
                    executor.shutdown();
                    FusionSlam.getInstance().getterminatedCount().decrementAndGet();
                    terminate();
                } else {
                    System.out.println("all services terminated");
                    StatisticalFolder.getInstance().setSystemRuntime(currentTick);
                    sendBroadcast(new TickBroadcast(-1));
                    Thread.sleep(2000);
                    executor.shutdown();
                    terminate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            subscribeBroadcast(TerminatedBroadcast.class, new Callback<TerminatedBroadcast>() {
                @Override
                public void call(TerminatedBroadcast term) {
                    if (term.getSenderName() == "FusionSlamService") {
                        terminate();
                        return;
                    }
                    FusionSlam.getInstance().getterminatedCount().decrementAndGet();

                }
            });

            subscribeBroadcast(CrashedBroadcast.class, new Callback<CrashedBroadcast>() {
                @Override
                public void call(CrashedBroadcast crashed) {
                    if (executor != null && !executor.isShutdown()) {
                        executor.shutdownNow();
                    }
                    terminate();
                }
            });

        }, 0, tickTime, TimeUnit.MILLISECONDS);
    }
}