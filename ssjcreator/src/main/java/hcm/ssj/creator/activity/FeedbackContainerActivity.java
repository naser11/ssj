/*
 * FeedbackContainerActivity.java
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

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import hcm.ssj.creator.R;
import hcm.ssj.creator.view.Feedback.FeedbackContainerOnDragListener;
import hcm.ssj.creator.view.Feedback.FeedbackLevelLayout;
import hcm.ssj.creator.view.Feedback.FeedbackListener;
import hcm.ssj.feedback.AndroidTactileFeedback;
import hcm.ssj.feedback.AuditoryFeedback;
import hcm.ssj.feedback.Feedback;
import hcm.ssj.feedback.FeedbackContainer;
import hcm.ssj.feedback.VisualFeedback;

public class FeedbackContainerActivity extends AppCompatActivity
{
	public static FeedbackContainer feedbackContainer = null;
	private FeedbackContainer innerFeedbackContainer = null;
	private LinearLayout levelLinearLayout;
	private List<FeedbackLevelLayout> feedbackLevelLayoutList;
	private FeedbackListener feedbackListener;
	private ImageView recycleBin;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback_container);
		mock();
		init();
		createLevels();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (innerFeedbackContainer != null)
		{
			List<Map<Feedback, FeedbackContainer.LevelBehaviour>> feedbackLevelList = new ArrayList<>();
			for (FeedbackLevelLayout feedbackLevelLayout : feedbackLevelLayoutList)
			{
				Map<Feedback, FeedbackContainer.LevelBehaviour> feedbackLevelBehaviourMap = feedbackLevelLayout.getFeedbackLevelBehaviourMap();
				if (feedbackLevelBehaviourMap != null && !feedbackLevelBehaviourMap.isEmpty())
				{
					feedbackLevelList.add(feedbackLevelLayout.getFeedbackLevelBehaviourMap());
				}
			}
			innerFeedbackContainer.setFeedbackList(feedbackLevelList);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

		for (FeedbackLevelLayout feedbackLevelLayout : feedbackLevelLayoutList)
		{
			feedbackLevelLayout.reorderFeedbackComponentGrid();
			feedbackLevelLayout.invalidate();
		}
	}

	private void mock()
	{
		if (feedbackContainer == null && innerFeedbackContainer == null)
		{
			feedbackContainer = new FeedbackContainer();
			feedbackContainer.addFeedback(new AuditoryFeedback(), 0, FeedbackContainer.LevelBehaviour.Regress);
			feedbackContainer.addFeedback(new VisualFeedback(), 0, FeedbackContainer.LevelBehaviour.Neutral);
			feedbackContainer.addFeedback(new AndroidTactileFeedback(), 0, FeedbackContainer.LevelBehaviour.Progress);
		}
	}

	private void init()
	{
		innerFeedbackContainer = feedbackContainer;
		feedbackContainer = null;
		if (innerFeedbackContainer == null)
		{
			finish();
			throw new RuntimeException("no feedbackcontainer given");
		}
		levelLinearLayout = (LinearLayout) findViewById(R.id.feedbackLinearLayout);
		recycleBin = (ImageView) findViewById(R.id.recycleBin);
		recycleBin.setOnDragListener(new FeedbackContainerOnDragListener(this));

		setTitle(innerFeedbackContainer.getComponentName());
		feedbackLevelLayoutList = new ArrayList<>();

		this.feedbackListener = new FeedbackListener()
		{
			@Override
			public void onComponentAdded()
			{
				deleteEmptyLevels();
				addEmptyLevel();
			}
		};
	}

	private void createLevels()
	{
		feedbackLevelLayoutList.clear();
		levelLinearLayout.removeAllViews();
		List<Map<Feedback, FeedbackContainer.LevelBehaviour>> feedbackLevelList = innerFeedbackContainer.getFeedbackList();
		for (int i = 0; i < feedbackLevelList.size(); i++)
		{
			FeedbackLevelLayout feedbackLevelLayout = new FeedbackLevelLayout(this, i, feedbackLevelList.get(i));
			feedbackLevelLayout.setOnDragListener(new FeedbackContainerOnDragListener(this));
			feedbackLevelLayout.setFeedbackListener(this.feedbackListener);
			feedbackLevelLayoutList.add(feedbackLevelLayout);
			levelLinearLayout.addView(feedbackLevelLayout);
		}
		addEmptyLevel();
	}

	private void addEmptyLevel()
	{
		FeedbackLevelLayout feedbackLevelLayout = new FeedbackLevelLayout(this, levelLinearLayout.getChildCount(), null);
		feedbackLevelLayout.setOnDragListener(new FeedbackContainerOnDragListener(this));
		feedbackLevelLayout.setFeedbackListener(this.feedbackListener);
		feedbackLevelLayoutList.add(feedbackLevelLayout);
		levelLinearLayout.addView(feedbackLevelLayout);
	}

	private void deleteEmptyLevels()
	{
		int counter = 0;
		Iterator<FeedbackLevelLayout> iterator = feedbackLevelLayoutList.iterator();
		while (iterator.hasNext())
		{
			FeedbackLevelLayout feedbackLevelLayout = iterator.next();
			if (feedbackLevelLayout.getFeedbackLevelBehaviourMap() != null && !feedbackLevelLayout.getFeedbackLevelBehaviourMap().isEmpty())
			{
				feedbackLevelLayout.setLevel(counter);
				counter++;
			}
			else
			{
				levelLinearLayout.removeView(feedbackLevelLayout);
				iterator.remove();
			}
		}
	}

	public void showDragIcons(int width, int height)
	{
		ViewGroup.LayoutParams lpBin = this.recycleBin.getLayoutParams();
		lpBin.width = width;
		lpBin.height = height;
		this.recycleBin.setLayoutParams(lpBin);
		this.recycleBin.setVisibility(View.VISIBLE);
		this.recycleBin.invalidate();
	}

	public void hideDragIcons()
	{
		this.recycleBin.setVisibility(View.GONE);
	}

	public ImageView getRecycleBin()
	{
		return recycleBin;
	}

	public void requestReorder() {
		for (final FeedbackLevelLayout feedbackLevelLayout : feedbackLevelLayoutList)
		{
			feedbackLevelLayout.post(new Runnable() {
				@Override
				public void run()
				{
					feedbackLevelLayout.reorderFeedbackComponentGrid();
				}
			});
		}
		deleteEmptyLevels();
		addEmptyLevel();
	}
}
