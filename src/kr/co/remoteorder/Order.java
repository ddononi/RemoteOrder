package kr.co.remoteorder;

/**
 * �ֹ������� ��� Ŭ����
 */
public class Order {
	private String[] products; 	 // ��ǰ���
	private int person; 		 // �ο�
	private int price;			 //	�� ����
	private int tableNum; 		 // ���̺� �ο�

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
