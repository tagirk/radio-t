package su.tagir.apps.radiot.service;

import su.tagir.apps.radiot.service.IAudioServiceCallback;
import su.tagir.apps.radiot.model.entries.Progress;

interface IAudioService {

    void registerCallback(IAudioServiceCallback callback);

    void unregisterCallback(IAudioServiceCallback callback);

    void onActivityStarted();

    void onActivityStopped();

    void getProgress(out Progress state);

    void seekTo(long secs);
}
