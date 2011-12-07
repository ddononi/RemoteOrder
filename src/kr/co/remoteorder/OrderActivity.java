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
 *	 상품 등록후 서버에 전송하는 클래스
 */
public class OrderActivity extends BaseActivity {
	private int tableNum;	//  테이블 번호
	private ProgressDialog progressDialog;	// 로딩 다이얼로그

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_layout);
        Intent intent = getIntent();
        // 테이블 번호 가져오기
        tableNum = intent.getIntExtra("tableNum", 0);
        if(tableNum == 0){	//  테이블번호가 없으면
        	finish();
        }
	}

	/** 추가 버튼 클릭 */
	public void mOnclick(View v){
		if(v.getId() == R.id.add_btn){
			doOrder();
		}
	}

	/**
	 * 주문하기
	 */
	private void doOrder() {

		final String product = ((EditText)findViewById(R.id.product)).getText().toString();
		final String person = ((EditText)findViewById(R.id.person)).getText().toString();
		final String needs = ((EditText)findViewById(R.id.needs)).getText().toString();

		if(TextUtils.isEmpty(product)){
			Toast.makeText(this, "상품명을 입력하세요", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(!TextUtils.isDigitsOnly(person)){
			Toast.makeText(this, "인원을 정확히 입력하세요", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(!TextUtils.isDigitsOnly(needs)){
			Toast.makeText(this, "가격을 정확히 입력하세요", Toast.LENGTH_SHORT).show();
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
					// 주문정보들을 넣는다.
					vars.add(new BasicNameValuePair("product", product));
					vars.add(new BasicNameValuePair("person", person));
					vars.add(new BasicNameValuePair("needs", needs));
					vars.add(new BasicNameValuePair("price", "10000"));
					// 한글깨짐을 방지하기 위해 utf-8 로 인코딩시키자
					UrlEncodedFormEntity entity = null;
					entity = new UrlEncodedFormEntity(vars, HTTP.UTF_8);	//utf-8 인코딩
					HttpPost request = new HttpPost(ORDER_URL); // post방식으로 보내기
					request.setEntity(entity);
					ResponseHandler<String> responseHandler = new BasicResponseHandler();
					HttpClient client = new DefaultHttpClient();
					// 서버전송후 음답내역
					String responseBody = client.execute(request, responseHandler); // 전송
					// ok면 정상처리
					if (!TextUtils.isEmpty(responseBody)
							&& !responseBody.equals(BaseActivity.ERROR_MESSAGE)) {
						
						// 주문 상태 db 변경
						changeState(Integer.valueOf(person), tableNum);
						// 정상처리결과 메세지를 설정
						setResult(RESULT_OK);
						Log.i(DEBUG_TAG, responseBody);
						OrderActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(OrderActivity.this,
										"정상주문처리되었습니다.", Toast.LENGTH_SHORT).show();
							}
						});
					}else{
						// 정상 응답이 아니면 
						setResult(RESULT_CANCELED);
					}
					finish();	// 액티비티 종료
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

	private void changeState(int person, int tableNum) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		DBHelper dbhp =  new DBHelper(this);
		SQLiteDatabase db = dbhp.getWritableDatabase();	// 읽기모도로 해주자
		ContentValues cv = new ContentValues();
		
		cv.put("state", "a");
		cv.put("person", person);
		db.update(DBHelper.ORDER_STATE_TABLE, cv, "table_num = ?", new String[]{String.valueOf(tableNum), });
	
    	// 디비는 꼭 닫아준다.
		db.close();
		dbhp.close();  
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
	
	

}
