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
 *	 ��ǰ ����� ������ �����ϴ� Ŭ����
 */
public class OrderActivity extends BaseActivity {
	private ArrayList<String> menus = new ArrayList<String>();
	private int[] prices = new int[100];					// �޴� ���ݵ�
	private String[] info = new String[100];					// �޴� ����
	private int tableNum;					//  ���̺� ��ȣ
	private String menuXml;					// �޴� xml ���ڿ�
	private ProgressDialog progressDialog;	// �ε� ���̾�α�
	private String selectedMenu;			// ���õ� �޴�
	private int selectedNum;				// ���õȹ�ȣ
	
	private NumberFormat formatter;			//  ��ȭ ������ ���� ������
	
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
        
        
        // ��ȭ ����
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
        
        // ������Ʈ ��ŷ
        TextView titleTV = (TextView)findViewById(R.id.title);
        final TextView priceTV = (TextView)findViewById(R.id.price);
        final SeekBar personSB = (SeekBar)findViewById(R.id.person);
        final TextView labelTV = (TextView)findViewById(R.id.label_person);
        final TextView infoTV = (TextView)findViewById(R.id.info);
		final Spinner menuSpinner = (Spinner)findViewById(R.id.menu_spinner);
		titleTV.setText("�ֹ� ���̺�-"+ tableNum);
		// �ƴ�Ÿ ����
		final ArrayAdapter<String> menuAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, menus );  
		menuSpinner.setAdapter(menuAdapter);
		menuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		menuSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				// ���õ� �޴��̸� ����
				selectedMenu = menuAdapter.getItem(position);
				selectedNum = position;	// ���õ� ��ȣ����
				// ���� * ����
				int p = personSB.getProgress();
				String price = formatter.format(prices[selectedNum] * p);
				// ������ �����ش�.
				priceTV.setText("���� : " + price.substring(1, price.length()-3) +" ��" );
				// ��ǰ ������ �����ش�.
				infoTV.setText(info[selectedNum]);
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		// ��ũ�� ����
		personSB.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			/** ��ũ�ٰ� ����ɶ��� ó�� */
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				int totalPrice = prices[selectedNum] * progress;
				String price = formatter.format(totalPrice);	
				priceTV.setText("���� : " + price.substring(1, price.length()-3) +" ��" );
				labelTV.setText(" * " + progress  + "��");
			}
		});
		
	}

	/**
	 * ��������� ���Ϸκ��� xml�� �����͸� �̾� 
	 * �迭�� ����
	 */
	/**
	 * @return
	 */
	private String getMenuFromMenuXml() {
		// ���� ����� ���� ��Ʈ�� ����
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
		    XmlPullParser parser = factory.newPullParser(); // xml �ļ� ����
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
			while(eventType != XmlResourceParser.END_DOCUMENT){	// ������ �������� �ƴҶ�����
				if(eventType == XmlResourceParser.START_TAG){	// �̺�Ʈ�� �����±׸�
					String strName = parser.getName();
					if(strName.equals("name")){				// product node�� �϶�
						parser.next();	// text���� �̵�
						name = parser.getText();			// ��ǰ�� �ֱ�  		
						Log.i(DEBUG_TAG, parser.getText());
					}else if(strName.equals("price")){		
						parser.next();	// text���� �̵�
						prices[i] = Integer.valueOf(parser.getText());			// �������� 	
						price = formatter.format(prices[i]);
						name += "  " + price.substring(1, price.length()-3) + "��";
						menus.add(name);
						Log.i(DEBUG_TAG, parser.getText());						
					}else  if(strName.equals("info")){		
						parser.next();	// text���� �̵�
						info[i] = parser.getText();	// ��ǰ����
						i++;
					}
				}
				eventType = parser.next();	// �����̺�Ʈ��..
			}

			return menus;
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
		// �ο� ��������
		final String person = ((SeekBar)findViewById(R.id.person)).getProgress() + "";
		// �ֹ����� ��������
		final String needs = ((EditText)findViewById(R.id.needs)).getText().toString();
		// �ο� �Է�üũ
		if(!TextUtils.isDigitsOnly(person)){
			Toast.makeText(this, "�ο��� ��Ȯ�� �Է��ϼ���", Toast.LENGTH_SHORT).show();
			return;
		}

		final int totalPrice =  prices[selectedNum] * ((SeekBar)findViewById(R.id.person)).getProgress();
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
					vars.add(new BasicNameValuePair("product", selectedMenu));
					vars.add(new BasicNameValuePair("person", person));
					vars.add(new BasicNameValuePair("needs", needs));
					vars.add(new BasicNameValuePair("price", String.valueOf(totalPrice)) );
					vars.add(new BasicNameValuePair("tableNum",String.valueOf(tableNum)) );
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
						changeState(Integer.valueOf(person), tableNum,
								totalPrice, needs );
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
						Toast.makeText(OrderActivity.this,
								"�ֹ� ���� �������¸� üũ�ϼ���.", Toast.LENGTH_SHORT).show();							
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

	private void changeState(int person, int tableNum, int totalPrice, String needs) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		DBHelper dbhp =  new DBHelper(this);
		SQLiteDatabase db = dbhp.getWritableDatabase();	// �б�𵵷� ������
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
