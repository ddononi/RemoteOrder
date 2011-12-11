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
 *	�⺻ ���� base Class
 */
public class BaseActivity extends Activity {
    /* Server setting */
	// ���������� dbó���� ���� url��
    public static final String SERVER_URL = "http://ddononi.cafe24.com/remoteOrder/";	
    public static final String ORDER_URL = SERVER_URL + "order.php";					
    public static final String REGISTER_URL = SERVER_URL + "register.php";	
    public static final String ORDERED_URL = SERVER_URL + "ordered.php";
    public static final String MENU_URL = SERVER_URL + "_menu.php";	
	public static final String CLEAN_URL = SERVER_URL + "clean.php";	
	
	// ���� ���� �޼���
	public static final String ERROR_MESSAGE = "error";								// ���� �޼���
	public static final String SUCCESS_MESSAGE = "ok";								// ���ó�� ����
	public static final int TIMEOUT = 6000;											// Ŀ���� Ÿ�Ӿƿ� ��
	
	/* app setting */
	public static final String DEBUG_TAG = "remoteOrder";	// ����� �±�
	public static final String APP_NAME = "";	// �� �̸�
	public final static int DB_VER = 1;			// ��� ����
	public static final String SHARED = "mychat";
	
	// �޴��� ������ xml ���ϸ�
	public static final String MENU_XML_FILE_NAME = "_menu.xml";
	
	// ��Ƽ��Ƽ ��û��
	public static final int REQUEST_ENABLE = 0;
	public static final int SEARCH_DEVICE = 1;	
	public static final int ORDER_STATE = 2;
	
	// �ڵ鷯 �޽��� ��
	public static final int CHANGE_STATE = 0;
	public static final int SHOW_TOAST = 1;	
	// uuid ����
	// RFCOMM Channel ����� ���� ������� ǥ�� uuid��
	public static final UUID BT_UUID =
			UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

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

	
}
