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
 *	 상품 등록후 서버에 전송하는 클래스
 */
public class RegisterActivity extends BaseActivity {
	private ProgressDialog progressDialog;	// 로딩 다이얼로그

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_layout);
        
	}

	/** 추가 버튼 클릭 */
	public void mOnclick(View v){
		if(v.getId() == R.id.add_btn){
			addProduct();
		}
	}

	/**
	 * 상품 추가하기
	 */
	private void addProduct() {

		final String product = ((EditText)findViewById(R.id.product)).getText().toString();
		final String stock = ((EditText)findViewById(R.id.stock)).getText().toString();
		final String info = ((EditText)findViewById(R.id.info)).getText().toString();
		final String price = ((EditText)findViewById(R.id.price)).getText().toString();

		if(TextUtils.isEmpty(product)){
			Toast.makeText(this, "상품명을 입력하세요", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if( TextUtils.isEmpty(stock) || !TextUtils.isDigitsOnly(stock)){
			Toast.makeText(this, "수량을 정확히 입력하세요", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if( TextUtils.isEmpty(price) || !TextUtils.isDigitsOnly(price)){
			Toast.makeText(this, "가격을 정확히 입력하세요", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(TextUtils.isEmpty(info)){
			Toast.makeText(this, "상품정보를 정확히 입력하세요", Toast.LENGTH_SHORT).show();
			return;
		}
		
  		// 데이터를 가져올동안 진행상태 다이얼로그를 표시
		progressDialog = new ProgressDialog(this);
		progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		progressDialog.setMessage("서버에 전송중...");		
		progressDialog.show();

		// 쓰레드로 처리
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 이름과 값을 저장할 vector
				Vector<NameValuePair> vars = new Vector<NameValuePair>();
				try {
					// 상품정보들을 넣는다.
					vars.add(new BasicNameValuePair("product", product));
					vars.add(new BasicNameValuePair("stock", stock));
					vars.add(new BasicNameValuePair("info", info));
					vars.add(new BasicNameValuePair("price", price));
					// 한글깨짐을 방지하기 위해 utf-8 로 인코딩시키자
					UrlEncodedFormEntity entity = null;
					entity = new UrlEncodedFormEntity(vars, HTTP.UTF_8);	//utf-8 인코딩
					HttpPost request = new HttpPost(REGISTER_URL); // post방식으로 보내기
					request.setEntity(entity);
					ResponseHandler<String> responseHandler = new BasicResponseHandler();
					HttpClient client = new DefaultHttpClient();
					// 서버전송후 음답내역
					String responseBody = client.execute(request, responseHandler); // 전송
					// ok면 정상처리
					if (!TextUtils.isEmpty(responseBody)
							&& !responseBody.equals(BaseActivity.ERROR_MESSAGE)) {
						Log.i(DEBUG_TAG, responseBody);
						RegisterActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(RegisterActivity.this,
										"정상 등록되었습니다.", Toast.LENGTH_SHORT).show();
							}
						});
					}
					Log.i(DEBUG_TAG, responseBody);
				} catch (ClientProtocolException e) {
					Log.e(DEBUG_TAG, "Failed to register id (protocol): ", e);
				} catch (IOException e) {
					Log.e(DEBUG_TAG, "Failed to register  i (io): ", e);
				} catch (Exception e) {
					Log.e(DEBUG_TAG, "파일 업로드 에러", e);
				}finally{
					// 로딩바 닫기
					if(progressDialog.isShowing()){
						progressDialog.dismiss();
					}
				}
			}
		}).start();		
		
	}

}
