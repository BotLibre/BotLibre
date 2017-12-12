package org.botlibre.sdk.activity.issuetracker;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.botlibre.sdk.activity.LibreActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpCreateIssueAction;
import org.botlibre.sdk.activity.actions.HttpGetTagsAction;
import org.botlibre.sdk.config.IssueConfig;

import org.botlibre.sdk.R;

/**
 * Safety issue main screen.
 */
public class CreateIssueActivity extends IssueEditorActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_issue);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        HttpAction action = new HttpGetTagsAction(this, "Issue");
        action.execute();

        Spinner spin = (Spinner) findViewById(R.id.prioritySpin);
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.priorities);
        spin.setAdapter(adapter);
	}

    /**
     * Create the instance.
     */
    public void create(View view) {
        IssueConfig config = new IssueConfig();
        saveProperties(config);
        config.tracker = MainActivity.instance.id;

        HttpAction action = new HttpCreateIssueAction(
                this,
                config);
        action.execute();
    }

    public void saveProperties(IssueConfig instance) {
        EditText text = (EditText) findViewById(R.id.titleText);
        instance.title = text.getText().toString().trim();

        text = (EditText) findViewById(R.id.tagsText);
        instance.tags = text.getText().toString().trim();

        Spinner spin = (Spinner) findViewById(R.id.prioritySpin);
        instance.priority = (String)spin.getSelectedItem();

        text = (EditText) findViewById(R.id.detailsText);
        instance.details = text.getText().toString().trim();
    }
}
