package kr.co.remoteorder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

/**
 *	메뉴정보를 서버에서부터 xml형식의 String을 받아
 *	파일로 저장하는 초기 클래스
 */
public class InitActivity extends BaseActivity{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);	// 타이틀바를 없앤다.
		setContentView(R.layout.init_layout);
		
		// 대화내용 가져오기
		AsyncTaskGetMenu atms = new AsyncTaskGetMenu();
		atms.execute();		
	}
	
	/**
	 *	쓰레드 클래스인 AsyncTask을 이용하여 서버에서 menu를 받아온다.
	 */
	private class AsyncTaskGetMenu extends AsyncTask<Object, String, Boolean> {
		ProgressDialog dialog = null; // 전송중 보여줄 프로그래스 다이얼로그

		@Override
		protected void onPostExecute(Boolean result) { // 전송 완료후

			dialog.dismiss(); // 프로그레스 다이얼로그 닫기
			if (result) { 
					Toast.makeText(InitActivity.this, "로딩 완료.", Toast.LENGTH_SHORT).show();
					// 메인 액티비티로..
					Intent intent = new Intent(InitActivity.this, MainActivity.class);
					startActivity(intent);
			} else {
				Toast.makeText(InitActivity.this, "로딩 에러!!", Toast.LENGTH_LONG).show();
			}
			// 완료되면 화면 고정 해지
			unLockScreenRotation();
			finish();
		}

		@Override
		protected void onPreExecute() { // 전송전 프로그래스 다이얼로그로 전송중임을 사용자에게 알린다.
			mLockScreenRotation();	// 우선 화면을 잠근다.
			dialog = ProgressDialog.show(InitActivity.this, "알림",
					"메뉴 내용을 가져오는중입니다. 잠시 기다려주세요♪♪", true);
			dialog.show();
		}

		@Override
		protected void onProgressUpdate(String... values) {
		}


		@Override
		protected Boolean doInBackground(Object... params) { // 전송중

			// TODO Auto-generated method stub
			boolean result = true;
			String menuXml = null;	// 메뉴 xml
			try {
				HttpGet request = new HttpGet(MENU_URL); // get method로
				// 응답 핸들러
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				HttpClient client = new DefaultHttpClient();
				String responseBody = client.execute(request, responseHandler); // 전송

				// 응답 내용이 있고 응답 메세지가 에러가 아닐경우
				if (!TextUtils.isEmpty(responseBody)
						&& !responseBody.equals(ERROR_MESSAGE)) {
					// 한글 깨짐 방지를 위해 decoding 해서 가져오자
					menuXml = URLDecoder.decode(responseBody);
					// 파일로 저장
					synchronized(this) {
						doWriteFile(menuXml);
					}
					Log.i(DEBUG_TAG, "xml 값 :\n" + menuXml);
					result = true;
				}
			} catch (ClientProtocolException e) {
				Log.e(DEBUG_TAG, "Failed to ClientProtocolException ", e);
				result = false;
			} catch (IOException e) {
				Log.e(DEBUG_TAG, "Failed to IOException: ", e);
				result = false;
			}
			return result;
		}

	}	
	
	/**
	 * 서버에서 받을 데이터를 내부 저장소에 파일로 저장한다.
	 * @param data
	 * 	파일내용
	 */
	private void doWriteFile(String data){
		FileOutputStream fos = null;
		try {
			fos = openFileOutput(MENU_XML_FILE_NAME, Context.MODE_PRIVATE);
			fos.write(data.getBytes());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				fos.close();
			} catch (IOException e) {}
		}
		
	}
	
}
