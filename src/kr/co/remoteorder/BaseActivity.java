package kr.co.remoteorder;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.MenuItem;

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
	
	// 액티비티 요청값
	public static final int REQUEST_ENABLE = 0;
	public static final int SEARCH_DEVICE = 1;	
	public static final int ORDER_STATE = 2;
	
	// 핸들러 메시지 값
	public static final int CHANGE_STATE = 0;
	public static final int SHOW_TOAST = 1;	
	// uuid 설정
	// RFCOMM Channel 통신을 위한 블루투스 표준 uuid값
	public static final UUID BT_UUID =
			UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

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
