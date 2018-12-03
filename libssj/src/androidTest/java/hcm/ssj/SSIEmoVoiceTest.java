/*
 * SSIEmoVoiceTest.java
 * Copyright (c) 2018
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

package hcm.ssj;

import android.os.Environment;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import hcm.ssj.androidSensor.AndroidSensor;
import hcm.ssj.androidSensor.AndroidSensorChannel;
import hcm.ssj.androidSensor.SensorType;
import hcm.ssj.audio.AudioChannel;
import hcm.ssj.audio.Microphone;
import hcm.ssj.core.Pipeline;
import hcm.ssj.core.Provider;
import hcm.ssj.mobileSSI.SSI;
import hcm.ssj.mobileSSI.SSITransformer;
import hcm.ssj.signal.AvgVar;
import hcm.ssj.signal.Median;
import hcm.ssj.signal.Merge;
import hcm.ssj.signal.MinMax;
import hcm.ssj.signal.Progress;
import hcm.ssj.test.Logger;

/**
 * Tests ssi emovoice component
 * Created by Michael Dietz on 03.12.2018.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class SSIEmoVoiceTest
{
    @Test
    public void testEmoVoice() throws Exception
    {
        //setup
        Pipeline frame = Pipeline.getInstance();
        frame.options.bufferSize.set(10.0f);

        //sensor
        Microphone microphone = new Microphone();
        AudioChannel audioChannel = new AudioChannel();
        audioChannel.options.sampleRate.set(8000);
        audioChannel.options.scale.set(true);
        frame.addSensor(microphone, audioChannel);

        SSITransformer ssiTransformer = new SSITransformer();
        ssiTransformer.options.name.set(SSI.TransformerName.EmoVoiceFeat);
        //ssiTransformer.options.ssioptions.set(new String[]{"maj->1", "min->1"});
        frame.addTransformer(ssiTransformer, audioChannel, 1);

        //logger
        Logger log = new Logger();
        frame.addConsumer(log, ssiTransformer, 1, 0);

        //start framework
        frame.start();
        //run test
        long end = System.currentTimeMillis() + TestHelper.DUR_TEST_NORMAL;
        try
        {
            while (System.currentTimeMillis() < end)
            {
                Thread.sleep(1);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        frame.stop();
        frame.release();
    }
}