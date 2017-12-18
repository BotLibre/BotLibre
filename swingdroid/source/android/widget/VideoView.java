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

package android.widget;
import javax.swing.JComponent;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.view.View;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;

/**
 * VideoView proxy for a Swing video view.
 */



public class VideoView extends View {
	private static VideoView videoView= new VideoView();
	private MediaPlayer mediaPlayer;
	private MediaView mediaView = new MediaView();
	
	private static JFXPanel jfx;
	public VideoView() {
		VideoView.videoView = this;
	}

	public VideoView(JComponent jfxPanel){
		super(jfxPanel);
		jfx = (JFXPanel) jfxPanel;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				setVideoView();
			}
		});
		
	}
	public void setLoop(boolean loop){
		if(loop){
			mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
		}else{
			mediaPlayer.setCycleCount(1);
		}
	}

	public void setVideoView() {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				final DoubleProperty width = mediaView.fitWidthProperty();
				final DoubleProperty height = mediaView.fitHeightProperty();
				width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
				height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
				BorderPane borderPane = new BorderPane();
				borderPane.setCenter(mediaView);
				final Scene scene = new Scene(borderPane, Color.WHITE);
				((JFXPanel) VideoView.jfx).setScene(scene);

			}
		});
		this.component = jfx;
	}

	public void setOnErrorListener(OnErrorListener listener) {
		System.out.println("OnErrorList...");
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					if (mediaPlayer != null) {
						mediaPlayer.stop();
					}
				} catch (Exception e) {
					System.out.println("SetOnErrorListener\nError Player Video: " + e.toString());
				}
			}
		});
	}
	
	public void setOnPreparedListener(OnPreparedListener listener) {
		System.out.println("OnPreparedListe...");
	}
	
	public void setOnCompletionListener(OnCompletionListener listener) {
		System.out.println("OnCompletionList...");
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					if (mediaPlayer != null) {
						mediaPlayer.stop();
					}
				} catch (Exception e) {
					System.out.println("SetOnCompletionListener\nError Player Video: " + e.toString());
				}
			}
		});
	}
	
	public void start() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					if (mediaPlayer != null) {
						mediaPlayer.play();
					}
				} catch (Exception e) {
					System.out.println("OnStart\nError Player Video: " + e.toString());
				}
			}
		});
	}
	
	public void setVideoURI(Uri uri) {
		System.out.println(uri.uri);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Media media = new Media(uri.uri);
				mediaPlayer = new MediaPlayer(media);
				mediaView.setMediaPlayer(mediaPlayer);
				mediaPlayer.setAutoPlay(true);
				mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
			}
		});
		
		
	}
	
	public static VideoView getVideoView(){
		return VideoView.videoView;
	}
}