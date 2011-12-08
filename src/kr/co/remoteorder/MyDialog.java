package kr.co.remoteorder;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

/**
 *	주문 정보를 보여주는 다이얼로그
 */
public final class MyDialog extends Dialog implements View.OnClickListener {
	private Context context;
	private LayoutInflater inflater;	// 레이아웃 전개를 위한 인플레이터
	private Order order = null;		
	
	private int tableNum;

	public MyDialog(Context context, int theme, int tableNum) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		this.context = context;	// db, toast에 context가 필요함
		this.tableNum = tableNum;
		// 다이얼로그를 전개시킬 인플레이터 서비스를 얻어오자
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}


	private Order setCeriData(String title) {
		// TODO Auto-generated method stub
		DBHelper mydb = new DBHelper(context);
		SQLiteDatabase db = mydb.getReadableDatabase(); // 읽기모도로 해주자
		Cursor cursor = null;
		Order data = new Order();
		String[] products;
		cursor = db.query("certificate", null, "title = ?",
				new String[] { title, }, null, null, null);
		if (cursor.moveToFirst()) { // cursor에 row가 1개 이상 있으면
			// 상품명을을 쪼개 배열에 넣어준다.
			products = cursor.getString(cursor.getColumnIndex("title")).split(",");
		//	data.setProducts(products);
			data.setPerson(cursor.getInt(cursor.getColumnIndex("info")));
			data.setTableNum(cursor.getInt(cursor.getColumnIndex("table_num")));
		}

		return data;
	}


	/**
	 * setCeriData 메소드로 자격증정보객체를 가져온후
	 * 뷰에 해당되는  내용을 넣고 이벤트를 할당후 반환
	 * @param title
	 * @return Dialog
	 */
	public final MyDialog doDialog(String title) {
		order = setCeriData(title);

		View layout = inflater.inflate(R.layout.info_dialog, null);

		// AniHandler anihandler = new AniHandler(message);
		// anihandler.onStart();
		this.setContentView(layout);	// 다이얼로그에 layout을 입힌다.


		// layout에 child View들을 얻어오자
		TextView agency = (TextView) layout.findViewById(R.id.agency);
		TextView info = (TextView) layout.findViewById(R.id.info);
		TextView test = (TextView) layout.findViewById(R.id.test);
		Button urlButton = (Button) layout.findViewById(R.id.url);

		// 각 view 값을 셋팅 해준다.
	//	agency.setText(data.getAgency());
	//	info.setText(data.getInfo());


		return this;
	}


	@Override
	public void onClick(View v) {

		if( v.getId() == R.id.check_out_btn){
			// 테이블을 비운다.
			checkout(tableNum);
		}
	}
	
	private void checkout(int tableNum) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		DBHelper dbhp =  new DBHelper(context);
		SQLiteDatabase db = dbhp.getWritableDatabase();	// 읽기모도로 해주자
		ContentValues cv = new ContentValues();
		cv.put("state", "d");
		cv.put("person", 0);
		db.update(DBHelper.ORDER_STATE_TABLE, cv, "table_num = ?", new String[]{String.valueOf(tableNum), });
	
    	// 디비는 꼭 닫아준다.
		db.close();
		dbhp.close();  
	}	



}
