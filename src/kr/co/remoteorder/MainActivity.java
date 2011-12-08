package kr.co.remoteorder;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends BaseActivity {
	private boolean isTwoClickBack = false;		// �ι� Ŭ������
	private static final int RESULT_CODE = 1;
	private int[] buttonId = {
			R.id.table1, R.id.table2, R.id.table3,
			R.id.table4, R.id.table5, R.id.table6,
			R.id.table7, R.id.table8, R.id.table9,
			R.id.table10, R.id.table11, R.id.table12
	};
	private Button[] btn = new Button[buttonId.length];
	
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

	}
	
	
	
	/**
	 * resume �Ǹ� ���̺���� ����ȸ
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		loadState();
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
				state = (state.trim().contains("d") )?"�������":"�ڸ�����\n �ο� : " + person;
				btn[i].setText("���̺�-" + j + state );
				i++;
			}while( cursor.moveToNext() );	// ���� Ŀ���� ������ ������ �����´�.
		}
		
    	// ���� �� �ݾ��ش�.
		db.close();
		dbhp.close();   		
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
			intent = new Intent(MainActivity.this, OrderInfoActivity.class);
		//	return;
		}else{
			intent = new Intent(MainActivity.this, OrderActivity.class);
		}
		intent.putExtra("tableNum", tableNum);
		startActivityForResult(intent, RESULT_CODE);
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
					moveTaskToBack(true);
	                finish();
					return true;
				}

			}
		}
		return false;
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
}
