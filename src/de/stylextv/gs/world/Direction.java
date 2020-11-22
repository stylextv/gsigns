package de.stylextv.gs.world;

import org.bukkit.block.BlockFace;

public class Direction {
	
	private int x;
	private int y;
	private int z;
	
	private BlockFace face;
	
	public Direction(int x, int y, int z, BlockFace face) {
		this.x=x;
		this.y=y;
		this.z=z;
		
		this.face=face;
	}
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getZ() {
		return z;
	}
	
	public BlockFace getFace() {
		return face;
	}
	
}
