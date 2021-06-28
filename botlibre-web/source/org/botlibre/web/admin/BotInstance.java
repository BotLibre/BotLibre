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

import java.util.List;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.Query;

import org.botlibre.Bot;
import org.botlibre.thought.forgetfulness.Forgetfulness;

import org.botlibre.web.Site;
import org.botlibre.web.chat.ChatChannel;
import org.botlibre.web.rest.InstanceConfig;
import org.botlibre.web.rest.WebMediumConfig;

@Entity
@AssociationOverrides({
	@AssociationOverride(name="admins", joinTable=@JoinTable(name="BOT_ADMINS", joinColumns=@JoinColumn(name="INSTANCES_ID"))),
	@AssociationOverride(name="users", joinTable=@JoinTable(name="BOT_USERS")),
	@AssociationOverride(name="tags", joinTable=@JoinTable(name="BOT_TAGS")),
	@AssociationOverride(name="categories", joinTable=@JoinTable(name="BOT_CATEGORIES")),
	@AssociationOverride(name="errors", joinTable=@JoinTable(name="BOT_ERRORS"))
})
public class BotInstance extends WebMedium {
	public static final String MARY = "Mary";
	public static final String RESPONSIVEVOICE = "ResponsiveVoice";
	public static final String BINGSPEECH = "BingSpeech";
	public static final String QQSPEECH = "QQSpeech";
	
	protected Long databaseId;
	protected int memorySize;
	protected int memoryLimit = Site.MEMORYLIMIT;
	
	protected int wins;
	protected int losses;
	protected int rank;
	
	protected String voice;
	protected String voiceMod;
	protected boolean nativeVoice;
	protected String nativeVoiceName;
	protected String nativeVoiceProvider;
	protected String nativeVoiceApiKey;
	protected String nativeVoiceAppId;
	protected String voiceApiEndpoint;
	protected String language;
	protected String speechRate;
	protected String pitch;
	
	protected boolean allowJavaScript;
	protected boolean disableFlag;
	@OneToOne(fetch=FetchType.LAZY)
	protected Avatar instanceAvatar;
	protected boolean archived;
	protected boolean isSchema;
	
	protected String apiURL;
	protected String apiPost;
	protected String apiResponse;
	protected boolean apiServerSide;
	protected boolean apiJSON;
	

	public BotInstance() {
	}

	public BotInstance(String name) {
		super(name);
		this.name = name;
	}
	
	public WebMediumConfig buildBrowseConfig() {
		InstanceConfig config = new InstanceConfig();
		toBrowseConfig(config);
		config.isArchived = this.archived;
		config.wins = String.valueOf(this.wins);
		config.losses = String.valueOf(this.losses);
		config.rank = String.valueOf(this.rank);
		config.size = String.valueOf(this.memorySize);
		return config;
	}
	
	/**
	 * Set config defaults into the instance.
	 */
	public void initialize(Bot bot) {
		bot.setName(getName());
		if (isAdult()) {
			bot.setFilterProfanity(false);
		} else {
			if (getContentRating() == ContentRating.Everyone) {
				bot.setContentRating(Bot.EVERYONE);
			} else if (getContentRating() == ContentRating.Teen) {
				bot.setContentRating(Bot.TEEN);
			} else if (getContentRating() == ContentRating.Mature) {
				bot.setContentRating(Bot.MATURE);
			} else if (getContentRating() == ContentRating.Adult) {
				bot.setContentRating(Bot.ADULT);
			}
		}
		bot.mind().getThought(Forgetfulness.class).setMaxSize(getMemoryLimit());
	}

	public boolean hasAPI() {
		return this.apiURL != null && !this.apiURL.isEmpty();
	}

	public boolean getApiServerSide() {
		return apiServerSide;
	}

	public void setApiServerSide(boolean apiServerSide) {
		this.apiServerSide = apiServerSide;
	}

	public boolean getApiJSON() {
		return apiJSON;
	}

	public void setApiJSON(boolean apiJSON) {
		this.apiJSON = apiJSON;
	}

	public String getApiURL() {
		return apiURL;
	}

	public void setApiURL(String apiURL) {
		this.apiURL = apiURL;
	}

	public boolean getAllowJavaScript() {
		return allowJavaScript;
	}

	public void setAllowJavaScript(boolean allowJavaScript) {
		this.allowJavaScript = allowJavaScript;
	}

	public boolean getDisableFlag() {
		return disableFlag;
	}

	public void setDisableFlag(boolean disableFlag) {
		this.disableFlag = disableFlag;
	}

	public String getNativeVoiceProvider() {
		return nativeVoiceProvider;
	}

	public void setNativeVoiceProvider(String nativeVoiceProvider) {
		this.nativeVoiceProvider = nativeVoiceProvider;
	}
	
	public String getNativeVoiceApiKey() {
		if (nativeVoiceApiKey == null) {
			return "";
		}
		return nativeVoiceApiKey;
	}

	public void setNativeVoiceApiKey(String nativeVoiceToken) {
		this.nativeVoiceApiKey = nativeVoiceToken;
	}
	
	public String getVoiceApiEndpoint() {
		if (voiceApiEndpoint == null || voiceApiEndpoint.isEmpty()) {
			return "https://eastus.api.cognitive.microsoft.com/sts/v1.0/issueToken";
		}
		return voiceApiEndpoint;
	}

	public void setVoiceApiEndpoint(String endpoint) {
		this.voiceApiEndpoint = endpoint;
	}
	
	public String getNativeVoiceAppId() {
		if(nativeVoiceAppId == null) {
			return "";
		}
		return nativeVoiceAppId;
	}

	public void setNativeVoiceAppId(String nativeVoiceAppId) {
		this.nativeVoiceAppId = nativeVoiceAppId;
	}

	public String getApiPost() {
		return apiPost;
	}

	public void setApiPost(String apiPost) {
		this.apiPost = apiPost;
	}

	public String getApiResponse() {
		return apiResponse;
	}

	public void setApiResponse(String apiResponse) {
		this.apiResponse = apiResponse;
	}

	public boolean isSchema() {
		return isSchema;
	}

	public void setSchema(boolean isSchema) {
		this.isSchema = isSchema;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public Avatar getInstanceAvatar() {
		return instanceAvatar;
	}

	public void setInstanceAvatar(Avatar instanceAvatar) {
		this.instanceAvatar = instanceAvatar;
	}

	public String getVoiceMod() {
		if (voiceMod == null) {
			return "";
		}
		return voiceMod;
	}

	public void setVoiceMod(String voiceMod) {
		this.voiceMod = voiceMod;
	}

	public String getVoice() {
		return voice;
	}

	public void setVoice(String voice) {
		this.voice = voice;
	}

	public boolean getNativeVoice() {
		return nativeVoice;
	}

	public void setNativeVoice(boolean nativeVoice) {
		this.nativeVoice = nativeVoice;
	}

	public String getNativeVoiceName() {
		return nativeVoiceName;
	}

	public void setNativeVoiceName(String nativeVoiceName) {
		this.nativeVoiceName = nativeVoiceName;
	}

	public String getSpeechRate() {
		return speechRate;
	}

	public void setSpeechRate(String speechRate) {
		this.speechRate = speechRate;
	}

	public String getPitch() {
		return pitch;
	}

	public void setPitch(String pitch) {
		this.pitch = pitch;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public int getWins() {
		return wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public int getLosses() {
		return losses;
	}

	public void setLosses(int losses) {
		this.losses = losses;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getMemoryLimit() {
		return memoryLimit;
	}

	public void setMemoryLimit(int memoryLimit) {
		this.memoryLimit = memoryLimit;
	}

	public int getMemorySize() {
		return memorySize;
	}

	public void setMemorySize(int memorySize) {
		this.memorySize = memorySize;
	}

	@Override
	public String getTypeName() {
		return "Bot";
	}

	public String getDatabaseName() {
		return Site.PERSISTENCE_UNIT + "_" + databaseId;
	}

	public Long getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(Long databaseId) {
		this.databaseId = databaseId;
	}

	public String getForwarderAddress() {
		return "/bot?instance=" + getId() + "&dynamicChat=true";
	}
			
	public InstanceConfig buildConfig() {
		InstanceConfig config = new InstanceConfig();
		toConfig(config);
		config.size = String.valueOf(this.memorySize);
		config.allowForking = this.allowForking;
		if (!getAdmins().isEmpty()) {
			config.admin = getAdmins().get(0).getUserId();
		}
		config.lastConnectedUser = this.lastConnectedUser;
		config.hasAPI = hasAPI();
		config.isArchived = this.archived;
		config.wins = String.valueOf(this.wins);
		config.losses = String.valueOf(this.losses);
		config.rank = String.valueOf(this.rank);
		if (getInstanceAvatar() != null) {
			config.instanceAvatar = String.valueOf(getInstanceAvatar().getId());
		}
		return config;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void preDelete(EntityManager em) {
		super.preDelete(em);
		Query query = em.createQuery("Select p from ChatChannel p where p.bot = :bot");
		query.setParameter("bot", detach());
		List<ChatChannel> channels = query.getResultList();
		for (ChatChannel channel : channels) {
			channel.setBot(null);
		}
		query = em.createQuery("Delete from BotAttachment p where p.bot = :bot");
		query.setParameter("bot", detach());
		query.executeUpdate();
	}
}
