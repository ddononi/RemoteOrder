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
 *	�޴������� ������������ xml������ String�� �޾�
 *	���Ϸ� �����ϴ� �ʱ� Ŭ����
 */
public class InitActivity extends BaseActivity{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);	// Ÿ��Ʋ�ٸ� ���ش�.
		setContentView(R.layout.init_layout);
		
		// ��ȭ���� ��������
		AsyncTaskGetMenu atms = new AsyncTaskGetMenu();
		atms.execute();		
	}
	
	/**
	 *	������ Ŭ������ AsyncTask�� �̿��Ͽ� �������� menu�� �޾ƿ´�.
	 */
	private class AsyncTaskGetMenu extends AsyncTask<Object, String, Boolean> {
		ProgressDialog dialog = null; // ������ ������ ���α׷��� ���̾�α�

		@Override
		protected void onPostExecute(Boolean result) { // ���� �Ϸ���

			dialog.dismiss(); // ���α׷��� ���̾�α� �ݱ�
			if (result) { 
					Toast.makeText(InitActivity.this, "�ε� �Ϸ�.", Toast.LENGTH_SHORT).show();
					// ���� ��Ƽ��Ƽ��..
					Intent intent = new Intent(InitActivity.this, MainActivity.class);
					startActivity(intent);
			} else {
				Toast.makeText(InitActivity.this, "�ε� ����!!", Toast.LENGTH_LONG).show();
			}
			// �Ϸ�Ǹ� ȭ�� ���� ����
			unLockScreenRotation();
			finish();
		}

		@Override
		protected void onPreExecute() { // ������ ���α׷��� ���̾�α׷� ���������� ����ڿ��� �˸���.
			mLockScreenRotation();	// �켱 ȭ���� ��ٴ�.
			dialog = ProgressDialog.show(InitActivity.this, "�˸�",
					"�޴� ������ �����������Դϴ�. ��� ��ٷ��ּ���ܢ�", true);
			dialog.show();
		}

		@Override
		protected void onProgressUpdate(String... values) {
		}


		@Override
		protected Boolean doInBackground(Object... params) { // ������

			// TODO Auto-generated method stub
			boolean result = true;
			String menuXml = null;	// �޴� xml
			try {
				HttpGet request = new HttpGet(MENU_URL); // get method��
				// ���� �ڵ鷯
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				HttpClient client = new DefaultHttpClient();
				String responseBody = client.execute(request, responseHandler); // ����

				// ���� ������ �ְ� ���� �޼����� ������ �ƴҰ��
				if (!TextUtils.isEmpty(responseBody)
						&& !responseBody.equals(ERROR_MESSAGE)) {
					// �ѱ� ���� ������ ���� decoding �ؼ� ��������
					menuXml = URLDecoder.decode(responseBody);
					// ���Ϸ� ����
					synchronized(this) {
						doWriteFile(menuXml);
					}
					Log.i(DEBUG_TAG, "xml �� :\n" + menuXml);
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
	 * �������� ���� �����͸� ���� ����ҿ� ���Ϸ� �����Ѵ�.
	 * @param data
	 * 	���ϳ���
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
