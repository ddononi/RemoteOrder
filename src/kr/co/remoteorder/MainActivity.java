package kr.co.remoteorder;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends BaseActivity {
	private int[] buttonId = {
			R.id.table1, R.id.table2, R.id.table3,
			R.id.table4, R.id.table5, R.id.table6,
			R.id.table7, R.id.table8, R.id.table9
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
	 * ���̺� ���� ��������
	 */
	private void loadState(){
		// TODO Auto-generated method stub
		DBHelper dbhp =  new DBHelper(this);
		SQLiteDatabase db = dbhp.getReadableDatabase();	// �б�𵵷� ������
		Cursor cursor = null;
    	// ���ü����� ����
		cursor = db.query(DBHelper.TABLE_NAME, null, null, null, null, null, "table_num asc");
		int person, j, i = 0;
		String state;
		if( cursor.moveToFirst() ){	// cursor�� row�� 1�� �̻� ������ 
			do{
				j = i + 1;
				state = cursor.getString( cursor.getColumnIndex("state") );
				person = cursor.getInt( cursor.getColumnIndex("person") );
				state = (state == "d")?"�������":"�ڸ�����\n �ο� : " + person;
				btn[i].setText("���̺�-" + j + state );
				i++;
			}while( cursor.moveToNext() );	// ���� Ŀ���� ������ ������ �����´�.
		}
		
    	// ���� �� �ݾ��ش�.
		db.close();
		dbhp.close();   		
	}	
	
	public void mOnclick(View v){
	//	Intent intent = new Intent(MainActivity.this, OrderActivity.class);
	//	startActivity(intent);
	}
}
