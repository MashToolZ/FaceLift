package xyz.mashtoolz.custom;

public enum FaceToolType {

	PICKAXE("PICKAXE"),
	WOODCUTTINGAXE("LUMBER AXE"),
	HOE("HOE"),
	BEDROCK("BEDROCK");

	private final String name;

	FaceToolType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}