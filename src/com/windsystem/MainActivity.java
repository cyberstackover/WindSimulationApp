package com.windsystem;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import com.windsystem.*;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class MainActivity extends Activity {

	private RadioGroup orientation;
	private Button kirim;
	private TextView curtxt,tingtxt;
	private SeekBar cur,ting;
	private int day=1,flag=0,status1;
	private float status2;
	private static final String TAG = "Hujan Indah";
	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	

	private static String address = "20:16:D8:D3:EA:AE";//laptop herwin
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		lihatkondisiBT();
		
		cur = (SeekBar) findViewById(R.id.curah);
		ting = (SeekBar) findViewById(R.id.tinggi);
		curtxt = (TextView) findViewById(R.id.curahtxt);
		tingtxt = (TextView) findViewById(R.id.tinggitxt);
		orientation = (RadioGroup) findViewById(R.id.orientation);
		kirim = (Button) findViewById(R.id.Day);
		
		kirim.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
		    	day++;
		    	kirimdata("hari"+day+"#");
			}
		});
		
		orientation.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch(checkedId){
				case R.id.Surabaya: flag=1; break;
				case R.id.Malang: flag=2; break;
				case R.id.Lamongan: flag=3; break;
				case R.id.Jombang: flag=4; break;
				case R.id.Tuban: flag=5; break;
				}
			}
			
		});
		
		cur.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				status1 = seekBar.getProgress();
				curtxt.setText("Kecepatan Angin : "+status1+" knot");
				if(flag==1){
					kirimdata("ca"+status1+"#");
				}
				else if(flag==2){
					kirimdata("cb"+status1+"#");
				}
				else if(flag==3){
					kirimdata("cc"+status1+"#");
				}
				else if(flag==4){
					kirimdata("cd"+status1+"#");
				}
				else if(flag==5){
					kirimdata("ce"+status1+"#");
				}
				else 
					Toast.makeText(getApplicationContext(), "data salah!", Toast.LENGTH_SHORT).show();
			}
		});
		
		ting.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

		@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				status2 = seekBar.getProgress();
				tingtxt.setText("Kelembapan : "+status2/20.0+" gr/m3");
				if(flag==1){
					kirimdata("ta"+status2+"#");
				}
				else if(flag==2){
					kirimdata("tb"+status2+"#");
				}
				else if(flag==3){
					kirimdata("tc"+status2+"#");
				}
				else if(flag==4){
					kirimdata("td"+status2+"#");
				}
				else if(flag==5){
					kirimdata("te"+status2+"#");
				}
				else 
					Toast.makeText(getApplicationContext(), "data salah!", Toast.LENGTH_SHORT).show();
			
			}
		});
		
	}
	
	
	
	private void lihatkondisiBT(){
		if(btAdapter==null){
			errorkeluar("Fatal Error", "Bluetooth Not supported. Aborting.");
		}
		else{
			if(btAdapter.isEnabled()){
				Log.d(TAG, "...Bluetooth is enabled...");
			} else {
				Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
		
	}
	
	private void kirimdata(String message){
		byte[] msgBuffer = message.getBytes();
		Log.d(TAG, "...Send Data: " + message + "...");
		
		try{
			outStream.write(msgBuffer);
		} catch (IOException e){
		}
	}
	
	private void errorkeluar(String title, String message){
		Toast msg = Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_SHORT);
		msg.show();
		finish();
	}
	
	public void onPause(){
		super.onPause();
		
		Log.d(TAG, "...In onPause()...");
		
		if(outStream != null){
			try{
				outStream.flush();
			} catch (IOException e ){
				errorkeluar("Fatal Error","In onPause() and failed to flush output stream: "+ e.getMessage() +".");
			}
		}
		try {
			btSocket.close();
		} catch (IOException e2){
			errorkeluar("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
		}
	}
	
	public void onResume(){
		super.onResume();
		Log.d(TAG, "...In onResume - Attempting client connect...");
		
		BluetoothDevice device = btAdapter.getRemoteDevice(address);
		
		try{
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			errorkeluar("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
		}
		btAdapter.cancelDiscovery();
		
		Log.d(TAG, "...Connecting to Remote...");
		try{
			btSocket.connect();
			Log.d(TAG, "...Connection established and data link opened...");
		} catch (IOException e){
			try{
				btSocket.close();
			} catch (IOException e2) {
				errorkeluar("Fatal Error", "In onResume() and unable to close socket during "
						+ "connection failure" + e2.getMessage() +".");
			}
		}
		Log.d(TAG, "...Creating Socket...");
		
		try{
			outStream = btSocket.getOutputStream();
		} catch (IOException e){
			errorkeluar("Fatal Error", "In onResume() and output stream creation failed: " + 
					e.getMessage() + ".");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
