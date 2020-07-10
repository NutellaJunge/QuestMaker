package paul05.de.QuestMaker.EntityUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import paul05.de.QuestMaker.main;

public class EntityWalker {
	
	private Entity ent;
	private boolean isMove = false;
	private int tn;
	private boolean stop;
	
	public EntityWalker(LivingEntity ent) {
		this.ent = ent;
	}
	
	public void walk(Location loc) {
		Bukkit.getScheduler().cancelTask(tn);
		tn = Bukkit.getScheduler().runTaskTimerAsynchronously(main.getInstance(), new Runnable() {

			@Override
			public void run() {
				if (!stop) {
					Location origin = ent.getLocation();
					Vector target = loc.toVector();
					origin.setDirection(target.subtract(origin.toVector()));
					Vector increase = origin.getDirection();
					
					float yaw = getLookAtYaw(increase)+100;
					origin.add(increase.multiply(0.1));
					origin.setYaw(yaw);
					ent.teleport(origin);
					if (((ent.getLocation().getX() >= loc.getX()-0.6 && ent.getLocation().getX() <= loc.getX()+0.6) && (ent.getLocation().getY() >= loc.getY()-0.6 && ent.getLocation().getY() <= loc.getY()+0.02) && (ent.getLocation().getZ() >= loc.getZ()-0.6 && ent.getLocation().getZ() <= loc.getZ()+0.6))) {
						Bukkit.getScheduler().cancelTask(tn);
						isMove = false;
					}
				}
			}
		}, 0, 1).getTaskId();
		isMove = true;
		return;
	}
	
	public void setStop(boolean stop) {
		this.stop = stop;
	}
	
	public boolean isMove() {
		return isMove;
	}
	
	public boolean isStop() {
		return stop;
	}
	
	private float getLookAtYaw(Vector motion) {
	        double dx = motion.getX();
	        double dz = motion.getZ();
	        double yaw = 0;
	        // Set yaw
	        if (dx != 0) {
	            // Set yaw start value based on dx
	            if (dx < 0) {
	                yaw = 1.5 * Math.PI;
	            } else {
	                yaw = 0.5 * Math.PI;
	            }
	            yaw -= Math.atan(dz / dx);
	        } else if (dz < 0) {
	            yaw = Math.PI;
	        }
	        return (float) (-yaw * 180 / Math.PI - 90);
	 }
}
