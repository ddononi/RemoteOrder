package kr.co.remoteorder;

import java.text.NumberFormat;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends BaseActivity {
	private boolean isTwoClickBack = false;		// 두번 클릭여부
	private static final int RESULT_CODE = 1;
	private int[] buttonId = {
			R.id.table1, R.id.table2, R.id.table3,
			R.id.table4, R.id.table5, R.id.table6,
			R.id.table7, R.id.table8, R.id.table9,
			R.id.table10, R.id.table11, R.id.table12
	};
	private Button[] btn = new Button[buttonId.length];
	private NumberFormat formatter;			//  통화 설정을 위한 포메터
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

	}
	
	
	
	/**
	 * resume 되면 테이블상태 재조회
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		loadState();
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
		db.close();
		dbhp.close();   		
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
			startActivityForResult(intent, RESULT_CODE);
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
			sb.append("요구사항 : "); 
			sb.append(cursor.getString( cursor.getColumnIndex("needs")) + "\n" );			
		}
		
		
    	// 디비는 꼭 닫아준다.
		db.close();
		dbhp.close();
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setTitle("주문 현황").setMessage(sb.toString())
		.setPositiveButton("체크 아웃", new OnClickListener() {	// 테이블을 비워준다.
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				DBHelper dbhp =  new DBHelper(MainActivity.this);
				SQLiteDatabase db = dbhp.getWritableDatabase();	// 읽기모도로 해주자
				ContentValues cv = new ContentValues();
				cv.put("state", "d");
				db.update(DBHelper.ORDER_STATE_TABLE, cv,
						"table_num = ?", new String[]{String.valueOf(fTableNum)});
		    	// 디비는 꼭 닫아준다.
				db.close();
				dbhp.close();
				Toast.makeText(MainActivity.this, "체크아웃 되었습니다.", Toast.LENGTH_SHORT).show();
				// 테이블 상태를 다시 로드 한다.
				loadState();
			}
		}).setNegativeButton("취소",null);
		dlg.create().show();

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
					moveTaskToBack(true);
	                finish();
					return true;
				}

			}
		}
		return false;
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
}
