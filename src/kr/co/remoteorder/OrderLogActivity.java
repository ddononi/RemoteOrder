package kr.co.remoteorder;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class OrderLogActivity extends BaseActivity {
	private ArrayList<Order> orders = null;	// �ֹ��������� ��� ����Ʈ
	private MyListAdapter adapter;	// Ŀ���� �����
	
	// ������Ʈ
	private ListView listLV;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);	// Ÿ��Ʋ�ٸ� ���ش�.
		setContentView(R.layout.ordered_list);
       
		// ������ Ŭ������ AsyncTask�� �̿��Ͽ� �ֹ� ���� �α׸� �����´�.
		AsyncTaskGetOrderLog asyncTask = new AsyncTaskGetOrderLog();
		asyncTask.execute();
		
		listLV = (ListView)findViewById(R.id.list);
		adapter = new MyListAdapter(orders);
		// 
		listLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
			}
		});

	}
	
	private  ArrayList<Order> praseOrdereduXml(String xmlStr) throws XmlPullParserException, IOException {
		ArrayList<Order> tmpOrders = new ArrayList<Order>();
	    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    XmlPullParser parser = factory.newPullParser(); // xml �ļ� ����
	    factory.setNamespaceAware(true);
	    /*
         *		<order>
		 *			<idx>{$row['idx']}</idx>
		 *			<product>{$row['product']}</product>
		 *			<person>{$row['stock']}</person>
		 *			<price>{$row['price']}</price>
		 *			<needs>{$row['needs']}</needs>
         *          <date>{$row['date']}</date>
         *          <table_num>{$row['table_num']}</table_num>
		 *		</order>
	     */
	    parser.setInput(new StringReader(xmlStr));
		int eventType = -1;
		Order order = null;
		while(eventType != XmlResourceParser.END_DOCUMENT){	// ������ �������� �ƴҶ�����
			if(eventType == XmlResourceParser.START_TAG){	// �̺�Ʈ�� �����±׸�
				String strName = parser.getName();
				if(strName.equals("order")){
					order = new Order();					
				}else if(strName.equals("product")){				// ��ǰ ��������
					parser.next();	// text���� �̵�
					order.setProducts(parser.getText());
					Log.i(DEBUG_TAG, parser.getText());
				}else if(strName.equals("price")){		// ���� ��������
					parser.next();	// text���� �̵�
					order.setPrice(Integer.valueOf(parser.getText()));						
				}else if(strName.equals("date")){		// ��¥�ֱ�
					parser.next();	// text���� �̵�
					order.setDate(parser.getText());	
				}else  if(strName.equals("table_num")){	// ���̺��ȣ	
					parser.next();		
					order.setTableNum(Integer.valueOf(parser.getText()));
					tmpOrders.add(order);	// ����Ʈ�� �־��ش�.
				}
			}
			
			eventType = parser.next();	// �����̺�Ʈ��..
		}

		return tmpOrders;
}
	
	
	/**
	 *	������ Ŭ������ AsyncTask�� �̿��Ͽ� �������� menu�� �޾ƿ´�.
	 */
	private class AsyncTaskGetOrderLog extends AsyncTask<Object, String, Boolean> {
		ProgressDialog dialog = null; // ������ ������ ���α׷��� ���̾�α�

		@Override
		protected void onPostExecute(Boolean result) { // ���� �Ϸ���

			dialog.dismiss(); // ���α׷��� ���̾�α� �ݱ�
			if (result && orders != null) { 
					Toast.makeText(OrderLogActivity.this, "�ε� �Ϸ�.", Toast.LENGTH_SHORT).show();
					// ����Ʈ�ο� �ƴ��� ����
					adapter = new MyListAdapter(orders);
					listLV.setAdapter(adapter);
					
			} else {
				Toast.makeText(OrderLogActivity.this, "�ε� ����!!", Toast.LENGTH_LONG).show();
				finish();	// ������ ��Ƽ��Ƽ ����
			}
			// �Ϸ�Ǹ� ȭ�� ���� ����
			unLockScreenRotation();
		}

		@Override
		protected void onPreExecute() { // ������ ���α׷��� ���̾�α׷� ���������� ����ڿ��� �˸���.
			mLockScreenRotation();	// �켱 ȭ���� ��ٴ�.
			dialog = ProgressDialog.show(OrderLogActivity.this, "�˸�",
					"�ֹ� ������ �����������Դϴ�. ��� ��ٷ��ּ���ܢ�", true);
			dialog.show();
		}

		@Override
		protected void onProgressUpdate(String... values) {
		}


		@Override
		protected Boolean doInBackground(Object... params) { // ������

			// TODO Auto-generated method stub
			boolean result = true;
			String orderedXml = null;	// �ֹ� xml
			try {
				HttpGet request = new HttpGet(ORDERED_URL); // get method��
				// ���� �ڵ鷯
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				HttpClient client = new DefaultHttpClient();
				String responseBody = client.execute(request, responseHandler); // ����

				// ���� ������ �ְ� ���� �޼����� ������ �ƴҰ��
				if (!TextUtils.isEmpty(responseBody)
						&& !responseBody.equals(ERROR_MESSAGE)) {
					// �ѱ� ���� ������ ���� decoding �ؼ� ��������
					orderedXml = URLDecoder.decode(responseBody);
					orders = praseOrdereduXml(orderedXml);
					Log.i(DEBUG_TAG, orderedXml);
					result = true;
				}
			} catch (XmlPullParserException e) {
				Log.e(DEBUG_TAG, "Failed to XmlPullParserException ", e);
				result = false;				
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
}
