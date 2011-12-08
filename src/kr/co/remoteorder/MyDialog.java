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
 *	�ֹ� ������ �����ִ� ���̾�α�
 */
public final class MyDialog extends Dialog implements View.OnClickListener {
	private Context context;
	private LayoutInflater inflater;	// ���̾ƿ� ������ ���� ���÷�����
	private Order order = null;		
	
	private int tableNum;

	public MyDialog(Context context, int theme, int tableNum) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		this.context = context;	// db, toast�� context�� �ʿ���
		this.tableNum = tableNum;
		// ���̾�α׸� ������ų ���÷����� ���񽺸� ������
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}


	private Order setCeriData(String title) {
		// TODO Auto-generated method stub
		DBHelper mydb = new DBHelper(context);
		SQLiteDatabase db = mydb.getReadableDatabase(); // �б�𵵷� ������
		Cursor cursor = null;
		Order data = new Order();
		String[] products;
		cursor = db.query("certificate", null, "title = ?",
				new String[] { title, }, null, null, null);
		if (cursor.moveToFirst()) { // cursor�� row�� 1�� �̻� ������
			// ��ǰ������ �ɰ� �迭�� �־��ش�.
			products = cursor.getString(cursor.getColumnIndex("title")).split(",");
		//	data.setProducts(products);
			data.setPerson(cursor.getInt(cursor.getColumnIndex("info")));
			data.setTableNum(cursor.getInt(cursor.getColumnIndex("table_num")));
		}

		return data;
	}


	/**
	 * setCeriData �޼ҵ�� �ڰ���������ü�� ��������
	 * �信 �ش�Ǵ�  ������ �ְ� �̺�Ʈ�� �Ҵ��� ��ȯ
	 * @param title
	 * @return Dialog
	 */
	public final MyDialog doDialog(String title) {
		order = setCeriData(title);

		View layout = inflater.inflate(R.layout.info_dialog, null);

		// AniHandler anihandler = new AniHandler(message);
		// anihandler.onStart();
		this.setContentView(layout);	// ���̾�α׿� layout�� ������.


		// layout�� child View���� ������
		TextView agency = (TextView) layout.findViewById(R.id.agency);
		TextView info = (TextView) layout.findViewById(R.id.info);
		TextView test = (TextView) layout.findViewById(R.id.test);
		Button urlButton = (Button) layout.findViewById(R.id.url);

		// �� view ���� ���� ���ش�.
	//	agency.setText(data.getAgency());
	//	info.setText(data.getInfo());


		return this;
	}


	@Override
	public void onClick(View v) {

		if( v.getId() == R.id.check_out_btn){
			// ���̺��� ����.
			checkout(tableNum);
		}
	}
	
	private void checkout(int tableNum) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		DBHelper dbhp =  new DBHelper(context);
		SQLiteDatabase db = dbhp.getWritableDatabase();	// �б�𵵷� ������
		ContentValues cv = new ContentValues();
		cv.put("state", "d");
		cv.put("person", 0);
		db.update(DBHelper.ORDER_STATE_TABLE, cv, "table_num = ?", new String[]{String.valueOf(tableNum), });
	
    	// ���� �� �ݾ��ش�.
		db.close();
		dbhp.close();  
	}	



}
