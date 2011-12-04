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
    public static final String SERVER_URL = "http://ddononi.cafe24.com/remoteOrder/";			// 기본 url
    public static final String ORDER_URL = SERVER_URL + "order.php";	// 메시지 url
    public static final String REGISTER_URL = SERVER_URL + "register.php";	
	public static final String CLEAN_URL = SERVER_URL + "clean.php";	
	public static final String ERROR_MESSAGE = "error";								// 에러 메세지
	public static final String SUCCESS_MESSAGE = "ok";								// 결과처리 성공
	public static final int TIMEOUT = 6000;											// 커낵션 타임아웃 시
	
	/* app setting */
	public static final String DEBUG_TAG = "remoteOrder";	// 디버그 태그
	public static final String APP_NAME = "";	// 앱 이름
	public final static int DB_VER = 1;			// 디비 버젼
	public static final String SHARED = "mychat";
	
	private boolean isTwoClickBack = false;		// 두번 클릭여부
	
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
				finish();
				return true;
    		case 2:
				intent = new Intent(getBaseContext(), RegisterActivity.class);
				startActivity(intent);
				finish();
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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		 * back 버튼이면 타이머(2초)를 이용하여 다시한번 뒤로 가기를 
		 * 누르면 어플리케이션이 종료 되도록한다.
		 */
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (!isTwoClickBack) {	// 연속 두번 클릭이 아니면
					Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르면 종료됩니다.",
							Toast.LENGTH_SHORT).show();
					CntTimer timer = new CntTimer(2000, 1); // 두번 클릭 타이머
					timer.start();
				} else {
					moveTaskToBack(true);
	                finish();
					return true;
				}

			}
		}
		return false;
	}

	// 뒤로가기 종료를 위한 타이머
	class CntTimer extends CountDownTimer {
		public CntTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			isTwoClickBack = true;
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			isTwoClickBack = false;
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
		}

	} 
	
}
