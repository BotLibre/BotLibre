/******************************************************************************
 *
 *  Copyright 2013-2019 Paphus Solutions Inc.
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
package org.botlibre.web.admin;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;

import org.botlibre.web.rest.AvatarConfig;
import org.botlibre.web.rest.WebMediumConfig;

@Entity
@AssociationOverrides({
	@AssociationOverride(name="admins", joinTable=@JoinTable(name="AVATAR_ADMINS")),
	@AssociationOverride(name="users", joinTable=@JoinTable(name="AVATAR_USERS")),
	@AssociationOverride(name="tags", joinTable=@JoinTable(name="AVATAR_TAGS")),
	@AssociationOverride(name="categories", joinTable=@JoinTable(name="AVATAR_CATEGORIES")),
	@AssociationOverride(name="errors", joinTable=@JoinTable(name="AVATAR_ERRORS"))
})
public class Avatar extends WebMedium {
	
	@OneToOne
	protected MediaFile background;
	@OneToMany(mappedBy="avatar")
	protected List<AvatarMedia> media;

	protected int width;
	protected int height;
	
	protected String nativeVoiceAppId;
	protected String nativeVoiceApiKey;
	protected String voiceApiEndpoint;
	
	public Avatar() {
	}

	public Avatar(String name) {
		super(name);
		this.name = name;
	}
	
	public WebMediumConfig buildBrowseConfig() {
		AvatarConfig config = new AvatarConfig();
		toBrowseConfig(config);
		return config;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public String getTypeName() {
		return "Avatar";
	}

	public AvatarConfig buildConfig() {
		AvatarConfig config = new AvatarConfig();
		toConfig(config);
		config.lastConnectedUser = this.lastConnectedUser;
		return config;
	}

	public List<AvatarMedia> getMedia() {
		if (this.media == null) {
			this.media = new ArrayList<AvatarMedia>();
		}
		return media;
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
		
		boolean[][] matching = new boolean[this.media.size()][4];
		int index = 0;
		// All match.
		for (AvatarMedia media : this.media) {
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
			for (AvatarMedia media : this.media) {
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
			for (AvatarMedia media : this.media) {
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
			for (AvatarMedia media : this.media) {
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
			for (AvatarMedia media : this.media) {
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
			for (AvatarMedia media : this.media) {
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
			for (AvatarMedia media : this.media) {
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
			for (AvatarMedia media : this.media) {
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
			for (AvatarMedia media : this.media) {
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
		for (AvatarMedia media : this.media) {
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
		for (AvatarMedia media : this.media) {
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
		
		boolean[][] matching = new boolean[this.media.size()][2];
		int index = 0;
		// All match.
		for (AvatarMedia media : this.media) {
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
			for (AvatarMedia media : this.media) {
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
			for (AvatarMedia media : this.media) {
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
		for (AvatarMedia media : this.media) {
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

	public MediaFile getBackground() {
		return background;
	}

	public void setBackground(MediaFile background) {
		this.background = background;
	}

	public List<AvatarMedia> getAudio(String action) {
		List<AvatarMedia> matching = new ArrayList<AvatarMedia>();
		for (AvatarMedia media : this.media) {
			if (media.isAudio() && media.getActions().contains(action)) {
				matching.add(media);
			}
		}
		return matching;
	}

	public void cloneMedia() {
		List<AvatarMedia> cloneMedia = new ArrayList<AvatarMedia>();
		for (AvatarMedia media : this.media){
			cloneMedia.add((AvatarMedia)media.clone());
		}
		this.media = cloneMedia;
	}

	public AvatarMedia getMedia(long id) {
		for (AvatarMedia media : this.media) {
			if (media.getMediaId() == id) {
				return media;
			}
		}
		return null;
	}

	public void setMedia(List<AvatarMedia> media) {
		this.media = media;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void preDelete(EntityManager em) {
		super.preDelete(em);
		if (this.background != null) {
			em.remove(this.background);
			em.remove(em.find(Media.class, this.background.getMediaId()));
			setBackground(null);
		}
		for (AvatarMedia media : this.media) {
			em.remove(media);
			em.remove(em.find(Media.class, media.getMediaId()));
		}
		Query query = em.createQuery("Select p from BotInstance p where p.instanceAvatar = :avatar");
		query.setParameter("avatar", detach());
		List<BotInstance> bots = query.getResultList();
		for (BotInstance bot : bots) {
			bot.setInstanceAvatar(null);
		}
		query = em.createQuery("Select p from User p where p.instanceAvatar = :avatar");
		query.setParameter("avatar", detach());
		List<User> users = query.getResultList();
		for (User user : users) {
			user.setInstanceAvatar(null);
		}
	}
	
	public String getNativeVoiceAppId() {
		return nativeVoiceAppId;
	}

	public void setNativeVoiceAppId(String nativeVoiceAppId) {
		this.nativeVoiceAppId = nativeVoiceAppId;
	}
	
	public String getNativeVoiceApiKey() {
		if (nativeVoiceApiKey != null) {
			return nativeVoiceApiKey;
		}
		return "";
	}

	public void setNativeVoiceApiKey(String nativeVoiceApiKey) {
		this.nativeVoiceApiKey = nativeVoiceApiKey;
	}
	
	public String getVoiceApiEndpoint() {
		if (voiceApiEndpoint != null && !voiceApiEndpoint.isEmpty()) {
			return voiceApiEndpoint; 
		}
		return "https://eastus.api.cognitive.microsoft.com/sts/v1.0/issueToken";
	}

	public void setVoiceApiEndpoint(String voiceApiEndpoint) {
		this.voiceApiEndpoint = voiceApiEndpoint;
	}
}
