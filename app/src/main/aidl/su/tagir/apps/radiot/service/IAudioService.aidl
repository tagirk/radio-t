package su.tagir.apps.radiot.service;

import su.tagir.apps.radiot.service.IAudioServiceCallback;

interface IAudioService {

    void registerCallback(IAudioServiceCallback callback);

    void unregisterCallback(IAudioServiceCallback callback);

    void onActivityStarted();

    void onActivityStopped();

    void requestProgress();

    void seekTo(long secs);
}
