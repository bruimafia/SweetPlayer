package com.example.gukov.sweetplayer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements View.OnClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    ImageView ivCover;
    TextView tvNumber, tvName, tvNow, tvFull;
    ImageButton btnLast, btnNext, btnPlay;
    Switch switchRepeat;
    SeekBar seekbar;
    MediaPlayer mPlayer;
//    AudioManager aManager;

    Handler myHandler = new Handler();

    double nowTime = 0; // текущее время проигрывания
    double fullTime = 0; // полная длина трека

    int currentIndex = 0; // номер трека


    // массив http-адресов треков
    String sounds[] = {
            "https://content.screencast.com/users/aa3422/folders/Default/media/d159d3dc-8295-4368-8e84-e3911a7cecdd/song_1.mid",
            "https://content.screencast.com/users/aa3422/folders/Default/media/a477f729-b327-4bce-8f97-7da6ed79f5f7/song_2.mid",
            "https://content.screencast.com/users/aa3422/folders/Default/media/020a7dba-7f1d-4fe9-a3cc-3978943f3ce9/song_3.mid",
            "https://content.screencast.com/users/aa3422/folders/Default/media/5aa0b36f-07c7-48ef-a1c9-9acf3acfc172/song_4.mid",
            "https://content.screencast.com/users/aa3422/folders/Default/media/b65f1c52-2c09-4781-b8dd-5f67e37b68db/song_5.mid",
            "https://content.screencast.com/users/aa3422/folders/Default/media/bc358780-c594-4152-9d31-c708099dd524/song_6.mid",
            "https://content.screencast.com/users/aa3422/folders/Default/media/8ccacb5e-e9af-4a88-b82c-5be4231f1554/song_7.mid",
            "https://content.screencast.com/users/aa3422/folders/Default/media/ab67b509-6b24-4efa-819b-8bd11fa813cd/song_8.mid",
            "https://content.screencast.com/users/aa3422/folders/Default/media/e66f17a6-a425-4aad-ad60-acd1590d3dcc/song_9.mid" };

    // массив обложек треков
    int soundsCovers[] = {
            R.drawable.sound_cover_1,
            R.drawable.sound_cover_2,
            R.drawable.sound_cover_3,
            R.drawable.sound_cover_4,
            R.drawable.sound_cover_5,
            R.drawable.sound_cover_6,
            R.drawable.sound_cover_7,
            R.drawable.sound_cover_8,
            R.drawable.sound_cover_9 };

    // массив отображаемых названий треков
    int soundsName[] = {
            R.string.soundName_1,
            R.string.soundName_2,
            R.string.soundName_3,
            R.string.soundName_4,
            R.string.soundName_5,
            R.string.soundName_6,
            R.string.soundName_7,
            R.string.soundName_8,
            R.string.soundName_9 };

    // массив дополнительной информации о номере текущего трека
    int soundsNumber[] = {
            R.string.soundNumber_1,
            R.string.soundNumber_2,
            R.string.soundNumber_3,
            R.string.soundNumber_4,
            R.string.soundNumber_5,
            R.string.soundNumber_6,
            R.string.soundNumber_7,
            R.string.soundNumber_8,
            R.string.soundNumber_9 };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // находим элементы
        ivCover = (ImageView) findViewById(R.id.ivCover);
        btnLast = (ImageButton) findViewById(R.id.btnLast);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        tvNumber = (TextView) findViewById(R.id.tvNumber);
        tvName = (TextView) findViewById(R.id.tvName);
        tvNow = (TextView) findViewById(R.id.tvNow);
        tvFull = (TextView) findViewById(R.id.tvFull);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        switchRepeat = (Switch) findViewById(R.id.switchRepeat);
//        aManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // устанавливаем слушатели
        btnPlay.setOnClickListener(this);
        btnLast.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        // настраиваем imageview таким образом, чтобы его высота была равна ширине экрана телефона
        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ivCover.getLayoutParams(); // получаем параметры
        params.height = displaymetrics.widthPixels; // высота равна ширине экрана
        ivCover.setLayoutParams(params);

        // переключатель повторения трека
        switchRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mPlayer != null)
                    mPlayer.setLooping(isChecked);

                if (isChecked)
                    Toast.makeText(getApplicationContext(), "Повтор трека включен", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Повтор трека выключен", Toast.LENGTH_SHORT).show();
            }
        });

        // изначально освобождаем ресурсы проигрывателя
        releaseMP();

        // создаем плеер и задаем источник
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(sounds[currentIndex]);
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // получаем информацию о времени
        fullTime = mPlayer.getDuration();
        nowTime = mPlayer.getCurrentPosition();

        // управляем сикбаром
        seekbar.setProgress(0);
        seekbar.setMax((int) fullTime);
        seekbar.setClickable(false);


        // изменяем значение сикбара в реальном времени
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mPlayer != null && fromUser){
                    mPlayer.seekTo(progress);
                }
            }
        });

    }


    // действия по нажатию кнопок
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPlay: // кнопка "play/pause"
                if (!mPlayer.isPlaying())
                    btnPlay();
                else
                    btnPause();
                break;
            case R.id.btnLast: // кнопка "предыдущий трек"
                btnLast();
                break;
            case R.id.btnNext: // кнопка "следующий трек"
                btnNext();
                break;
            default:
                break;
        }
    }


    // обработчик
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            if (mPlayer != null) {
                nowTime = mPlayer.getCurrentPosition();
                tvNow.setText(String.format("%d мин %d сек",
                        TimeUnit.MILLISECONDS.toMinutes((long) nowTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) nowTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) nowTime)))
                );

                // иконка кнопки Play/Pause
                if (!mPlayer.isPlaying())
                    btnPlay.setImageResource(R.drawable.ic_play);
                else
                    btnPlay.setImageResource(R.drawable.ic_pause);


                seekbar.setProgress((int)nowTime);
                myHandler.postDelayed(this, 100);
            }
        }
    };


    // освобождает используемые проигрывателем ресурсы
    private void releaseMP() {
        if (mPlayer != null) {
            try {
                mPlayer.release();
                mPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // кнопка "play"
    private void btnPlay() {
        if (!mPlayer.isPlaying())
            mPlayer.start();
        Toast.makeText(getApplicationContext(), "Проигрывание началось", Toast.LENGTH_SHORT).show();

        ivCover.setImageResource(soundsCovers[currentIndex]);
        tvNumber.setText(soundsNumber[currentIndex]);
        tvName.setText(soundsName[currentIndex]);

        tvNow.setText(String.format("%d мин %d сек",
                TimeUnit.MILLISECONDS.toMinutes((long) nowTime),
                TimeUnit.MILLISECONDS.toSeconds((long) nowTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) nowTime)))
        );

        tvFull.setText(String.format("%d мин %d сек",
                TimeUnit.MILLISECONDS.toMinutes((long) fullTime),
                TimeUnit.MILLISECONDS.toSeconds((long) fullTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) fullTime)))
        );

        seekbar.setProgress((int)nowTime);
        myHandler.postDelayed(UpdateSongTime,100);
    }


    // кнопка "pause"
    private void btnPause() {
        if (mPlayer.isPlaying())
            mPlayer.pause();
        Toast.makeText(getApplicationContext(), "Проигрывание приостановлено", Toast.LENGTH_SHORT).show();
    }


    // кнопка "предыдущий трек"
    private void btnLast() {
        if (currentIndex > 0) {
            currentIndex--;
        } else currentIndex = sounds.length - 1;

        ivCover.setImageResource(soundsCovers[currentIndex]);
        tvNumber.setText(soundsNumber[currentIndex]);
        tvName.setText(soundsName[currentIndex]);

        if (mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(sounds[currentIndex]);
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.start();

        fullTime = mPlayer.getDuration();
        nowTime = mPlayer.getCurrentPosition();

        seekbar.setProgress(0);
        seekbar.setMax((int) fullTime);

        tvNow.setText(String.format("%d мин %d сек",
                TimeUnit.MILLISECONDS.toMinutes((long) nowTime),
                TimeUnit.MILLISECONDS.toSeconds((long) nowTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) nowTime)))
        );

        tvFull.setText(String.format("%d мин %d сек",
                TimeUnit.MILLISECONDS.toMinutes((long) fullTime),
                TimeUnit.MILLISECONDS.toSeconds((long) fullTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) fullTime)))
        );
    }


    // кнопка "следующий трек"
    private void btnNext() {
        if (currentIndex < sounds.length - 1) {
            currentIndex++;
        } else {currentIndex = 0; Toast.makeText(getApplicationContext(), "Начало плейлиста", Toast.LENGTH_SHORT).show();}

        ivCover.setImageResource(soundsCovers[currentIndex]);
        tvNumber.setText(soundsNumber[currentIndex]);
        tvName.setText(soundsName[currentIndex]);

        if (mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(sounds[currentIndex]);
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.start();

        fullTime = mPlayer.getDuration();
        nowTime = mPlayer.getCurrentPosition();

        seekbar.setProgress(0);
        seekbar.setMax((int) fullTime);

        tvNow.setText(String.format("%d мин %d сек",
                TimeUnit.MILLISECONDS.toMinutes((long) nowTime),
                TimeUnit.MILLISECONDS.toSeconds((long) nowTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) nowTime)))
        );

        tvFull.setText(String.format("%d мин %d сек",
                TimeUnit.MILLISECONDS.toMinutes((long) fullTime),
                TimeUnit.MILLISECONDS.toSeconds((long) fullTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) fullTime)))
        );
    }


    // вызываем, плеер готов к проигрыванию
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }


    // вызываем, когда достигнут конец проигрываемого содержимого
    @Override
    public void onCompletion(MediaPlayer mp) { }


    // приостанавливаем проигрывание при скрытии приложения (кнопки "домой" и "запущенные приложения")
//    @Override
//    public void onStop() {
//        super.onStop();
//        if (mPlayer.isPlaying())
//            mPlayer.pause();
//        Toast.makeText(getApplicationContext(), "Проигрывание приостановлено", Toast.LENGTH_SHORT).show();
//    }


    // освобождаем ресурсы проигрывателя при выходе из приложения
    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMP();
    }

}