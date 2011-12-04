package kr.co.remoteorder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// SQLiteOpenHelper 재정의 클래스
public class DBHelper extends SQLiteOpenHelper {
	public static final String TABLE_NAME = "orderState";
	public static final int DB_VER = 1;
	public DBHelper(Context context){
		// 디비명 및 버젼 설정
		super(context, "remoteOrder.db", null, DB_VER);
	}

	/** 디비가 생성시 테이블을 만들어준다. */
	@Override
	public void onCreate(SQLiteDatabase db) {	// db가 생성될때 테이블도 생성
		// TODO Auto-generated method stub
		// 테이블 상태 테이블
		String sql = "CREATE TABLE "+ TABLE_NAME + " (table_num INTEGER, " +
				     " state TEXT NOT NULL, person INTEGER);";
		db.execSQL(sql);
		
		// 9개의 테이블 정보를 기본 설정 해준다. state: a =  activate, d = deactivate
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