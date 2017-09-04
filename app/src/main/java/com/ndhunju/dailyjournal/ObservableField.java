package com.ndhunju.dailyjournal;

import android.util.SparseArray;

/**
 * Created by ndhunju on 9/3/17.
 * Convenient class to listen for changes in a value. This way views can update
 * its state as soon as the value changes.
 */

public class ObservableField<T> {

    public interface Observer {
        void onChanged(ObservableField observableField);
    }

    private SparseArray<Observer> mObservers;
    private T mValue;

    public ObservableField(T value) {
        mObservers = new SparseArray<Observer>(10);
        mValue = value;
    }

    public void addObserver(Observer observer) {
        mObservers.put(observer.hashCode(), observer);
    }

    public T get() {
        return mValue;
    }

    public void set(T value) {
        mValue = value;
        notifyChange();

    }

    private void notifyChange() {
        for (int i = 0; i < mObservers.size(); i++) {
            mObservers.valueAt(i).onChanged(this);
        }
    }


}
