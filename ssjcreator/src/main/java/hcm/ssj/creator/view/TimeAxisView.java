/*
 * TimeAxisView.java
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

package hcm.ssj.creator.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

import hcm.ssj.audio.AudioDecoder;
import hcm.ssj.creator.R;

/**
 * View that draws time axis with timestamp labels and playback marker.
 */
public class TimeAxisView extends View
{
	private static final int TIME_AXIS_OFFSET = 80;
	private static final int TIME_CODE_OFFSET = 25;
	private static final int PADDING = 25;
	private static final int TIME_STEP_PEG_LENGTH = 20;
	private static final int TEXT_SIZE = 36;
	private static final int AXIS_STROKE_WIDTH = 4;
	private static final boolean ENABLE_ANTI_ALIAS = true;

	private int audioLength;
	private int width;
	private int height;

	private TextPaint textPaint;
	private Paint axisPaint;

	public TimeAxisView(Context context)
	{
		super(context);
		init(context, null, 0);
	}

	public TimeAxisView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs, 0);
	}

	public TimeAxisView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		if (audioLength == 0)
		{
			return;
		}
		super.onDraw(canvas);
		drawTimeAxis(canvas);
	}

	@Override
	protected void onSizeChanged(int w, int h, int ow, int oh)
	{
		width = getMeasuredWidth();
		height = getMeasuredHeight();
	}

	/**
	 * Sets the length of the audio in milliseconds. To set the length of the audio use
	 * {@link AudioDecoder#getAudioLength()} which will calculate appropriate length of the decoded
	 * audio file.
	 * @param length Length of the audio file in milliseconds.
	 */
	public void setAudioLength(int length)
	{
		audioLength = length;
		invalidate();
	}

	/**
	 * Retrieves custom XML attribute values of TimeAxisView and initializes
	 * colors and text size.
	 * @param context Context of activity that uses the TimeAxisView.
	 * @param attrs Set of attributes.
	 * @param defStyle Default style.
	 */
	private void init(Context context, AttributeSet attrs, int defStyle)
	{
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
																 R.styleable.TimeAxisView,
																 defStyle, 0);

		int timeCodeColor = a.getColor(R.styleable.TimeAxisView_timeCodeColor,
									   ContextCompat.getColor(context, R.color.colorBlack));
		int axisColor = a.getColor(R.styleable.TimeAxisView_axisColor,
								   ContextCompat.getColor(context, R.color.colorBlack));
		a.recycle();

		textPaint = new TextPaint();
		textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setColor(timeCodeColor);
		textPaint.setTextSize(TEXT_SIZE);

		axisPaint = new Paint();
		axisPaint.setStyle(Paint.Style.STROKE);
		axisPaint.setStrokeWidth(AXIS_STROKE_WIDTH);
		axisPaint.setAntiAlias(ENABLE_ANTI_ALIAS);
		axisPaint.setColor(axisColor);
	}

	/**
	 * Draws time axis with appropriate time steps as labels.
	 * @param canvas Canvas to draw the axis on.
	 */
	private void drawTimeAxis(Canvas canvas)
	{
		int seconds = audioLength / 1000;
		float xStep = width / (audioLength / 1000f);
		float textWidth = textPaint.measureText("10.00");
		float secondStep = (textWidth * seconds * 2) / width;
		secondStep = Math.max(secondStep, 1) - 0.5f;

		for (float i = 0; i <= seconds; i += secondStep)
		{
			// Draw timestamp labels.
			canvas.drawText(String.format(Locale.ENGLISH, "%.1f", i),
							PADDING + i * xStep, height - TIME_CODE_OFFSET, textPaint);

			// Make every second time step peg half the length.
			int cutOff = i % 1 != 0 ? TIME_STEP_PEG_LENGTH / 2 : 0;
			canvas.drawLine(PADDING + i * xStep, height - TIME_AXIS_OFFSET,
							PADDING + i * xStep,
							height - TIME_AXIS_OFFSET + TIME_STEP_PEG_LENGTH - cutOff,
							axisPaint);
		}
		canvas.drawLine(0, height - TIME_AXIS_OFFSET, width,
						height - TIME_AXIS_OFFSET, axisPaint);
	}
}
