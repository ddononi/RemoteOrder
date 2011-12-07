package kr.co.remoteorder;

import java.io.IOException;
import java.util.Calendar;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 *	 ��ǰ ����� ������ �����ϴ� Ŭ����
 */
public class OrderActivity extends BaseActivity {
	private int tableNum;	//  ���̺� ��ȣ
	private ProgressDialog progressDialog;	// �ε� ���̾�α�

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_layout);
        Intent intent = getIntent();
        // ���̺� ��ȣ ��������
        tableNum = intent.getIntExtra("tableNum", 0);
        if(tableNum == 0){	//  ���̺��ȣ�� ������
        	finish();
        }
	}

	/** �߰� ��ư Ŭ�� */
	public void mOnclick(View v){
		if(v.getId() == R.id.add_btn){
			doOrder();
		}
	}

	/**
	 * �ֹ��ϱ�
	 */
	private void doOrder() {

		final String product = ((EditText)findViewById(R.id.product)).getText().toString();
		final String person = ((EditText)findViewById(R.id.person)).getText().toString();
		final String needs = ((EditText)findViewById(R.id.needs)).getText().toString();

		if(TextUtils.isEmpty(product)){
			Toast.makeText(this, "��ǰ���� �Է��ϼ���", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(!TextUtils.isDigitsOnly(person)){
			Toast.makeText(this, "�ο��� ��Ȯ�� �Է��ϼ���", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(!TextUtils.isDigitsOnly(needs)){
			Toast.makeText(this, "������ ��Ȯ�� �Է��ϼ���", Toast.LENGTH_SHORT).show();
			return;
		}
		
  		// �����͸� �����õ��� ������� ���̾�α׸� ǥ��
		progressDialog = new ProgressDialog(this);
		progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		progressDialog.setMessage("������ ������...");		
		progressDialog.show();

		// ������� ó��
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// �̸��� ���� ������ vector
				Vector<NameValuePair> vars = new Vector<NameValuePair>();
				try {
					// �ֹ��������� �ִ´�.
					vars.add(new BasicNameValuePair("product", product));
					vars.add(new BasicNameValuePair("person", person));
					vars.add(new BasicNameValuePair("needs", needs));
					vars.add(new BasicNameValuePair("price", "10000"));
					// �ѱ۱����� �����ϱ� ���� utf-8 �� ���ڵ���Ű��
					UrlEncodedFormEntity entity = null;
					entity = new UrlEncodedFormEntity(vars, HTTP.UTF_8);	//utf-8 ���ڵ�
					HttpPost request = new HttpPost(ORDER_URL); // post������� ������
					request.setEntity(entity);
					ResponseHandler<String> responseHandler = new BasicResponseHandler();
					HttpClient client = new DefaultHttpClient();
					// ���������� ���䳻��
					String responseBody = client.execute(request, responseHandler); // ����
					// ok�� ����ó��
					if (!TextUtils.isEmpty(responseBody)
							&& !responseBody.equals(BaseActivity.ERROR_MESSAGE)) {
						
						// �ֹ� ���� db ����
						changeState(Integer.valueOf(person), tableNum);
						// ����ó����� �޼����� ����
						setResult(RESULT_OK);
						Log.i(DEBUG_TAG, responseBody);
						OrderActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(OrderActivity.this,
										"�����ֹ�ó���Ǿ����ϴ�.", Toast.LENGTH_SHORT).show();
							}
						});
					}else{
						// ���� ������ �ƴϸ� 
						setResult(RESULT_CANCELED);
					}
					finish();	// ��Ƽ��Ƽ ����
					Log.i(DEBUG_TAG, responseBody);
				} catch (ClientProtocolException e) {
					Log.e(DEBUG_TAG, "Failed to register id (protocol): ", e);
				} catch (IOException e) {
					Log.e(DEBUG_TAG, "Failed to register  i (io): ", e);
				} catch (Exception e) {
					Log.e(DEBUG_TAG, "���� ���ε� ����", e);
				}finally{
					// �ε��� �ݱ�
					if(progressDialog.isShowing()){
						progressDialog.dismiss();
					}
				}
			}
		}).start();		
		
	}

	private void changeState(int person, int tableNum) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		DBHelper dbhp =  new DBHelper(this);
		SQLiteDatabase db = dbhp.getWritableDatabase();	// �б�𵵷� ������
		ContentValues cv = new ContentValues();
		
		cv.put("state", "a");
		cv.put("person", person);
		db.update(DBHelper.ORDER_STATE_TABLE, cv, "table_num = ?", new String[]{String.valueOf(tableNum), });
	
    	// ���� �� �ݾ��ش�.
		db.close();
		dbhp.close();  
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
	
	

}
