package kr.co.remoteorder;

/**
 * 주문내역을 담는 클래스
 */
public class Order {
	private String[] products; 	 // 상품명들
	private int person; 		 // 인원
	private int price;			 //	총 가격
	private int tableNum; 		 // 테이블 인원

	public String[] getProducts() {
		return products;
	}

	public void setProducts(String[] products) {
		this.products = products;
	}

	public int getPerson() {
		return person;
	}

	public void setPerson(int person) {
		this.person = person;
	}

	public int getTableNum() {
		return tableNum;
	}

	public void setTableNum(int tableNum) {
		this.tableNum = tableNum;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

}
