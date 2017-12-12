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

import java.util.Arrays;

import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetLearningAction;
import org.botlibre.sdk.activity.actions.HttpSaveLearningAction;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.LearningConfig;

import org.botlibre.sdk.R;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Activity for administering a bot's learning.
 */
public class LearningActivity extends LibreActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);
        
        HttpGetImageAction.fetchImage(this, MainActivity.instance.avatar, findViewById(R.id.icon));
        
        HttpGetLearningAction action = new HttpGetLearningAction(this, (InstanceConfig)MainActivity.instance.credentials());
    	action.execute();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetView() {
		LearningConfig learning = MainActivity.learning;
		
		Spinner spin = (Spinner)findViewById(R.id.learningModeSpin);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.learningModes);
		spin.setAdapter(adapter);
		spin.setSelection(Arrays.asList(MainActivity.learningModes).indexOf(MainActivity.learning.learningMode));

		spin = (Spinner)findViewById(R.id.correctionModeSpin);
		adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.correctionModes);
		spin.setAdapter(adapter);
		spin.setSelection(Arrays.asList(MainActivity.correctionModes).indexOf(MainActivity.learning.correctionMode));

		EditText text = (EditText)findViewById(R.id.learningRateText);
		text.setText(learning.learningRate);
		text = (EditText)findViewById(R.id.scriptTimeoutText);
		text.setText(String.valueOf(learning.scriptTimeout));
		text = (EditText)findViewById(R.id.responseTimeoutText);
		text.setText(String.valueOf(learning.responseMatchTimeout));
		text = (EditText)findViewById(R.id.conversationMatchText);
		text.setText(learning.conversationMatchPercentage);
		text = (EditText)findViewById(R.id.discussionMatchText);
		text.setText(learning.discussionMatchPercentage);
		
		CheckBox checkbox = (CheckBox)findViewById(R.id.emotingCheckBox);
		checkbox.setChecked(learning.enableEmoting);
		checkbox = (CheckBox)findViewById(R.id.emotionsCheckBox);
		checkbox.setChecked(learning.enableEmotions);
		checkbox = (CheckBox)findViewById(R.id.comprehensionCheckBox);
		checkbox.setChecked(learning.enableComprehension);
		checkbox = (CheckBox)findViewById(R.id.consciousnessCheckBox);
		checkbox.setChecked(learning.enableConsciousness);
		checkbox = (CheckBox)findViewById(R.id.wiktionaryCheckBox);
		checkbox.setChecked(learning.enableWiktionary);
		checkbox = (CheckBox)findViewById(R.id.responseMatchingCheckBox);
		checkbox.setChecked(learning.enableResponseMatch);
		checkbox = (CheckBox)findViewById(R.id.checkEactMatchFirstCheckBox);
		checkbox.setChecked(learning.checkExactMatchFirst);
		checkbox = (CheckBox)findViewById(R.id.fixTemplateCaseCheckBox);
		checkbox.setChecked(learning.fixFormulaCase);
		checkbox = (CheckBox)findViewById(R.id.learnGrammarCheckBox);
		checkbox.setChecked(learning.learnGrammar);
		checkbox = (CheckBox)findViewById(R.id.synthsizeResponseCheckBox);
		checkbox.setChecked(learning.synthesizeResponse);
	}

	public void save(View view) {
		LearningConfig config = new LearningConfig();
        config.instance = MainActivity.instance.id;
        
        Spinner spin = (Spinner) findViewById(R.id.learningModeSpin);
        config.learningMode = spin.getSelectedItem().toString();
        spin = (Spinner) findViewById(R.id.correctionModeSpin);
        config.correctionMode = spin.getSelectedItem().toString();

		EditText text = (EditText)findViewById(R.id.learningRateText);
		config.learningRate = text.getText().toString();
		text = (EditText)findViewById(R.id.scriptTimeoutText);
		config.scriptTimeout = Integer.valueOf(text.getText().toString());
		text = (EditText)findViewById(R.id.responseTimeoutText);
		config.responseMatchTimeout = Integer.valueOf(text.getText().toString());
		text = (EditText)findViewById(R.id.conversationMatchText);
		config.conversationMatchPercentage = text.getText().toString();
		text = (EditText)findViewById(R.id.discussionMatchText);
		config.discussionMatchPercentage = text.getText().toString();
		
		CheckBox checkbox = (CheckBox)findViewById(R.id.emotingCheckBox);
		config.enableEmoting = checkbox.isChecked();
		checkbox = (CheckBox)findViewById(R.id.emotionsCheckBox);
		config.enableEmotions = checkbox.isChecked();
		checkbox = (CheckBox)findViewById(R.id.comprehensionCheckBox);
		config.enableComprehension = checkbox.isChecked();
		checkbox = (CheckBox)findViewById(R.id.consciousnessCheckBox);
		config.enableConsciousness = checkbox.isChecked();
		checkbox = (CheckBox)findViewById(R.id.wiktionaryCheckBox);
		config.enableWiktionary = checkbox.isChecked();
		checkbox = (CheckBox)findViewById(R.id.responseMatchingCheckBox);
		config.enableResponseMatch = checkbox.isChecked();
		checkbox = (CheckBox)findViewById(R.id.checkEactMatchFirstCheckBox);
		config.checkExactMatchFirst = checkbox.isChecked();
		checkbox = (CheckBox)findViewById(R.id.fixTemplateCaseCheckBox);
		config.fixFormulaCase = checkbox.isChecked();
		checkbox = (CheckBox)findViewById(R.id.learnGrammarCheckBox);
		config.learnGrammar = checkbox.isChecked();
		checkbox = (CheckBox)findViewById(R.id.synthsizeResponseCheckBox);
		config.synthesizeResponse = checkbox.isChecked();
        
        HttpSaveLearningAction action = new HttpSaveLearningAction(this, config);
		action.execute();
	}
}
