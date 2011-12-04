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
public class RegisterActivity extends BaseActivity {
	private ProgressDialog progressDialog;	// �ε� ���̾�α�

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_layout);
        
	}

	/** �߰� ��ư Ŭ�� */
	public void mOnclick(View v){
		if(v.getId() == R.id.add_btn){
			addProduct();
		}
	}

	/**
	 * ��ǰ �߰��ϱ�
	 */
	private void addProduct() {

		final String product = ((EditText)findViewById(R.id.product)).getText().toString();
		final String stock = ((EditText)findViewById(R.id.stock)).getText().toString();
		final String info = ((EditText)findViewById(R.id.info)).getText().toString();
		final String price = ((EditText)findViewById(R.id.price)).getText().toString();

		if(TextUtils.isEmpty(product)){
			Toast.makeText(this, "��ǰ���� �Է��ϼ���", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if( TextUtils.isEmpty(stock) || !TextUtils.isDigitsOnly(stock)){
			Toast.makeText(this, "������ ��Ȯ�� �Է��ϼ���", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if( TextUtils.isEmpty(price) || !TextUtils.isDigitsOnly(price)){
			Toast.makeText(this, "������ ��Ȯ�� �Է��ϼ���", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(TextUtils.isEmpty(info)){
			Toast.makeText(this, "��ǰ������ ��Ȯ�� �Է��ϼ���", Toast.LENGTH_SHORT).show();
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
					// ��ǰ�������� �ִ´�.
					vars.add(new BasicNameValuePair("product", product));
					vars.add(new BasicNameValuePair("stock", stock));
					vars.add(new BasicNameValuePair("info", info));
					vars.add(new BasicNameValuePair("price", price));
					// �ѱ۱����� �����ϱ� ���� utf-8 �� ���ڵ���Ű��
					UrlEncodedFormEntity entity = null;
					entity = new UrlEncodedFormEntity(vars, HTTP.UTF_8);	//utf-8 ���ڵ�
					HttpPost request = new HttpPost(REGISTER_URL); // post������� ������
					request.setEntity(entity);
					ResponseHandler<String> responseHandler = new BasicResponseHandler();
					HttpClient client = new DefaultHttpClient();
					// ���������� ���䳻��
					String responseBody = client.execute(request, responseHandler); // ����
					// ok�� ����ó��
					if (!TextUtils.isEmpty(responseBody)
							&& !responseBody.equals(BaseActivity.ERROR_MESSAGE)) {
						Log.i(DEBUG_TAG, responseBody);
						RegisterActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(RegisterActivity.this,
										"���� ��ϵǾ����ϴ�.", Toast.LENGTH_SHORT).show();
							}
						});
					}
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

}
