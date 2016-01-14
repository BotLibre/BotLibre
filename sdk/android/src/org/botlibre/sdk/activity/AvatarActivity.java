package org.botlibre.sdk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.actions.HttpAddAvatarAction;
import org.botlibre.sdk.activity.actions.HttpDeleteAllAvatarsAction;
import org.botlibre.sdk.activity.actions.HttpDeleteAvatarAction;
import org.botlibre.sdk.activity.actions.HttpGetAvatarsAction;
import org.botlibre.sdk.activity.actions.HttpTagAvatarAction;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.InstanceConfig;

/**
 * Activity for adding and tagging a bot's avatars.
 */
public class AvatarActivity extends Activity {
	AvatarConfig avatar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setTitle("Avatar: " + MainActivity.instance.name);
		
        resetView();
	}
	
	@Override
	public void onResume() {		
        resetView();
        
        super.onResume();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.menu_avatar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
         
        switch (item.getItemId())
        {
        case R.id.menuAdd:
        	addAvatar();
            return true;
 
        case R.id.menuUpload:
        	uploadImage();
            return true;
 
        case R.id.menuDelete:
        	delete();
            return true;
            
        case R.id.menuDeleteAll:
        	deleteAll();
            return true;
 
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	public void resetView() {
        setContentView(R.layout.activity_avatar);
		
        resetAvatars();
	}
	
	public void resetAvatars() {        
        HttpGetAvatarsAction action = new HttpGetAvatarsAction(this, (InstanceConfig)MainActivity.instance.credentials());
		action.execute();
	}
	
	/**
	 * Tag the image.
	 */
	public void tag(View view) {
        ListView list = (ListView) findViewById(R.id.avatarsList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select an image to tag", this);
        	return;
        }
        this.avatar = MainActivity.avatars.get(index);
        this.avatar.instance = MainActivity.instance.id;
        
        CheckBox checkbox = (CheckBox) findViewById(R.id.defaultCheckBox);
        this.avatar.isDefault = checkbox.isChecked();
        checkbox = (CheckBox) findViewById(R.id.noneCheckBox);
        this.avatar.none = checkbox.isChecked();

        checkbox = (CheckBox) findViewById(R.id.ecstaticCheckBox);
        this.avatar.ecstatic = checkbox.isChecked();
        checkbox = (CheckBox) findViewById(R.id.happyCheckBox);
        this.avatar.happy = checkbox.isChecked();
        checkbox = (CheckBox) findViewById(R.id.sadCheckBox);
        this.avatar.sad = checkbox.isChecked();
        checkbox = (CheckBox) findViewById(R.id.cryingCheckBox);
        this.avatar.crying = checkbox.isChecked();

        checkbox = (CheckBox) findViewById(R.id.loveCheckBox);
        this.avatar.love = checkbox.isChecked();
        checkbox = (CheckBox) findViewById(R.id.likeCheckBox);
        this.avatar.like = checkbox.isChecked();
        checkbox = (CheckBox) findViewById(R.id.dislikeCheckBox);
        this.avatar.dislike = checkbox.isChecked();
        checkbox = (CheckBox) findViewById(R.id.hateCheckBox);
        this.avatar.hate = checkbox.isChecked();

        checkbox = (CheckBox) findViewById(R.id.sereneCheckBox);
        this.avatar.serene = checkbox.isChecked();
        checkbox = (CheckBox) findViewById(R.id.calmCheckBox);
        this.avatar.calm = checkbox.isChecked();
        checkbox = (CheckBox) findViewById(R.id.angerCheckBox);
        this.avatar.anger = checkbox.isChecked();
        checkbox = (CheckBox) findViewById(R.id.rageCheckBox);
        this.avatar.rage = checkbox.isChecked();

        checkbox = (CheckBox) findViewById(R.id.courageousCheckBox);
        this.avatar.courageous = checkbox.isChecked();
        checkbox = (CheckBox) findViewById(R.id.confidentCheckBox);
        this.avatar.confident = checkbox.isChecked();
        checkbox = (CheckBox) findViewById(R.id.afraidCheckBox);
        this.avatar.afraid = checkbox.isChecked();
        checkbox = (CheckBox) findViewById(R.id.panicCheckBox);
        this.avatar.panic = checkbox.isChecked();

        checkbox = (CheckBox) findViewById(R.id.surpriseCheckBox);
        this.avatar.surprise = checkbox.isChecked();
        checkbox = (CheckBox) findViewById(R.id.boredCheckBox);
        this.avatar.bored = checkbox.isChecked();
        
        checkbox = (CheckBox) findViewById(R.id.laughterCheckBox);
        this.avatar.laughter = checkbox.isChecked();
        checkbox = (CheckBox) findViewById(R.id.seriousCheckBox);
        this.avatar.serious = checkbox.isChecked();
        
        HttpTagAvatarAction action = new HttpTagAvatarAction(this, this.avatar);
		action.execute();
	}

	public void resetTags() {
        ListView list = (ListView) findViewById(R.id.avatarsList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {            
            CheckBox checkbox = (CheckBox) findViewById(R.id.defaultCheckBox);
            checkbox.setChecked(false);
            checkbox = (CheckBox) findViewById(R.id.noneCheckBox);
            checkbox.setChecked(false);

            checkbox = (CheckBox) findViewById(R.id.ecstaticCheckBox);
            checkbox.setChecked(false);
            checkbox = (CheckBox) findViewById(R.id.happyCheckBox);
            checkbox.setChecked(false);
            checkbox = (CheckBox) findViewById(R.id.sadCheckBox);
            checkbox.setChecked(false);
            checkbox = (CheckBox) findViewById(R.id.cryingCheckBox);
            checkbox.setChecked(false);

            checkbox = (CheckBox) findViewById(R.id.loveCheckBox);
            checkbox.setChecked(false);
            checkbox = (CheckBox) findViewById(R.id.likeCheckBox);
            checkbox.setChecked(false);
            checkbox = (CheckBox) findViewById(R.id.dislikeCheckBox);
            checkbox.setChecked(false);
            checkbox = (CheckBox) findViewById(R.id.hateCheckBox);
            checkbox.setChecked(false);

            checkbox = (CheckBox) findViewById(R.id.sereneCheckBox);
            checkbox.setChecked(false);
            checkbox = (CheckBox) findViewById(R.id.calmCheckBox);
            checkbox.setChecked(false);
            checkbox = (CheckBox) findViewById(R.id.angerCheckBox);
            checkbox.setChecked(false);
            checkbox = (CheckBox) findViewById(R.id.rageCheckBox);
            checkbox.setChecked(false);

            checkbox = (CheckBox) findViewById(R.id.courageousCheckBox);
            checkbox.setChecked(false);
            checkbox = (CheckBox) findViewById(R.id.confidentCheckBox);
            checkbox.setChecked(false);
            checkbox = (CheckBox) findViewById(R.id.afraidCheckBox);
            checkbox.setChecked(false);
            checkbox = (CheckBox) findViewById(R.id.panicCheckBox);
            checkbox.setChecked(false);

            checkbox = (CheckBox) findViewById(R.id.surpriseCheckBox);
            checkbox.setChecked(false);
            checkbox = (CheckBox) findViewById(R.id.boredCheckBox);
            checkbox.setChecked(false);
            
            checkbox = (CheckBox) findViewById(R.id.laughterCheckBox);
            checkbox.setChecked(false);
            checkbox = (CheckBox) findViewById(R.id.seriousCheckBox);
            checkbox.setChecked(false);
        	return;
        }
        this.avatar = MainActivity.avatars.get(index);
        
        CheckBox checkbox = (CheckBox) findViewById(R.id.defaultCheckBox);
        checkbox.setChecked(this.avatar.isDefault);
        checkbox = (CheckBox) findViewById(R.id.noneCheckBox);
        checkbox.setChecked(this.avatar.none);

        checkbox = (CheckBox) findViewById(R.id.ecstaticCheckBox);
        checkbox.setChecked(this.avatar.ecstatic);
        checkbox = (CheckBox) findViewById(R.id.happyCheckBox);
        checkbox.setChecked(this.avatar.happy);
        checkbox = (CheckBox) findViewById(R.id.sadCheckBox);
        checkbox.setChecked(this.avatar.sad);
        checkbox = (CheckBox) findViewById(R.id.cryingCheckBox);
        checkbox.setChecked(this.avatar.crying);

        checkbox = (CheckBox) findViewById(R.id.loveCheckBox);
        checkbox.setChecked(this.avatar.love);
        checkbox = (CheckBox) findViewById(R.id.likeCheckBox);
        checkbox.setChecked(this.avatar.like);
        checkbox = (CheckBox) findViewById(R.id.dislikeCheckBox);
        checkbox.setChecked(this.avatar.dislike);
        checkbox = (CheckBox) findViewById(R.id.hateCheckBox);
        checkbox.setChecked(this.avatar.hate);

        checkbox = (CheckBox) findViewById(R.id.sereneCheckBox);
        checkbox.setChecked(this.avatar.serene);
        checkbox = (CheckBox) findViewById(R.id.calmCheckBox);
        checkbox.setChecked(this.avatar.calm);
        checkbox = (CheckBox) findViewById(R.id.angerCheckBox);
        checkbox.setChecked(this.avatar.anger);
        checkbox = (CheckBox) findViewById(R.id.rageCheckBox);
        checkbox.setChecked(this.avatar.rage);

        checkbox = (CheckBox) findViewById(R.id.courageousCheckBox);
        checkbox.setChecked(this.avatar.courageous);
        checkbox = (CheckBox) findViewById(R.id.confidentCheckBox);
        checkbox.setChecked(this.avatar.confident);
        checkbox = (CheckBox) findViewById(R.id.afraidCheckBox);
        checkbox.setChecked(this.avatar.afraid);
        checkbox = (CheckBox) findViewById(R.id.panicCheckBox);
        checkbox.setChecked(this.avatar.panic);

        checkbox = (CheckBox) findViewById(R.id.surpriseCheckBox);
        checkbox.setChecked(this.avatar.surprise);
        checkbox = (CheckBox) findViewById(R.id.boredCheckBox);
        checkbox.setChecked(this.avatar.bored);
        
        checkbox = (CheckBox) findViewById(R.id.laughterCheckBox);
        checkbox.setChecked(this.avatar.laughter);
        checkbox = (CheckBox) findViewById(R.id.seriousCheckBox);
        checkbox.setChecked(this.avatar.serious);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		try {
			String file = MainActivity.getFilePathFromURI(this, data.getData());
			HttpAddAvatarAction action = new HttpAddAvatarAction(this, file, (InstanceConfig)MainActivity.instance.credentials());
			action.execute().get();
    		if (action.getException() != null) {
    			throw action.getException();
    		}
			resetView();
		} catch (Exception exception) {
			MainActivity.error(exception.getMessage(), exception, this);
			return;
		}
	}

	public void menu(View view) {
		openOptionsMenu();
	}

	public void uploadImage() {
		Intent upload = new Intent(Intent.ACTION_GET_CONTENT);
		upload.setType("image/*");
		startActivityForResult(upload, 1);
	}
	
	/**
	 * Delete the image.
	 */
	public void delete() {
        ListView list = (ListView) findViewById(R.id.avatarsList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select image", this);
        	return;
        }
        this.avatar = MainActivity.avatars.get(index);
        this.avatar.instance = MainActivity.instance.id;
        
        HttpDeleteAvatarAction action = new HttpDeleteAvatarAction(this, this.avatar);
		action.execute();
	}
	
	/**
	 * Delete all images.
	 */
	public void deleteAll() {        
        HttpDeleteAllAvatarsAction action = new HttpDeleteAllAvatarsAction(this, (InstanceConfig)MainActivity.instance.credentials());
		action.execute();
	}

	public void addAvatar() {
        Intent intent = new Intent(this, AddAvatarActivity.class);		
        startActivity(intent);
	}
	
}
