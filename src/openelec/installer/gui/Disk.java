package openelec.installer.gui;

public class Disk {
	private String path;
	private String size;
	private String modelName;
	
	public Disk(String path, String size, String modelName){
		this.path = path;
		this.size = size;
		this.modelName = modelName;
	}

	public String getPath() {
		return path;
	}

	public String getSize() {
		return size;
	}

	public String getModelName() {
		return modelName;
	}

	@Override
	public String toString() {
		String name = modelName;
		if(size != null && !size.isEmpty()){
				name = name + " (" + size + ")";
		}
		return name;
	}
}
