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
	private ArrayList<Order> orders = null;	// 주문내역들을 담는 리스트
	private MyListAdapter adapter;	// 커스텀 어댑터
	
	// 엘리먼트
	private ListView listLV;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);	// 타이틀바를 없앤다.
		setContentView(R.layout.ordered_list);
       
		// 쓰레드 클래스인 AsyncTask를 이용하여 주문 내역 로그를 가져온다.
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
	    XmlPullParser parser = factory.newPullParser(); // xml 파서 생성
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
		while(eventType != XmlResourceParser.END_DOCUMENT){	// 문서의 마지막이 아닐때까지
			if(eventType == XmlResourceParser.START_TAG){	// 이벤트가 시작태그면
				String strName = parser.getName();
				if(strName.equals("order")){
					order = new Order();					
				}else if(strName.equals("product")){				// 상품 가져오기
					parser.next();	// text으로 이동
					order.setProducts(parser.getText());
					Log.i(DEBUG_TAG, parser.getText());
				}else if(strName.equals("price")){		// 가격 가져오기
					parser.next();	// text으로 이동
					order.setPrice(Integer.valueOf(parser.getText()));						
				}else if(strName.equals("date")){		// 날짜넣긴
					parser.next();	// text으로 이동
					order.setDate(parser.getText());	
				}else  if(strName.equals("table_num")){	// 테이블번호	
					parser.next();		
					order.setTableNum(Integer.valueOf(parser.getText()));
					tmpOrders.add(order);	// 리스트에 넣어준다.
				}
			}
			
			eventType = parser.next();	// 다음이벤트로..
		}

		return tmpOrders;
}
	
	
	/**
	 *	쓰레드 클래스인 AsyncTask을 이용하여 서버에서 menu를 받아온다.
	 */
	private class AsyncTaskGetOrderLog extends AsyncTask<Object, String, Boolean> {
		ProgressDialog dialog = null; // 전송중 보여줄 프로그래스 다이얼로그

		@Override
		protected void onPostExecute(Boolean result) { // 전송 완료후

			dialog.dismiss(); // 프로그레스 다이얼로그 닫기
			if (result && orders != null) { 
					Toast.makeText(OrderLogActivity.this, "로딩 완료.", Toast.LENGTH_SHORT).show();
					// 리스트부에 아답터 설정
					adapter = new MyListAdapter(orders);
					listLV.setAdapter(adapter);
					
			} else {
				Toast.makeText(OrderLogActivity.this, "로딩 에러!!", Toast.LENGTH_LONG).show();
				finish();	// 에러면 액티비티 종료
			}
			// 완료되면 화면 고정 해지
			unLockScreenRotation();
		}

		@Override
		protected void onPreExecute() { // 전송전 프로그래스 다이얼로그로 전송중임을 사용자에게 알린다.
			mLockScreenRotation();	// 우선 화면을 잠근다.
			dialog = ProgressDialog.show(OrderLogActivity.this, "알림",
					"주문 내역을 가져오는중입니다. 잠시 기다려주세요♪♪", true);
			dialog.show();
		}

		@Override
		protected void onProgressUpdate(String... values) {
		}


		@Override
		protected Boolean doInBackground(Object... params) { // 전송중

			// TODO Auto-generated method stub
			boolean result = true;
			String orderedXml = null;	// 주문 xml
			try {
				HttpGet request = new HttpGet(ORDERED_URL); // get method로
				// 응답 핸들러
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				HttpClient client = new DefaultHttpClient();
				String responseBody = client.execute(request, responseHandler); // 전송

				// 응답 내용이 있고 응답 메세지가 에러가 아닐경우
				if (!TextUtils.isEmpty(responseBody)
						&& !responseBody.equals(ERROR_MESSAGE)) {
					// 한글 깨짐 방지를 위해 decoding 해서 가져오자
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
