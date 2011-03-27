package com.raverun.coreutil.api;


/**
 * @author Gavin Bong
 */
public interface ContentObserver {

    /**
     * Notification of new data
     *
     * @param line - possibly an empty string, but never null
     */
    void onNewLine( String line );

    /**
     * Clients must invoke {@link FileTailer#turnOn()} on receiving this call back
     */
    void onFinishNormal();

    /**
     * Clients must invoke {@link FileTailer#turnOn()} on receiving this call back
     */
    void onFinishWithException( String error );
}
