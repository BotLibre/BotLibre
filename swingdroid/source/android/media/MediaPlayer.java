/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package android.media;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.widget.VideoView;

/**
 * Stub class.
 */
public class MediaPlayer {
	public boolean looping;
	VideoView videoView = VideoView.getVideoView();
	public static interface OnCompletionListener {
		void onCompletion(MediaPlayer mp);
	}

	public static interface OnErrorListener {
		boolean onError(MediaPlayer mp, int what, int extra);
	}

	public static interface OnPreparedListener {
		void onPrepared(MediaPlayer mp);
	}
	
	public void setOnCompletionListener(OnCompletionListener listener) {
		
	}
	
	public void setOnErrorListener(OnErrorListener listener) {
		
	}
	
	public void setDataSource(Context context, Uri uri) {
		
	}
	
	public boolean isLooping() {
		return looping;
	}

	public void setLooping(boolean looping) {
		this.looping = looping;
		videoView.setLoop(looping);
	}
	
	public void prepare() {
		
	}

	public void start() {
		
	}
	
	public void stop() {
		
	}
	
	public void release() {
		
	}

	public static MediaPlayer create(Activity liveChatActivity, String chime) {
		
		return null;
	}

	public boolean isPlaying() {
		
		return false;
	}
}