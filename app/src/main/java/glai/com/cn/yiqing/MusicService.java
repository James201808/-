package glai.com.cn.yiqing;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

public class MusicService extends Service {

    private final String TAG = "MusicService";

    private MediaPlayer mMediaPlayer;

    private AudioManager mAudioManager;

    private AudioManager.OnAudioFocusChangeListener mAudioFocusChange = new
            AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            Log.e(TAG, "AUDIOFOCUS_GAIN");
                            try {
                                startPlayMusic();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                            Log.e(TAG, "AUDIOFOCUS_LOSS");
                            mAudioManager.abandonAudioFocus(mAudioFocusChange);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                            break;
                    }
                }
            };

    public MusicService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //音标处理
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (mAudioManager != null)
            mAudioManager.requestAudioFocus(mAudioFocusChange, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.test);
        mMediaPlayer.setLooping(true);

        startPlayMusic();

        return START_STICKY;
    }


    private void startPlayMusic() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            Log.e(TAG, "启动后台播放音乐");
            mMediaPlayer.start();
        }
    }

    private void stopPlayMusic() {
        if (mMediaPlayer != null) {
            Log.e(TAG, "关闭后台播放音乐");
            mMediaPlayer.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayMusic();
        Log.e(TAG, TAG + "---->onDestroy,停止服务");
    }


}
