package openelec.installer.gui;

public class Disk {
	private String path;
	private String size;
	private String transportType;
	private String logicalSectorSize;
	private String physicalSectorSize;
	private String partitionTableType;
	private String modelName; //this should always be provided
	
	public Disk(String[] diskAttributes){
		path = diskAttributes[0];
		size = diskAttributes[1];
		transportType = diskAttributes[2];
		logicalSectorSize = diskAttributes[3];
		physicalSectorSize = diskAttributes[4];
		partitionTableType = diskAttributes[5];
		modelName = diskAttributes[6];
	}

	public String getPath() {
		return path;
	}

	public String getSize() {
		return size;
	}

	public String getTransportType() {
		return transportType;
	}

	public String getLogicalSectorSize() {
		return logicalSectorSize;
	}

	public String getPhysicalSectorSize() {
		return physicalSectorSize;
	}

	public String getPartitionTableType() {
		return partitionTableType;
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
