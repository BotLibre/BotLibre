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

package org.botlibre.sdk.activity.issuetracker;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetTagsAction;
import org.botlibre.sdk.activity.actions.HttpUpdateIssueAction;
import org.botlibre.sdk.config.IssueConfig;
import org.botlibre.sdk.config.IssueTrackerConfig;

import java.util.Arrays;

import org.botlibre.offline.pizzabot.R;

/**
 * Activity for editing an issue.
 */
public class EditIssueActivity extends IssueEditorActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_issue);

		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
				1);
        
        resetView();
	}

	public void resetView() {
        HttpGetImageAction.fetchImage(this, MainActivity.issue.avatar, findViewById(R.id.icon));

    	HttpAction action = new HttpGetTagsAction(this, "Issue");
    	action.execute();
		
		IssueConfig instance = MainActivity.issue;
        
        AutoCompleteTextView tagsText = (AutoCompleteTextView)findViewById(R.id.tagsText);
        tagsText.setText(instance.tags);
    	
        EditText text = (EditText) findViewById(R.id.titleText);
        text.setText(instance.title);
        text = (EditText) findViewById(R.id.detailsText);
        text.setText(instance.details);

        Spinner spin = (Spinner) findViewById(R.id.prioritySpin);
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.priorities);
        spin.setAdapter(adapter);
        spin.setSelection(Arrays.asList(MainActivity.priorities).indexOf(instance.priority));

		//CheckBox checkbox = (CheckBox) findViewById(R.id.isPriorityCheckBox);
		//checkbox.setChecked(instance.isPriority);
        
        if (!(MainActivity.instance instanceof IssueTrackerConfig) || !MainActivity.instance.isAdmin) {
        	//findViewById(R.id.isPriorityCheckBox).setVisibility(View.GONE);
        }
	}
    
    /**
     * Create the instance.
     */
    public void save(View view) {
        IssueConfig config = new IssueConfig();
    	saveProperties(config);
		
    	HttpAction action = new HttpUpdateIssueAction(
        		this, 
        		config);
        action.execute();
    }

    public void saveProperties(IssueConfig config) {
        EditText text = (EditText) findViewById(R.id.titleText);
        config.title = text.getText().toString().trim();

        text = (EditText) findViewById(R.id.detailsText);
        config.details = text.getText().toString().trim();

    	text = (EditText) findViewById(R.id.tagsText);
    	config.tags = text.getText().toString().trim();

        Spinner spin = (Spinner) findViewById(R.id.prioritySpin);
        config.priority = (String)spin.getSelectedItem();

		//CheckBox checkbox = (CheckBox) findViewById(R.id.isPriorityCheckBox);
		//config.isPriority = checkbox.isChecked();

		config.id = MainActivity.issue.id;
		config.tracker = MainActivity.issue.tracker;
    }

    public void cancel(View view) {        
    	finish();
    }
}
