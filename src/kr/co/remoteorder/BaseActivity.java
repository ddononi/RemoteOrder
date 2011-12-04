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
 *	�⺻ ���� base Class
 */
public class BaseActivity extends Activity {
    /* Server setting */
    public static final String SERVER_URL = "http://ddononi.cafe24.com/remoteOrder/";			// �⺻ url
    public static final String ORDER_URL = SERVER_URL + "order.php";	// �޽��� url
    public static final String REGISTER_URL = SERVER_URL + "register.php";	
	public static final String CLEAN_URL = SERVER_URL + "clean.php";	
	public static final String ERROR_MESSAGE = "error";								// ���� �޼���
	public static final String SUCCESS_MESSAGE = "ok";								// ���ó�� ����
	public static final int TIMEOUT = 6000;											// Ŀ���� Ÿ�Ӿƿ� ��
	
	/* app setting */
	public static final String DEBUG_TAG = "remoteOrder";	// ����� �±�
	public static final String APP_NAME = "";	// �� �̸�
	public final static int DB_VER = 1;			// ��� ����
	public static final String SHARED = "mychat";
	
	private boolean isTwoClickBack = false;		// �ι� Ŭ������
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	menu.add(0,1,0, "�ֹ�����");//.setIcon(android.R.drawable.ic_menu_search);  
    	menu.add(0,2,0, "��ǰ���");//.setIcon(android.R.drawable.ic_menu_gallery); 
    	//item.setIcon();
    	return true;
    }
    
    /**
     * �ɼ� �޴� ���ÿ� ���� �ش� ó���� ����
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
	 * �ε��߿� ȭ���� ȸ���ϸ� ������ �߻��ϱ� ������
	 * �Ϸᰡ �ɶ����� ȭ���� ��ٴ�.
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
	 * ȭ�� ��� ����
	 */
	public void unLockScreenRotation() {
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		 * back ��ư�̸� Ÿ�̸�(2��)�� �̿��Ͽ� �ٽ��ѹ� �ڷ� ���⸦ 
		 * ������ ���ø����̼��� ���� �ǵ����Ѵ�.
		 */
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (!isTwoClickBack) {	// ���� �ι� Ŭ���� �ƴϸ�
					Toast.makeText(this, "'�ڷ�' ��ư�� �ѹ� �� ������ ����˴ϴ�.",
							Toast.LENGTH_SHORT).show();
					CntTimer timer = new CntTimer(2000, 1); // �ι� Ŭ�� Ÿ�̸�
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

	// �ڷΰ��� ���Ḧ ���� Ÿ�̸�
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
