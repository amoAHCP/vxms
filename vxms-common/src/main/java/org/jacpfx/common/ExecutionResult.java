/*
 * Copyright [2017] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.common;


/**
 * Created by amo on 19.09.16.
 * Defines the result wrapper for async operations
 */
public class ExecutionResult<T> {

    private final T result;
    private final boolean succeeded;
    private final Throwable cause;
    private final boolean errorHandling;


    /**
     * The default constructor
     *
     * @param result    the result value
     * @param succeeded the connection status
     * @param cause     The failure cause
     */
    public ExecutionResult(T result, boolean succeeded, Throwable cause) {
        this(result, succeeded, false, cause);
    }

    /**
     * The default constructor
     *
     * @param result        the result value
     * @param succeeded     the connection status
     * @param errorHandling true if an error was handled while execution
     * @param cause         The failure cause
     */
    public ExecutionResult(T result, boolean succeeded, boolean errorHandling, Throwable cause) {
        this.result = result;
        this.succeeded = succeeded;
        this.errorHandling = errorHandling;
        this.cause = cause;
    }

    /**
     * The stream of services found
     *
     * @return a stream with requested ServiceInfos
     */
    public T getResult() {
        return result;
    }


    /**
     * The connection status
     *
     * @return true if connection to ServiceRepository succeeded
     */
    public boolean succeeded() {
        return succeeded;
    }

    /**
     * The connection status
     *
     * @return true if connection to ServiceRepository NOT succeeded
     */
    public boolean failed() {
        return !succeeded;
    }

    /**
     * Returns the failure cause
     *
     * @return The failure cause when connection to ServiceRegistry was not successful
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Returns true if result was created during error handling
     *
     * @return true if error occured
     */
    public boolean handledError() {
        return errorHandling;
    }

}
