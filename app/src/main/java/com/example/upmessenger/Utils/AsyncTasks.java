package com.example.upmessenger.Utils;

import android.os.Handler;
import android.os.Looper;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AsyncTasks {
    private final ExecutorService executors;

    public AsyncTasks() {
        this.executors = Executors.newSingleThreadExecutor();
    }

    private void startBackground() throws MalformedURLException {
        onPreExecute();
        executors.execute(new Runnable() {
            @Override
            public void run() {
                doInBackground();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        onPostExecute();
                    }
                });
            }
        });
    }

    public void execute() throws MalformedURLException {
        startBackground();
    }

    public void shutdown() {
        executors.shutdown();
    }

    public boolean isShutdown() {
        return executors.isShutdown();
    }

    public abstract void onPreExecute() throws MalformedURLException;

    public abstract void doInBackground();

    public abstract void onPostExecute();
}