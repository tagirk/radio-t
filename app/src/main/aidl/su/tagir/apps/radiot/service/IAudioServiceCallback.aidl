package su.tagir.apps.radiot.service;

import su.tagir.apps.radiot.model.entries.Progress;

interface IAudioServiceCallback {

    void onStateChanged(boolean loading, int state);

    void onError(String error);

    void progress(in Progress progress);

}
