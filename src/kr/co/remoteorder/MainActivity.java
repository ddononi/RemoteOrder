package kr.co.remoteorder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends BaseActivity {
	private boolean isTwoClickBack = false;		// �ι� Ŭ������
	private int[] buttonId = {
			R.id.table1, R.id.table2, R.id.table3,
			R.id.table4, R.id.table5, R.id.table6,
			R.id.table7, R.id.table8, R.id.table9,
			R.id.table10, R.id.table11, R.id.table12
	};
	private Button[] btn = new Button[buttonId.length];
	private NumberFormat formatter;			//  ��ȭ ������ ���� ������
	
	private BluetoothAdapter mBluetoothAdapter = null;	// ������� �ƴ���
	private BluetoothDevice mBluetoothDevice = null;	// ������� ����̽�
	
	// ���� ������ Ŭ����
	private ConnectedThread mConnectedThread;
	private ConnectThread mConnectThread;	// 
	private AcceptThread mAcceptThread;
	
	private boolean isServer = false;	// ������ �������� üũ
	
	// ui �޽��� ������ ���� �ڵ鷯
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CHANGE_STATE:	// ���̺� ��ư ���� ����
				doChageState((String) msg.obj);
				break;
			case SHOW_TOAST: {	// �佺Ʈ ����
				Toast toast = Toast.makeText(MainActivity.this,
						(String) msg.obj, Toast.LENGTH_SHORT);
				toast.show();
				break;
			}
			case 3: 
				sendState((String) msg.obj);
				break;
	
			}
		}
	};	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);	// Ÿ��Ʋ�ٸ� ���ش�.
		setContentView(R.layout.main);

		// ��ư ����
		for(int i=0; i < buttonId.length; i++){
			btn[i] = (Button)findViewById(buttonId[i]);
		}
		
		loadState();
		
        // ��ȭ ����
        Locale ko = Locale.KOREA; /* CANADA, CHINA, FRANCE, ENGLISH ...*/
        formatter = NumberFormat.getCurrencyInstance(ko);
        
        //	������� ���� ����
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
        	// null �̸� �������� ����..
        	Toast.makeText(this, 
        			"�ܸ��⿡�� ��������� �������� �ʽ��ϴ�.", Toast.LENGTH_SHORT).show();
        	finish();
        }

	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	menu.add(0,1,0, "�ֹ�����");//.setIcon(android.R.drawable.ic_menu_search);  
    	menu.add(0,2,0, "��ǰ���");//.setIcon(android.R.drawable.ic_menu_gallery); 
    	menu.add(0,3,0, "������� ������ ����");
    	menu.add(0,4,0, "������� Ž�� Ȱ��ȭ");
    	menu.add(0,5,0, "������� ����");
    	//item.setIcon();
    	return true;
    }
    
    /**
     * �ɼ� �޴� ���ÿ� ���� �ش� ó���� ����
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	Intent intent = null;
    	switch(item.getItemId()){

			case 1:
				intent = new Intent(getBaseContext(), OrderLogActivity.class);
				startActivity(intent);
				return true;
    		case 2:
				intent = new Intent(getBaseContext(), RegisterActivity.class);
				startActivity(intent);
    			return true;
    		case 3:	// ������ ����
    			isServer = true;
    			// �켱 ������ ���� �ٽ� ����
				if(mAcceptThread != null){
					mAcceptThread.cancel();
					mAcceptThread = null;
				}
				// ���� ������ ����
				mAcceptThread = new AcceptThread(this);
				Thread thread = new Thread(mAcceptThread);
				thread.start();
    			return true;  
    		case 4:	// �������  ���� ������ ���·� �Ѵ�.
				Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				// �ִ� ����ð��� 300���� �����Ѵ�.
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
				startActivity(discoverableIntent);
    			return true; 
    			
    		case 5:	//	��������� �˻����� Ŭ���̾�Ʈ��  ����
    			isServer = false;
				intent = new Intent(getBaseContext(), SearchBluetoothDeviceActivity.class);
				startActivityForResult(intent,SEARCH_DEVICE);
    			return true;     			
    			
    	}
    	return false;
    }	

	/**
	 * resume �Ǹ� ���̺���� ����ȸ
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	//	loadState();
		// ��������� Ȱ��ȭ�� �ȵǾ� ������
		if(!mBluetoothAdapter.isEnabled()){
			// ������� ���ѿ�û�ϱ�
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_ENABLE);
		}
	}
	

	/**
	 * ������� ���� 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if( requestCode == REQUEST_ENABLE && resultCode == Activity.RESULT_OK){	// ������� Ȱ��ȭ�� �����̸�
			//A Set is a data structure which does not allow duplicate elements.
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			if(pairedDevices.size() > 0){
				for(BluetoothDevice device:pairedDevices){
					Log.i(DEBUG_TAG, "device name-->" + device.getName() );
				}
			}
		}else if(requestCode == REQUEST_ENABLE && resultCode == Activity.RESULT_CANCELED){
			Toast.makeText(this, "��������� �������� �ʾ� ���� �����մϴ�.", Toast.LENGTH_SHORT).show();
			finish();
		}else if(resultCode == SEARCH_DEVICE){
			//	Intent�� ������� ����̽� ��ü ���
			mBluetoothDevice = data.getParcelableExtra("device");
			
			// Ŭ���̾�Ʈ�� �ٸ���⿡ �����ϱ� ���� ���� �����带 �����Ų��.
			if(mAcceptThread != null){
				mAcceptThread.cancel();
				mAcceptThread = null;
			}
			
			if(mConnectedThread != null){
				mConnectedThread.cancel();
				mConnectedThread = null;
			}
			
			ConnectThread mConnectThread =  new ConnectThread(mBluetoothDevice);
			mConnectThread.start();
		// �ֹ�ó���� �����̸�
		}else if( requestCode == ORDER_STATE && resultCode == RESULT_OK ){
			int tableNum = data.getIntExtra("tableNum", 0);
			String person = data.getStringExtra("person");
			String product = data.getStringExtra("product");
			String price = data.getStringExtra("price");
			String needs = data.getStringExtra("needs");
			if(needs.length() <= 0){
				needs = "����";
			}
			String activate = "a";
			// ���� �޼��� ����
			StringBuilder sb = new StringBuilder();
			sb.append(activate);
			sb.append(":");
			sb.append(tableNum);
			sb.append(":");
			sb.append(person);
			sb.append(":");
			sb.append(product);
			sb.append(":");
			sb.append(price);
			sb.append(":");			
			sb.append(needs);	
			sendState(sb.toString());	// �ٸ� �ܸ��⿡ ���̺� ���� ���¸� �˸���.
			doChageState(sb.toString());
			Log.i(DEBUG_TAG,tableNum + "onActivityResult resultMessage--->" + sb.toString());
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		 * back ��ư�̸� Ÿ�̸�(2��)�� �̿��Ͽ� �ٽ��ѹ� �ڷ� ���⸦ 
		 * ������ ���ø����̼��� ���� �ǵ����Ѵ�.
		 */
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (!isTwoClickBack) {	// ���� �ι� Ŭ���� �ƴϸ�
					Toast.makeText(this, "'�ڷ�' ��ư�� �ѹ� �� ������ ����˴ϴ�.",
							Toast.LENGTH_SHORT).show();
					CntTimer timer = new CntTimer(2000, 1); // �ι� Ŭ�� Ÿ�̸�
					timer.start();
				} else {
					
					// ���� ����
					if(mConnectedThread != null){
						mConnectedThread.cancel();
						mConnectedThread = null;
					}
					
					if(mConnectThread != null){
						mConnectThread.cancel();
						mConnectThread = null;
					}
					
					if(mAcceptThread != null){
						mAcceptThread.cancel();
						mAcceptThread = null;
					}				
					moveTaskToBack(true);
	                finish();
					return true;
				}

			}
		}
		return false;
	}	

	/**
	 * ���̺� ���� ��������
	 */
	private void loadState(){
		// TODO Auto-generated method stub
		DBHelper dbhp =  new DBHelper(this);
		SQLiteDatabase db = dbhp.getReadableDatabase();	// �б�𵵷� ������
		Cursor cursor = null;
    	// ���ü����� ����
		cursor = db.query(DBHelper.ORDER_STATE_TABLE, null, null, null, null, null, "table_num asc");
		int person, j, i = 0;
		String state;
		if( cursor.moveToFirst() ){	// cursor�� row�� 1�� �̻� ������ 
			do{
				j = i + 1;
				state = cursor.getString( cursor.getColumnIndex("state") );
				person = cursor.getInt( cursor.getColumnIndex("person") );
				if( state.trim().contains("a") ){
					btn[i].setBackgroundResource(R.drawable.checkin_selector);
					state = "�ڸ�����\n �ο� : " + person;
				}else{
					state = "�������";
					btn[i].setBackgroundResource(R.drawable.selector);
				}

				btn[i].setText("���̺�-" + j + state );
				i++;
			}while( cursor.moveToNext() );	// ���� Ŀ���� ������ ������ �����´�.
		}
		
    	// ���� �� �ݾ��ش�.
		cursor.close();
		db.close();
		dbhp.close();   		
	}
	
	/**
	 * ��ư ���� �����ϱ�
	 * @param msg
	 * 	���� ����
	 */
	private void doChageState(String msg){
		DBHelper dbhp =  new DBHelper(MainActivity.this);
		SQLiteDatabase db = dbhp.getWritableDatabase();	// �б�𵵷� ������
		ContentValues cv = new ContentValues();
		
		String[] arr = msg.split(":");
		Log.i(DEBUG_TAG, "chageState message---->"+ msg);
		int index = 1;
		try {
			index = Integer.parseInt(arr[1]);
			if(index > 12){
				index = Integer.valueOf(arr[1].replace("'", "").substring(0, 1) );
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String state;
		if(arr[0].equals("a") ){	// �ֹ�ó���� ���·�
			state = "�ڸ�����\n �ο� : " + arr[2];
			btn[index-1].setText("���̺�-" + index + state );
			btn[index-1].setBackgroundResource(R.drawable.checkin_selector);
			
			cv.put("person", Integer.valueOf(arr[2]));
			cv.put("product", arr[3]);
			cv.put("total_price", arr[4]);
			cv.put("needs", arr[5]);
			db.update(DBHelper.ORDER_TABLE, cv, "table_num = ?", new String[]{String.valueOf(index)});
			
		}else if(arr[0].equals("d")){
			state = "���̺�-" + index + "�������";
			btn[index-1].setText(state);
			btn[index-1].setBackgroundResource(R.drawable.selector);
			cv.put("state", "d");
			db.update(DBHelper.ORDER_STATE_TABLE, cv,
					"table_num = ?", new String[]{String.valueOf(index)});
		}
		
		db.close();
		dbhp.close();	
	}

	
	/**
	 * ���� ������� ���� �޼��� ������
	 * @param message
	 */
	private void sendState(String stateMessage){
		if(mConnectedThread != null){
			mConnectedThread.write(stateMessage.getBytes());
			Log.i(DEBUG_TAG, "sendState-->" +  stateMessage);
		}
	}
	
	/**
	 * ���̺��ư�� Ŭ���� �ֹ� ��Ƽ��Ƽ�� �̵�
	 */
	public void mOnclick(View v){
		Intent intent = null; 
		// ���� ���̵����� ��ư�˾Ƴ���
		int tableNum = 0;
		switch(v.getId()){
		case R.id.table1 :
			tableNum = 1;
			break;
		case R.id.table2 :
			tableNum = 2;
			break;
		case R.id.table3 :
			tableNum = 3;
			break;			
		case R.id.table4 :
			tableNum = 4;
			break;			
		case R.id.table5 :
			tableNum = 5;
			break;			
		case R.id.table6 :
			tableNum = 6;
			break;			
		case R.id.table7 :
			tableNum = 7;
			break;			
		case R.id.table8 :
			tableNum = 8;
			break;			
		case R.id.table9 :
			tableNum = 9;
			break;	
		case R.id.table10 :
			tableNum = 10;
			break;	
		case R.id.table11 :
			tableNum = 11;
			break;	
		case R.id.table12 :
			tableNum = 12;
			break;				
		}
		// �ڸ��� �ִ��� üũ
		String text = btn[tableNum-1].getText().toString();
		if( text.contains("�ڸ�����") ){
			orderInfo(tableNum);

		}else{
			intent = new Intent(MainActivity.this, OrderActivity.class);
			intent.putExtra("tableNum", tableNum);
			startActivityForResult(intent, ORDER_STATE);
		}
	}
	
	/**
	 * �ֹ� ���� ���� ��������
	 * @param tableNum
	 * 	���̺� �ڸ�
	 */
	private void orderInfo(int tableNum) {
		// TODO Auto-generated method stub
		final int fTableNum = tableNum;
		DBHelper dbhp =  new DBHelper(this);
		SQLiteDatabase db = dbhp.getReadableDatabase();	// �б�𵵷� ������
		Cursor cursor = null;
		// �ش� ���̺� ���� ��������
		cursor = db.query(DBHelper.ORDER_TABLE, null, "table_num = ?", 
				new String[]{String.valueOf(fTableNum)}, null, null, null);
		StringBuilder sb = new StringBuilder();
		String price;
		if( cursor.moveToFirst() ){	
			sb.append("��ǰ�� : "); 
			sb.append(cursor.getString( cursor.getColumnIndex("product")) + "\n" );
			sb.append("�ο� : "); 
			sb.append(cursor.getString( cursor.getColumnIndex("person")) + "\n" );			
			sb.append("���� : "); 
			price = formatter.format(cursor.getInt( cursor.getColumnIndex("total_price")));
			sb.append(price.substring(1, price.length()-3) + "��\n" );
			sb.append("�ֹ����� : "); 
			sb.append(cursor.getString( cursor.getColumnIndex("needs")) + "\n" );			
		}

		cursor.close();
		db.close();
		dbhp.close();
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setTitle("�ֹ� ��Ȳ").setMessage(sb.toString())
		.setPositiveButton("üũ �ƿ�", new OnClickListener() {	// ���̺��� ����ش�.
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						DBHelper dbhp =  new DBHelper(MainActivity.this);
						SQLiteDatabase db = dbhp.getWritableDatabase();	// �б�𵵷� ������
						ContentValues cv = new ContentValues();
						cv.put("state", "d");
						db.update(DBHelper.ORDER_STATE_TABLE, cv,
								"table_num = ?", new String[]{String.valueOf(fTableNum)});
				    	// ���� �� �ݾ��ش�.]
						String separate = ":";
						String deactivate = "d";
						// ���̺� ���� �޼��� ������
						final String msg = deactivate + separate + fTableNum ;
						sendState(msg);
						MainActivity.this.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								doChageState(msg);
							}
						});
						db.close();
						dbhp.close();
						showToast("üũ�ƿ� �Ǿ����ϴ�.");
					}
				}).start();
				// ���̺� ���¸� �ٽ� �ε� �Ѵ�.
			//	loadState();
			}
		}).setNegativeButton("���",null);
		dlg.create().show();

	}

	// �ڷΰ��� ���Ḧ ���� Ÿ�̸�
	class CntTimer extends CountDownTimer {
		public CntTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			isTwoClickBack = true;
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			isTwoClickBack = false;
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
		}

	} 	

	
	/**
	 * ���� ��Ĺ�� ���� �����ͼۼ����� ������ �����ϱ�
	 * @param socket
	 * ConnectThread Ȥ�� AcceptThread�κ��� ���� ��Ĺ
	 */
	private void startConnectedThread(BluetoothSocket socket) {
		// AcceptThread�κ��� ȣ��Ǿ��ٸ� mConnectThread�� null �� ���̹Ƿ�..
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();

		if(isServer){
			isServer = false;
			syncTableOrder();
		}

	}

	/**
	 * Ŭ���̾�Ʈ ������� ������ ������ �Ǹ� 
	 * ConnectedThread�� ������� ��Ĺ�� ������.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;

		public ConnectThread(BluetoothDevice device) {
			BluetoothSocket tmp = null;
			try {
				// Ŭ���̾�Ʈ ��Ĺ�� ����� ���� �޼ҵ�
				// ���� ��Ĺ�� UUID���� ���ƾ� �Ѵ�.
				tmp = device.createRfcommSocketToServiceRecord(BT_UUID);
			} catch (IOException e) {
			}
			mmSocket = tmp;
			if(mmSocket == null){
				finish();
			}
		}

		public void run() {
			// ��ġ �˻��� ����ǰ� �ִ��� Ȯ���Ͽ� �����մϴ�. ��ġ �˻���
			// ���� ���϶� ������ ������ ���� �ӵ��� ������ ���Դϴ�.
			mBluetoothAdapter.cancelDiscovery();
			try {
				mmSocket.connect();	// ������  ����
			} catch (IOException connectException) {
				connectException.printStackTrace();
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}
			// ConnectedThread�� ������ �ۼ����� �ϱ� ���ؼ� ��Ĺ�� ������.
			startConnectedThread(mmSocket);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 *	���� ��Ĺ Ȥ�� Ŭ���̾�Ʈ ��Ĺ�� ����
	 *	������ �ۼ����� �� ������ 
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;	// ������� ��Ĺ
		private final InputStream mmInStream;	// �Է� ��Ʈ��
		private final OutputStream mmOutStream;	// ��� ��Ʈ��

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;	//�ӽ� ��Ʈ��
			OutputStream tmpOut = null;
			try {
				// ��Ĺ���� �Է� �� ��� ��Ʈ���� ���´�.
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}
			mmInStream = tmpIn;
			mmOutStream = tmpOut;

		}

		public void run() {
			// �佺Ʈ �޼����� ����.
			showToast("������ ����Ǿ����ϴ�.");
			byte[] buffer = new byte[4096];
			// InputStream ���κ��� �Է��� �о���Դϴ�.
			
			while (true) {
					try {
						// Read from the InputStream
						//mmInStream.read(buffer);
					//	InputStreamReader isr = new InputStreamReader(mmInStream);
						mmInStream.read(buffer);

						String msg = new String(buffer).trim();
						Log.i(DEBUG_TAG, "���ŵ� �޼���->" +msg);
						// ui thread�� ��ư ���� ����
						// �ڵ鷯�� ��ư ���� ����
						if(!TextUtils.isEmpty(msg))
							chageState(msg);
					} catch (IOException e) {
						break;
					}
			}
		}

		/**
		 * outputstream�� write�� ���� �޼��� ������ ����.
		 * @param bytes
		 */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) {
			}
		}

		/**
		 * ��Ĺ �ݱ�
		 */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 *	���� ���� ������� rfcomm ä���� ���� ���� ��Ĺ�� ����� �ش�.
	 *
	 */
	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;
		public AcceptThread(Context context) {
			BluetoothServerSocket tmp = null;	// �ӽ� ���� ��Ĺ
			try {
				// UUID�� ����Ͽ� ���� ������ ����ϴ�.
				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
						"My Bluetooth", BT_UUID);
			} catch (IOException e) {
				showToast("���� ������ ����µ� �����Ͽ����ϴ�. " + e.toString());
			}
			mmServerSocket = tmp;
		}

		public void run() {
			showToast("Ŭ���̾�Ʈ�� ��ٸ��� ���Դϴ�.");
			BluetoothSocket socket = null;
			// Ŭ���̾�Ʈ�� ������ �õ��Ҷ����� ��ٸ��ϴ�.
			while (true) {
				try {
					if (mmServerSocket != null) {
						socket = mmServerSocket.accept();
					}
				} catch (IOException e) {
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Ŭ���̾�Ʈ�� ����ǰ� ������ �����Ǹ�
					// ������ ���� ������ �ۼ����� �����մϴ�.
					startConnectedThread(socket);
					showToast("Ŭ���̾�Ʈ�� ����Ǿ����ϴ�.");
					try {
						if (mmServerSocket != null) {
							mmServerSocket.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
						showToast("���� ������ �����ϴ� �� ������ �߻��Ͽ����ϴ�. " + e.toString());
					}
					break;
				}
			}
		}

		// ������ ������ �ݰ� �����带 �����մϴ�.
		public void cancel() {
			try {
				if (mmServerSocket != null) {
					mmServerSocket.close();
				}
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * ��ư ���� ����
	 * @param data
	 */
	private void chageState(String data) {
		if (data != null && data.length() > 0) {
			Message message = handler.obtainMessage();
			message.what = CHANGE_STATE;
			message.arg1 = 0;
			message.arg2 = 0;
			message.obj = data;
			handler.sendMessage(message);
		}
	}	

	/**
	 * �ڵ鷯�� ���� �佺Ʈ �޼��� �����ֱ�
	 * @param msg
	 * 	�޼���
	 */
	private void showToast(String msg) {
		Message message = handler.obtainMessage();
		message.what = SHOW_TOAST;
		message.arg1 = 0;
		message.arg2 = 0;
		message.obj = msg;
		handler.sendMessage(message);
	}
	
	private void sync(String data) {
		if (data != null && data.length() > 0) {
			Message message = handler.obtainMessage();
			message.what = 3;
			message.arg1 = 0;
			message.arg2 = 0;
			message.obj = data;
			handler.sendMessage(message);
		}
	}	
	
	private void syncTableOrder() {
		// �����̸� Ŭ���̾�Ʈ���� ���� ��� �ֹ� ������ ������.
		DBHelper dbhp = new DBHelper(MainActivity.this);
		SQLiteDatabase db = dbhp.getReadableDatabase(); // �б�𵵷� ������
		Cursor cursor1 = null;
		Cursor cursor2 = null;
		// ���ü����� ����
		cursor1 = db.query(DBHelper.ORDER_STATE_TABLE, null, null, null, null,
				null, "table_num asc");
		cursor2 = db.query(DBHelper.ORDER_TABLE, null, null, null, null, null,
				"table_num asc");
		int person, j, i = 0;
		String state;
		final String[] syncData = new String[cursor1.getCount()] ;
		if (cursor1.moveToFirst() && cursor2.moveToFirst()) { // cursor�� row�� 1��
																// �̻� ������
			do {
				j = i + 1;
				state = cursor1.getString(cursor1.getColumnIndex("state"));
				if (state.trim().contains("a")) {
					String needs = cursor2.getString(cursor2
							.getColumnIndex("needs"));
					if (needs.length() <= 0) {
						needs = "����";
					}
						syncData[i] = "a:" + j;
						syncData[i] += ":" +cursor2.getInt(cursor2
									.getColumnIndex("person"));
						syncData[i] += ":" +cursor2.getString(cursor2
									.getColumnIndex("product"));
						syncData[i] += ":" + cursor2.getInt(cursor2
									.getColumnIndex("total_price"));
						syncData[i] += ":" + needs;
						Log.i(DEBUG_TAG, "data!!!---->" + syncData[i]);


				} else {
						syncData[i] = "d:" +j;
				//		Log.i(DEBUG_TAG, "data--->" + syncData[i]);
				}
				Log.i(DEBUG_TAG, "data!!!---->" + syncData[i]);
				i++;
			} while (cursor1.moveToNext() && cursor2.moveToNext()); // ���� Ŀ����
																	// ������ ������
																	// �����´�.
			for(String d :syncData){
				
				Log.i(DEBUG_TAG, "data---------->" + d);
				try {
					sync(d);
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	
		}

		cursor1.close();
		cursor2.close();
		db.close();
		dbhp.close();

	}


}
