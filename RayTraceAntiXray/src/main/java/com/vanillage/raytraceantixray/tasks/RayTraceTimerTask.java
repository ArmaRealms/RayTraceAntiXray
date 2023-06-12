package com.vanillage.raytraceantixray.tasks;

import com.vanillage.raytraceantixray.RayTraceAntiXray;
import com.vanillage.raytraceantixray.data.PlayerData;
import com.vanillage.raytraceantixray.util.TimeFormatting;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class RayTraceTimerTask implements Runnable {

    private final RayTraceAntiXray plugin;

    public RayTraceTimerTask(RayTraceAntiXray plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            final List<? extends Callable<?>> jobs = plugin.getPlayerData().values().stream().map(PlayerData::getCallable).toList();
            plugin.getExecutorService().submit(() -> {
                final boolean timings = plugin.isTimingsEnabled();
                final long startTime = timings ? System.nanoTime() : 0L;
                try {
                    for (Callable<?> job : jobs) {
                        job.call();
                    }
                } catch (Throwable t) {
                    plugin.getLogger().log(Level.SEVERE, "Error thrown while raytracing: ", t);
                }
                if (timings) {
                    plugin.getLogger().info((TimeFormatting.format(TimeUnit.NANOSECONDS, System.nanoTime() - startTime, TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS)) + " per ray trace tick.");
                }
            });
        } catch (RejectedExecutionException e) {}
    }
}
