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
	private boolean isTwoClickBack = false;		// 두번 클릭여부
	private int[] buttonId = {
			R.id.table1, R.id.table2, R.id.table3,
			R.id.table4, R.id.table5, R.id.table6,
			R.id.table7, R.id.table8, R.id.table9,
			R.id.table10, R.id.table11, R.id.table12
	};
	private Button[] btn = new Button[buttonId.length];
	private NumberFormat formatter;			//  통화 설정을 위한 포메터
	
	private BluetoothAdapter mBluetoothAdapter = null;	// 블루투스 아답터
	private BluetoothDevice mBluetoothDevice = null;	// 블루투스 디바이스
	
	// 연결 쓰레드 클래스
	private ConnectedThread mConnectedThread;
	private ConnectThread mConnectThread;	// 
	private AcceptThread mAcceptThread;
	
	private boolean isServer = false;	// 서버로 실행인지 체크
	
	// ui 메시지 전송을 위한 핸들러
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CHANGE_STATE:	// 테이블 버튼 상태 변경
				doChageState((String) msg.obj);
				break;
			case SHOW_TOAST: {	// 토스트 굽기
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);	// 타이틀바를 없앤다.
		setContentView(R.layout.main);

		// 버튼 설정
		for(int i=0; i < buttonId.length; i++){
			btn[i] = (Button)findViewById(buttonId[i]);
		}
		
		loadState();
		
        // 통화 설정
        Locale ko = Locale.KOREA; /* CANADA, CHINA, FRANCE, ENGLISH ...*/
        formatter = NumberFormat.getCurrencyInstance(ko);
        
        //	블루투스 지원 여부
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
        	// null 이면 지원하지 않음..
        	Toast.makeText(this, 
        			"단말기에서 블루투스를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
        	finish();
        }

	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	menu.add(0,1,0, "주문내역");//.setIcon(android.R.drawable.ic_menu_search);  
    	menu.add(0,2,0, "상품등록");//.setIcon(android.R.drawable.ic_menu_gallery); 
    	menu.add(0,3,0, "블루투스 서버로 실행");
    	menu.add(0,4,0, "블루투스 탐색 활성화");
    	menu.add(0,5,0, "블루투스 연결");
    	//item.setIcon();
    	return true;
    }
    
    /**
     * 옵션 메뉴 선택에 따라 해당 처리를 해줌
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
    		case 3:	// 서버로 실행
    			isServer = true;
    			// 우선 연결을 끊고 다시 연결
				if(mAcceptThread != null){
					mAcceptThread.cancel();
					mAcceptThread = null;
				}
				// 서버 쓰레드 시작
				mAcceptThread = new AcceptThread(this);
				Thread thread = new Thread(mAcceptThread);
				thread.start();
    			return true;  
    		case 4:	// 블루투스  연결 가능한 상태로 한다.
				Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				// 최대 연결시간은 300까지 지원한다.
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
				startActivity(discoverableIntent);
    			return true; 
    			
    		case 5:	//	블루투스를 검색한후 클라이언트로  연결
    			isServer = false;
				intent = new Intent(getBaseContext(), SearchBluetoothDeviceActivity.class);
				startActivityForResult(intent,SEARCH_DEVICE);
    			return true;     			
    			
    	}
    	return false;
    }	

	/**
	 * resume 되면 테이블상태 재조회
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	//	loadState();
		// 블루투스가 활성화가 안되어 있으면
		if(!mBluetoothAdapter.isEnabled()){
			// 블루투스 권한요청하기
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_ENABLE);
		}
	}
	

	/**
	 * 블루투스 응답 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if( requestCode == REQUEST_ENABLE && resultCode == Activity.RESULT_OK){	// 블루투스 활성화가 정상이면
			//A Set is a data structure which does not allow duplicate elements.
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			if(pairedDevices.size() > 0){
				for(BluetoothDevice device:pairedDevices){
					Log.i(DEBUG_TAG, "device name-->" + device.getName() );
				}
			}
		}else if(requestCode == REQUEST_ENABLE && resultCode == Activity.RESULT_CANCELED){
			Toast.makeText(this, "블루투스를 연결하지 않아 앱을 종료합니다.", Toast.LENGTH_SHORT).show();
			finish();
		}else if(resultCode == SEARCH_DEVICE){
			//	Intent로 블루투스 디바이스 객체 얻기
			mBluetoothDevice = data.getParcelableExtra("device");
			
			// 클라이언트로 다른기기에 접근하기 전에 서버 쓰레드를 종료시킨다.
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
		// 주문처리후 응답이면
		}else if( requestCode == ORDER_STATE && resultCode == RESULT_OK ){
			int tableNum = data.getIntExtra("tableNum", 0);
			String person = data.getStringExtra("person");
			String product = data.getStringExtra("product");
			String price = data.getStringExtra("price");
			String needs = data.getStringExtra("needs");
			if(needs.length() <= 0){
				needs = "없음";
			}
			String activate = "a";
			// 보낼 메세지 조립
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
			sendState(sb.toString());	// 다른 단말기에 테이블 변경 상태를 알린다.
			doChageState(sb.toString());
			Log.i(DEBUG_TAG,tableNum + "onActivityResult resultMessage--->" + sb.toString());
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		 * back 버튼이면 타이머(2초)를 이용하여 다시한번 뒤로 가기를 
		 * 누르면 어플리케이션이 종료 되도록한다.
		 */
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (!isTwoClickBack) {	// 연속 두번 클릭이 아니면
					Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르면 종료됩니다.",
							Toast.LENGTH_SHORT).show();
					CntTimer timer = new CntTimer(2000, 1); // 두번 클릭 타이머
					timer.start();
				} else {
					
					// 연결 종료
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
	 * 테이블 상태 가져오기
	 */
	private void loadState(){
		// TODO Auto-generated method stub
		DBHelper dbhp =  new DBHelper(this);
		SQLiteDatabase db = dbhp.getReadableDatabase();	// 읽기모도로 해주자
		Cursor cursor = null;
    	// 교시순으로 정렬
		cursor = db.query(DBHelper.ORDER_STATE_TABLE, null, null, null, null, null, "table_num asc");
		int person, j, i = 0;
		String state;
		if( cursor.moveToFirst() ){	// cursor에 row가 1개 이상 있으면 
			do{
				j = i + 1;
				state = cursor.getString( cursor.getColumnIndex("state") );
				person = cursor.getInt( cursor.getColumnIndex("person") );
				if( state.trim().contains("a") ){
					btn[i].setBackgroundResource(R.drawable.checkin_selector);
					state = "자리있음\n 인원 : " + person;
				}else{
					state = "비어있음";
					btn[i].setBackgroundResource(R.drawable.selector);
				}

				btn[i].setText("테이블-" + j + state );
				i++;
			}while( cursor.moveToNext() );	// 다음 커서가 있으면 내용을 가져온다.
		}
		
    	// 디비는 꼭 닫아준다.
		cursor.close();
		db.close();
		dbhp.close();   		
	}
	
	/**
	 * 버튼 상태 변경하기
	 * @param msg
	 * 	변경 정보
	 */
	private void doChageState(String msg){
		DBHelper dbhp =  new DBHelper(MainActivity.this);
		SQLiteDatabase db = dbhp.getWritableDatabase();	// 읽기모도로 해주자
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
		if(arr[0].equals("a") ){	// 주문처리로 상태로
			state = "자리있음\n 인원 : " + arr[2];
			btn[index-1].setText("테이블-" + index + state );
			btn[index-1].setBackgroundResource(R.drawable.checkin_selector);
			
			cv.put("person", Integer.valueOf(arr[2]));
			cv.put("product", arr[3]);
			cv.put("total_price", arr[4]);
			cv.put("needs", arr[5]);
			db.update(DBHelper.ORDER_TABLE, cv, "table_num = ?", new String[]{String.valueOf(index)});
			
		}else if(arr[0].equals("d")){
			state = "테이블-" + index + "비어있음";
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
	 * 연결 쓰레드로 상태 메세지 보내기
	 * @param message
	 */
	private void sendState(String stateMessage){
		if(mConnectedThread != null){
			mConnectedThread.write(stateMessage.getBytes());
			Log.i(DEBUG_TAG, "sendState-->" +  stateMessage);
		}
	}
	
	/**
	 * 테이블버튼을 클릭시 주문 액티비티로 이동
	 */
	public void mOnclick(View v){
		Intent intent = null; 
		// 뷰의 아이디값으로 버튼알아내기
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
		// 자리가 있는지 체크
		String text = btn[tableNum-1].getText().toString();
		if( text.contains("자리있음") ){
			orderInfo(tableNum);

		}else{
			intent = new Intent(MainActivity.this, OrderActivity.class);
			intent.putExtra("tableNum", tableNum);
			startActivityForResult(intent, ORDER_STATE);
		}
	}
	
	/**
	 * 주문 세부 정보 가져오기
	 * @param tableNum
	 * 	테이블 자리
	 */
	private void orderInfo(int tableNum) {
		// TODO Auto-generated method stub
		final int fTableNum = tableNum;
		DBHelper dbhp =  new DBHelper(this);
		SQLiteDatabase db = dbhp.getReadableDatabase();	// 읽기모도로 해주자
		Cursor cursor = null;
		// 해당 테이블 정보 가져오기
		cursor = db.query(DBHelper.ORDER_TABLE, null, "table_num = ?", 
				new String[]{String.valueOf(fTableNum)}, null, null, null);
		StringBuilder sb = new StringBuilder();
		String price;
		if( cursor.moveToFirst() ){	
			sb.append("상품명 : "); 
			sb.append(cursor.getString( cursor.getColumnIndex("product")) + "\n" );
			sb.append("인원 : "); 
			sb.append(cursor.getString( cursor.getColumnIndex("person")) + "\n" );			
			sb.append("가격 : "); 
			price = formatter.format(cursor.getInt( cursor.getColumnIndex("total_price")));
			sb.append(price.substring(1, price.length()-3) + "원\n" );
			sb.append("주문사항 : "); 
			sb.append(cursor.getString( cursor.getColumnIndex("needs")) + "\n" );			
		}

		cursor.close();
		db.close();
		dbhp.close();
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setTitle("주문 현황").setMessage(sb.toString())
		.setPositiveButton("체크 아웃", new OnClickListener() {	// 테이블을 비워준다.
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						DBHelper dbhp =  new DBHelper(MainActivity.this);
						SQLiteDatabase db = dbhp.getWritableDatabase();	// 읽기모도로 해주자
						ContentValues cv = new ContentValues();
						cv.put("state", "d");
						db.update(DBHelper.ORDER_STATE_TABLE, cv,
								"table_num = ?", new String[]{String.valueOf(fTableNum)});
				    	// 디비는 꼭 닫아준다.]
						String separate = ":";
						String deactivate = "d";
						// 테이블 변경 메세지 보내기
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
						showToast("체크아웃 되었습니다.");
					}
				}).start();
				// 테이블 상태를 다시 로드 한다.
			//	loadState();
			}
		}).setNegativeButton("취소",null);
		dlg.create().show();

	}

	// 뒤로가기 종료를 위한 타이머
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
	 * 받은 소캣을 통해 데이터송수신할 쓰레드 시작하기
	 * @param socket
	 * ConnectThread 혹은 AcceptThread로부터 받은 소캣
	 */
	private void startConnectedThread(BluetoothSocket socket) {
		// AcceptThread로부터 호출되었다면 mConnectThread는 null 일 것이므로..
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
	 * 클라이언트 쓰레드로 서버와 연결이 되면 
	 * ConnectedThread에 블루투스 소캣을 보낸다.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;

		public ConnectThread(BluetoothDevice device) {
			BluetoothSocket tmp = null;
			try {
				// 클라이언트 소캣을 만들기 위한 메소드
				// 서버 소캣의 UUID값이 같아야 한다.
				tmp = device.createRfcommSocketToServiceRecord(BT_UUID);
			} catch (IOException e) {
			}
			mmSocket = tmp;
			if(mmSocket == null){
				finish();
			}
		}

		public void run() {
			// 장치 검색이 실행되고 있는지 확인하여 종료합니다. 장치 검색이
			// 실행 중일때 연결을 맺으면 연결 속도가 느려질 것입니다.
			mBluetoothAdapter.cancelDiscovery();
			try {
				mmSocket.connect();	// 서버와  연결
			} catch (IOException connectException) {
				connectException.printStackTrace();
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}
			// ConnectedThread로 데이터 송수신을 하기 위해서 소캣을 보낸다.
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
	 *	서버 소캣 혹은 클라이언트 소캣을 통해
	 *	데이터 송수신을 할 쓰레드 
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;	// 블루투스 소캣
		private final InputStream mmInStream;	// 입력 스트림
		private final OutputStream mmOutStream;	// 출력 스트림

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;	//임시 스트림
			OutputStream tmpOut = null;
			try {
				// 소캣에서 입력 및 출력 스트림을 얻어온다.
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}
			mmInStream = tmpIn;
			mmOutStream = tmpOut;

		}

		public void run() {
			// 토스트 메세지를 띄운다.
			showToast("서버에 연결되었습니다.");
			byte[] buffer = new byte[4096];
			// InputStream 으로부터 입력을 읽어들입니다.
			
			while (true) {
					try {
						// Read from the InputStream
						//mmInStream.read(buffer);
					//	InputStreamReader isr = new InputStreamReader(mmInStream);
						mmInStream.read(buffer);

						String msg = new String(buffer).trim();
						Log.i(DEBUG_TAG, "수신된 메세지->" +msg);
						// ui thread로 버튼 상태 변경
						// 핸들러로 버튼 상태 변경
						if(!TextUtils.isEmpty(msg))
							chageState(msg);
					} catch (IOException e) {
						break;
					}
			}
		}

		/**
		 * outputstream의 write를 통해 메세지 내용을 쓴다.
		 * @param bytes
		 */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) {
			}
		}

		/**
		 * 소캣 닫기
		 */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 *	서버 연결 쓰레드로 rfcomm 채널을 통해 서버 소캣을 만들어 준다.
	 *
	 */
	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;
		public AcceptThread(Context context) {
			BluetoothServerSocket tmp = null;	// 임시 서버 소캣
			try {
				// UUID를 사용하여 서버 소켓을 만듭니다.
				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
						"My Bluetooth", BT_UUID);
			} catch (IOException e) {
				showToast("서버 소켓을 만드는데 실패하였습니다. " + e.toString());
			}
			mmServerSocket = tmp;
		}

		public void run() {
			showToast("클라이언트를 기다리는 중입니다.");
			BluetoothSocket socket = null;
			// 클라이언트가 접속을 시도할때까지 기다립니다.
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
					// 클라이언트와 연결되고 소켓이 생성되면
					// 소켓을 통해 데이터 송수신을 시작합니다.
					startConnectedThread(socket);
					showToast("클라이언트와 연결되었습니다.");
					try {
						if (mmServerSocket != null) {
							mmServerSocket.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
						showToast("서버 소켓을 종료하는 중 에러가 발생하였습니다. " + e.toString());
					}
					break;
				}
			}
		}

		// 리스닝 소켓을 닫고 스레드를 종료합니다.
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
	 * 버튼 상태 변경
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
	 * 핸들러를 통해 토스트 메세지 보여주기
	 * @param msg
	 * 	메세지
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
		// 서버이면 클라이언트에게 먼저 모든 주문 내역을 보낸다.
		DBHelper dbhp = new DBHelper(MainActivity.this);
		SQLiteDatabase db = dbhp.getReadableDatabase(); // 읽기모도로 해주자
		Cursor cursor1 = null;
		Cursor cursor2 = null;
		// 교시순으로 정렬
		cursor1 = db.query(DBHelper.ORDER_STATE_TABLE, null, null, null, null,
				null, "table_num asc");
		cursor2 = db.query(DBHelper.ORDER_TABLE, null, null, null, null, null,
				"table_num asc");
		int person, j, i = 0;
		String state;
		final String[] syncData = new String[cursor1.getCount()] ;
		if (cursor1.moveToFirst() && cursor2.moveToFirst()) { // cursor에 row가 1개
																// 이상 있으면
			do {
				j = i + 1;
				state = cursor1.getString(cursor1.getColumnIndex("state"));
				if (state.trim().contains("a")) {
					String needs = cursor2.getString(cursor2
							.getColumnIndex("needs"));
					if (needs.length() <= 0) {
						needs = "없음";
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
			} while (cursor1.moveToNext() && cursor2.moveToNext()); // 다음 커서가
																	// 있으면 내용을
																	// 가져온다.
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
