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
 *	������� ��ġ�˻� ��Ƽ��Ƽ
 */
public class SearchBluetoothDeviceActivity extends ListActivity {
	private ArrayAdapter<String> mArrayList;	// �����
	private ArrayList<BluetoothDevice> mBluetoothDeviceList;
	
	// ������� ��ġ �˻��� ���� BluetoothAdapter
	BluetoothAdapter mBluetoothAdapter;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_bluetooth);
		
		ArrayList<String> arrayList = new ArrayList<String>();
		mArrayList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
		//����� ����
		setListAdapter(mArrayList);
		
		// ������� ����� ��������
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		mBluetoothDeviceList = new ArrayList<BluetoothDevice>();
		
		if(mBluetoothAdapter == null){	// ������ ����
			Toast.makeText(this, "��������� �����ϴ�.", Toast.LENGTH_SHORT).show();
			finish();
		}
		
		// ��Ȱ��ȭ ���� üũ
		if(!mBluetoothAdapter.isEnabled()){
			// Ȱ��ȭ ��Ű��
			Intent it = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(it,BaseActivity.REQUEST_ENABLE);
		}
		
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if(pairedDevices.size() > 0){	// ���� ����̽��� ������
			for(BluetoothDevice device :  pairedDevices){
				mArrayList.add(device.getName() + "\n" + device.getAddress() );	// ����Ʈ�� �߰����ش�.
				mBluetoothDeviceList.add(device);
			}

		}
	}
	
	/**
	 * ������ ��������� ������ �ѱ��.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		// ������ ������� ����̽� ���
		BluetoothDevice device = mBluetoothDeviceList.get(position);
		Intent intent = new Intent();
		intent.putExtra("device", device);
		// ȣ���� ��Ƽ��Ƽ�� �̵�
		setResult(BaseActivity.SEARCH_DEVICE, intent);
		
		finish();
	}
	
}
