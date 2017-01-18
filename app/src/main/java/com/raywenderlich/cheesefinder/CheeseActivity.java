/*
 * Copyright (c) 2016 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.cheesefinder;


import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class CheeseActivity extends BaseSearchActivity {

    private static final String TAG = CheeseActivity.class.getSimpleName();

    @Override
    protected void onStart() {
        super.onStart();   // It is always important to let the parent perform first!

        /*
         * The 'onStart' method is an ideal place to subscribe to any Observable
         */
        // Subscription to Button-click events
        this.createButtonClickObservable()
                .observeOn(AndroidSchedulers.mainThread())   // Ensures that the next operator in chain will be run on the main thread
                .doOnNext(new Consumer<String>() {   // Called every time a new item is emitted
                    @Override
                    public void accept(String s) throws Exception {
                        CheeseActivity.super.showProgressBar();
                    }
                })
                .observeOn(Schedulers.io())   // The next operator should be called on the I/O thread
                .map(new Function<String, List<String>>() {
                    // For each search query, a list of results is returned
                    @Override
                    public List<String> apply(String query) throws Exception {
                        return CheeseActivity.super.mCheeseSearchEngine.search(query);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<String>>() {
                    // 'accept' will be called when the observable emits an item
                    @Override
                    public void accept(List<String> result) throws Exception {
                        CheeseActivity.super.hideProgressBar();   // Hides the progress bar just before displaying a result
                        CheeseActivity.super.showResult(result);
                    }
                });
        // Subscription to EditText-change events
        this.createTextChangeObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<String>() {   // Called every time a new item is emitted
                    @Override
                    public void accept(String string) throws Exception {

                        CheeseActivity.super.showProgressBar();
                    }
                })
                .observeOn(Schedulers.io())   // The next operator should be called on the I/O thread
                .map(new Function<String, List<String>>() {
                    // For each search query, a list of results is returned
                    @Override
                    public List<String> apply(String query) throws Exception {
                        return CheeseActivity.super.mCheeseSearchEngine.search(query);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<String>>() {
                    // 'accept' will be called when the observable emits an item
                    @Override
                    public void accept(List<String> result) throws Exception {
                        CheeseActivity.super.hideProgressBar();   // Hides the progress bar just before displaying a result
                        CheeseActivity.super.showResult(result);
                    }
                });
    }

    // Declares a method that returns an observable that will emit strings when a Button is clicked
    private Observable<String> createButtonClickObservable() {

        // 'ObservableOnSubscribe' belongs to the 'rxjava2' library
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) throws Exception {

                CheeseActivity.super.mSearchButton.setOnClickListener(new View.OnClickListener() {
                    // When the click event happens, call 'onNext' on the emitter to send a String
                    @Override
                    public void onClick(View v) {
                        Log.d(CheeseActivity.TAG, "'Search button' clicked");
                        emitter.onNext(CheeseActivity.super.mQueryEditText.getText().toString());
                    }
                });

                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        CheeseActivity.super.mSearchButton.setOnClickListener(null);
                    }
                });
            }
        });
    }

    // Declares a method that returns an observable that will emit strings when an EditText changes
    private Observable<String> createTextChangeObservable() {

        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) throws Exception {

                CheeseActivity.super.mQueryEditText.addTextChangedListener(new TextWatcher() {
                    // When the EditText content changes, 'onNext' on the emitter to send a String
                    @Override
                    public void beforeTextChanged(CharSequence string, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence string, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        emitter.onNext(editable.toString());
                    }
                });

                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        CheeseActivity.super.mQueryEditText.addTextChangedListener(null);
                    }
                });
            }
        });
    }
}
