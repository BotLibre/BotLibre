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

package org.botlibre.sdk.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpDeleteResponseAction;
import org.botlibre.sdk.activity.actions.HttpGetConversationsAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.activity.actions.HttpGetResponsesAction;
import org.botlibre.sdk.activity.actions.HttpSaveResponseAction;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ConversationConfig;
import org.botlibre.sdk.config.InputConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.ResponseConfig;
import org.botlibre.sdk.config.ResponseSearchConfig;
import org.botlibre.sdk.config.ScriptConfig;

import org.botlibre.sdk.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;

/**
 * Activity for training a bot's responses.
 */
public class TrainingActivity extends LibreActivity {
	
	protected InstanceConfig instance;
	protected List<ResponseConfig> responses = new ArrayList<ResponseConfig>();
	protected List<ConversationConfig> conversations = new ArrayList<ConversationConfig>();
	@SuppressWarnings("rawtypes")
	protected List conversationInput = new ArrayList();
	protected boolean first = true;
	protected int page = 0;
			
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        
        HttpGetImageAction.fetchImage(this, MainActivity.instance.avatar, findViewById(R.id.icon));

		Spinner spin = (Spinner) findViewById(R.id.responseTypeSpin);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.responseTypes);
		spin.setAdapter(adapter);
		spin.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView parentView, View view, int position, long id) {
		    	if (!first) {
		    		search(view);
		    	}
		    	first = false;
		    }
		    @Override
		    public void onNothingSelected(AdapterView parentView) {
		        
		    }
		});

		ListView list = (ListView) findViewById(R.id.responseList);
		GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTapEvent(MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					ListView list = (ListView) findViewById(R.id.responseList);
			        int index = list.getCheckedItemPosition();
			        if (index < 0) {
						return false;
			        } else {
			        	edit(list);
			        }
					return true;
				}
				return false;
			}
		};
		final GestureDetector listDetector = new GestureDetector(this, listener);
		list.setOnTouchListener(new View.OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return listDetector.onTouchEvent(event);
			}
		});

		spin = (Spinner) findViewById(R.id.durationSpin);
		adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.durations);
		spin.setAdapter(adapter);

		spin = (Spinner) findViewById(R.id.typeSpin);
		adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.inputTypes);
		spin.setAdapter(adapter);

		spin = (Spinner) findViewById(R.id.restrictSpin);
		adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.responseRestrictions);
		spin.setAdapter(adapter);
		searchOptions(null);

        this.instance = (InstanceConfig)MainActivity.instance;
	}
	
	public List<ResponseConfig> getResponses() {
		return responses;
	}

	@SuppressWarnings("rawtypes")
	public void setResponses(List<ResponseConfig> responses) {
		this.responses = responses;
		this.conversations = new ArrayList<ConversationConfig>();
		this.conversationInput = new ArrayList();
	}

	public List<ConversationConfig> getConversations() {
		return conversations;
	}

	@SuppressWarnings("rawtypes")
	public List getConversationInput() {
		return conversationInput;
	}

	@SuppressWarnings("rawtypes")
	public void setConversationInput(List conversationInput) {
		this.conversationInput = conversationInput;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setConversations(List<ConversationConfig> conversations) {
		this.responses = new ArrayList<ResponseConfig>();
		this.conversations = conversations;
		this.conversationInput = new ArrayList();
		for (ConversationConfig conversation : conversations) {
			this.conversationInput.add(conversation);
			if (conversation.input != null) {
				for (InputConfig input : conversation.input) {
					this.conversationInput.add(input);
				}
			}
		}
		
	}
	
	@Override
	public void onResume() {
		if (MainActivity.instance instanceof InstanceConfig && MainActivity.instance.id.equals(this.instance.id)) {
			this.instance = (InstanceConfig)MainActivity.instance;
		} else {
			MainActivity.instance = this.instance;
		}
		MainActivity.browsing = false;
		MainActivity.importingBotLog = false;
		ResponseConfig response = HttpSaveResponseAction.response;
		if (response != null) {
			HttpSaveResponseAction.response = null;
			Spinner spin = (Spinner) findViewById(R.id.responseTypeSpin);
			String responseType = (String)spin.getSelectedItem();
			if ((response.type != null && response.type.equals("greeting") && !responseType.equals("greetings"))) {
				spin.setSelection(Arrays.asList(MainActivity.responseTypes).indexOf("greetings"));
			} else if (response.type != null && response.type.equals("default") && !responseType.equals("default")) {
				spin.setSelection(Arrays.asList(MainActivity.responseTypes).indexOf("default"));
			} else if (responseType.equals("flagged")) {
				this.first = true;
				spin.setSelection(Arrays.asList(MainActivity.responseTypes).indexOf("responses"));
				this.responses = new ArrayList<ResponseConfig>();
				resetView(response);
			} else if (responseType.equals("default") && (response.type == null || !response.type.equals("default"))) {
				this.first = true;
				spin.setSelection(Arrays.asList(MainActivity.responseTypes).indexOf("responses"));
				this.responses = new ArrayList<ResponseConfig>();
				resetView(response);
			} else if (responseType.equals("greetings") && (response.type == null || !response.type.equals("greeting"))) {
				this.first = true;
				spin.setSelection(Arrays.asList(MainActivity.responseTypes).indexOf("responses"));
				this.responses = new ArrayList<ResponseConfig>();
				resetView(response);
			} else if (responseType.equals("conversations")) {
				// na
			} else{
				resetView(response);
			}
		} else {
			resetView();
		}
		super.onResume();
	}

	public void resetView(ResponseConfig config) {
		int index = this.responses.indexOf(config);
		if (index == -1) {
			index = 0;
		}
		if (HttpSaveResponseAction.oldResponse != null) {
			this.responses.remove(HttpSaveResponseAction.oldResponse);
		}
		this.responses.remove(config);
		Spinner spin = (Spinner) findViewById(R.id.responseTypeSpin);
		String responseType = (String)spin.getSelectedItem();
		if (config.type == null || !responseType.equals("flagged") || !(config.type.equals("unflag"))) {
			this.responses.add(index, config);
		}
		resetView();
	}

	public void resetView() {
		View next = (View) findViewById(R.id.nextButton);
		Spinner spin = (Spinner) findViewById(R.id.responseTypeSpin);
		String responseType = (String)spin.getSelectedItem();
		if (responseType.equals("conversations")) {
			ListView list = (ListView) findViewById(R.id.responseList);
			ConversationListAdapter adapter = new ConversationListAdapter(this, R.layout.conversation_list, this.conversationInput);
			list.setAdapter(adapter);
			if (this.conversations.size() >= 50 || this.page > 0) {
				if (this.conversations.size() >= 50) {
					next.setVisibility(View.VISIBLE);
				} else {
					next.setVisibility(View.GONE);
				}
			} else {
				next.setVisibility(View.GONE);
			}
		} else {
			ListView list = (ListView) findViewById(R.id.responseList);
			ResponseListAdapter adapter = new ResponseListAdapter(this, R.layout.response_list, this.responses);
			list.setAdapter(adapter);
			if (this.responses.size() >= 100 || this.page > 0) {
				if (this.responses.size() >= 100) {
					next.setVisibility(View.VISIBLE);
				} else {
					next.setVisibility(View.GONE);
				}
			} else {
				next.setVisibility(View.GONE);
			}
		}
		View previous = (View) findViewById(R.id.previousButton);
		if (this.page > 0) {
			previous.setVisibility(View.VISIBLE);
		} else {
			previous.setVisibility(View.GONE);
		}
	}

	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.layout.menu_training, popup.getMenu());
	    onPrepareOptionsMenu(popup.getMenu());
	    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
	        @Override
	        public boolean onMenuItemClick(MenuItem item) {
	            return onOptionsItemSelected(item);
	        }
	    });
	    popup.show();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.menu_training, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
	    case R.id.menuAddResponse:
	    	addResponse();
	        return true;
	    case R.id.menuAddGreeting:
	    	addGreeting();
	        return true;
	    case R.id.menuAddDefaultResponse:
	    	addDefaultResponse();
	        return true;
	    case R.id.menuImport:
	    	importLog(null);
	        return true;
        case R.id.menuEdit:
        	edit();
            return true;
        case R.id.menuBrowse:
        	browse();
            return true;
        case R.id.menuValidate:
        	validate();
            return true;
        case R.id.menuInvalidate:
        	invalidate();
            return true;
        case R.id.menuFlag:
        	flag();
            return true;
        case R.id.menuUnflag:
        	unflag();
            return true;
        case R.id.menuDelete:
        	delete();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	public void delete(View view) {
		delete();
	}
	
	public ResponseConfig getSelectedResponse() {
		Spinner spin = (Spinner) findViewById(R.id.responseTypeSpin);
		String type = (String)spin.getSelectedItem();
        ListView list = (ListView) findViewById(R.id.responseList);
        int index = list.getCheckedItemPosition();
		if (index < 0) {
			return null;
		}
		
		if (type.equals("conversations")) {
			Object selected = this.conversationInput.get(index);
			if (selected instanceof ConversationConfig) {
				return null;
			} else {
				ResponseConfig response = new ResponseConfig();
				response.response = ((InputConfig)selected).value;
				if (index > 1) {
					Object previous = this.conversationInput.get(index - 1);
					if (previous instanceof InputConfig) {
						response.question = ((InputConfig)previous).value;
					}
				}
				return response;
			}
		} else {
			return this.responses.get(index);
		}
	}
	
	public void delete() {
		ResponseConfig config = new ResponseConfig();
        config.instance = MainActivity.instance.id;

		Spinner spin = (Spinner) findViewById(R.id.responseTypeSpin);
		config.type = (String)spin.getSelectedItem();
		if (config.type.equals("conversations")) {
			config.type = "conversation";
		} else if (config.type.equals("greetings")) {
			config.type = "greeting";
		} else if (config.type.equals("responses")) {
			config.type = "response";
		} else if (config.type.equals("flagged")) {
			unflag();
			return;
		}
        ListView list = (ListView) findViewById(R.id.responseList);
        int index = list.getCheckedItemPosition();
		if (index < 0) {
			MainActivity.error("Select response to remove", null, this);
			return;
		}
		if (config.type.equals("conversation")) {
			Object selected = this.conversationInput.get(index);
			if (selected instanceof ConversationConfig) {
				config.responseId = ((ConversationConfig)selected).id;
			} else {
				config.responseId = ((InputConfig)selected).id;
			}
		} else {
			ResponseConfig selected = this.responses.get(index);
			config.responseId = selected.responseId;
			config.questionId = selected.questionId;
		}
        
		HttpAction action = new HttpDeleteResponseAction(this, config);
		action.execute();
	}

	public void addResponse() {
		ResponseConfig config = new ResponseConfig();
		add(config);
	}

	public void addGreeting() {
		ResponseConfig config = new ResponseConfig();
		config.type = "greeting";
		add(config);
	}

	public void addDefaultResponse() {
		ResponseConfig config = new ResponseConfig();
		config.type = "default";
		add(config);
	}

	public void add(View view) {
		ResponseConfig config = new ResponseConfig();

		Spinner spin = (Spinner) findViewById(R.id.responseTypeSpin);
		String type = (String)spin.getSelectedItem();
		if (type.equals("default")) {
			config.type = "default";
		} else if (type.equals("greetings")) {
			config.type = "greeting";
		}
		add(config);
	}
	
	public void add(ResponseConfig config) {
        MainActivity.response = config;
		
        Intent intent = new Intent(this, ResponseActivity.class);		
        startActivity(intent);
	}

	public void edit(View view) {
		edit();
	}

	public void edit() {
        MainActivity.response = getSelectedResponse();
        if (MainActivity.response == null) {
			MainActivity.error("Select a response to edit", null, this);
        	return;
        }
		
        Intent intent = new Intent(this, ResponseActivity.class);		
        startActivity(intent);
	}
	
	public void importLog(View view) {
		MainActivity.browsing = true;
		MainActivity.importingBotLog = true;
		
		BrowseConfig config = new BrowseConfig();
		config.type = "Script";
		config.typeFilter = "Featured";

		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}

	public void validate() {
        save("validate");
	}

	public void invalidate() {
        save("invalidate");
	}

	public void flag() {
        save("flag");
	}

	public void unflag() {
        save("unflag");
	}

	public void save(String type) {
		ResponseConfig config = getSelectedResponse();
        if (config == null) {
			MainActivity.error("Select a response to " + type, null, this);
        	return;
        }
        config.instance = MainActivity.instance.id;
		config.type = type;
        
		HttpAction action = new HttpSaveResponseAction(this, config);
		action.execute();
	}
	
	public void search(View view) {
		ResponseSearchConfig config = new ResponseSearchConfig();
		config.instance = this.instance.id;

		Spinner spin = (Spinner) findViewById(R.id.responseTypeSpin);
		config.responseType = (String)spin.getSelectedItem();

		spin = (Spinner) findViewById(R.id.durationSpin);
		config.duration = (String)spin.getSelectedItem();

		spin = (Spinner) findViewById(R.id.typeSpin);
		config.inputType = (String)spin.getSelectedItem();

		spin = (Spinner) findViewById(R.id.restrictSpin);
		config.restrict = (String)spin.getSelectedItem();

		EditText text = (EditText) findViewById(R.id.filterText);
		config.filter = text.getText().toString();
		
		this.page = 0;

		if (config.responseType.equals("conversations")) {
		    HttpAction action = new HttpGetConversationsAction(this, config);
			action.execute();
		} else {
		    HttpAction action = new HttpGetResponsesAction(this, config);
			action.execute();
		}
	}
	
	public void nextPage(View view) {
		ResponseSearchConfig config = new ResponseSearchConfig();
		config.instance = this.instance.id;

		Spinner spin = (Spinner) findViewById(R.id.responseTypeSpin);
		config.responseType = (String)spin.getSelectedItem();

		spin = (Spinner) findViewById(R.id.durationSpin);
		config.duration = (String)spin.getSelectedItem();

		spin = (Spinner) findViewById(R.id.typeSpin);
		config.inputType = (String)spin.getSelectedItem();

		spin = (Spinner) findViewById(R.id.restrictSpin);
		config.restrict = (String)spin.getSelectedItem();

		EditText text = (EditText) findViewById(R.id.filterText);
		config.filter = text.getText().toString();
		
		this.page++;
		config.page = String.valueOf(this.page);

		if (config.responseType.equals("conversations")) {
		    HttpAction action = new HttpGetConversationsAction(this, config);
			action.execute();
		} else {
		    HttpAction action = new HttpGetResponsesAction(this, config);
			action.execute();
		}
	}
	
	public void previousPage(View view) {
		ResponseSearchConfig config = new ResponseSearchConfig();
		config.instance = this.instance.id;

		Spinner spin = (Spinner) findViewById(R.id.responseTypeSpin);
		config.responseType = (String)spin.getSelectedItem();

		spin = (Spinner) findViewById(R.id.durationSpin);
		config.duration = (String)spin.getSelectedItem();

		spin = (Spinner) findViewById(R.id.typeSpin);
		config.inputType = (String)spin.getSelectedItem();

		spin = (Spinner) findViewById(R.id.restrictSpin);
		config.restrict = (String)spin.getSelectedItem();

		EditText text = (EditText) findViewById(R.id.filterText);
		config.filter = text.getText().toString();
		
		this.page--;
		config.page = String.valueOf(this.page);

		if (config.responseType.equals("conversations")) {
		    HttpAction action = new HttpGetConversationsAction(this, config);
			action.execute();
		} else {
		    HttpAction action = new HttpGetResponsesAction(this, config);
			action.execute();
		}
	}
	
	public void browse() {
		ResponseConfig response = getSelectedResponse();
		if (response == null) {
			MainActivity.error("Select a response browse", null, this);
        	return;
		}
		ResponseSearchConfig config = new ResponseSearchConfig();
		config.instance = this.instance.id;
		config.responseType = "responses";
		config.duration = "all";
		config.inputType = "all";
		config.restrict = "exact";
		config.filter = response.response;

		this.first = true;
		Spinner spin = (Spinner) findViewById(R.id.responseTypeSpin);
		spin.setSelection(Arrays.asList(MainActivity.responseTypes).indexOf("responses"));
		this.responses = new ArrayList<ResponseConfig>();
		
	    HttpAction action = new HttpGetResponsesAction(this, config);
		action.execute();
	}

	public void searchOptions(View view) {
		View searchView = findViewById(R.id.searchView);
		if (searchView.getVisibility() == View.GONE) {
			searchView.setVisibility(View.VISIBLE);
		} else {
			searchView.setVisibility(View.GONE);
		}
	}
}
