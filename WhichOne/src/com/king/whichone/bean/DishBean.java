package com.king.whichone.bean;

public class DishBean {
	private String imagePath;
	private String dishName;
	private String dishDes;

	public DishBean(String dishName, String imagePath, String dishDes) {
		this.imagePath = imagePath;
		this.dishName = dishName;
		this.dishDes = dishDes;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getDishName() {
		return dishName;
	}

	public void setDishName(String dishName) {
		this.dishName = dishName;
	}

	public String getDishDes() {
		return dishDes;
	}

	public void setDishDes(String dishDes) {
		this.dishDes = dishDes;
	}

}
