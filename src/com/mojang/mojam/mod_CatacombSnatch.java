package com.mojang.mojam;

import com.mojang.mojam.entity.Entity;
import com.mojang.mojam.entity.Player;
import com.mojang.mojam.entity.mob.Bat;
import com.mojang.mojam.entity.mob.Mob;
import com.mojang.mojam.entity.mob.Mummy;
import com.mojang.mojam.entity.mob.Snake;

public class mod_CatacombSnatch extends Mod
{
	long invulnTimer = 0;

	public mod_CatacombSnatch()
	{
		Snatch.addEntity(new Bat(0,0));
		Snatch.addEntity(new Snake(0,0));
		Snatch.addEntity(new Mummy(0,0));
	}
	
	@Override
	public Entity getEntityInstanceById(int type, double x, double y)
	{
		Mob te = null;
		if(type == 0) te = new Bat(x, y);
		if(type == 1) te = new Snake(x, y);
		if(type == 2) te = new Mummy(x, y);
		return te;
	}

	@Override
	public void OnTick()
	{
		Player player = Snatch.getMojam().player;
		if(invulnTimer > 0)
		{
			player.isImmortal = true;
			invulnTimer--;
		}
		else if(player.useMoney(1))
		{
			invulnTimer = 50;
		} else {
			player.isImmortal = false;
		}
	}

	@Override
	public String getVersion()
	{
		return "";
	}

}
