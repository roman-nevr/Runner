package org.berendeev.roma.runner.presentation.presenter;

import android.os.AsyncTask;

import org.berendeev.roma.runner.di.MyScope;
import org.berendeev.roma.runner.presentation.fragment.ServiceControlFragment;

import javax.inject.Inject;

@MyScope
public class LocationPresenter {

    public long time;
    private ServiceControlFragment view;

    @Inject
    public LocationPresenter() {
        time = System.currentTimeMillis();
    }


    public void setView(ServiceControlFragment view) {
        this.view = view;
    }

    public void setText() {

        AsyncTask asyncTask = new AsyncTask() {
            @Override protected String doInBackground(Object[] params) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "hello";
            }

            @Override protected void onPostExecute(Object s) {
                view.setText("hello");
            }
        };
        asyncTask.execute();
    }
}
