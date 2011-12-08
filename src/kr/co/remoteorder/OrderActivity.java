package kr.co.remoteorder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 *	 상품 등록후 서버에 전송하는 클래스
 */
public class OrderActivity extends BaseActivity {
	private ArrayList<String> menus = new ArrayList<String>();
	private int[] prices = new int[100];					// 메뉴 가격들
	private String[] info = new String[100];					// 메뉴 정보
	private int tableNum;					//  테이블 번호
	private String menuXml;					// 메뉴 xml 문자열
	private ProgressDialog progressDialog;	// 로딩 다이얼로그
	private String selectedMenu;			// 선택된 메뉴
	private int selectedNum;				// 선택된번호
	
	private NumberFormat formatter;			//  통화 설정을 위한 포메터
	
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
        
        
        // 통화 설정
        Locale ko = Locale.KOREA; /* CANADA, CHINA, FRANCE, ENGLISH ...*/
        formatter = NumberFormat.getCurrencyInstance(ko);
        
        menuXml = getMenuFromMenuXml();
        try {
			praseMenuXml(menuXml);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // 엘리먼트 후킹
        TextView titleTV = (TextView)findViewById(R.id.title);
        final TextView priceTV = (TextView)findViewById(R.id.price);
        final SeekBar personSB = (SeekBar)findViewById(R.id.person);
        final TextView labelTV = (TextView)findViewById(R.id.label_person);
        final TextView infoTV = (TextView)findViewById(R.id.info);
		final Spinner menuSpinner = (Spinner)findViewById(R.id.menu_spinner);
		titleTV.setText("주문 테이블-"+ tableNum);
		// 아답타 설정
		final ArrayAdapter<String> menuAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, menus );  
		menuSpinner.setAdapter(menuAdapter);
		menuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		menuSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				// 선택된 메뉴이름 저장
				selectedMenu = menuAdapter.getItem(position);
				selectedNum = position;	// 선택된 번호저장
				// 가격 * 수량
				int p = personSB.getProgress();
				String price = formatter.format(prices[selectedNum] * p);
				// 가격을 보여준다.
				priceTV.setText("가격 : " + price.substring(1, price.length()-3) +" 원" );
				// 상품 정보를 보여준다.
				infoTV.setText(info[selectedNum]);
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		// 시크바 설정
		personSB.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			/** 시크바가 변경될때만 처리 */
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				int totalPrice = prices[selectedNum] * progress;
				String price = formatter.format(totalPrice);	
				priceTV.setText("가격 : " + price.substring(1, price.length()-3) +" 원" );
				labelTV.setText(" * " + progress  + "명");
			}
		});
		
	}

	/**
	 * 내부저장소 파일로부터 xml을 데이터를 뽑아 
	 * 배열로 저장
	 */
	/**
	 * @return
	 */
	private String getMenuFromMenuXml() {
		// 내부 저장소 파일 스트림 생성
		FileInputStream fis = null;
		try {
			fis = openFileInput(MENU_XML_FILE_NAME);
			byte[] bytes = new byte[2048];
			StringBuilder sb = new StringBuilder();
			while( fis.read(bytes) != -1){
				sb.append(new String(bytes));
			}
			Log.i(DEBUG_TAG, sb.toString() );			
			return sb.toString();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				fis.close();
			} catch (IOException e) {}
		}
		return null;
	}
		
	private ArrayList<String> praseMenuXml(String xmlStr) throws XmlPullParserException, IOException {
		    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		    XmlPullParser parser = factory.newPullParser(); // xml 파서 생성
		    factory.setNamespaceAware(true);
		    
		    /*
		     * 			<product>
			 *				<idx>{$row['idx']}</idx>
			 *				<product>{$row['product']}</product>
			 *				<stock>{$row['stock']}</stock>
			 *				<price>{$row['price']}</price>
			 *				<info>{$row['info']}</info>
			 *			</product>\n";
		     */
		    parser.setInput(new StringReader(xmlStr));
			int eventType = -1;
			int i =0;
			String name ="", price;
			while(eventType != XmlResourceParser.END_DOCUMENT){	// 문서의 마지막이 아닐때까지
				if(eventType == XmlResourceParser.START_TAG){	// 이벤트가 시작태그면
					String strName = parser.getName();
					if(strName.equals("name")){				// product node로 일때
						parser.next();	// text으로 이동
						name = parser.getText();			// 상품명 넣기  		
						Log.i(DEBUG_TAG, parser.getText());
					}else if(strName.equals("price")){		
						parser.next();	// text으로 이동
						prices[i] = Integer.valueOf(parser.getText());			// 가격저장 	
						price = formatter.format(prices[i]);
						name += "  " + price.substring(1, price.length()-3) + "원";
						menus.add(name);
						Log.i(DEBUG_TAG, parser.getText());						
					}else  if(strName.equals("info")){		
						parser.next();	// text으로 이동
						info[i] = parser.getText();	// 상품정보
						i++;
					}
				}
				eventType = parser.next();	// 다음이벤트로..
			}

			return menus;
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
		// 인원 가져오기
		final String person = ((SeekBar)findViewById(R.id.person)).getProgress() + "";
		// 주문사항 가져오기
		final String needs = ((EditText)findViewById(R.id.needs)).getText().toString();
		// 인원 입력체크
		if(!TextUtils.isDigitsOnly(person)){
			Toast.makeText(this, "인원을 정확히 입력하세요", Toast.LENGTH_SHORT).show();
			return;
		}

		final int totalPrice =  prices[selectedNum] * ((SeekBar)findViewById(R.id.person)).getProgress();
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
					vars.add(new BasicNameValuePair("product", selectedMenu));
					vars.add(new BasicNameValuePair("person", person));
					vars.add(new BasicNameValuePair("needs", needs));
					vars.add(new BasicNameValuePair("price", String.valueOf(totalPrice)) );
					vars.add(new BasicNameValuePair("tableNum",String.valueOf(tableNum)) );
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
						changeState(Integer.valueOf(person), tableNum,
								totalPrice, needs );
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
						Toast.makeText(OrderActivity.this,
								"주문 실패 서버상태를 체크하세요.", Toast.LENGTH_SHORT).show();							
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

	private void changeState(int person, int tableNum, int totalPrice, String needs) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		DBHelper dbhp =  new DBHelper(this);
		SQLiteDatabase db = dbhp.getWritableDatabase();	// 읽기모도로 해주자
		ContentValues cv = new ContentValues();
		
		cv.put("state", "a");
		cv.put("person", person);
		db.update(DBHelper.ORDER_STATE_TABLE, cv, "table_num = ?", new String[]{String.valueOf(tableNum), });
	
		cv.clear();
		cv.put("person", person);
		cv.put("product", selectedMenu);
		cv.put("total_price", totalPrice);
		cv.put("needs", needs);
		db.update(DBHelper.ORDER_TABLE, cv, "table_num = ?", new String[]{String.valueOf(tableNum), });
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
