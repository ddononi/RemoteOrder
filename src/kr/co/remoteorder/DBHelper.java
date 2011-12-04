package kr.co.remoteorder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// SQLiteOpenHelper ������ Ŭ����
public class DBHelper extends SQLiteOpenHelper {
	public static final String TABLE_NAME = "orderState";
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
		String sql = "CREATE TABLE "+ TABLE_NAME + " (table_num INTEGER, " +
				     " state TEXT NOT NULL, person INTEGER);";
		db.execSQL(sql);
		
		// 9���� ���̺� ������ �⺻ ���� ���ش�. state: a =  activate, d = deactivate
		sql = "insert into " + TABLE_NAME + " (table_num, state, person) " +
					" values (1, 'd', 0 )";
		db.execSQL(sql);
		sql = "insert into " + TABLE_NAME + " (table_num, state, person) " +
					" values (2, 'd', 0 )";
		db.execSQL(sql);
		sql = "insert into " + TABLE_NAME + " (table_num, state, person) " +
					" values (3, 'd', 0 )";
		db.execSQL(sql);
		sql = "insert into " + TABLE_NAME + " (table_num, state, person) " +
					" values (4, 'd', 0 )";
		db.execSQL(sql);
		sql = "insert into " + TABLE_NAME + " (table_num, state, person) " +
					" values (5, 'd', 0 )";
		db.execSQL(sql);
		sql = "insert into " + TABLE_NAME + " (table_num, state, person) " +
					" values (6, 'd', 0 )";
		db.execSQL(sql);
		sql = "insert into " + TABLE_NAME + " (table_num, state, person) " +
					" values (7, 'd', 0 )";
		db.execSQL(sql);
		sql = "insert into " + TABLE_NAME + " (table_num, state, person) " +
					" values (8, 'd', 0 )";	
		db.execSQL(sql);
		sql = "insert into " + TABLE_NAME + " (table_num, state, person) " +
				" values (9, 'd', 0 )";		
		db.execSQL(sql);

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