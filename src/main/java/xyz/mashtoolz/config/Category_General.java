package xyz.mashtoolz.config;

public class Category_General {

	public boolean mountThirdPerson = true;

	public static Category_General getDefault() {
		return new Category_General();
	}
}
