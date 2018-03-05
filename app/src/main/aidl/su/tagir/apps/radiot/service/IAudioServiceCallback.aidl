package su.tagir.apps.radiot.service;

interface IAudioServiceCallback {

    void onStateChanged(boolean loading, int state);

    void onError(String error);

}
