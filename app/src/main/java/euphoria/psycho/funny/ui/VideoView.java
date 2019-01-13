package euphoria.psycho.funny.ui;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import euphoria.psycho.funny.util.AndroidServices;
import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoView extends FrameLayout {
    private static final int STATE_IDLE = 1;
    private static final int STATE_PERPARING = 2;


    private Context mAppContext;
    private int mCurrentAspectRatio;
    private IRenderView.IRenderCallback mIRenderCallback;
    private IMediaPlayer mMediaPlayer;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener = iMediaPlayer -> {

    };
    private IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = (iMediaPlayer, i, i1, i2, i3) -> {

    };
    private IRenderView mRenderView;
    private TextView mSubtitleView;
    private SurfaceHolder mSurfaceHolder;
    private Uri mUri;
    private int mVideoRotationDegree;
    private int mVideoWidth, mVideoHeight, mCurrentState, mTargetState, mVideoSarNum, mVideoSarDen;

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAppContext = context.getApplicationContext();
        mVideoWidth = 0;
        mVideoHeight = 0;

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = mTargetState = STATE_IDLE;
        mSubtitleView = new TextView(context);
        mSubtitleView.setTextSize(24);
        mSubtitleView.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
        addView(mSubtitleView, layoutParams);
    }

    public IMediaPlayer createPlayer(Type type) {
        IMediaPlayer mediaPlayer = null;

        switch (type) {

            case ANDROID:
                AndroidMediaPlayer androidMediaPlayer = new AndroidMediaPlayer();
                mediaPlayer = androidMediaPlayer;
                break;
            case IJK:
                IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
                ijkMediaPlayer.setLogEnabled(true);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
                mediaPlayer = ijkMediaPlayer;
                break;
        }
        return mediaPlayer;
    }

    private void initializeRender() {
        TextureRenderView renderView = new TextureRenderView(getContext());

        if (mMediaPlayer != null) {

            renderView.getSurfaceHolder().bindToMediaPlayer(mMediaPlayer);
            renderView.setVideoSize(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
            renderView.setVideoSampleAspectRatio(mMediaPlayer.getVideoSarNum(), mMediaPlayer.getVideoSarDen());
            renderView.setAspectRatio(mCurrentAspectRatio);

        }
        setRenderView(renderView);
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            return;
        }
        AudioManager audioManager = AndroidServices.instance().provideAudioManager();
        audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        mMediaPlayer = createPlayer(Type.IJK);
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setScreenOnWhilePlaying(true);
        mMediaPlayer.prepareAsync();
        mCurrentState = STATE_PERPARING;
    }

    private void setRenderView(IRenderView renderView) {
        if (mRenderView != null) {
            if (mMediaPlayer != null) {
                mMediaPlayer.setDisplay(null);
            }
            View renderUIView = mRenderView.getView();
            mRenderView.removeRenderCallback(mIRenderCallback);
            mRenderView = null;
            removeView(renderUIView);
        }
        if (renderView == null) return;
        mRenderView = renderView;
        renderView.setAspectRatio(mCurrentAspectRatio);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            renderView.setVideoSize(mVideoWidth, mVideoHeight);
        }
        if (mVideoSarNum > 0 && mVideoSarDen > 0) {
            renderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
        }


        View renderUIView = mRenderView.getView();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
        renderUIView.setLayoutParams(layoutParams);
        addView(renderUIView);
        mRenderView.addRenderCallback(mIRenderCallback);
        mRenderView.setVideoRotation(mVideoRotationDegree);

    }

    public enum Type {
        ANDROID,
        IJK
    }
}
