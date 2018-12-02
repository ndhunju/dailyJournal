package com.ndhunju.dailyjournal.controller.party;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.BaseActivity;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.model.Party.Type;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.Utils;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.io.File;

public class PartyActivity extends BaseActivity {

    private static final String TAG = PartyActivity.class.getSimpleName();
    private static final int REQUEST_IMAGE = 123;


    //Variables
    private ImageView partyPicIV;
    private Spinner typeSpinner;
	private Services mServices;
	private EditText phoneET;
    private EditText nameET;
    private EditText noteET;
	private Party mParty;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.activity_party);

		//Get the party id
		long partyId = getIntent().getLongExtra(Constants.KEY_PARTY_ID, Constants.ID_NEW_PARTY);
		mServices =  Services.getInstance(this);

		if(partyId == Constants.ID_NEW_PARTY)
        {mParty = new Party(getString(R.string.str_new), Constants.ID_NEW_PARTY);}
		else{mParty = mServices.getParty(partyId);}

        mParty.setTag(getIntent().getIntExtra(Constants.KEY_POS, -1));
		setTitle(mParty.getName());

		//Wire up widgets
		TextView idTV = (TextView)findViewById(R.id.activity_party_id_tv);
		idTV.setText(UtilsFormat.getStringId(mParty.getId(), UtilsFormat.NUM_OF_DIGITS));

        partyPicIV = (ImageView)findViewById(R.id.activity_party_pic_iv);
        //make the image circular
        if (!TextUtils.isEmpty(mParty.getPicturePath())) {
            RoundedBitmapDrawable bitmapDrawable = RoundedBitmapDrawableFactory.create(
                    getResources(),
                    mParty.getPicturePath());

            bitmapDrawable.setCircular(true);
            partyPicIV.setImageDrawable(bitmapDrawable);
        } else {
            partyPicIV.setImageResource(R.drawable.default_party_pic);
        }

        partyPicIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_IMAGE);
            }
        });


		nameET = (EditText)findViewById(R.id.activity_party_name_et);
		nameET.setText(mParty.getName());
		
		phoneET = (EditText)findViewById(R.id.activity_party_phone_et);
		phoneET.setText(mParty.getPhone());
		
		typeSpinner = (Spinner)findViewById(R.id.activity_party_type_spinner);

		String[] partyTypes = new String[Type.values().length];
		for(int i = 0; i < Type.values().length ; i++)
			partyTypes[i] = Type.values()[i].toString();

		typeSpinner.setAdapter(new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, partyTypes));
		typeSpinner.setSelection(mParty.getType().ordinal());

        noteET = (EditText) findViewById(R.id.activity_party_note_et);
        noteET.setText(mParty.getNote());
		
		Button okBtn = (Button)findViewById(R.id.activity_party_ok_btn);
		okBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mParty.setName(nameET.getText().toString());
                mParty.setNote(noteET.getText().toString());
				mParty.setPhone(phoneET.getText().toString());
				mParty.setType(Party.Type.valueOf(typeSpinner.getSelectedItem().toString()));

				if(mParty.getId() == Constants.ID_NEW_PARTY)
					mServices.addParty(mParty);
				else
					mServices.updateParty(mParty);

				finish();
			}
		});

        Button deleteBtn = (Button)findViewById(R.id.activity_party_delete_btn);
        deleteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                UtilsView.alert(PartyActivity.this,
                        getString(R.string.msg_delete_confirm, mParty.getName()), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mServices.deleteParty(mParty);
                                UtilsView.toast(PartyActivity.this, getString(R.string.msg_deleted, mParty.getName()));
                                finish();
                            }
                        }, null);

            }
        });

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(UtilsFormat.getPartyFromPref(this));
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case REQUEST_IMAGE:

                //If the result is not success, return
                if (resultCode != Activity.RESULT_OK){
                    UtilsView.alert(PartyActivity.this, String.format(getString(R.string.msg_failed), getString(R.string.str_save)));
                    return;
                }

                Uri selectedImage = data.getData();
                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    bitmap = Utils.getResizedBitmap(bitmap, partyPicIV.getHeight(), partyPicIV.getWidth());
                } catch (Exception e) { Log.d(TAG, "couldn't load selected image");
                    return;
                }

                File picFile = UtilsFile.getPartyPicture(mParty, PartyActivity.this);
                UtilsFile.storeImage(bitmap, picFile, PartyActivity.this);
                mParty.setPicturePath(picFile.getAbsolutePath());
                partyPicIV.setImageDrawable(Drawable.createFromPath(mParty.getPicturePath()));
                partyPicIV.invalidate();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
