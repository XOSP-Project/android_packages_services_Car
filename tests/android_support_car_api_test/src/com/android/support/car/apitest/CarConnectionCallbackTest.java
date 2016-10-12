/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.support.car.apitest;

import android.support.car.Car;
import android.support.car.CarConnectionCallback;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@MediumTest
public class CarConnectionCallbackTest extends CarApiTestBase {
    private static final String TAG = CarConnectionCallbackTest.class.getSimpleName();

    public void testRegisterUnregister() throws Exception {
        CarConnectionCallbackImpl listener = new CarConnectionCallbackImpl();
        getCar().registerCarConnectionCallback(listener);
        assertTrue(listener.waitForConnection(DEFAULT_WAIT_TIMEOUT_MS));
        getCar().unregisterCarConnectionCallback(listener);
    }

    public void testMultiple() throws Exception {
        CarConnectionCallbackImpl listener1 = new CarConnectionCallbackImpl();
        getCar().registerCarConnectionCallback(listener1);
        assertTrue(listener1.waitForConnection(DEFAULT_WAIT_TIMEOUT_MS));
        CarConnectionCallbackImpl listener2 = new CarConnectionCallbackImpl();
        getCar().registerCarConnectionCallback(listener2);
        assertTrue(listener2.waitForConnection(DEFAULT_WAIT_TIMEOUT_MS));
        assertFalse(listener1.waitForConnection(DEFAULT_WAIT_TIMEOUT_MS));
        getCar().unregisterCarConnectionCallback(listener2);
        getCar().unregisterCarConnectionCallback(listener1);
    }

    private class CarConnectionCallbackImpl extends CarConnectionCallback {
        int mConnectionType;
        boolean mIsConnected = false;
        private Semaphore mWaitSemaphore = new Semaphore(0);

        @Override
        public void onConnected(Car car, int connectionType) {
            Log.i(TAG, "onConnected " + connectionType);
            mConnectionType = connectionType;
            mIsConnected = true;
            mWaitSemaphore.release();
        }

        @Override
        public void onDisconnected(Car car) {
            Log.i(TAG, "onDisconnected");
            mIsConnected = false;
            mWaitSemaphore.release();
        }

        public boolean waitForConnection(long timeoutMs) throws Exception {
            if (!mWaitSemaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) {
                return false;
            }
            assertTrue(mIsConnected);
            assertEquals(Car.CONNECTION_TYPE_EMBEDDED, mConnectionType);
            return true;
        }

        public boolean waitForDisconnect(long timeoutMs) throws Exception {
            if (!mWaitSemaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) {
                return false;
            }
            assertFalse(mIsConnected);
            return true;
        }
    }
}
