package com.floodeer.hybrid.utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Runner {

    private int delay = 1;
    private int period = 1;
    private JavaPlugin main;
    private Runnable run;
    private Boolean cancelable;

    private int cycles = 1;
    private int cycled = 0;

    public Runner(JavaPlugin main, int delay, int period, int cycles) {
        this.main = main;
        this.delay = delay;
        this.period = period;
        this.cycled = cycles;
    }

    public Runner(JavaPlugin main) {
        this.main = main;
    }

    public static Runner make(JavaPlugin main) {
        return new Runner(main);
    }

    public Runner delay(int ticks) {
        if (ticks < 1)
            ticks = 1;
        this.delay = ticks;
        return this;
    }

    public Runner interval(int ticks) {
        if (ticks < 1)
            ticks = 1;
        this.period = ticks;
        return this;
    }

    public Runner limit(int cycles) {
        this.cycles = cycles;
        return this;
    }
    public Runner unlim() {
        this.cycles = 0;
        return this;
    }

    public BukkitTask run(Runnable run) {
        this.run = run;

        if (cycles < 1)
            return buildUnlimTimer();

        else if (cycles > 1)
            return buildLimitedTimer();

        else
            return buildDelayedTask();
    }

    public BukkitTask cancelable(Boolean run) {
        this.cancelable = run;

        if (cycles < 1)
            return buildUnlimCancelableTimer();

        else if (cycles > 1)
            return buildLimCancelableTimer();

        else
            return buildDelayedCancelableTask();
    }

    private BukkitTask buildLimitedTimer() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (cycles <= cycled) {
                    cancel();
                    return;
                }

                cycled++;

                run.run();
            }
        }.runTaskTimer(main, delay, period);
    }

    private BukkitTask buildUnlimTimer() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                run.run();
            }
        }.runTaskTimer(main, delay, period);
    }

    private BukkitTask buildDelayedTask() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                run.run();
            }
        }.runTaskLater(main, delay);
    }

    private BukkitTask buildLimCancelableTimer() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (cycles <= cycled) {
                    cancel();
                    return;
                }

                cycled++;

                if (!cancelable)
                    cancel();
            }
        }.runTaskTimer(main, delay, period);
    }

    private BukkitTask buildUnlimCancelableTimer() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!cancelable)
                    cancel();
            }
        }.runTaskTimer(main, delay, period);
    }

    private BukkitTask buildDelayedCancelableTask() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!cancelable)
                    cancel();
            }
        }.runTaskLater(main, delay);
    }
}