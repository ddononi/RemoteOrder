package kr.co.remoteorder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// SQLiteOpenHelper ������ Ŭ����
public class DBHelper extends SQLiteOpenHelper {
	public static final String ORDER_STATE_TABLE = "orderState";
	public static final String ORDER_TABLE = "orderProduct";
	public static final int DB_VER = 1;
	public DBHelper(Context context){
		// ���� �� ���� ����
		super(context, "remoteOrder.db", null, DB_VER);
	}

	/** ��� ������ ���̺��� ������ش�. */
	@Override
	public void onCreate(SQLiteDatabase db) {	// db�� �����ɶ� ���̺� ����
		// TODO Auto-generated method stub
		// ���̺� ���� ���̺�
		String sql = "CREATE TABLE "+ ORDER_STATE_TABLE + " (table_num INTEGER, " +
				     " state TEXT NOT NULL, person INTEGER);";
		db.execSQL(sql);
		
		for(int i=1; i <=12; i++){
			sql = "insert into " + ORDER_STATE_TABLE + " (table_num, state, person) " +
					" values ("+ i +", 'd', 0 )";	
			db.execSQL(sql);	
		}		

		
		//	�ֹ����� ���̺�
		sql = "CREATE TABLE "+ ORDER_TABLE + " (table_num INTEGER, " +
			     " product TEXT, total_price INTEGER NOT NULL, needs TEXT,  person INTEGER);";
		db.execSQL(sql);		
		for(int i=1; i <=12; i++){
			sql = "insert into " + ORDER_TABLE + " (table_num, product, total_price, needs, person) " +
					" values (" + i + ", '', 0, '', 0 )";		
			db.execSQL(sql);	
		}
		

	}
	

	@Override
	public void onOpen(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		super.onOpen(db);
	}	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}