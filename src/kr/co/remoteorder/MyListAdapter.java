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
	private NumberFormat formatter;			//  통화 설정을 위한 포메터
	private String price;
	public MyListAdapter(ArrayList<Order> list){
		this.list = list;

        // 통화 설정
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
		// 엘리먼트 후킹후 내용 삽입
		position++;
		TextView orderTV = (TextView)item.findViewById(R.id.item);
		TextView priceTV = (TextView)item.findViewById(R.id.price);
		TextView dateTV = (TextView)item.findViewById(R.id.date);
		orderTV.setText(position++  + ". table-" + order.getTableNum() +
				"  " + order.getProducts() );
		price = formatter.format(order.getPrice());
		// 총가격 사입
		priceTV.setText(price.substring(1, price.length()-3)+"원" );
		dateTV.setText(order.getDate());
		
		return item;	
	}
	
	/*
	 * 뷰가 재사용 가능한지 체크
	 */
	private ViewGroup getViewGroup(View reuse, ViewGroup parent){
		if(reuse instanceof ViewGroup){	// 뷰의 재사용
			return (ViewGroup)reuse;
		}
		
		Context context = parent.getContext();	// 컨택스트를 얻어온다.
		LayoutInflater inflater = LayoutInflater.from(context);
		// custom list를 만들어준다.
		ViewGroup item = (ViewGroup)inflater.inflate(R.layout.list, null);
		return item;
	}	

}