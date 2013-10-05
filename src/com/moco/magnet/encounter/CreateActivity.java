package com.moco.magnet.encounter;

import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class CreateActivity extends Activity {
	int rand_num = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create);
		
		Random r = new Random();
		rand_num = r.nextInt(1001);
		String new_code = String.format("%04d", rand_num);
		
		TextView code = (TextView) CreateActivity.this.findViewById(R.id.new_pin);
		code.setText(new_code);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
