package kr.co.remoteorder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyListAdapter extends BaseAdapter {
	private ArrayList<Order> list;
	private NumberFormat formatter;			//  ��ȭ ������ ���� ������
	private String price;
	public MyListAdapter(ArrayList<Order> list){
		this.list = list;

        // ��ȭ ����
        Locale ko = Locale.KOREA; /* CANADA, CHINA, FRANCE, ENGLISH ...*/
        formatter = NumberFormat.getCurrencyInstance(ko);		
	}

	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewGroup item = getViewGroup(convertView, parent);

		Order order = (Order)getItem(position);
		// ������Ʈ ��ŷ�� ���� ����
		position++;
		TextView orderTV = (TextView)item.findViewById(R.id.item);
		TextView priceTV = (TextView)item.findViewById(R.id.price);
		TextView dateTV = (TextView)item.findViewById(R.id.date);
		orderTV.setText(position++  + ". table-" + order.getTableNum() +
				"  " + order.getProducts() );
		price = formatter.format(order.getPrice());
		// �Ѱ��� ����
		priceTV.setText(price.substring(1, price.length()-3)+"��" );
		dateTV.setText(order.getDate());
		
		return item;	
	}
	
	/*
	 * �䰡 ���� �������� üũ
	 */
	private ViewGroup getViewGroup(View reuse, ViewGroup parent){
		if(reuse instanceof ViewGroup){	// ���� ����
			return (ViewGroup)reuse;
		}
		
		Context context = parent.getContext();	// ���ý�Ʈ�� ���´�.
		LayoutInflater inflater = LayoutInflater.from(context);
		// custom list�� ������ش�.
		ViewGroup item = (ViewGroup)inflater.inflate(R.layout.list, null);
		return item;
	}	

}