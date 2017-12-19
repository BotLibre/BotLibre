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

package org.botlibre.sdk.config;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.botlibre.sdk.micro.MicroConnection;
import org.botlibre.util.Utils;
import org.w3c.dom.Element;
/**
 * DTO for XML avatar config.
 */
public class AvatarConfig extends WebMediumConfig {
	public Map<String,AvatarMedia> mediaConfig;
	private String avatarFormat;
	private boolean avatarHD;
	public String background;
	public String icon;
	
	public String getType() {
		return "avatar";
	}
	public AvatarConfig(){
		mediaConfig = new HashMap<String, AvatarMedia>();
	}
	
	
	public ChatResponse chatReplay(ChatResponse chat){
		String emotion =  MicroConnection.getBot().mood().currentEmotionalState().name();
		String action = MicroConnection.getBot().avatar().getAction();
		String pose =  MicroConnection.getBot().avatar().getPose();
//		System.out.println("the action is : " + action);
		if (this.getBackground() != null) {
			String background = this.getBackground();
			chat.avatarBackground = background;
		}
		List<AvatarMedia> matching = null;
		
		// Audio
		if (action != null) {
			matching = this.getAudio(action, pose);
			if (!matching.isEmpty()) {
				AvatarMedia media = Utils.random(matching);
				String [] array = media.media.split("\\.");
				chat.avatarActionAudio = media.mediaId +  "." + array[1];
				chat.avatarActionAudioType = media.getType();
			}
		}
		matching = this.getAudio("", pose);
		if (!matching.isEmpty()) {
			AvatarMedia media = Utils.random(matching);
			String [] array = media.media.split("\\.");
			chat.avatarAudio = media.mediaId + "." + array[1];
			chat.avatarAudioType = media.getType();
		}
		// Image/video
		if (action != null) {
			matching = this.getMedia(emotion, action, pose, "");
			if (!matching.isEmpty()) {
				AvatarMedia media = randomMatch(matching);
				chat.avatarAction = media.getActions();
//				System.out.println("MediaAction: " + media.getActions());
				chat.avatarActionType = media.getType();
//				System.out.println("getType: " + media.getType());
				if (media.isImage()) {
					chat.avatar = this.getIcon();
					chat.avatarType = media.getType();
				}
				chat.avatarAction = null;
			}
		}
		matching = this.getMedia(emotion, "", pose, "");
		if (!matching.isEmpty()) {
			AvatarMedia media = randomMatch(matching);
			String[] array = media.media.split("\\.");
			chat.avatar = media.mediaId+"."+array[1];
			chat.avatar2 = null;
			chat.avatar3 = null;
			chat.avatar4 = null;
			chat.avatar5 = null;
			if (matching.size() > 1) {
				AvatarMedia media2 = randomMatch(matching);
				AvatarMedia media3 = randomMatch(matching);
				AvatarMedia media4 = randomMatch(matching);
				AvatarMedia media5 = randomMatch(matching);
				if (media != media2 || media != media3 || media != media4 || media != media5) {
					array = media2.media.split("\\.");
					chat.avatar2 = media2.mediaId+"."+array[1];
					array = media3.media.split("\\.");
					chat.avatar3 = media3.mediaId+"."+array[1];
					array = media4.media.split("\\.");
					chat.avatar4 = media4.mediaId+"."+array[1];
					array = media5.media.split("\\.");
					chat.avatar5 = media5.mediaId+"."+array[1];
				}
			}
			chat.avatarType = media.getType();
		}
		matching = this.getMedia(emotion, "", pose, "talking");
		if (!matching.isEmpty()) {
			AvatarMedia media = randomMatch(matching);
			String [] array = media.media.split("\\.");
			chat.avatarTalk = media.mediaId +"." +array[1];
			chat.avatarTalkType = media.getType();
		}
//		System.out.println(chat.toString());
		
		return chat;
	}
	
	public WebMediumConfig credentials() {
		AvatarConfig config = new AvatarConfig();
		config.id = this.id;
		return config;
	}
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<avatar");
		writeXML(writer);
		writer.write("</avatar>");
		return writer.toString();
	}
	
	
	public void parseXML(Element element) {
		super.parseXML(element);
	}
	
	
	/**
	 * Find any media that match the emotion, action, and pose, or the best matches.
	 */
	public List<AvatarMedia> getMedia(String emotion, String action, String pose, String pose2) {
		if (emotion == null || emotion.isEmpty()) {
			emotion = "NONE";
		} else {
			emotion = emotion.toUpperCase();
		}
		if (action == null) {
			action = "";
		}
		if (pose == null) {
			pose = "";
		}
		if (pose2 == null) {
			pose2 = "";
		}
		boolean talking = pose2.equals("talking");
		List<AvatarMedia> matches = new ArrayList<AvatarMedia>();
		boolean none = emotion.equals("NONE");
		
		boolean[][] matching = new boolean[this.mediaConfig.size()][4];
		int index = 0;
		// All match.
		for (AvatarMedia media : this.mediaConfig.values()) {
			if (media.isAudio()) {
				index++;
				continue;
			}
			boolean emotionMatch = media.getEmotions().contains(emotion) || (none && media.getEmotions().isEmpty());
			if (emotionMatch && emotion.equals("RAGE") && media.getEmotions().contains("COURAGE")) {
				// Both contain "rage".
				emotionMatch = false;
			}
			if (emotionMatch && emotion.equals("LIKE") && media.getEmotions().contains("DISLIKE")) {
				// Both contain "like".
				emotionMatch = false;
			}
			boolean actionMatch = (!action.isEmpty() && media.getActions().contains(action)) || (action.isEmpty() && media.getActions().isEmpty());
			boolean poseMatch = (!pose.isEmpty() && media.getPoses().contains(pose)) || (pose.isEmpty() && media.getPoses().isEmpty());
			boolean pose2Match = (!pose2.isEmpty() && media.getPoses().contains(pose2)) || pose2.isEmpty();
			if (!pose2Match && pose2.equals("talking")) {
				pose2Match = media.getTalking();
			} else if (media.getTalking() && !talking) {
				pose2Match = false;
			}
			matching[index][0] = emotionMatch;
			matching[index][1] = actionMatch;
			matching[index][2] = poseMatch;
			matching[index][3] = pose2Match;
			index++;
			if (emotionMatch && actionMatch && poseMatch && pose2Match) {
				matches.add(media);
			}
		}
		if (!matches.isEmpty()) {
			return matches;
		}
		// Pose2 and pose matches.
		if (!pose2.isEmpty()) {
			index = 0;
			for (AvatarMedia media : this.mediaConfig.values()) {
				if (media.isAudio()) {
					index++;
					continue;
				}
				boolean poseMatch = matching[index][2];
				boolean pose2Match = matching[index][3];
				if (poseMatch && pose2Match) {
					matches.add(media);
				}
				index++;
			}
			if (!matches.isEmpty()) {
				return matches;
			}
			// Pose2 and emotion matches.
			index = 0;
			for (AvatarMedia media :this.mediaConfig.values()) {
				if (media.isAudio()) {
					index++;
					continue;
				}
				boolean emotionMatch = matching[index][0];
				boolean pose2Match = matching[index][3];
				if (emotionMatch && pose2Match) {
					if (!media.getPoses().isEmpty()) {
						index++;
						continue;
					}
					matches.add(media);
				}
				index++;
			}
			if (!matches.isEmpty()) {
				return matches;
			}
			// Pose2 matches.
			index = 0;
			for (AvatarMedia media : this.mediaConfig.values()) {
				if (media.isAudio()) {
					index++;
					continue;
				}
				boolean pose2Match = matching[index][3];
				if (pose2Match) {
					if (!media.getPoses().isEmpty()) {
						index++;
						continue;
					}
					matches.add(media);
				}
				index++;
			}
			if (!matches.isEmpty()) {
				return matches;
			}
			// Pose2 any matches.
			index = 0;
			for (AvatarMedia media : this.mediaConfig.values()) {
				if (media.isAudio()) {
					index++;
					continue;
				}
				boolean pose2Match = matching[index][3];
				if (pose2Match) {
					matches.add(media);
				}
				index++;
			}
			// For pose2, the pose is required (i.e. talking).
			return matches;
		}
		// Action and pose matches.
		if (!action.isEmpty()) {
			index = 0;
			for (AvatarMedia media : this.mediaConfig.values()) {
				if (media.isAudio()) {
					index++;
					continue;
				}
				if (!talking && media.getTalking()) {
					index++;
					continue;
				}
				boolean actionMatch = matching[index][1];
				boolean poseMatch = matching[index][2];
				if (actionMatch && poseMatch) {
					matches.add(media);
				}
				index++;
			}
			if (!matches.isEmpty()) {
				return matches;
			}
			// Action matches.
			index = 0;
			for (AvatarMedia media : this.mediaConfig.values()) {
				if (media.isAudio()) {
					index++;
					continue;
				}
				if (!talking && media.getTalking()) {
					index++;
					continue;
				}
				boolean actionMatch = matching[index][1];
				if (actionMatch
						&& (media.getPoses().isEmpty() || media.getPoses().contains("none"))) {
					matches.add(media);
				}
				index++;
			}
			// For actions, the action is required.
			return matches;
		}
		// Pose and emotion matches.
		if (!pose.isEmpty()) {
			index = 0;
			for (AvatarMedia media : this.mediaConfig.values()) {
				if (media.isAudio()) {
					index++;
					continue;
				}
				if (!talking && media.getTalking()) {
					index++;
					continue;
				}
				boolean emotionMatch = matching[index][0];
				boolean poseMatch = matching[index][2];
				if (poseMatch && emotionMatch && media.getActions().isEmpty()) {
					matches.add(media);
				}
				index++;
			}
			if (!matches.isEmpty()) {
				return matches;
			}
			// Pose matches.
			index = 0;
			for (AvatarMedia media : this.mediaConfig.values()) {
				if (media.isAudio()) {
					index++;
					continue;
				}
				if (!talking && media.getTalking()) {
					index++;
					continue;
				}
				boolean poseMatch = matching[index][2];
				if (poseMatch && media.getActions().isEmpty() 
						&& (media.getEmotions().isEmpty() || media.getEmotions().contains("NONE"))) {
					matches.add(media);
				}
				index++;
			}
			if (!matches.isEmpty()) {
				return matches;
			}
		}
		// Emotion matches.
		index = 0;
		for (AvatarMedia media : this.mediaConfig.values()) {
			if (media.isAudio()) {
				index++;
				continue;
			}
			if (!talking && media.getTalking()) {
				index++;
				continue;
			}
			boolean emotionMatch = matching[index][0];
			if (emotionMatch
						&& (media.getActions().isEmpty() || media.getActions().contains("none"))
						&& (media.getPoses().isEmpty() || media.getPoses().contains("none"))) {
				matches.add(media);
			}
			index++;
		}
		if (!matches.isEmpty()) {
			return matches;
		}
		// Default matches.
		index = 0;
		for (AvatarMedia media : this.mediaConfig.values()) {
			if (media.isAudio()) {
				index++;
				continue;
			}
			if (!talking && media.getTalking()) {
				index++;
				continue;
			}
			if ((media.getEmotions().isEmpty() || media.getEmotions().contains("NONE"))
						&& (media.getActions().isEmpty() || media.getActions().contains("none"))
						&& (media.getPoses().isEmpty() || media.getPoses().contains("none"))) {
				matches.add(media);
			}
			index++;
		}
		return matches;
	}
	
	/**
	 * Find an audio media that matches the action or pose.
	 */
	public List<AvatarMedia> getAudio(String action, String pose) {
		if (action == null) {
			action = "";
		}
		if (pose == null) {
			pose = "";
		}
		List<AvatarMedia> matches = new ArrayList<AvatarMedia>();
		
		boolean[][] matching = new boolean[this.mediaConfig.size()][2];
		int index = 0;
		// All match.
		for (AvatarMedia media : this.mediaConfig.values()) {
			if (!media.isAudio()) {
				continue;
			}
			boolean actionMatch = (!action.isEmpty() && media.getActions().contains(action)) || (action.isEmpty() && media.getActions().isEmpty());
			boolean poseMatch = (!pose.isEmpty() && media.getPoses().contains(pose)) || (pose.isEmpty() && media.getPoses().isEmpty());
			matching[index][0] = actionMatch;
			matching[index][1] = poseMatch;
			index++;
			if (actionMatch && poseMatch) {
				matches.add(media);
			}
		}
		if (!matches.isEmpty()) {
			return matches;
		}
		// Action matches.
		if (!action.isEmpty()) {
			index = 0;
			for (AvatarMedia media : this.mediaConfig.values()) {
				if (!media.isAudio()) {
					continue;
				}
				boolean actionMatch = matching[index][0];
				if (actionMatch && media.getPoses().isEmpty()) {
					matches.add(media);
				}
				index++;
			}
			// For actions, the action is required.
			return matches;
		}
		// Pose matches.
		if (!pose.isEmpty()) {
			index = 0;
			for (AvatarMedia media : this.mediaConfig.values()) {
				if (!media.isAudio()) {
					continue;
				}
				boolean poseMatch = matching[index][1];
				if (poseMatch && media.getActions().isEmpty()) {
					matches.add(media);
				}
				index++;
			}
			if (!matches.isEmpty()) {
				return matches;
			}
		}
		// Default matches.
		index = 0;
		for (AvatarMedia media : this.mediaConfig.values()) {
			if (!media.isAudio()) {
				continue;
			}
			if ((media.getEmotions().isEmpty() || media.getEmotions().contains("NONE"))
						&& (media.getActions().isEmpty() || media.getActions().contains("none"))
						&& (media.getPoses().isEmpty() || media.getPoses().contains("none"))) {
				matches.add(media);
			}
			index++;
		}
		return matches;
	}
	public AvatarMedia randomMatch(List<AvatarMedia> matches) {
		List<AvatarMedia> formatMatches = new ArrayList<AvatarMedia>();
		String format = "mp4";
		if (this.avatarFormat != null && !this.avatarFormat.isEmpty()) {
			format = this.avatarFormat;
		}
		for (AvatarMedia media : matches) {
			if (media.getType().indexOf(format) != -1) {
				formatMatches.add(media);
			}
		}
		if (formatMatches.isEmpty()) {
			List<AvatarMedia> hdMatches = new ArrayList<AvatarMedia>();
			for (AvatarMedia media : matches) {
				if (this.avatarHD && media.getHD()) {
					hdMatches.add(media);
				} else if (!this.avatarHD && !media.getHD()) {
					hdMatches.add(media);
				}
			}
			if (!hdMatches.isEmpty()) {
				return Utils.random(hdMatches);
			}
			return Utils.random(matches);
		} else {
			List<AvatarMedia> hdMatches = new ArrayList<AvatarMedia>();
			for (AvatarMedia media : formatMatches) {
				if (this.avatarHD && media.getHD()) {
					hdMatches.add(media);
				} else if (!this.avatarHD && !media.getHD()) {
					hdMatches.add(media);
				}
			}
			if (!hdMatches.isEmpty()) {
				return Utils.random(hdMatches);
			}
			return Utils.random(formatMatches);
		}
	}
	public String getBackground(){
		return background;
	}
	public String getIcon(){
		return icon;
	}
}