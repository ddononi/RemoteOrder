package kr.co.remoteorder;

import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

public class OrderInfoActivity extends BaseActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);	// 타이틀바를 없앤다.
		setContentView(R.layout.order_info_layout);


	}
}
