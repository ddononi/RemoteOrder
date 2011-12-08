package kr.co.remoteorder;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 *	기본 설정 base Class
 */
public class BaseActivity extends Activity {
    /* Server setting */
	// 웹서버에서 db처리를 위한 url들
    public static final String SERVER_URL = "http://ddononi.cafe24.com/remoteOrder/";	
    public static final String ORDER_URL = SERVER_URL + "order.php";					
    public static final String REGISTER_URL = SERVER_URL + "register.php";	
    public static final String ORDERED_URL = SERVER_URL + "ordered.php";
    public static final String MENU_URL = SERVER_URL + "_menu.php";	
	public static final String CLEAN_URL = SERVER_URL + "clean.php";	
	
	// 서버 응답 메세지
	public static final String ERROR_MESSAGE = "error";								// 에러 메세지
	public static final String SUCCESS_MESSAGE = "ok";								// 결과처리 성공
	public static final int TIMEOUT = 6000;											// 커낵션 타임아웃 시
	
	/* app setting */
	public static final String DEBUG_TAG = "remoteOrder";	// 디버그 태그
	public static final String APP_NAME = "";	// 앱 이름
	public final static int DB_VER = 1;			// 디비 버젼
	public static final String SHARED = "mychat";
	
	// 메뉴를 저장할 xml 파일명
	public static final String MENU_XML_FILE_NAME = "_menu.xml";
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	menu.add(0,1,0, "주문내역");//.setIcon(android.R.drawable.ic_menu_search);  
    	menu.add(0,2,0, "상품등록");//.setIcon(android.R.drawable.ic_menu_gallery); 
    	//item.setIcon();
    	return true;
    }
    
    /**
     * 옵션 메뉴 선택에 따라 해당 처리를 해줌
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	Intent intent = null;
    	switch(item.getItemId()){

			case 1:
				intent = new Intent(getBaseContext(), OrderLogActivity.class);
				startActivity(intent);
				return true;
    		case 2:
				intent = new Intent(getBaseContext(), RegisterActivity.class);
				startActivity(intent);
    			return true;
    	}
    	return false;
    }	
	
	/**
	 * 로딩중에 화면을 회전하면 에러가 발생하기 때문에
	 * 완료가 될때까지 화면을 잠근다.
	 */
	public void mLockScreenRotation() {
		// Stop the screen orientation changing during an event
		switch (this.getResources().getConfiguration().orientation) {
		case Configuration.ORIENTATION_PORTRAIT:
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		}
	}

	/**
	 * 화면 잠금 해제
	 */
	public void unLockScreenRotation() {
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}	

	
}
