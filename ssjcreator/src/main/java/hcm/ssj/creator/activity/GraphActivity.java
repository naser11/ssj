/*
 * GraphActivity.java
 * Copyright (c) 2017
 * Authors: Ionut Damian, Michael Dietz, Frank Gaibler, Daniel Langerenken, Simon Flutura,
 * Vitalijs Krumins, Antonio Grieco
 * *****************************************************
 * This file is part of the Social Signal Interpretation for Java (SSJ) framework
 * developed at the Lab for Human Centered Multimedia of the University of Augsburg.
 *
 * SSJ has been inspired by the SSI (http://openssi.net) framework. SSJ is not a
 * one-to-one port of SSI to Java, it is an approximation. Nor does SSJ pretend
 * to offer SSI's comprehensive functionality and performance (this is java after all).
 * Nevertheless, SSJ borrows a lot of programming patterns from SSI.
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package hcm.ssj.creator.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.util.ArrayList;

import hcm.ssj.audio.AudioDecoder;
import hcm.ssj.audio.PlaybackListener;
import hcm.ssj.audio.PlaybackThread;
import hcm.ssj.creator.R;
import hcm.ssj.creator.view.StreamLayout;
import hcm.ssj.creator.view.WaveformView;
import hcm.ssj.file.FileUtils;

/**
 * Visualizes user-saved data. This class supports visualization of stream files (.stream~) as well
 * as multiple media files like .mp3, .mp4, and .wav.
 */
public class GraphActivity extends AppCompatActivity
{
	private static final String SUPPORTED_MEDIA_TYPES = "mp3|mp4|wav";

	private ChooserDialog chooserDialog;
	private ArrayList<PlaybackThread> playbackThreads = new ArrayList<>();
	private StreamLayout streamLayout;

	private Button playButton;
	private Button resetButton;

	private int maxAudioLength = Integer.MIN_VALUE;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graph_layout);

		streamLayout = (StreamLayout) findViewById(R.id.stream_layout);

		initializeUI();
	}

	private void initializeUI()
	{
		playButton = (Button) findViewById(R.id.play);
		playButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				for (PlaybackThread playbackThread : playbackThreads)
				{
					playbackThread.play();
					playButton.setText(R.string.play);
				}
			}
		});

		resetButton = (Button) findViewById(R.id.reset);
		resetButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				for (PlaybackThread playbackThread : playbackThreads)
				{
					playbackThread.reset();
				}
				playButton.setText(R.string.play);
			}
		});


		Button loadButton = (Button) findViewById(R.id.load_stream_file);
		loadButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				chooserDialog = new ChooserDialog().with(GraphActivity.this);
				chooserDialog.withStartFile(Environment.getExternalStorageDirectory().getPath());
				chooserDialog.withChosenListener(new ChooserDialog.Result() {
					@Override
					public void onChoosePath(String path, File file) {
						try
						{
							String type = FileUtils.getFileType(file);
							if (type.matches(SUPPORTED_MEDIA_TYPES))
							{
								AudioDecoder decoder = new AudioDecoder(file.getPath());
								int audioLength = decoder.getAudioLength();

								WaveformView waveform = new WaveformView(GraphActivity.this);
								waveform.setSamples(decoder.getSamples());
								streamLayout.addView(waveform, 0);

								// Create horizontal separator line if multiple waveforms are present.
								if (streamLayout.getChildCount() > 1)
								{
									addWaveformSeparator();
								}

								showMediaButtons();

								playbackThreads.add(new PlaybackThread(GraphActivity.this, file));

								if (audioLength > maxAudioLength)
								{
									maxAudioLength = audioLength;
									streamLayout.setAudioLength(audioLength);

									for (PlaybackThread playbackThread : playbackThreads)
									{
										playbackThread.removePlaybackListener();
									}

									PlaybackListener playbackListener = new PlaybackListener() {
										@Override
										public void onProgress(int progress)
										{
											streamLayout.setMarkerPosition(progress);
										}

										@Override
										public void onCompletion()
										{
											playButton.setText(R.string.play);
											streamLayout.setMarkerPosition(-1);
										}
									};
									playbackThreads.get(playbackThreads.size() - 1).setPlaybackListener(playbackListener);
								}
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}).build();
				chooserDialog.show();
			}
		});
	}

	private void showMediaButtons()
	{
		playButton.setVisibility(View.VISIBLE);
		resetButton.setVisibility(View.VISIBLE);
	}

	private void addWaveformSeparator()
	{
		View separator = new View(GraphActivity.this);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, 4);
		separator.setLayoutParams(params);
		separator.setBackgroundColor(getResources().getColor(R.color.colorSeparator));
		streamLayout.addView(separator, 1);
	}
}
