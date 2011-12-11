package kr.co.remoteorder;

import java.util.ArrayList;
import java.util.Set;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 *	블루투스 장치검색 액티비티
 */
public class SearchBluetoothDeviceActivity extends ListActivity {
	private ArrayAdapter<String> mArrayList;	// 어댑터
	private ArrayList<BluetoothDevice> mBluetoothDeviceList;
	
	// 블루투스 장치 검색에 사용될 BluetoothAdapter
	BluetoothAdapter mBluetoothAdapter;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_bluetooth);
		
		ArrayList<String> arrayList = new ArrayList<String>();
		mArrayList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
		//어댑터 설정
		setListAdapter(mArrayList);
		
		// 블루투스 어댑터 가져오기
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		mBluetoothDeviceList = new ArrayList<BluetoothDevice>();
		
		if(mBluetoothAdapter == null){	// 없으면 종료
			Toast.makeText(this, "블루투스가 없습니다.", Toast.LENGTH_SHORT).show();
			finish();
		}
		
		// 비활성화 인지 체크
		if(!mBluetoothAdapter.isEnabled()){
			// 활성화 시키기
			Intent it = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(it,BaseActivity.REQUEST_ENABLE);
		}
		
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if(pairedDevices.size() > 0){	// 페어링된 디바이스가 있으면
			for(BluetoothDevice device :  pairedDevices){
				mArrayList.add(device.getName() + "\n" + device.getAddress() );	// 리스트에 추가해준다.
				mBluetoothDeviceList.add(device);
			}

		}
	}
	
	/**
	 * 선택한 블루투스를 정보를 넘긴다.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		// 선택한 블루투스 디바이스 얻기
		BluetoothDevice device = mBluetoothDeviceList.get(position);
		Intent intent = new Intent();
		intent.putExtra("device", device);
		// 호출한 액티비티로 이동
		setResult(BaseActivity.SEARCH_DEVICE, intent);
		
		finish();
	}
	
}
