package xyz.mashtoolz.config;

public class Searchbar {

	public boolean highlight = false;
	public String query = "";

	public boolean regex = false;
	public boolean caseSensitive = false;

	public static Searchbar getDefault() {
		return new Searchbar();
	}
}
